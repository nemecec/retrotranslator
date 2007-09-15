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
public class InheritedConstantVisitor extends ClassAdapter {

    private final ClassReaderFactory factory;

    public InheritedConstantVisitor(ClassVisitor visitor, ClassReaderFactory factory) {
        super(visitor);
        this.factory = factory;
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);
        return visitor == null ? null : new InheritedConstantMethodVisitor(visitor);
    }

    private String getDeclaringInterface(String className, String constantName) {
        ClassReader classReader = factory.findClassReader(className);
        if (classReader == null) {
            return null;
        }
        ConstantSearchingVisitor visitor = new ConstantSearchingVisitor(constantName);
        classReader.accept(visitor, true);
        if (visitor.constantFound) {
            return (visitor.access & Opcodes.ACC_INTERFACE) != 0 ? className : null;
        }
        for (String interfaceName : visitor.interfaces) {
            String result = getDeclaringInterface(interfaceName, constantName);
            if (result != null) {
                return result;
            }
        }
        return visitor.superName != null ? getDeclaringInterface(visitor.superName, constantName) : null;
    }

    private class InheritedConstantMethodVisitor extends MethodAdapter {

        public InheritedConstantMethodVisitor(MethodVisitor visitor) {
            super(visitor);
        }

        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            if (opcode == Opcodes.GETSTATIC) {
                String declaringInterface = getDeclaringInterface(owner, name);
                if (declaringInterface != null) {
                    owner = declaringInterface;
                }
            }
            super.visitFieldInsn(opcode, owner, name, desc);
        }
    }

    private class ConstantSearchingVisitor extends EmptyVisitor {

        private final String constantName;
        public boolean constantFound;
        public int access;
        public String superName;
        public String[] interfaces;

        public ConstantSearchingVisitor(String fieldName) {
            this.constantName = fieldName;
        }

        public void visit(int version, int access, String name,
                          String signature, String superName, String[] interfaces) {
            this.access = access;
            this.superName = superName;
            this.interfaces = interfaces;
        }

        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            if (name.equals(constantName) && (access & Opcodes.ACC_STATIC) != 0) {
                constantFound = true;
            }
            return null;
        }
    }

}
