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

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author Taras Puchko
 */
public class ConstructorSubstitutionVisitor extends ClassAdapter {

    private static final String BIG_DECIMAL_NAME = Type.getInternalName(BigDecimal.class);
    private static final String BIG_INTEGER_NAME = Type.getInternalName(BigInteger.class);
    private static final String BIG_INTEGER_DESCRIPTOR = Type.getDescriptor(BigInteger.class);

    public ConstructorSubstitutionVisitor(final ClassVisitor cv) {
        super(cv);
    }

    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        return new MethodAdapter(super.visitMethod(access, name, desc, signature, exceptions)) {
            public void visitMethodInsn(final int opcode, final String owner, final String name, String desc) {
                if (opcode == INVOKESPECIAL && owner.equals(BIG_DECIMAL_NAME) && name.equals(TypeTools.CONSTRUCTOR_NAME)) {
                    if (desc.equals("(J)V") || desc.equals("(I)V")) {
                        if (desc.equals("(I)V")) {
                            mv.visitInsn(I2L);
                        }
                        mv.visitMethodInsn(INVOKESTATIC, BIG_INTEGER_NAME, "valueOf", "(J)" + BIG_INTEGER_DESCRIPTOR);
                        desc = "(" + BIG_INTEGER_DESCRIPTOR + ")V";
                    }
                }
                super.visitMethodInsn(opcode, owner, name, desc);
            }
        };
    }
}
