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
import net.sf.retrotranslator.runtime.impl.RuntimeTools;
import static net.sf.retrotranslator.runtime.impl.RuntimeTools.CONSTRUCTOR_NAME;

/**
 * @author Taras Puchko
 */
class InstantiationAnalysisVisitor extends ClassAdapter {

    private final ReplacementLocator locator;
    private final Map<String, List<InstantiationPoint>> pointListMap;
    private final SystemLogger logger;
    private String thisName;
    private String superName;

    public InstantiationAnalysisVisitor(ClassVisitor visitor, ReplacementLocator locator,
                                        Map<String, List<InstantiationPoint>> pointListMap, SystemLogger logger) {
        super(visitor);
        this.locator = locator;
        this.pointListMap = pointListMap;
        this.logger = logger;
    }

    public void visit(final int version, final int access, final String name,
                      final String signature, final String superName, final String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.thisName = name;
        this.superName = superName;
    }

    public MethodVisitor visitMethod(final int access, final String name, final String desc,
                                     final String signature, final String[] exceptions) {
        MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);
        return visitor == null ? null : new InstantiationAnalysisMethodVisitor(visitor, name, desc);
    }

    private class InstantiationAnalysisMethodVisitor extends AbstractMethodVisitor {

        private final List<InstantiationPoint> points = new ArrayList<InstantiationPoint>();
        private final Map<Label, InstantiationFrame> frames = new HashMap<Label, InstantiationFrame>();
        private InstantiationFrame currentFrame;
        private final String methodName;
        private final String methodDesc;
        private boolean active = true;
        private InstantiationPoint currentPoint;
        private int allocationIndex;
        private int duplicationIndex;
        private int initializationIndex;

        public InstantiationAnalysisMethodVisitor(MethodVisitor visitor, String methodName, String methodDesc) {
            super(visitor);
            this.methodName = methodName;
            this.methodDesc = methodDesc;
            this.currentFrame = new InstantiationFrame(!methodName.equals(CONSTRUCTOR_NAME));
        }

        protected void flush() {
            currentPoint = null;
        }

        public void visitLabel(Label label) {
            super.visitLabel(label);
            InstantiationFrame frame = frames.remove(label);
            if (frame != null) {
                currentFrame = frame;
            }
        }

        public void visitJumpInsn(int opcode, Label label) {
            super.visitJumpInsn(opcode, label);
            saveFrame(label);
        }

        public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
            super.visitLookupSwitchInsn(dflt, keys, labels);
            saveFrames(dflt, labels);
        }

        public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {
            super.visitTableSwitchInsn(min, max, dflt, labels);
            saveFrames(dflt, labels);
        }

        private void saveFrames(Label label, Label[] labels) {
            saveFrame(label);
            for (Label currentLabel : labels) {
                saveFrame(currentLabel);
            }
        }

        private void saveFrame(Label label) {
            if (!frames.containsKey(label)) {
                frames.put(label, new InstantiationFrame(currentFrame));
            }
        }

        public void visitTypeInsn(int opcode, String desc) {
            super.visitTypeInsn(opcode, desc);
            if (opcode == NEW && active) {
                currentPoint = new InstantiationPoint(desc, ++allocationIndex);
                currentFrame.addLast(currentPoint);
            }
        }

        public void visitInsn(int opcode) {
            if (opcode == DUP && active) {
                duplicationIndex++;
                if (currentPoint != null) {
                    currentPoint.setDuplicationIndex(duplicationIndex);
                }
            }
            super.visitInsn(opcode);
        }

        public void visitMethodInsn(int opcode, String owner, String name, String desc) {
            super.visitMethodInsn(opcode, owner, name, desc);
            if (opcode != INVOKESPECIAL || !name.equals(CONSTRUCTOR_NAME) || !active) {
                return;
            }
            initializationIndex++;
            InstantiationPoint point = currentFrame.pollLast();
            if (point != null) {
                if (!owner.equals(point.getInternalName()) || point.getInitializationIndex() != 0) {
                    active = false;
                    return;
                }
            } else {
                if (currentFrame.isInitialized() || (!owner.equals(thisName) && !owner.equals(superName))) {
                    active = false;
                    return;
                }
                currentFrame.setInitialized(true);
            }
            ClassReplacement classReplacement = locator.getReplacement(owner);
            if (classReplacement == null) {
                return;
            }
            MemberReplacement memberReplacement = classReplacement.getInstantiationReplacements().get(desc);
            if (memberReplacement == null) {
                return;
            }
            if (point == null || point.getDuplicationIndex() == 0) {
                logger.logForFile(Level.WARNING, "Cannot translate " +
                        RuntimeTools.getDisplayClassName(owner) + 
                        " constructor call in " + methodName + " method.");
                return;
            }
            point.setInitializationIndex(initializationIndex);
            point.setReplacement(memberReplacement);
            points.add(point);
        }

        public void visitEnd() {
            super.visitEnd();
            if (active) {
                if (!points.isEmpty()) {
                    pointListMap.put(methodName + methodDesc, points);
                }
            } else {
                logger.logForFile(Level.INFO, "Cannot analyze " + methodName + " method.");
            }
        }
    }

}
