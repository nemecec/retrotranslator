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

import net.sf.retrotranslator.runtime.impl.TypeTools;
import org.objectweb.asm.*;
import static org.objectweb.asm.Opcodes.*;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Taras Puchko
 */
public class ClassLiteralVisitor extends ClassAdapter {

    private static final String LOADER_NAME = "class$";
    private static final String FOR_NAME_METHOD_NAME = "forName";
    private static final String CLASS_DESCRIPTOR = Type.getDescriptor(Class.class);
    private static final String FOR_NAME_DESCRIPTOR = Type.getMethodDescriptor(Type.getType(Class.class), new Type[]{Type.getType(String.class)});
    private static final String GET_MESSAGE_DESCRIPTOR = Type.getMethodDescriptor(Type.getType(String.class), new Type[0]);
    private static final String INIT_DESCRIPTOR = Type.getMethodDescriptor(Type.VOID_TYPE, new Type[]{Type.getType(String.class)});

    private Set<String> syntheticVars = new HashSet<String>();
    private String currentClassName;
    private boolean isInterface;

    public ClassLiteralVisitor(final ClassVisitor cv) {
        super(cv);
    }

    public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
        currentClassName = name;
        isInterface = (access & ACC_INTERFACE) != 0;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        return methodVisitor == null ? null : new ClassLiteralMethodVisitor(methodVisitor);
    }

    public void visitEnd() {
        if (!syntheticVars.isEmpty()) addSynthetics();
        super.visitEnd();
    }

    private void addSynthetics() {
        for (String syntheticVar : syntheticVars) {
            visitField(ACC_STATIC + ACC_SYNTHETIC, syntheticVar, CLASS_DESCRIPTOR, null, null).visitEnd();
        }
        addClassLoader();
    }

    private void addClassLoader() {
        MethodVisitor mv = visitMethod(ACC_STATIC + ACC_SYNTHETIC, LOADER_NAME, FOR_NAME_DESCRIPTOR, null, null);
        mv.visitCode();

        Label startTryLabel = new Label();
        mv.visitLabel(startTryLabel);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Class.class), FOR_NAME_METHOD_NAME, FOR_NAME_DESCRIPTOR);
        Label endTryLabel = new Label();
        mv.visitLabel(endTryLabel);
        mv.visitInsn(ARETURN);
        generateClassNotFoundHandler(mv, startTryLabel, endTryLabel);
        mv.visitMaxs(3, 2);
        mv.visitEnd();
    }

    private void generateClassNotFoundHandler(MethodVisitor mv, Label startTryLabel, Label endTryLabel) {
        Label handlerLabel = new Label();
        mv.visitLabel(handlerLabel);
        int var = isInterface ? 0 : 1;
        mv.visitVarInsn(ASTORE, var);
        mv.visitTypeInsn(NEW, Type.getInternalName(NoClassDefFoundError.class));
        mv.visitInsn(DUP);
        mv.visitVarInsn(ALOAD, var);
        mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(ClassNotFoundException.class), "getMessage", GET_MESSAGE_DESCRIPTOR);
        mv.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(NoClassDefFoundError.class), TypeTools.CONSTRUCTOR_NAME, INIT_DESCRIPTOR);
        mv.visitInsn(ATHROW);
        mv.visitTryCatchBlock(startTryLabel, endTryLabel, handlerLabel, Type.getInternalName(ClassNotFoundException.class));
    }

    private class ClassLiteralMethodVisitor extends MethodAdapter {

        public ClassLiteralMethodVisitor(final MethodVisitor mv) {
            super(mv);
        }

        public void visitLdcInsn(final Object cst) {
            if (cst instanceof Type) {
                loadClassLiteral((Type) cst);
            } else {
                super.visitLdcInsn(cst);
            }
        }

        private void loadClassLiteral(Type type) {
            if (isInterface) {
                loadClassLiteralInInterface(type);
            } else {
                loadClassLiteralInClass(type);
            }
        }

        private void loadClassLiteralInInterface(Type type) {
            Label startTryLabel = new Label();
            mv.visitLabel(startTryLabel);
            visitLdcInsn(TypeTools.getClassName(type));
            mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Class.class), FOR_NAME_METHOD_NAME, FOR_NAME_DESCRIPTOR);
            Label endTryLabel = new Label();
            mv.visitLabel(endTryLabel);
            Label successLabel = new Label();
            visitJumpInsn(GOTO, successLabel);
            generateClassNotFoundHandler(mv, startTryLabel, endTryLabel);
            visitLabel(successLabel);
        }

        private void loadClassLiteralInClass(Type type) {
            String var = getVar(type);
            syntheticVars.add(var);
            visitFieldInsn(GETSTATIC, currentClassName, var, CLASS_DESCRIPTOR);
            Label notNullLabel = new Label();
            visitJumpInsn(IFNONNULL, notNullLabel);

            visitLdcInsn(TypeTools.getClassName(type));
            visitMethodInsn(INVOKESTATIC, currentClassName, LOADER_NAME, FOR_NAME_DESCRIPTOR);
            visitInsn(DUP);
            visitFieldInsn(PUTSTATIC, currentClassName, var, CLASS_DESCRIPTOR);
            Label returnLabel = new Label();
            visitJumpInsn(GOTO, returnLabel);

            visitLabel(notNullLabel);
            visitFieldInsn(GETSTATIC, currentClassName, var, CLASS_DESCRIPTOR);
            visitLabel(returnLabel);
        }

        private String getVar(Type type) {
            String var = type.getDescriptor();
            if (var.startsWith("L")) {
                var = "class$" + var.substring(1);
            } else if (var.startsWith("[")) {
                var = "array$" + var.substring(1);
            }
            if (var.endsWith(";")) {
                var = var.substring(0, var.length() - 1);
            }
            return var.replace('[', '$').replace('/', '$');
        }
    }
}
