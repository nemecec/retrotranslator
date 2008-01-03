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

import static net.sf.retrotranslator.runtime.asm.Opcodes.*;
import net.sf.retrotranslator.runtime.asm.*;
import java.util.*;

/**
 * @author Taras Puchko
 */
public class SynchronizedBlockVisitor extends ClassAdapter {

    public SynchronizedBlockVisitor(ClassVisitor visitor) {
        super(visitor);
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        return methodVisitor == null ? null : new SynchronizedBlockMethodVisitor(methodVisitor);
    }

    private static class TryCatchBlock {
        public final Label start;
        public final Label end;
        public Label handler;
        public final String type;
        public boolean monitorBlock;

        public TryCatchBlock(Label start, Label end, Label handler, String type) {
            this.start = start;
            this.end = end;
            this.handler = handler;
            this.type = type;
        }

    }

    private enum Step {
        BLOCK_STARTED, EXCEPTION_STORED, MONITOR_LOADED, MONITOR_EXITED, BLOCK_FINISHED, EXCEPTION_LOADED
    }

    private static class SynchronizedBlockMethodVisitor extends AbstractMethodVisitor {

        private final List<TryCatchBlock> blocks = new ArrayList<TryCatchBlock>();
        private Step step;
        private Label startLabel;
        private Label endLabel;
        private Integer exceptionVar;
        private int monitorVar;

        public SynchronizedBlockMethodVisitor(MethodVisitor visitor) {
            super(visitor);
        }

        public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
            blocks.add(new TryCatchBlock(start, end, handler, type));
        }

        public void visitEnd() {
            for (TryCatchBlock block : blocks) {
                mv.visitTryCatchBlock(block.start, block.end, block.handler, block.type);
            }
            mv.visitEnd();
        }

        protected void flush() {
            step = null;
        }

        public void visitLabel(Label label) {
            mv.visitLabel(label);
            if (step == Step.MONITOR_EXITED) {
                endLabel = label;
                step = Step.BLOCK_FINISHED;
            } else {
                startLabel = label;
                step = Step.BLOCK_STARTED;
            }
        }

        public void visitVarInsn(int opcode, int var) {
            mv.visitVarInsn(opcode, var);
            if (step == Step.BLOCK_STARTED && opcode == ASTORE) {
                exceptionVar = var;
                step = Step.EXCEPTION_STORED;
            } else if (step == Step.BLOCK_STARTED && opcode == ALOAD) {
                exceptionVar = null;
                monitorVar = var;
                step = Step.MONITOR_LOADED;
            } else if (step == Step.EXCEPTION_STORED && opcode == ALOAD) {
                monitorVar = var;
                step = Step.MONITOR_LOADED;
            } else if (step == Step.BLOCK_FINISHED && opcode == ALOAD && var == exceptionVar) {
                step = Step.EXCEPTION_LOADED;
            } else {
                step = null;
            }
        }

        public void visitInsn(int opcode) {
            mv.visitInsn(opcode);
            if (step == Step.MONITOR_LOADED && opcode == MONITOREXIT) {
                step = Step.MONITOR_EXITED;
            } else if (step == Step.EXCEPTION_LOADED && opcode == ATHROW) {
                checkBlock();
                step = null;
            } else if (step == Step.BLOCK_FINISHED && opcode == ATHROW && exceptionVar == null) {
                checkBlock();
                step = null;
            } else {
                step = null;
            }
        }

        private void checkBlock() {
            int blockIndex = findBlockIndex();
            if (blockIndex < 0) {
                return;
            }
            TryCatchBlock block = blocks.get(blockIndex);
            block.monitorBlock = true;
            if (block.handler != block.start) {
                return;
            }
            if (blockIndex > 0 && blocks.get(blockIndex - 1).monitorBlock) {
                return;
            }
            Label startLabel = new Label();
            Label endLabel = new Label();
            block.handler = startLabel;
            mv.visitLabel(startLabel);
            if (exceptionVar != null) {
                mv.visitVarInsn(ASTORE, exceptionVar);
            }
            mv.visitVarInsn(ALOAD, monitorVar);
            mv.visitInsn(MONITOREXIT);
            mv.visitLabel(endLabel);
            if (exceptionVar != null) {
                mv.visitVarInsn(ALOAD, exceptionVar);
            }
            mv.visitInsn(ATHROW);
            blocks.add(blockIndex + 1, new TryCatchBlock(startLabel, endLabel, startLabel, null));
        }

        private int findBlockIndex() {
            for (int i = 0; i < blocks.size(); i++) {
                TryCatchBlock block = blocks.get(i);
                if (block.start == startLabel && block.end == endLabel && block.type == null) {
                    return i;
                }
            }
            return -1;
        }
    }

}
