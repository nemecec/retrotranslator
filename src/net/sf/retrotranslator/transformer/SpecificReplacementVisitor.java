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

import edu.emory.mathcs.backport.java.util.concurrent.*;
import edu.emory.mathcs.backport.java.util.concurrent.helpers.Utils;
import edu.emory.mathcs.backport.java.util.concurrent.locks.*;
import java.lang.ref.*;
import java.util.*;
import net.sf.retrotranslator.runtime.asm.*;
import static net.sf.retrotranslator.runtime.asm.Opcodes.*;
import net.sf.retrotranslator.runtime.impl.RuntimeTools;

/**
 * @author Taras Puchko
 */
class SpecificReplacementVisitor extends ClassAdapter {

    private static final String ILLEGAL_STATE_EXCEPTION = Type.getInternalName(IllegalStateException.class);
    private static final String ILLEGAL_ARGUMENT_EXCEPTION = Type.getInternalName(IllegalArgumentException.class);
    private static final String SOFT_REFERENCE = Type.getInternalName(SoftReference.class);
    private static final String WEAK_REFERENCE = Type.getInternalName(WeakReference.class);

    private static final String SYSTEM_NAME = Type.getInternalName(System.class);
    private static final String CONDITION_NAME = Type.getInternalName(Condition.class);
    private static final String DELAY_QUEUE_NAME = Type.getInternalName(DelayQueue.class);
    private static final String REENTRANT_READ_WRITE_LOCK_NAME = Type.getInternalName(ReentrantReadWriteLock.class);

    private static final String ORIGINAL_COLLECTIONS_NAME = Type.getInternalName(java.util.Collections.class);
    private static final String BACKPORTED_COLLECTIONS_NAME =
            Type.getInternalName(edu.emory.mathcs.backport.java.util.Collections.class);
    private static final Map<String, String> COLLECTIONS_FIELDS = new HashMap<String, String>();
    private static final Map<String, String> COLLECTIONS_METHODS = new HashMap<String, String>();

    private final boolean advanced;

    static {
        COLLECTIONS_FIELDS.put("emptyList", "EMPTY_LIST");
        COLLECTIONS_FIELDS.put("emptyMap", "EMPTY_MAP");
        COLLECTIONS_FIELDS.put("emptySet", "EMPTY_SET");
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
        putMethod(Set.class, "newSetFromMap", Map.class);
    }

    public SpecificReplacementVisitor(ClassVisitor visitor, boolean advanced) {
        super(visitor);
        this.advanced = advanced;
    }

    private static void putMethod(Class returnType, String name, Class... parameterTypes) {
        COLLECTIONS_METHODS.put(name, TransformerTools.descriptor(returnType, parameterTypes));
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);
        return visitor == null ? null : new ConstructorReplacementMethodVisitor(visitor);
    }

    private class ConstructorReplacementMethodVisitor extends MethodAdapter {

        public ConstructorReplacementMethodVisitor(MethodVisitor visitor) {
            super(visitor);
        }

        public void visitMethodInsn(int opcode, String owner, String name, String desc) {
            if (opcode == INVOKESPECIAL && name.equals(RuntimeTools.CONSTRUCTOR_NAME)) {
                if (fixException(owner, desc)) {
                    return;
                }
                if (fixReference(owner, desc)) {
                    return;
                }
            }
            if (owner.equals(SYSTEM_NAME) & name.equals("nanoTime")) {
                mv.visitMethodInsn(opcode, Type.getInternalName(Utils.class), name, desc);
                return;
            }
            if (owner.equals(CONDITION_NAME) & name.equals("awaitNanos")) {
                mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Utils.class), name,
                        TransformerTools.descriptor(long.class, Condition.class, long.class));
                return;
            }
            if (fixDelayQueue(opcode, owner, name, desc)) {
                return;
            }
            if (fixLock(opcode, owner, name, desc)) {
                return;
            }
            if (fixCollections(opcode, owner, name, desc)) {
                return;
            }
            super.visitMethodInsn(opcode, owner, name, desc);
        }

        private boolean fixDelayQueue(int opcode, String owner, String name, String desc) {
            if (!owner.equals(DELAY_QUEUE_NAME)) {
                return false;
            }
            String fixedDesc = new NameTranslator() {
                protected String typeName(String s) {
                    return Type.getInternalName(Delayed.class).equals(s) ?
                            Type.getInternalName(Object.class) : s;
                }
            }.methodDescriptor(desc);
            mv.visitMethodInsn(opcode, owner, name, fixedDesc);
            Type returnType = Type.getReturnType(desc);
            if (returnType.equals(Type.getType(Delayed.class))) {
                mv.visitTypeInsn(CHECKCAST, returnType.getInternalName());
            }
            return true;
        }

        private boolean fixLock(int opcode, String owner, String name, String desc) {
            if (!owner.equals(REENTRANT_READ_WRITE_LOCK_NAME)) {
                return false;
            }
            Type returnType = Type.getReturnType(desc);
            if (returnType.equals(Type.getType(ReentrantReadWriteLock.ReadLock.class)) ||
                    returnType.equals(Type.getType(ReentrantReadWriteLock.WriteLock.class))) {
                desc = Type.getMethodDescriptor(Type.getType(Lock.class), Type.getArgumentTypes(desc));
                mv.visitMethodInsn(opcode, owner, name, desc);
                mv.visitTypeInsn(CHECKCAST, returnType.getInternalName());
                return true;
            }
            return false;
        }

        private boolean fixCollections(int opcode, String owner, String name, String desc) {
            if (!owner.equals(ORIGINAL_COLLECTIONS_NAME)) {
                return false;
            }
            String field = COLLECTIONS_FIELDS.get(name);
            if (field != null) {
                mv.visitFieldInsn(Opcodes.GETSTATIC, ORIGINAL_COLLECTIONS_NAME,
                        field, Type.getReturnType(desc).toString());
                return true;
            }
            if (desc.equals(COLLECTIONS_METHODS.get(name))) {
                mv.visitMethodInsn(opcode, BACKPORTED_COLLECTIONS_NAME, name, desc);
                return true;
            }
            return false;
        }

        private boolean fixException(String owner, String desc) {
            if (!owner.equals(ILLEGAL_STATE_EXCEPTION) && !owner.equals(ILLEGAL_ARGUMENT_EXCEPTION)) {
                return false;
            }
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

        private boolean fixReference(String owner, String desc) {
            if (!advanced || (!owner.equals(SOFT_REFERENCE) && !owner.equals(WEAK_REFERENCE))) {
                return false;
            }
            if (!desc.equals(TransformerTools.descriptor(void.class, Object.class, ReferenceQueue.class))) {
                return false;
            }
            Label notNullLabel = new Label();
            Label continueLabel = new Label();
            mv.visitInsn(DUP);
            mv.visitJumpInsn(IFNONNULL, notNullLabel);
            mv.visitInsn(POP);
            mv.visitMethodInsn(INVOKESPECIAL, owner,
                    RuntimeTools.CONSTRUCTOR_NAME, TransformerTools.descriptor(void.class, Object.class));
            mv.visitJumpInsn(GOTO, continueLabel);
            mv.visitLabel(notNullLabel);
            mv.visitMethodInsn(INVOKESPECIAL, owner, RuntimeTools.CONSTRUCTOR_NAME, desc);
            mv.visitLabel(continueLabel);
            return true;
        }
    }

}
