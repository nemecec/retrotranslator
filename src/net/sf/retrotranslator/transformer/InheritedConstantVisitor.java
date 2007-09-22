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
import net.sf.retrotranslator.runtime.impl.EmptyVisitor;

/**
 * @author Taras Puchko
 */
class InheritedConstantVisitor extends ClassAdapter {

    private final ReplacementLocator locator;

    public InheritedConstantVisitor(ClassVisitor visitor, ReplacementLocator locator) {
        super(visitor);
        this.locator = locator;
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);
        return visitor == null ? null : new MethodAdapter(visitor) {
            public void visitFieldInsn(int opcode, String owner, String name, String desc) {
                super.visitFieldInsn(opcode, fixFieldOwner(opcode, owner, name), name, desc);
            }
        };
    }

    private String fixFieldOwner(int opcode, String owner, String name) {
        if (opcode == Opcodes.GETSTATIC) {
            String fieldOwner = findFieldOwner(owner, name);
            if (fieldOwner != null) {
                return fieldOwner;
            }
        }
        return owner;
    }

    private String findFieldOwner(String className, String fieldName) {
        String uniqueTypeName = locator.getUniqueTypeName(className);
        ClassReader classReader = locator.getClassReaderFactory().findClassReader(uniqueTypeName);
        if (classReader == null) {
            return null;
        }
        FieldSearchingVisitor visitor = new FieldSearchingVisitor(fieldName);
        classReader.accept(visitor, true);
        if (visitor.fieldFound) {
            return (visitor.access & Opcodes.ACC_INTERFACE) != 0 ? uniqueTypeName : null;
        }
        if (visitor.interfaces != null) {
            for (String interfaceName : visitor.interfaces) {
                String result = findFieldOwner(interfaceName, fieldName);
                if (result != null) {
                    return result;
                }
            }
        }
        return visitor.superName != null ? findFieldOwner(visitor.superName, fieldName) : null;
    }

    private class FieldSearchingVisitor extends EmptyVisitor {

        private final String fieldName;
        public boolean fieldFound;
        public int access;
        public String superName;
        public String[] interfaces;

        public FieldSearchingVisitor(String fieldName) {
            this.fieldName = fieldName;
        }

        public void visit(int version, int access, String name,
                          String signature, String superName, String[] interfaces) {
            this.access = access;
            this.superName = superName;
            this.interfaces = interfaces;
        }

        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            if (name.equals(fieldName) && (access & Opcodes.ACC_STATIC) != 0) {
                fieldFound = true;
            }
            return null;
        }
    }

}
