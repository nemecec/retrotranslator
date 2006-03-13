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
import edu.emory.mathcs.backport.java.util.concurrent.helpers.Utils;
import net.sf.retrotranslator.runtime.asm.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Taras Puchko
 */
class UtilBackportVisitor extends ClassAdapter {

    private static final String DELAY_QUEUE_NAME = Type.getInternalName(DelayQueue.class);
    private static final String DELAYED_NAME = Type.getInternalName(Delayed.class);
    private static final String SYSTEM_NAME = Type.getInternalName(System.class);
    private static final String COLLECTIONS_NAME = Type.getInternalName(Collections.class);

    private static final Map<String, String> FIELDS = new HashMap<String, String>();

    static {
        FIELDS.put("emptyList", "EMPTY_LIST");
        FIELDS.put("emptyMap", "EMPTY_MAP");
        FIELDS.put("emptySet", "EMPTY_SET");
    }

    private static DescriptorTransformer DELAYED_TRANSFORMER = new DescriptorTransformer() {
        protected String transformInternalName(String internalName) {
            return DELAYED_NAME.equals(internalName) ? Type.getInternalName(Object.class) : internalName;
        }
    };

    public UtilBackportVisitor(final ClassVisitor cv) {
        super(cv);
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        return new MethodAdapter(super.visitMethod(access, name, desc, signature, exceptions)) {
            public void visitMethodInsn(int opcode, String owner, String name, String desc) {
                if (owner.equals(DELAY_QUEUE_NAME)) {
                    desc = DELAYED_TRANSFORMER.transformDescriptor(desc);
                } else if (owner.equals(SYSTEM_NAME) & name.equals("nanoTime")) {
                    owner = Type.getInternalName(Utils.class);
                } else if (owner.equals(COLLECTIONS_NAME)) {
                    String field = FIELDS.get(name);
                    if (field != null) {
                        mv.visitFieldInsn(Opcodes.GETSTATIC, COLLECTIONS_NAME,
                                field, Type.getReturnType(desc).toString());
                        return;
                    }
                }
                super.visitMethodInsn(opcode, owner, name, desc);
            }
        };
    }
}
