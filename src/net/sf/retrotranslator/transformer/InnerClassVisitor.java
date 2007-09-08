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
import static net.sf.retrotranslator.runtime.impl.RuntimeTools.CONSTRUCTOR_NAME;
import net.sf.retrotranslator.runtime.asm.*;

/**
 * @author Taras Puchko
 */
class InnerClassVisitor extends ClassAdapter {

    private String thisName;
    private String superName;

    public InnerClassVisitor(ClassVisitor visitor) {
        super(visitor);
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.thisName = name;
        this.superName = superName;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        if (methodVisitor != null && superName != null && name.equals(CONSTRUCTOR_NAME)) {
            return new InnerClassMethodVisitor(methodVisitor);
        }
        return methodVisitor;
    }

    private class InnerClassMethodVisitor extends AbstractMethodVisitor {

        private boolean initialized;
        private int thisCount;
        private int superCount;
        private Integer firstLoad;
        private Integer secondLoad;
        private String fieldName;
        private String fieldType;

        public InnerClassMethodVisitor(MethodVisitor visitor) {
            super(visitor);
        }

        protected void flush() {
            if (!initialized && fieldName != null) {
                return;
            }
            if (firstLoad == null) {
                return;
            }
            mv.visitVarInsn(ALOAD, firstLoad);
            firstLoad = null;
            if (secondLoad == null) {
                return;
            }
            mv.visitVarInsn(ALOAD, secondLoad);
            secondLoad = null;
            if (fieldName == null) {
                return;
            }
            mv.visitFieldInsn(PUTFIELD, thisName, fieldName, fieldType);
            fieldName = null;
            fieldType = null;
        }

        public void visitVarInsn(int opcode, int var) {
            if (!initialized && opcode == ALOAD) {
                if (firstLoad == null) {
                    firstLoad = var;
                    return;
                }
                if (secondLoad == null) {
                    secondLoad = var;
                    return;
                }
            }
            super.visitVarInsn(opcode, var);
        }

        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            if (!initialized && opcode == PUTFIELD && owner.equals(thisName) && secondLoad != null) {
                fieldName = name;
                fieldType = desc;
                return;
            }
            super.visitFieldInsn(opcode, owner, name, desc);
        }

        public void visitTypeInsn(int opcode, String desc) {
            super.visitTypeInsn(opcode, desc);
            if (initialized || opcode != NEW) {
                return;
            }
            if (desc.equals(thisName)) {
                thisCount++;
            }
            if (desc.equals(superName)) {
                superCount++;
            }
        }

        public void visitMethodInsn(int opcode, String owner, String name, String desc) {
            super.visitMethodInsn(opcode, owner, name, desc);
            if (initialized || opcode != INVOKESPECIAL || !name.equals(CONSTRUCTOR_NAME)) {
                return;
            }
            if (owner.equals(thisName)) {
                if (thisCount > 0) {
                    thisCount--;
                } else {
                    initialized = true;
                }
            }
            if (owner.equals(superName)) {
                if (superCount > 0) {
                    superCount--;
                } else {
                    initialized = true;
                }
            }
        }

        public void visitEnd() {
            super.visitEnd();
            if (!initialized) {
                throw new IllegalStateException("Constructor not called.");
            }
        }
    }

}
