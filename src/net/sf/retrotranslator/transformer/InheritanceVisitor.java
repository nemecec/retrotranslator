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

import java.util.Set;

/**
 * @author Taras Puchko
 */
class InheritanceVisitor extends ClassAdapter {

    private final BackportLocator locator;

    public InheritanceVisitor(ClassVisitor visitor, BackportLocator locator) {
        super(visitor);
        this.locator = locator;
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);
        return visitor == null ? null : new InheritanceMethodVisitor(visitor);
    }

    private class InheritanceMethodVisitor extends AbstractMethodVisitor {

        private String deferredArrayType;

        public InheritanceMethodVisitor(MethodVisitor visitor) {
            super(visitor);
        }

        protected void flush() {
            if (deferredArrayType != null) {
                mv.visitTypeInsn(ANEWARRAY, fixArrayType(deferredArrayType));
                deferredArrayType = null;
            }
        }

        public void visitTypeInsn(final int opcode, final String desc) {
            flush();
            if (opcode == CHECKCAST) {
                visitCheckCast(desc);
            } else if (opcode == INSTANCEOF) {
                visitInstanceOf(desc);
            } else if (opcode == ANEWARRAY) {
                deferredArrayType = desc;
            } else {
                mv.visitTypeInsn(opcode, desc);
            }
        }

        public void visitMultiANewArrayInsn(final String desc, final int dims) {
            super.visitMultiANewArrayInsn(fixArrayType(desc), dims);
        }

        public void visitMethodInsn(int opcode, String owner, String name, String desc) {
            if (deferredArrayType != null && opcode == INVOKEVIRTUAL &&
                    name.equals("getClass") && desc.equals(TransformerTools.descriptor(Class.class))) {
                mv.visitTypeInsn(ANEWARRAY, deferredArrayType);
                deferredArrayType = null;
            }
            super.visitMethodInsn(opcode, owner, name, desc);
        }

        private String fixArrayType(String desc) {
            if (desc.charAt(0) != '[') {
                return locator.getImplementations(desc) == null ? desc : Type.getInternalName(Object.class);
            }
            int first = 1;
            while (desc.charAt(first) == '[') first++;
            if (desc.charAt(first) != 'L') return desc;
            int last = desc.length() - 1;
            if (desc.charAt(last) != ';') return desc;
            if (locator.getImplementations(desc.substring(first + 1, last)) == null) return desc;
            return desc.substring(0, first) + Type.getDescriptor(Object.class);
        }

        private void visitCheckCast(String desc) {
            if (desc.charAt(0) == '[') {
                mv.visitTypeInsn(CHECKCAST, fixArrayType(desc));
                return;
            }
            Set<String> implementations = locator.getImplementations(desc);
            if (implementations == null) {
                mv.visitTypeInsn(CHECKCAST, desc);
                return;
            }
            Label exit = new Label();
            for (String name : implementations) {
                visitInsn(DUP);
                mv.visitTypeInsn(INSTANCEOF, name);
                mv.visitJumpInsn(IFNE, exit);
            }
            mv.visitTypeInsn(CHECKCAST, desc);
            mv.visitLabel(exit);
        }

        private void visitInstanceOf(String desc) {
            Set<String> implementations = locator.getImplementations(desc);
            if (implementations == null) {
                mv.visitTypeInsn(INSTANCEOF, desc);
                return;
            }
            Label okLabel = new Label();
            Label exitLabel = new Label();
            mv.visitInsn(DUP);
            mv.visitTypeInsn(INSTANCEOF, desc);
            mv.visitJumpInsn(IFNE, okLabel);
            for (String name : implementations) {
                mv.visitInsn(DUP);
                mv.visitTypeInsn(INSTANCEOF, name);
                mv.visitJumpInsn(IFNE, okLabel);
            }
            mv.visitInsn(POP);
            mv.visitInsn(ICONST_0);
            mv.visitJumpInsn(GOTO, exitLabel);
            mv.visitLabel(okLabel);
            mv.visitInsn(POP);
            mv.visitInsn(ICONST_1);
            mv.visitLabel(exitLabel);
        }

    }
}
