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

import net.sf.retrotranslator.runtime.impl.RuntimeTools;
import net.sf.retrotranslator.runtime.asm.*;
import static net.sf.retrotranslator.runtime.asm.Opcodes.*;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author Taras Puchko
 */
public class ConstructorSubstitutionVisitor extends ClassAdapter {

    private static final String BIG_DECIMAL = Type.getInternalName(BigDecimal.class);
    private static final String ILLEGAL_STATE_EXCEPTION = Type.getInternalName(IllegalStateException.class);

    public ConstructorSubstitutionVisitor(final ClassVisitor cv) {
        super(cv);
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        return new MethodAdapter(super.visitMethod(access, name, desc, signature, exceptions)) {

            public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc) {
                if (opcode == INVOKESPECIAL && name.equals(RuntimeTools.CONSTRUCTOR_NAME)) {
                    if (owner.equals(BIG_DECIMAL) && initBigDecimal(desc)) return;
                    if (owner.equals(ILLEGAL_STATE_EXCEPTION) && initIllegalStateException(desc)) return;
                }
                super.visitMethodInsn(opcode, owner, name, desc);
            }

            private boolean initBigDecimal(String desc) {
                boolean longParameter = desc.equals(TransformerTools.descriptor(void.class, long.class));
                boolean intParameter = desc.equals(TransformerTools.descriptor(void.class, int.class));
                if (!longParameter && !intParameter) return false;
                if (intParameter) mv.visitInsn(I2L);
                mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(BigInteger.class),
                        "valueOf", TransformerTools.descriptor(BigInteger.class, long.class));
                mv.visitMethodInsn(INVOKESPECIAL, BIG_DECIMAL,
                        RuntimeTools.CONSTRUCTOR_NAME, TransformerTools.descriptor(void.class, BigInteger.class));
                return true;
            }

            private boolean initIllegalStateException(String desc) {
                boolean oneParameter = desc.equals(TransformerTools.descriptor(void.class, Throwable.class));
                boolean twoParameters = desc.equals(TransformerTools.descriptor(void.class, String.class, Throwable.class));
                if (!oneParameter && !twoParameters) return false;
                if (oneParameter) {
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
                } else {
                    mv.visitInsn(DUP_X2);
                    mv.visitInsn(POP);
                    mv.visitInsn(SWAP);
                    mv.visitInsn(DUP_X2);
                    mv.visitInsn(SWAP);
                }
                mv.visitMethodInsn(INVOKESPECIAL, ILLEGAL_STATE_EXCEPTION,
                        RuntimeTools.CONSTRUCTOR_NAME, TransformerTools.descriptor(void.class, String.class));
                mv.visitMethodInsn(INVOKEVIRTUAL, ILLEGAL_STATE_EXCEPTION,
                        "initCause", TransformerTools.descriptor(Throwable.class, Throwable.class));
                mv.visitInsn(POP);
                return true;
            }

        };
    }

}
