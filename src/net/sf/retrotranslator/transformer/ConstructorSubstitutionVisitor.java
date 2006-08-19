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

/**
 * @author Taras Puchko
 */
class ConstructorSubstitutionVisitor extends ClassAdapter {

    private static final String ILLEGAL_STATE_EXCEPTION = Type.getInternalName(IllegalStateException.class);
    private static final String ILLEGAL_ARGUMENT_EXCEPTION = Type.getInternalName(IllegalArgumentException.class);

    private BackportFactory backportFactory = BackportFactory.getInstance();
    private boolean advanced;

    public ConstructorSubstitutionVisitor(final ClassVisitor cv, boolean advanced) {
        super(cv);
        this.advanced = advanced;
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);
        return visitor == null ? null : new MethodAdapter(visitor) {

            public void visitMethodInsn(final int opcode, final String owner, final String name, String desc) {
                if (opcode == INVOKESPECIAL && name.equals(RuntimeTools.CONSTRUCTOR_NAME)) {
                    if (owner.equals(ILLEGAL_STATE_EXCEPTION) || owner.equals(ILLEGAL_ARGUMENT_EXCEPTION)) {
                        if (initException(desc, owner)) return;
                    }
                    ClassMember converter = backportFactory.getConverter(owner, desc);
                    if (converter != null && (advanced | !converter.advanced)) {
                        mv.visitMethodInsn(INVOKESTATIC, converter.owner, converter.name, converter.desc);
                        desc = Type.getMethodDescriptor(Type.VOID_TYPE, new Type[] {Type.getReturnType(converter.desc)});
                    }
                }
                super.visitMethodInsn(opcode, owner, name, desc);
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
        };
    }

}
