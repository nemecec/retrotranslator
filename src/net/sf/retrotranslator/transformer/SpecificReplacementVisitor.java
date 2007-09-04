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

    private static final String UTILS_NAME = Type.getInternalName(Utils.class);
    private static final String THREAD_NAME = Type.getInternalName(Thread.class);
    private static final String SYSTEM_NAME = Type.getInternalName(System.class);
    private static final String CONDITION_NAME = Type.getInternalName(Condition.class);
    private static final String DELAY_QUEUE_NAME = Type.getInternalName(DelayQueue.class);
    private static final String SOFT_REFERENCE_NAME = Type.getInternalName(SoftReference.class);
    private static final String WEAK_REFERENCE_NAME = Type.getInternalName(WeakReference.class);
    private static final String ORIGINAL_ARRAYS_NAME = Type.getInternalName(java.util.Arrays.class);
    private static final String ORIGINAL_COLLECTIONS_NAME = Type.getInternalName(java.util.Collections.class);
    private static final String REENTRANT_READ_WRITE_LOCK_NAME = Type.getInternalName(ReentrantReadWriteLock.class);

    private static final String BACKPORTED_ARRAYS_NAME =
            Type.getInternalName(edu.emory.mathcs.backport.java.util.Arrays.class);
    private static final String BACKPORTED_COLLECTIONS_NAME =
            Type.getInternalName(edu.emory.mathcs.backport.java.util.Collections.class);
    private static final String UNCAUGHT_EXCEPTION_HANDLER_KEY = "handleUncaughtException" +
            TransformerTools.descriptor(void.class, Throwable.class);

    private static final Set<String> ARRAYS_METHODS = getArrayMethods();
    private static final Set<String> COLLECTIONS_METHODS = getCollectionMethods();
    private static final Map<String, String> COLLECTIONS_FIELDS = getCollectionFields();

    private final ReplacementLocator locator;
    private final OperationMode mode;
    private MemberReplacement uncaughtExceptionHandler;

    public SpecificReplacementVisitor(ClassVisitor visitor, ReplacementLocator locator, OperationMode mode) {
        super(visitor);
        this.locator = locator;
        this.mode = mode;
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        uncaughtExceptionHandler = getUncaughtExceptionHandler(superName);
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);
        if (visitor == null) {
            return null;
        }
        visitor = new SpecificReplacementMethodVisitor(visitor);
        if (uncaughtExceptionHandler != null &&
                name.equals("run") && desc.equals(TransformerTools.descriptor(void.class))) {
            visitor = new RunMethodVisitor(visitor);
        }
        return visitor;
    }

    private class RunMethodVisitor extends MethodAdapter {

        private final Label end = new Label();

        public RunMethodVisitor(MethodVisitor visitor) {
            super(visitor);
        }

        public void visitCode() {
            Label start = new Label();
            mv.visitTryCatchBlock(start, end, end, Type.getInternalName(Throwable.class));
            mv.visitLabel(start);
            super.visitCode();

        }

        public void visitMaxs(final int maxStack, final int maxLocals) {
            mv.visitLabel(end);
            mv.visitMethodInsn(INVOKESTATIC, uncaughtExceptionHandler.getOwner(),
                    uncaughtExceptionHandler.getName(), uncaughtExceptionHandler.getDesc());
            mv.visitInsn(RETURN);
            super.visitMaxs(maxStack, maxLocals);
        }
    }

    private class SpecificReplacementMethodVisitor extends MethodAdapter {

        public SpecificReplacementMethodVisitor(MethodVisitor visitor) {
            super(visitor);
        }

        public void visitMethodInsn(int opcode, String owner, String name, String desc) {
            if (fixReference(opcode, owner, name, desc)) {
                return;
            }
            if (fixNanos(opcode, owner, name, desc)) {
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
            if (fixArrays(opcode, owner, name, desc)) {
                return;
            }
            super.visitMethodInsn(opcode, owner, name, desc);
        }

        private boolean fixReference(int opcode, String owner, String name, String desc) {
            if (opcode != INVOKESPECIAL || !name.equals(RuntimeTools.CONSTRUCTOR_NAME)) {
                return false;
            }
            if (owner.equals(SOFT_REFERENCE_NAME)) {
                if (!mode.isSupportedFeature("SoftReference.NullReferenceQueue")) {
                    return false;
                }
            } else if (owner.equals(WEAK_REFERENCE_NAME)) {
                if (!mode.isSupportedFeature("WeakReference.NullReferenceQueue")) {
                    return false;
                }
            } else {
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

        private boolean fixNanos(int opcode, String owner, String name, String desc) {
            if (owner.equals(SYSTEM_NAME) & name.equals("nanoTime")) {
                mv.visitMethodInsn(opcode, UTILS_NAME, name, desc);
                return true;
            }
            if (owner.equals(CONDITION_NAME) & name.equals("awaitNanos")) {
                mv.visitMethodInsn(INVOKESTATIC, UTILS_NAME, name,
                        TransformerTools.descriptor(long.class, Condition.class, long.class));
                return true;
            }
            return false;
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
            if (COLLECTIONS_METHODS.contains(name + desc)) {
                mv.visitMethodInsn(opcode, BACKPORTED_COLLECTIONS_NAME, name, desc);
                return true;
            }
            return false;
        }

        private boolean fixArrays(int opcode, String owner, String name, String desc) {
            if (!owner.equals(ORIGINAL_ARRAYS_NAME)) {
                return false;
            }
            if (ARRAYS_METHODS.contains(name + desc)) {
                mv.visitMethodInsn(opcode, BACKPORTED_ARRAYS_NAME, name, desc);
                return true;
            }
            return false;
        }
    }

    private MemberReplacement getUncaughtExceptionHandler(String superName) {
        if (!THREAD_NAME.equals(superName)) {
            return null;
        }
        if (!mode.isSupportedFeature("Thread.setDefaultUncaughtExceptionHandler") &&
                !mode.isSupportedFeature("Thread.setUncaughtExceptionHandler")) {
            return null;
        }
        ClassReplacement threadReplacement = locator.getReplacement(THREAD_NAME);
        if (threadReplacement == null) {
            return null;
        }
        return threadReplacement.getMethodReplacements().get(UNCAUGHT_EXCEPTION_HANDLER_KEY);
    }

    private static Set<String> getArrayMethods() {
        Set<String> result = new HashSet<String>();
        for (Class arrayType : new Class[]{boolean[].class, byte[].class, char[].class,
                double[].class, float[].class, int[].class, long[].class, short[].class, Object[].class}) {
            result.add("copyOf" +
                    TransformerTools.descriptor(arrayType, arrayType, int.class));
            result.add("copyOfRange" +
                    TransformerTools.descriptor(arrayType, arrayType, int.class, int.class));
        }
        result.add("copyOf" +
                TransformerTools.descriptor(Object[].class, Object[].class, int.class, Class.class));
        result.add("copyOfRange" +
                TransformerTools.descriptor(Object[].class, Object[].class, int.class, int.class, Class.class));
        return result;
    }

    private static Set<String> getCollectionMethods() {
        Set<String> result = new HashSet<String>();
        for (String method : new String[]{
                getMethod(boolean.class, "addAll", Collection.class, Object[].class),
                getMethod(Collection.class, "checkedCollection", Collection.class, Class.class),
                getMethod(List.class, "checkedList", List.class, Class.class),
                getMethod(Map.class, "checkedMap", Map.class, Class.class, Class.class),
                getMethod(Set.class, "checkedSet", Set.class, Class.class),
                getMethod(SortedMap.class, "checkedSortedMap", SortedMap.class, Class.class, Class.class),
                getMethod(SortedSet.class, "checkedSortedSet", SortedSet.class, Class.class),
                getMethod(boolean.class, "disjoint", Collection.class, Collection.class),
                getMethod(int.class, "frequency", Collection.class, Object.class),
                getMethod(Comparator.class, "reverseOrder", Comparator.class),
                getMethod(Set.class, "newSetFromMap", Map.class)}) {
            result.add(method);
        }
        return result;
    }

    private static String getMethod(Class returnType, String name, Class... parameterTypes) {
        return name + TransformerTools.descriptor(returnType, parameterTypes);
    }

    private static Map<String, String> getCollectionFields() {
        Map<String, String> result = new HashMap<String, String>();
        result.put("emptyList", "EMPTY_LIST");
        result.put("emptyMap", "EMPTY_MAP");
        result.put("emptySet", "EMPTY_SET");
        return result;
    }

}
