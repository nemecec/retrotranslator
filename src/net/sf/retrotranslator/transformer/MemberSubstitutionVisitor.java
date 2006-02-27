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

import edu.emory.mathcs.backport.java.util.concurrent.DelayQueue;
import edu.emory.mathcs.backport.java.util.concurrent.Delayed;
import net.sf.retrotranslator.runtime.asm.*;
import static net.sf.retrotranslator.runtime.asm.Opcodes.*;

/**
 * @author Taras Puchko
 */
class MemberSubstitutionVisitor extends ClassAdapter {

    private static final String DELAY_QUEUE_NAME = Type.getInternalName(DelayQueue.class);
    private static final String DELAYED_NAME = Type.getInternalName(Delayed.class);

    private static DescriptorTransformer DELAYED_TRANSFORMER = new DescriptorTransformer() {
        protected String transformInternalName(String internalName) {
            return DELAYED_NAME.equals(internalName) ? Type.getInternalName(Object.class) : internalName;
        }
    };

    private String currentClass;

    public MemberSubstitutionVisitor(final ClassVisitor cv) {
        super(cv);
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        currentClass = name;
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        return new MethodAdapter(super.visitMethod(access, name, desc, signature, exceptions)) {
            public void visitMethodInsn(int opcode, String owner, String name, String desc) {
                if (!owner.equals(currentClass)) {
                    ClassMember method = BackportFactory.getMethod(opcode == INVOKESTATIC, owner, name, desc);
                    if (method != null) {
                        opcode = method.isStatic ? INVOKESTATIC : INVOKEINTERFACE;
                        owner = method.owner;
                        name = method.name;
                        desc = method.desc;
                    }
                }
                if (owner.equals(DELAY_QUEUE_NAME)) {
                    desc = DELAYED_TRANSFORMER.transformDescriptor(desc);
                }
                super.visitMethodInsn(opcode, owner, name, desc);
            }
        };
    }

}
