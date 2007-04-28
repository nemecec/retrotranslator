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
import net.sf.retrotranslator.runtime.impl.RuntimeTools;
import static net.sf.retrotranslator.runtime.impl.RuntimeTools.CONSTRUCTOR_NAME;
import net.sf.retrotranslator.runtime.java.lang.Enum_;

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
    private boolean enumTranslated;

    public GeneralReplacementVisitor(ClassVisitor classVisitor, final ReplacementLocator locator) {
        super(classVisitor);
        this.locator = locator;
        translator = new NameTranslator() {
            protected String typeName(String s) {
                if (isExcluded(s)) return s;
                ClassReplacement replacement = locator.getReplacement(s);
                return replacement == null ? s : replacement.getUniqueTypeName();
            }
        };
    }

    protected String identifier(String s) {
        return s == null ? null : s.replace('+', '$');
    }

    protected String typeName(String s) {
        if (isExcluded(s)) return s;
        ClassReplacement replacement = locator.getReplacement(s);
        return replacement == null ? s : replacement.getReferenceTypeName();
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        currentClassName = name;
        threadLocalExcluded = name.endsWith("ThreadLocal_$Container");
        if ((access & ACC_ENUM) != 0) {
            String translatedSuperName = translator.typeName(superName);
            if (!translatedSuperName.equals(superName)) {
                enumTranslated = translatedSuperName.equals(Type.getInternalName(Enum_.class));
            }
        }
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

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);
        return visitor != null && enumTranslated &&
                name.equals(RuntimeTools.STATIC_NAME) ? new EnumMethodVisitor(visitor) : visitor;
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
                MemberReplacement field = replacement.getFieldReplacements().get(name + typeDescriptor(desc));
                if (field != null) {
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
                String methodKey = name + (opcode == INVOKESTATIC ? desc : prepend(owner, desc));
                MemberReplacement method = replacement.getMethodReplacements().get(methodKey);
                if (method == null) {
                    replacement = locator.getReplacement(owner);
                    method = replacement == null ? null : replacement.getMethodReplacements().get(methodKey);
                }
                if (method != null && !method.getOwner().equals(currentClassName)) {
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
            buildInstance(visitor, constructorReplacement);
            return true;
        }
        MemberReplacement converter = replacement.getConverterReplacements().get(desc);
        if (converter != null) {
            visitor.visitMethodInsn(INVOKESTATIC, converter.getOwner(), converter.getName(), converter.getDesc());
            desc = Type.getMethodDescriptor(Type.VOID_TYPE, new Type[]{Type.getReturnType(converter.getDesc())});
            visitor.visitMethodInsn(INVOKESPECIAL, owner, CONSTRUCTOR_NAME, desc);
            return true;
        }
        return false;
    }

    private static String prepend(String typeName, String methodDesc) {
        Type[] source = Type.getArgumentTypes(methodDesc);
        Type[] target = new Type[source.length + 1];
        System.arraycopy(source, 0, target, 1, source.length);
        target[0] = TransformerTools.getTypeByInternalName(typeName);
        return Type.getMethodDescriptor(Type.getReturnType(methodDesc), target);
    }

    private void buildInstance(MethodVisitor visitor, ConstructorReplacement replacement) {
        MemberReplacement creator = replacement.getCreator();
        MemberReplacement[] arguments = replacement.getArguments();
        MemberReplacement constructor = replacement.getConstructor();
        MemberReplacement initializer = replacement.getInitializer();
        visitor.visitMethodInsn(INVOKESTATIC, creator.getOwner(), creator.getName(), creator.getDesc());
        if (initializer != null) {
            visitor.visitInsn(DUP2);
        }
        if (arguments.length == 0) {
            visitor.visitInsn(POP);
        } else {
            pushArguments(visitor, arguments);
        }
        visitor.visitMethodInsn(INVOKESPECIAL, constructor.getOwner(), CONSTRUCTOR_NAME, constructor.getDesc());
        if (initializer != null) {
            visitor.visitInsn(SWAP);
            visitor.visitMethodInsn(INVOKEVIRTUAL, initializer.getOwner(), initializer.getName(), initializer.getDesc());
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

    private class EnumMethodVisitor extends MethodAdapter {

        private boolean complete;

        public EnumMethodVisitor(final MethodVisitor visitor) {
            super(visitor);
        }

        public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
            super.visitFieldInsn(opcode, owner, name, desc);
            if (opcode == PUTSTATIC && name.equals("$VALUES") &&
                    owner.equals(currentClassName) && desc.equals("[L" + owner + ";")) {
                loadClassLiteral();
                mv.visitFieldInsn(GETSTATIC, owner, name, desc);
                visitSetEnumConstants();
            }
        }

        public void visitInsn(final int opcode) {
            if (opcode == RETURN && !complete) {
                loadClassLiteral();
                mv.visitMethodInsn(INVOKESTATIC, currentClassName, "values", "()[L" + currentClassName + ";");
                visitSetEnumConstants();
            }
            super.visitInsn(opcode);
        }

        private void loadClassLiteral() {
            mv.visitLdcInsn(TransformerTools.getTypeByInternalName(currentClassName));
        }

        private void visitSetEnumConstants() {
            mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Enum_.class), "setEnumConstants",
                    TransformerTools.descriptor(void.class, Class.class, Enum_[].class));
            complete = true;
        }
    }

}
