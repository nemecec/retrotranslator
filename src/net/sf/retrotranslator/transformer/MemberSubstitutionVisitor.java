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

/**
 * @author Taras Puchko
 */
class MemberSubstitutionVisitor extends ClassAdapter {

    private BackportFactory backportFactory = BackportFactory.getInstance();
    private String currentClass;
    private boolean advanced;

    public MemberSubstitutionVisitor(boolean advanced, final ClassVisitor cv) {
        super(cv);
        this.advanced = advanced;
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        currentClass = name;
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);
        return visitor == null ? null : new MethodAdapter(visitor) {

            public void visitMethodInsn(int opcode, String owner, String name, String desc) {
                if (!owner.equals(currentClass)) {
                    ClassMember method = backportFactory.getMethod(opcode == INVOKESTATIC, owner, name, desc);
                    if (method != null && !method.owner.equals(currentClass) && (advanced | !method.advanced)) {
                        opcode = method.isStatic ? INVOKESTATIC : INVOKEINTERFACE;
                        owner = method.owner;
                        name = method.name;
                        desc = method.desc;
                    }
                }
                super.visitMethodInsn(opcode, owner, name, desc);
            }

            public void visitFieldInsn(int opcode, String owner, String name, String desc) {
                if (opcode == GETSTATIC || opcode == PUTSTATIC) {
                    ClassMember field = backportFactory.getField(owner, name, desc);
                    if (field != null && !field.owner.equals(currentClass) && (advanced | !field.advanced)) {
                        owner = field.owner;
                        name = field.name;
                        desc = field.desc;
                    }
                }
                super.visitFieldInsn(opcode, owner, name, desc);
            }
        };
    }

}
