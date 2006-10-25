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

import net.sf.retrotranslator.runtime.asm.MethodVisitor;
import net.sf.retrotranslator.runtime.asm.AnnotationVisitor;
import net.sf.retrotranslator.runtime.asm.Attribute;
import net.sf.retrotranslator.runtime.asm.Label;

/**
 * @author Taras Puchko
 */
abstract class AbstractMethodVisitor implements MethodVisitor {

    protected final MethodVisitor mv;

    protected AbstractMethodVisitor(MethodVisitor mv) {
        this.mv = mv;
    }

    protected abstract void flush();

    public AnnotationVisitor visitAnnotationDefault() {
        flush();
        return mv.visitAnnotationDefault();
    }

    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        flush();
        return mv.visitAnnotation(desc, visible);
    }

    public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
        flush();
        return mv.visitParameterAnnotation(parameter, desc, visible);
    }

    public void visitAttribute(Attribute attr) {
        flush();
        mv.visitAttribute(attr);
    }

    public void visitCode() {
        flush();
        mv.visitCode();
    }

    public void visitInsn(int opcode) {
        flush();
        mv.visitInsn(opcode);
    }

    public void visitIntInsn(int opcode, int operand) {
        flush();
        mv.visitIntInsn(opcode, operand);
    }

    public void visitVarInsn(int opcode, int var) {
        flush();
        mv.visitVarInsn(opcode, var);
    }

    public void visitTypeInsn(int opcode, String desc) {
        flush();
        mv.visitTypeInsn(opcode, desc);
    }

    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        flush();
        mv.visitFieldInsn(opcode, owner, name, desc);
    }

    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        flush();
        mv.visitMethodInsn(opcode, owner, name, desc);
    }

    public void visitJumpInsn(int opcode, Label label) {
        flush();
        mv.visitJumpInsn(opcode, label);
    }

    public void visitLabel(Label label) {
        flush();
        mv.visitLabel(label);
    }

    public void visitLdcInsn(Object cst) {
        flush();
        mv.visitLdcInsn(cst);
    }

    public void visitIincInsn(int var, int increment) {
        flush();
        mv.visitIincInsn(var, increment);
    }

    public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {
        flush();
        mv.visitTableSwitchInsn(min, max, dflt, labels);
    }

    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        flush();
        mv.visitLookupSwitchInsn(dflt, keys, labels);
    }

    public void visitMultiANewArrayInsn(String desc, int dims) {
        flush();
        mv.visitMultiANewArrayInsn(desc, dims);
    }

    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        flush();
        mv.visitTryCatchBlock(start, end, handler, type);
    }

    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        flush();
        mv.visitLocalVariable(name, desc, signature, start, end, index);
    }

    public void visitLineNumber(int line, Label start) {
        flush();
        mv.visitLineNumber(line, start);
    }

    public void visitMaxs(int maxStack, int maxLocals) {
        flush();
        mv.visitMaxs(maxStack, maxLocals);
    }

    public void visitEnd() {
        flush();
        mv.visitEnd();
    }

}