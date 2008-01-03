/***
 * Retrotranslator: a Java bytecode transformer that translates Java classes
 * compiled with JDK 5.0 into classes that can be run on JVM 1.4.
 *
 * Copyright (c) 2005 - 2008 Taras Puchko
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

import java.util.*;
import net.sf.retrotranslator.runtime.asm.*;
import static net.sf.retrotranslator.runtime.asm.Opcodes.*;

/**
 * @author Taras Puchko
 */
class ClassLiteralVisitor extends ClassAdapter {

    private static final Map<Integer, Integer> primitiveTypes = getPrimitiveTypes();

    private Set<String> currentFieldNames = new HashSet<String>();
    private Set<String> syntheticFieldNames = new HashSet<String>();
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

    public void visitEnd() {
        for (String fieldName : syntheticFieldNames) {
            if (!currentFieldNames.contains(fieldName)) {
                cv.visitField(ACC_STATIC + ACC_SYNTHETIC, fieldName, Type.getDescriptor(Class.class), null, null).visitEnd();
            }
        }
        super.visitEnd();
    }

    public FieldVisitor visitField(final int access, final String name, final String desc, final String signature, final Object value) {
        currentFieldNames.add(name);
        return super.visitField(access, name, desc, signature, value);
    }

    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);
        return visitor == null ? null : new MethodAdapter(visitor) {

            public void visitLdcInsn(final Object cst) {
                if (cst instanceof Type) {
                    visitClassLiteral((Type) cst);
                } else {
                    super.visitLdcInsn(cst);
                }
            }

            private void visitClassLiteral(Type type) {
                if (isInterface) {
                    loadClassLiteral(type);
                    return;
                }
                String fieldName = getFieldName(type);
                syntheticFieldNames.add(fieldName);
                mv.visitFieldInsn(GETSTATIC, currentClassName, fieldName, Type.getDescriptor(Class.class));
                mv.visitInsn(DUP);
                Label label = new Label();
                visitJumpInsn(IFNONNULL, label);
                mv.visitInsn(POP);
                loadClassLiteral(type);
                mv.visitInsn(DUP);
                visitFieldInsn(PUTSTATIC, currentClassName, fieldName, Type.getDescriptor(Class.class));
                visitLabel(label);
            }

            private void loadClassLiteral(Type type) {
                mv.visitInsn(ICONST_0);
                visitNewArray(type);
                mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(Object.class),
                        "getClass", TransformerTools.descriptor(Class.class));
                if (type.getSort() != Type.ARRAY) {
                    mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(Class.class),
                            "getComponentType", TransformerTools.descriptor(Class.class));
                }
            }

            private void visitNewArray(Type type) {
                if (type.getSort() != Type.ARRAY) {
                    mv.visitTypeInsn(ANEWARRAY, type.getInternalName());
                } else if (type.getDimensions() != 1) {
                    mv.visitTypeInsn(ANEWARRAY, type.toString().substring(1));
                } else {
                    Type elementType = type.getElementType();
                    if (elementType.getSort() == Type.OBJECT) {
                        mv.visitTypeInsn(ANEWARRAY, elementType.getInternalName());
                    } else {
                        mv.visitIntInsn(NEWARRAY, primitiveTypes.get(elementType.getSort()));
                    }
                }
            }

        };
    }

    private static String getFieldName(Type type) {
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

    private static Map<Integer, Integer> getPrimitiveTypes() {
        Map<Integer, Integer> types = new HashMap<Integer, Integer>();
        types.put(Type.BOOLEAN, T_BOOLEAN);
        types.put(Type.CHAR, T_CHAR);
        types.put(Type.FLOAT, T_FLOAT);
        types.put(Type.DOUBLE, T_DOUBLE);
        types.put(Type.BYTE, T_BYTE);
        types.put(Type.SHORT, T_SHORT);
        types.put(Type.INT, T_INT);
        types.put(Type.LONG, T_LONG);
        return types;
    }
}
