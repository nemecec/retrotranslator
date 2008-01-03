/***
 * Retrotranslator: a Java bytecode transformer that translates Java classes
 * compiled with JDK 5.0 into classes that can be run on JVM 1.4.
 *
 * Copyright (c) 2005 - 2008 Taras Puchko
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
import edu.emory.mathcs.backport.java.util.concurrent.locks.*;
import java.lang.ref.*;
import java.util.Collections;
import net.sf.retrotranslator.runtime.asm.*;
import static net.sf.retrotranslator.runtime.asm.Opcodes.*;
import net.sf.retrotranslator.runtime.impl.RuntimeTools;

/**
 * @author Taras Puchko
 */
class SpecificReplacementVisitor extends ClassAdapter {

    private static final String THREAD_NAME = Type.getInternalName(Thread.class);
    private static final String DELAY_QUEUE_NAME = Type.getInternalName(DelayQueue.class);
    private static final String SOFT_REFERENCE_NAME = Type.getInternalName(SoftReference.class);
    private static final String WEAK_REFERENCE_NAME = Type.getInternalName(WeakReference.class);
    private static final String COLLECTIONS_NAME = Type.getInternalName(Collections.class);
    private static final String REENTRANT_READ_WRITE_LOCK_NAME = Type.getInternalName(ReentrantReadWriteLock.class);
    private static final MemberKey UNCAUGHT_EXCEPTION_HANDLER_KEY =
            new MemberKey(true, "handleUncaughtException", TransformerTools.descriptor(void.class, Throwable.class));

    private final ClassVersion target;
    private final ReplacementLocator locator;
    private final OperationMode mode;
    private MemberReplacement uncaughtExceptionHandler;

    public SpecificReplacementVisitor(ClassVisitor visitor, ClassVersion target,
                                      ReplacementLocator locator, OperationMode mode) {
        super(visitor);
        this.target = target;
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

        private final Label start = new Label();

        public RunMethodVisitor(MethodVisitor visitor) {
            super(visitor);
        }

        public void visitCode() {
            mv.visitLabel(start);
            super.visitCode();
        }

        public void visitMaxs(final int maxStack, final int maxLocals) {
            Label end = new Label();
            mv.visitLabel(end);
            mv.visitMethodInsn(INVOKESTATIC, uncaughtExceptionHandler.getOwner(),
                    uncaughtExceptionHandler.getName(), uncaughtExceptionHandler.getDesc());
            mv.visitInsn(RETURN);
            mv.visitTryCatchBlock(start, end, end, Type.getInternalName(Throwable.class));
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
            if (!target.isBefore(ClassVersion.VERSION_15)) {
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
            if (opcode != INVOKESTATIC || !owner.equals(COLLECTIONS_NAME)) {
                return false;
            }
            String field = getReplacementField(name);
            if (field == null) {
                return false;
            }
            mv.visitFieldInsn(GETSTATIC, COLLECTIONS_NAME, field, Type.getReturnType(desc).toString());
            return true;
        }
    }

    private String getReplacementField(String methodName) {
        if (!target.isBefore(ClassVersion.VERSION_15)) {
            return null;
        }
        if (target.isBefore(ClassVersion.VERSION_12)) {
            return null;
        }
        if (methodName.equals("emptyList")) {
            return "EMPTY_LIST";
        }
        if (methodName.equals("emptySet")) {
            return "EMPTY_SET";
        }
        if (target.isBefore(ClassVersion.VERSION_13)) {
            return null;
        }
        if (methodName.equals("emptyMap")) {
            return "EMPTY_MAP";
        }
        return null;
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

}
