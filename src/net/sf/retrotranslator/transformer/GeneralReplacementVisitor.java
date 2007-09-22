/***
 * Retrotranslator: a Java bytecode transformer that translates Java classes
 * compiled with JDK 5.0 into classes that can be run on JVM 1.4.
 *
 * Copyright (c) 2005 - 2007 Taras Puchko
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
import static net.sf.retrotranslator.runtime.impl.RuntimeTools.CONSTRUCTOR_NAME;

/**
 * @author Taras Puchko
 */
class GeneralReplacementVisitor extends GenericClassVisitor {

    private static final String LONG_ARG_DESCRIPTOR = TransformerTools.descriptor(long.class);
    private static final String DOUBLE_ARG_DESCRIPTOR = TransformerTools.descriptor(double.class);

    private final ReplacementLocator locator;
    private final NameTranslator translator;
    private String currentClassName;
    private boolean threadLocalExcluded;

    public GeneralReplacementVisitor(ClassVisitor classVisitor, final ReplacementLocator locator) {
        super(classVisitor);
        this.locator = locator;
        translator = new NameTranslator() {
            protected String typeName(String s) {
                if (isExcluded(s)) return s;
                return locator.getUniqueTypeName(s);
            }
        };
    }

    protected String identifier(String s) {
        return fixIdentifier(s);
    }

    protected String typeName(String s) {
        if (isExcluded(s)) return s;
        return locator.getReferenceTypeName(s);
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        currentClassName = name;
        threadLocalExcluded = name.endsWith("ThreadLocal_$Container");
        super.visit(version, access,
                translator.typeName(name),
                translator.declarationSignature(signature),
                translator.typeName(superName),
                translator.typeNames(interfaces));
    }

    private boolean isExcluded(String name) {
        return name == null || threadLocalExcluded &&
                (name.equals("java/lang/ThreadLocal") || name.equals("java/lang/InheritableThreadLocal"));
    }

    protected void visitTypeInstruction(MethodVisitor visitor, int opcode, String desc) {
        if (opcode == CHECKCAST || opcode == INSTANCEOF) {
            ClassReplacement classReplacement = locator.getReplacement(desc);
            if (classReplacement != null) {
                MemberReplacement memberReplacement = opcode == CHECKCAST ?
                        classReplacement.getCheckCastReplacement() :
                        classReplacement.getInstanceOfReplacement();
                if (memberReplacement != null) {
                    visitor.visitMethodInsn(INVOKESTATIC, memberReplacement.getOwner(),
                            memberReplacement.getName(), memberReplacement.getDesc());
                    return;
                }
            }
        }
        super.visitTypeInstruction(visitor, opcode, desc);
    }

    protected void visitFieldInstruction(MethodVisitor visitor, int opcode, String owner, String name, String desc) {
        if (opcode == GETSTATIC || opcode == PUTSTATIC) {
            ClassReplacement replacement = locator.getReplacement(owner);
            if (replacement != null) {
                MemberKey key = new MemberKey(true, name, typeDescriptor(desc));
                MemberReplacement field = replacement.getFieldReplacements().get(key);
                if (field != null && !owner.equals(currentClassName)) {
                    visitor.visitFieldInsn(opcode, field.getOwner(), field.getName(), field.getDesc());
                    return;
                }
            }
        }
        super.visitFieldInstruction(visitor, opcode, owner, name, desc);
    }

    protected void visitMethodInstruction(MethodVisitor visitor, int opcode, String owner, String name, String desc) {
        ClassReplacement replacement = locator.getReplacement(owner);
        if (replacement != null) {
            owner = typeName(owner);
            desc = methodDescriptor(desc);
            if (opcode == INVOKESPECIAL && name.equals(CONSTRUCTOR_NAME)) {
                if (visitConstructor(replacement, visitor, owner, desc)) {
                    return;
                }
                replacement = locator.getReplacement(owner);
                if (replacement != null && visitConstructor(replacement, visitor, owner, desc)) {
                    return;
                }
            } else {
                MemberKey key = new MemberKey(opcode == INVOKESTATIC, name, desc);
                MemberReplacement method = replacement.getMethodReplacements().get(key);
                if (method == null) {
                    replacement = locator.getReplacement(owner);
                    method = replacement == null ? null : replacement.getMethodReplacements().get(key);
                }
                if (method != null && !owner.equals(currentClassName) && !method.getOwner().equals(currentClassName)) {
                    visitor.visitMethodInsn(INVOKESTATIC, method.getOwner(), method.getName(), method.getDesc());
                    return;
                }
            }
        }
        super.visitMethodInstruction(visitor, opcode, owner, name, desc);
    }

    private boolean visitConstructor(ClassReplacement replacement, MethodVisitor visitor, String owner, String desc) {
        ConstructorReplacement constructorReplacement = replacement.getConstructorReplacements().get(desc);
        if (constructorReplacement != null) {
            buildInstance(visitor, owner, constructorReplacement);
            return true;
        }
        MemberReplacement converter = replacement.getConverterReplacements().get(desc);
        if (converter != null) {
            visitor.visitMethodInsn(INVOKESTATIC, converter.getOwner(), converter.getName(), converter.getDesc());
            visitor.visitMethodInsn(INVOKESPECIAL, owner, CONSTRUCTOR_NAME,
                    ClassReplacement.getConstructorDesc(converter));
            return true;
        }
        return false;
    }

    private void buildInstance(MethodVisitor visitor, String owner, ConstructorReplacement replacement) {
        MemberReplacement creator = replacement.getCreator();
        visitor.visitMethodInsn(INVOKESTATIC, creator.getOwner(), creator.getName(), creator.getDesc());
        MemberReplacement initializer = replacement.getInitializer();
        if (initializer != null) {
            visitor.visitInsn(DUP2);
        }
        MemberReplacement[] arguments = replacement.getArguments();
        if (arguments.length == 0) {
            visitor.visitInsn(POP);
        } else {
            pushArguments(visitor, arguments);
        }
        visitor.visitMethodInsn(INVOKESPECIAL, owner, CONSTRUCTOR_NAME, replacement.getConstructorDesc());
        if (initializer != null) {
            visitor.visitInsn(SWAP);
            visitor.visitMethodInsn(INVOKEVIRTUAL,
                    initializer.getOwner(), initializer.getName(), initializer.getDesc());
        }
    }

    private void pushArguments(MethodVisitor visitor, MemberReplacement[] arguments) {
        for (int i = 0; i < arguments.length; i++) {
            MemberReplacement argument = arguments[i];
            boolean notLast = i + 1 < arguments.length;
            if (notLast) {
                visitor.visitInsn(DUP);
            }
            visitor.visitMethodInsn(INVOKEVIRTUAL, argument.getOwner(), argument.getName(), argument.getDesc());
            if (notLast) {
                swap(visitor, argument);
            }
        }
    }

    private void swap(MethodVisitor visitor, MemberReplacement argument) {
        if (argument.getDesc().equals(LONG_ARG_DESCRIPTOR) || argument.getDesc().equals(DOUBLE_ARG_DESCRIPTOR)) {
            visitor.visitInsn(DUP2_X1);
            visitor.visitInsn(POP2);
        } else {
            visitor.visitInsn(SWAP);
        }
    }

}
