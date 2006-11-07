/***
 * Retrotranslator: a Java bytecode transformer that translates Java classes
 * compiled with JDK 5.0 into classes that can be run on JVM 1.4.
 * 
 * Copyright (c) 2005, 2006 Taras Puchko
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.sf.retrotranslator.transformer;

import net.sf.retrotranslator.runtime.asm.*;
import static net.sf.retrotranslator.runtime.asm.Opcodes.*;
import net.sf.retrotranslator.runtime.impl.RuntimeTools;

import java.lang.ref.*;

/**
 * @author Taras Puchko
 */
class ConstructorSubstitutionVisitor extends ClassAdapter {

    private static final String ILLEGAL_STATE_EXCEPTION = Type.getInternalName(IllegalStateException.class);
    private static final String ILLEGAL_ARGUMENT_EXCEPTION = Type.getInternalName(IllegalArgumentException.class);
    private static final String SOFT_REFERENCE = Type.getInternalName(SoftReference.class);
    private static final String WEAK_REFERENCE = Type.getInternalName(WeakReference.class);
    private static final String LONG_ARG_DESCRIPTOR = TransformerTools.descriptor(long.class);
    private static final String DOUBLE_ARG_DESCRIPTOR = TransformerTools.descriptor(double.class);

    private final BackportLocator locator;
    private final boolean advanced;
    private String currentClass;

    public ConstructorSubstitutionVisitor(final ClassVisitor cv, BackportLocator locator, boolean advanced) {
        super(cv);
        this.locator = locator;
        this.advanced = advanced;
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        currentClass = name;
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);
        return visitor == null ? null : new ConstructorSubstitutionMethodVisitor(visitor);
    }

    private class ConstructorSubstitutionMethodVisitor extends MethodAdapter {

        public ConstructorSubstitutionMethodVisitor(MethodVisitor visitor) {
            super(visitor);
        }

        public void visitMethodInsn(final int opcode, final String owner, final String name, String desc) {
            if (opcode == INVOKESPECIAL && name.equals(RuntimeTools.CONSTRUCTOR_NAME) && !owner.equals(currentClass)) {
                InstanceBuilder builder = locator.getBuilder(owner, desc);
                if (builder != null && (advanced | !builder.creator.advanced)) {
                    buildInstance(builder);
                    return;
                }
                ClassMember converter = locator.getConverter(owner, desc);
                if (converter != null && (advanced | !converter.advanced)) {
                    mv.visitMethodInsn(INVOKESTATIC, converter.owner, converter.name, converter.desc);
                    desc = Type.getMethodDescriptor(Type.VOID_TYPE, new Type[]{Type.getReturnType(converter.desc)});
                } else if (owner.equals(ILLEGAL_STATE_EXCEPTION) || owner.equals(ILLEGAL_ARGUMENT_EXCEPTION)) {
                    if (initException(desc, owner)) {
                        return;
                    }
                } else if (advanced && (owner.equals(SOFT_REFERENCE) || owner.equals(WEAK_REFERENCE))) {
                    if (initReference(desc, owner)) {
                        return;
                    }
                }
            }
            super.visitMethodInsn(opcode, owner, name, desc);
        }

        private void buildInstance(InstanceBuilder builder) {
            ClassMember creator = builder.creator;
            ClassMember[] arguments = builder.arguments;
            ClassMember constructor = builder.constructor;
            ClassMember initializer = builder.initializer;
            mv.visitMethodInsn(INVOKESTATIC, creator.owner, creator.name, creator.desc);
            if (initializer != null) {
                mv.visitInsn(DUP2);
            }
            if (arguments.length == 0) {
                mv.visitInsn(POP);
            } else {
                pushArguments(arguments);
            }
            mv.visitMethodInsn(INVOKESPECIAL, constructor.owner, RuntimeTools.CONSTRUCTOR_NAME, constructor.desc);
            if (initializer != null) {
                mv.visitInsn(SWAP);
                mv.visitMethodInsn(INVOKEVIRTUAL, initializer.owner, initializer.name, initializer.desc);
            }
        }

        private void pushArguments(ClassMember[] arguments) {
            for (int i = 0; i < arguments.length; i++) {
                ClassMember argument = arguments[i];
                boolean notLast = i + 1 < arguments.length;
                if (notLast) {
                    mv.visitInsn(DUP);
                }
                mv.visitMethodInsn(INVOKEVIRTUAL, argument.owner, argument.name, argument.desc);
                if (notLast) {
                    swap(argument);
                }
            }
        }

        private void swap(ClassMember argument) {
            if (argument.desc.equals(LONG_ARG_DESCRIPTOR) || argument.desc.equals(DOUBLE_ARG_DESCRIPTOR)) {
                mv.visitInsn(DUP2_X1);
                mv.visitInsn(POP2);
            } else {
                mv.visitInsn(SWAP);
            }
        }

        private boolean initException(String desc, String owner) {
            if (desc.equals(TransformerTools.descriptor(void.class, Throwable.class))) {
                Label toStringLabel = new Label();
                Label continueLabel = new Label();
                mv.visitInsn(DUP2);
                mv.visitInsn(DUP);
                mv.visitJumpInsn(IFNONNULL, toStringLabel);
                mv.visitInsn(POP);
                mv.visitInsn(ACONST_NULL);
                mv.visitJumpInsn(GOTO, continueLabel);
                mv.visitLabel(toStringLabel);
                mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(Throwable.class),
                        "toString", TransformerTools.descriptor(String.class));
                mv.visitLabel(continueLabel);
            } else if (desc.equals(TransformerTools.descriptor(void.class, String.class, Throwable.class))) {
                mv.visitInsn(DUP_X2);
                mv.visitInsn(POP);
                mv.visitInsn(SWAP);
                mv.visitInsn(DUP_X2);
                mv.visitInsn(SWAP);
            } else {
                return false;
            }
            mv.visitMethodInsn(INVOKESPECIAL, owner,
                    RuntimeTools.CONSTRUCTOR_NAME, TransformerTools.descriptor(void.class, String.class));
            mv.visitMethodInsn(INVOKEVIRTUAL, owner,
                    "initCause", TransformerTools.descriptor(Throwable.class, Throwable.class));
            mv.visitInsn(POP);
            return true;
        }

        private boolean initReference(String desc, String owner) {
            if (!desc.equals(TransformerTools.descriptor(void.class, Object.class, ReferenceQueue.class))) {
                return false;
            }
            Label notNullLabel = new Label();
            Label continueLabel = new Label();
            mv.visitInsn(DUP);
            mv.visitJumpInsn(IFNONNULL, notNullLabel);
            mv.visitInsn(POP);
            mv.visitMethodInsn(INVOKESPECIAL, owner,
                    RuntimeTools.CONSTRUCTOR_NAME, TransformerTools.descriptor(void.class, Object.class));
            mv.visitJumpInsn(GOTO, continueLabel);
            mv.visitLabel(notNullLabel);
            mv.visitMethodInsn(INVOKESPECIAL, owner, RuntimeTools.CONSTRUCTOR_NAME, desc);
            mv.visitLabel(continueLabel);
            return true;
        }
    }

}
