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

import static net.sf.retrotranslator.runtime.asm.Opcodes.*;
import net.sf.retrotranslator.runtime.impl.RuntimeTools;
import net.sf.retrotranslator.runtime.java.lang.Enum_;
import net.sf.retrotranslator.runtime.asm.*;

/**
 * @author Taras Puchko
 */
class EnumVisitor extends ClassAdapter {

    private static final String ENUM_NAME = Type.getInternalName(Enum_.class);
    private static final String SET_ENUM_CONSTANTS_NAME = "setEnumConstants";
    private static final String SET_ENUM_CONSTANTS_DESC =
            TransformerTools.descriptor(void.class, Class.class, Enum_[].class);

    private Type type;

    public EnumVisitor(final ClassVisitor cv) {
        super(cv);
    }

    public void visit(final int version, final int access, final String name,
                      final String signature, final String superName, final String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        if ((access & ACC_ENUM) != 0 && ENUM_NAME.equals(superName)) {
            type = TransformerTools.getTypeByInternalName(name);
        }
    }

    public MethodVisitor visitMethod(final int access, final String name,
                                     final String desc, final String signature, final String[] exceptions) {
        MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);
        if (visitor == null || type == null || !name.equals(RuntimeTools.STATIC_NAME)) return visitor;
        return new MethodAdapter(visitor) {

            private boolean alreadyProcessed;

            public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc) {
                if (opcode == INVOKESTATIC && owner.equals(ENUM_NAME) &&
                        name.equals(SET_ENUM_CONSTANTS_NAME) && desc.equals(SET_ENUM_CONSTANTS_DESC)) {
                    alreadyProcessed = true;
                }
                super.visitMethodInsn(opcode, owner, name, desc);
            }

            public void visitInsn(final int opcode) {
                if (opcode == RETURN && !alreadyProcessed) {
                    mv.visitLdcInsn(type);
                    mv.visitMethodInsn(INVOKESTATIC, type.getInternalName(), "values", "()[" + type.getDescriptor());
                    mv.visitMethodInsn(INVOKESTATIC, ENUM_NAME, SET_ENUM_CONSTANTS_NAME, SET_ENUM_CONSTANTS_DESC);
                }
                super.visitInsn(opcode);
            }
        };
    }
}
