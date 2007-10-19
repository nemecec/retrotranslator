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

import static net.sf.retrotranslator.runtime.asm.Opcodes.*;
import net.sf.retrotranslator.runtime.asm.*;
import net.sf.retrotranslator.runtime.impl.EmptyVisitor;

/**
 * @author Taras Puchko
 */
class ObjectMethodsVisitor extends ClassAdapter {

    private static final String OBJECT_NAME = Type.getInternalName(Object.class);

    private static ObjectMethod[] PUBLIC_METHODS = {
            new ObjectMethod(boolean.class, "equals", Object.class),
            new ObjectMethod(int.class, "hashCode"),
            new ObjectMethod(String.class, "toString")
    };

    private static ObjectMethod[] PROTECTED_METHODS = {
            new ObjectMethod(Object.class, "clone"),
            new ObjectMethod(void.class, "finalize")
    };

    private final ReplacementLocator locator;

    public ObjectMethodsVisitor(ClassVisitor visitor, ReplacementLocator locator) {
        super(visitor);
        this.locator = locator;
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);
        return visitor == null ? null : new MethodAdapter(visitor) {
            public void visitMethodInsn(int opcode, String owner, String name, String desc) {
                if (opcode == INVOKEVIRTUAL && owner.charAt(0) == '[') {
                    mv.visitMethodInsn(opcode, OBJECT_NAME, name, desc);
                    return;
                }
                if (opcode == INVOKEINTERFACE) {
                    for (ObjectMethod method : PUBLIC_METHODS) {
                        if (method.equals(name, desc)) {
                            mv.visitMethodInsn(INVOKEVIRTUAL, OBJECT_NAME, name, desc);
                            return;
                        }
                    }
                    for (ObjectMethod method : PROTECTED_METHODS) {
                        if (method.equals(name, desc)) {
                            String methodOwner = findMethodOwner(owner, method);
                            if (methodOwner != null) {
                                mv.visitMethodInsn(opcode, methodOwner, name, desc);
                                return;
                            }
                        }
                    }
                }
                super.visitMethodInsn(opcode, owner, name, desc);
            }
        };
    }

    private String findMethodOwner(String className, ObjectMethod method) {
        String uniqueTypeName = locator.getUniqueTypeName(className);
        ClassReader reader = locator.getEnvironment().findClassReader(uniqueTypeName);
        if (reader == null) {
            return null;
        }
        MethodSearchingVisitor visitor = new MethodSearchingVisitor(method);
        reader.accept(visitor, true);
        if (visitor.methodFound) {
            return uniqueTypeName;
        }
        if (visitor.interfaces != null) {
            for (String anInterface : visitor.interfaces) {
                String owner = findMethodOwner(anInterface, method);
                if (owner != null) {
                    return owner;
                }
            }
        }
        return null;
    }

    private static class MethodSearchingVisitor extends EmptyVisitor {

        private final ObjectMethod method;
        public boolean methodFound;
        public String[] interfaces;

        public MethodSearchingVisitor(ObjectMethod method) {
            this.method = method;
        }

        public void visit(int version, int access, String name,
                          String signature, String superName, String[] interfaces) {
            this.interfaces = interfaces;
        }

        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (method.equals(name, desc)) {
                methodFound = true;
            }
            return null;
        }
    }

    private static class ObjectMethod {

        public final String name;
        public final String desc;

        public ObjectMethod(Class returnType, String name, Class... parameterTypes) {
            this.name = name;
            this.desc = TransformerTools.descriptor(returnType, parameterTypes);
        }

        public boolean equals(String name, String desc) {
            return name.equals(this.name) && desc.equals(this.desc);
        }
    }

}
