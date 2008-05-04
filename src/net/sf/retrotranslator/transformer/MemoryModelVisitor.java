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

import java.util.*;
import net.sf.retrotranslator.runtime.asm.*;
import static net.sf.retrotranslator.runtime.asm.Opcodes.*;
import net.sf.retrotranslator.runtime.asm.commons.LocalVariablesSorter;
import net.sf.retrotranslator.runtime.impl.*;

/**
 * @author Taras Puchko
 */
public class MemoryModelVisitor extends ClassAdapter {

    private static final int ACCESS_MASK = ACC_PUBLIC | ACC_PRIVATE | ACC_PROTECTED;
    private static final Type OBJECT_TYPE = Type.getType(Object.class);

    private final Map<String, Map<String, FieldInfo>> locks = new HashMap<String, Map<String, FieldInfo>>();
    private final TargetEnvironment environment;
    private final boolean syncvolatile;
    private final boolean syncfinal;
    private String internalName;
    private boolean initialized;

    public MemoryModelVisitor(ClassVisitor visitor, TargetEnvironment environment,
                              boolean syncvolatile, boolean syncfinal) {
        super(visitor);
        this.environment = environment;
        this.syncvolatile = syncvolatile;
        this.syncfinal = syncfinal;
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        internalName = name;
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);
        if (name.equals(RuntimeTools.STATIC_NAME)) {
            initialized = true;
            List<FieldInfo> locks = getNewLocks(internalName);
            if (!locks.isEmpty()) {
                visitor = new LockIntroductionVisitor(visitor, locks);
            }
        }
        return new SynchronizationVisitor(access, desc, visitor);
    }

    public void visitEnd() {
        if (!initialized) {
            List<FieldInfo> locks = getNewLocks(internalName);
            if (!locks.isEmpty()) {
                MethodVisitor visitor = new LockIntroductionVisitor(super.visitMethod(ACC_STATIC,
                        RuntimeTools.STATIC_NAME, TransformerTools.descriptor(void.class), null, null), locks);
                visitor.visitCode();
                visitor.visitInsn(RETURN);
                visitor.visitMaxs(0, 0);
                visitor.visitEnd();
            }
        }
        super.visitEnd();
    }

    private List<FieldInfo> getNewLocks(String owner) {
        List<FieldInfo> result = new ArrayList<FieldInfo>();
        for (FieldInfo info : getLocks(owner).values()) {
            if (info.desc == null) {
                result.add(info);
            }
        }
        return result;
    }

    private Map<String, FieldInfo> getLocks(String owner) {
        Map<String, FieldInfo> result = locks.get(owner);
        if (result == null) {
            LockDetectionVisitor lockDetectionVisitor = new LockDetectionVisitor();
            ClassReader reader = environment.findClassReader(owner);
            if (reader != null) {
                reader.accept(lockDetectionVisitor, true);
            }
            result = lockDetectionVisitor.getLocks();
            locks.put(owner, result);
        }
        return result;
    }

    private static boolean isSet(int access, int flag) {
        return (access & flag) != 0;
    }

    private static class FieldInfo {

        public final int access;
        public final String name;
        public final String desc;

        private FieldInfo(int access, String name, String desc) {
            this.access = access;
            this.name = name;
            this.desc = desc;
        }

    }

    private class LockDetectionVisitor extends EmptyVisitor {

        private final Map<String, FieldInfo> fields = new HashMap<String, FieldInfo>();
        private boolean isJava5Class;

        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            isJava5Class = ClassVersion.VERSION_15.getVersion() == version || ClassVersion.VERSION_15.isBefore(version);
            super.visit(version, access, name, signature, superName, interfaces);
        }

        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            fields.put(name, new FieldInfo(access, name, desc));
            return super.visitField(access, name, desc, signature, value);
        }

        public Map<String, FieldInfo> getLocks() {
            Map<String, FieldInfo> result = new TreeMap<String, FieldInfo>();
            for (FieldInfo info : fields.values()) {
                if (syncvolatile && isSet(info.access, ACC_VOLATILE) ||
                        syncfinal && isSet(info.access, ACC_FINAL) && !isSet(info.access, ACC_STATIC)) {
                    FieldInfo fieldInfo = getLock(info);
                    if (fieldInfo != null) {
                        result.put(info.name, fieldInfo);
                    }
                }
            }
            return result;
        }

        private FieldInfo getLock(FieldInfo fieldInfo) {
            String lockName = fieldInfo.name + "$lock";
            FieldInfo lockInfo;
            while ((lockInfo = fields.get(lockName)) != null) {
                if (isSet(lockInfo.access, ACC_STATIC) &&
                        lockInfo.desc.equals(OBJECT_TYPE.getDescriptor()) &&
                        (lockInfo.access & ACCESS_MASK) == (fieldInfo.access & ACCESS_MASK)) {
                    return lockInfo;
                }
                lockName += "$";
            }
            if (isJava5Class) {
                return new FieldInfo(fieldInfo.access & ACCESS_MASK |
                        ACC_STATIC | ACC_FINAL | ACC_SYNTHETIC, lockName, null);
            }
            return null;
        }

    }

    private class LockIntroductionVisitor extends MethodAdapter {

        private final List<FieldInfo> locks;

        public LockIntroductionVisitor(MethodVisitor visitor, List<FieldInfo> locks) {
            super(visitor);
            this.locks = locks;
        }

        public void visitCode() {
            super.visitCode();
            for (FieldInfo lock : locks) {
                mv.visitTypeInsn(NEW, OBJECT_TYPE.getInternalName());
                mv.visitInsn(DUP);
                mv.visitMethodInsn(INVOKESPECIAL, OBJECT_TYPE.getInternalName(),
                        RuntimeTools.CONSTRUCTOR_NAME, TransformerTools.descriptor(void.class));
                mv.visitFieldInsn(PUTSTATIC, internalName, lock.name, OBJECT_TYPE.getDescriptor());
            }
        }

        public void visitEnd() {
            super.visitEnd();
            for (FieldInfo lock : locks) {
                cv.visitField(lock.access, lock.name, OBJECT_TYPE.getDescriptor(), null, null).visitEnd();
            }
        }
    }

    private class SynchronizationVisitor extends LocalVariablesSorter {

        public SynchronizationVisitor(int access, String desc, MethodVisitor mv) {
            super(access, desc, mv);
        }

        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            FieldInfo lock = getLocks(owner).get(name);
            if (lock == null) {
                mv.visitFieldInsn(opcode, owner, name, desc);
                return;
            }
            int lockVar = newLocal(1);
            int exceptionVar = newLocal(1);
            Label syncStart = new Label();
            Label syncEnd = new Label();
            Label catchStart = new Label();
            Label catchEnd = new Label();
            Label blockEnd = new Label();
            mv.visitTryCatchBlock(syncStart, syncEnd, catchStart, null);
            mv.visitTryCatchBlock(catchStart, catchEnd, catchStart, null);
            mv.visitFieldInsn(GETSTATIC, owner, lock.name, OBJECT_TYPE.getDescriptor());
            mv.visitInsn(DUP);
            mv.visitVarInsn(ASTORE, lockVar);
            mv.visitInsn(MONITORENTER);
            mv.visitLabel(syncStart);
            mv.visitFieldInsn(opcode, owner, name, desc);
            mv.visitVarInsn(ALOAD, lockVar);
            mv.visitInsn(MONITOREXIT);
            mv.visitLabel(syncEnd);
            mv.visitJumpInsn(GOTO, blockEnd);
            mv.visitLabel(catchStart);
            mv.visitVarInsn(ASTORE, exceptionVar);
            mv.visitVarInsn(ALOAD, lockVar);
            mv.visitInsn(MONITOREXIT);
            mv.visitLabel(catchEnd);
            mv.visitVarInsn(ALOAD, exceptionVar);
            mv.visitInsn(ATHROW);
            mv.visitLabel(blockEnd);
        }

    }

}

