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

import java.util.*;

/**
 * @author Taras Puchko
 */
class UtilBackportVisitor extends ClassAdapter {

    private static final String BACKPORT_CONCURRENT = BackportFactory.BACKPORT + BackportFactory.CONCURRENT;
    private static final String DELAY_QUEUE_NAME = BACKPORT_CONCURRENT + "DelayQueue";
    private static final String DELAYED_NAME = BACKPORT_CONCURRENT + "Delayed";
    private static final String UTILS_NAME = BACKPORT_CONCURRENT + "helpers/Utils";
    private static final String SYSTEM_NAME = Type.getInternalName(System.class);
    private static final String COLLECTIONS_NAME = Type.getInternalName(Collections.class);
    private static final String BACKPORTED_COLLECTIONS_NAME = BackportFactory.BACKPORT + COLLECTIONS_NAME;

    private static final Map<String, String> FIELDS = new HashMap<String, String>();
    private static final Map<String, String> METHODS = new HashMap<String, String>();

    static {
        FIELDS.put("emptyList", "EMPTY_LIST");
        FIELDS.put("emptyMap", "EMPTY_MAP");
        FIELDS.put("emptySet", "EMPTY_SET");
        putMethod(boolean.class, "addAll", Collection.class, Object[].class);
        putMethod(Collection.class, "checkedCollection", Collection.class, Class.class);
        putMethod(List.class, "checkedList", List.class, Class.class);
        putMethod(Map.class, "checkedMap", Map.class, Class.class, Class.class);
        putMethod(Set.class, "checkedSet", Set.class, Class.class);
        putMethod(SortedMap.class, "checkedSortedMap", SortedMap.class, Class.class, Class.class);
        putMethod(SortedSet.class, "checkedSortedSet", SortedSet.class, Class.class);
        putMethod(boolean.class, "disjoint", Collection.class, Collection.class);
        putMethod(int.class, "frequency", Collection.class, Object.class);
        putMethod(Comparator.class, "reverseOrder", Comparator.class);
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
                    owner = UTILS_NAME;
                } else if (owner.equals(COLLECTIONS_NAME)) {
                    String field = FIELDS.get(name);
                    if (field != null) {
                        mv.visitFieldInsn(Opcodes.GETSTATIC, COLLECTIONS_NAME,
                                field, Type.getReturnType(desc).toString());
                        return;
                    } else if (desc.equals(METHODS.get(name))) {
                        owner = BACKPORTED_COLLECTIONS_NAME;
                    }
                }
                super.visitMethodInsn(opcode, owner, name, desc);
            }
        };
    }

    private static void putMethod(Class returnType, String name, Class... parameterTypes) {
        Type[] argumentTypes = new Type[parameterTypes.length];
        for (int i = 0; i < argumentTypes.length; i++) {
            argumentTypes[i] = Type.getType(parameterTypes[i]);
        }
        METHODS.put(name, Type.getMethodDescriptor(Type.getType(returnType), argumentTypes));
    }

}
