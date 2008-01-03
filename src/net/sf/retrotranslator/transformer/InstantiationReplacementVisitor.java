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
import static net.sf.retrotranslator.runtime.impl.RuntimeTools.CONSTRUCTOR_NAME;

/**
 * @author Taras Puchko
 */
class InstantiationReplacementVisitor extends ClassAdapter {

    private final Map<String, List<InstantiationPoint>> pointListMap;

    public InstantiationReplacementVisitor(ClassVisitor visitor, Map<String, List<InstantiationPoint>> pointListMap) {
        super(visitor);
        this.pointListMap = pointListMap;
    }

    public MethodVisitor visitMethod(final int access, final String name, final String desc,
                                     final String signature, final String[] exceptions) {
        List<InstantiationPoint> points = pointListMap.get(name + desc);
        MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);
        return visitor == null || points == null ? visitor : new InstantiationReplacementMethodVisitor(visitor, points);
    }

    private static class InstantiationReplacementMethodVisitor extends MethodAdapter {

        private final List<InstantiationPoint> points;
        private int allocationIndex;
        private int duplicationIndex;
        private int initializationIndex;

        public InstantiationReplacementMethodVisitor(MethodVisitor mv, List<InstantiationPoint> points) {
            super(mv);
            this.points = points;
        }

        public void visitTypeInsn(int opcode, String desc) {
            if (opcode == NEW) {
                int index = ++allocationIndex;
                for (InstantiationPoint point : points) {
                    if (point.getAllocationIndex() == index) {
                        return;
                    }
                }
            }
            super.visitTypeInsn(opcode, desc);
        }

        public void visitInsn(int opcode) {
            if (opcode == DUP) {
                int index = ++duplicationIndex;
                for (InstantiationPoint point : points) {
                    if (point.getDuplicationIndex() == index) {
                        return;
                    }
                }
            }
            super.visitInsn(opcode);
        }

        public void visitMethodInsn(int opcode, String owner, String name, String desc) {
            if (opcode == INVOKESPECIAL && name.equals(CONSTRUCTOR_NAME)) {
                int index = ++initializationIndex;
                for (InstantiationPoint point : points) {
                    if (point.getInitializationIndex() == index) {
                        if (!owner.equals(point.getInternalName())) {
                            throw new IllegalStateException();
                        }
                        MemberReplacement replacement = point.getReplacement();
                        mv.visitMethodInsn(INVOKESTATIC,
                                replacement.getOwner(), replacement.getName(), replacement.getDesc());
                        return;
                    }
                }
            }
            super.visitMethodInsn(opcode, owner, name, desc);
        }
    }

}
