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

import net.sf.retrotranslator.runtime.asm.*;

/**
 * @author Taras Puchko
 */
abstract class GenericClassVisitor extends NameTranslator implements ClassVisitor {

    private final ClassVisitor classVisitor;

    public GenericClassVisitor(ClassVisitor classVisitor) {
        this.classVisitor = classVisitor;
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        classVisitor.visit(version, access, typeName(name),
                declarationSignature(signature), typeName(superName), typeNames(interfaces));
    }

    public void visitSource(String source, String debug) {
        classVisitor.visitSource(source, debug);
    }

    public void visitOuterClass(String owner, String name, String desc) {
        classVisitor.visitOuterClass(typeName(owner), identifier(name), methodDescriptor(desc));
    }

    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        return wrap(classVisitor.visitAnnotation(typeDescriptor(desc), visible));
    }

    public void visitAttribute(Attribute attr) {
        classVisitor.visitAttribute(attr);
    }

    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        classVisitor.visitInnerClass(typeName(name), typeName(outerName), identifier(innerName), access);
    }

    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        FieldVisitor result = classVisitor.visitField(access,
                identifier(name), typeDescriptor(desc), typeSignature(signature), value);
        return result == null ? null : new GenericFieldVisitor(result);
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor result = classVisitor.visitMethod(access,
                identifier(name), methodDescriptor(desc), declarationSignature(signature), typeNames(exceptions));
        return result == null ? null : new GenericMethodVisitor(result);
    }

    protected void visitTypeInstruction(MethodVisitor visitor, int opcode, String desc) {
        visitor.visitTypeInsn(opcode, typeNameOrTypeDescriptor(desc));
    }

    protected void visitFieldInstruction(MethodVisitor visitor, int opcode, String owner, String name, String desc) {
        visitor.visitFieldInsn(opcode, typeName(owner), identifier(name), typeDescriptor(desc));
    }

    protected void visitMethodInstruction(MethodVisitor visitor, int opcode, String owner, String name, String desc) {
        visitor.visitMethodInsn(opcode, typeNameOrTypeDescriptor(owner), identifier(name), methodDescriptor(desc));
    }

    public void visitEnd() {
        classVisitor.visitEnd();
    }

    private AnnotationVisitor wrap(AnnotationVisitor av) {
        return av == null ? null : new GenericAnnotationVisitor(av);
    }

    private class GenericAnnotationVisitor implements AnnotationVisitor {

        private AnnotationVisitor annotationVisitor;

        public GenericAnnotationVisitor(AnnotationVisitor annotationVisitor) {
            this.annotationVisitor = annotationVisitor;
        }

        public void visit(String name, Object value) {
            annotationVisitor.visit(identifier(name), typeOrValue(value));
        }

        public void visitEnum(String name, String desc, String value) {
            annotationVisitor.visitEnum(identifier(name), typeDescriptor(desc), value);
        }

        public AnnotationVisitor visitAnnotation(String name, String desc) {
            return wrap(annotationVisitor.visitAnnotation(identifier(name), typeDescriptor(desc)));
        }

        public AnnotationVisitor visitArray(String name) {
            return wrap(annotationVisitor.visitArray(identifier(name)));
        }

        public void visitEnd() {
            annotationVisitor.visitEnd();
        }
    }

    private class GenericFieldVisitor implements FieldVisitor {

        private FieldVisitor fieldVisitor;

        public GenericFieldVisitor(FieldVisitor fieldVisitor) {
            this.fieldVisitor = fieldVisitor;
        }

        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            return wrap(fieldVisitor.visitAnnotation(typeDescriptor(desc), visible));
        }

        public void visitAttribute(Attribute attr) {
            fieldVisitor.visitAttribute(attr);
        }

        public void visitEnd() {
            fieldVisitor.visitEnd();
        }
    }

    private class GenericMethodVisitor implements MethodVisitor {

        protected final MethodVisitor mv;
        private String deferredConstant;

        public GenericMethodVisitor(MethodVisitor visitor) {
            mv = visitor;
        }

        private void flush() {
            if (deferredConstant != null) {
                mv.visitLdcInsn(deferredConstant);
                deferredConstant = null;
            }
        }

        public AnnotationVisitor visitAnnotationDefault() {
            flush();
            return wrap(mv.visitAnnotationDefault());
        }

        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            flush();
            return wrap(mv.visitAnnotation(typeDescriptor(desc), visible));
        }

        public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
            flush();
            return wrap(mv.visitParameterAnnotation(parameter, typeDescriptor(desc), visible));
        }

        public void visitTypeInsn(int opcode, String desc) {
            flush();
            visitTypeInstruction(mv, opcode, desc);
        }

        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            flush();
            visitFieldInstruction(mv, opcode, owner, name, desc);
        }

        public void visitMethodInsn(int opcode, String owner, String name, String desc) {
            if (deferredConstant != null && deferredConstant.indexOf('/') < 0 &&
                    opcode == Opcodes.INVOKESTATIC && name.equals("class$") &&
                    desc.equals(TransformerTools.descriptor(Class.class, String.class))) {
                deferredConstant = typeNameOrTypeDescriptor(deferredConstant.replace('.', '/')).replace('/', '.');
            }
            flush();
            visitMethodInstruction(mv, opcode, owner, name, desc);
        }

        public void visitLdcInsn(Object cst) {
            flush();
            if (cst instanceof String) {
                deferredConstant = (String) cst;
            } else {
                mv.visitLdcInsn(typeOrValue(cst));
            }
        }

        public void visitMultiANewArrayInsn(String desc, int dims) {
            flush();
            mv.visitMultiANewArrayInsn(typeDescriptor(desc), dims);
        }

        public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
            flush();
            mv.visitTryCatchBlock(start, end, handler, typeName(type));
        }

        public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
            flush();
            mv.visitLocalVariable(identifier(name), typeDescriptor(desc), typeSignature(signature), start, end, index);
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

        public void visitJumpInsn(int opcode, Label label) {
            flush();
            mv.visitJumpInsn(opcode, label);
        }

        public void visitLabel(Label label) {
            flush();
            mv.visitLabel(label);
        }

        public void visitIincInsn(int var, int increment) {
            flush();
            mv.visitIincInsn(var, increment);
        }

        public void visitTableSwitchInsn(int min, int max, Label dflt, Label labels[]) {
            flush();
            mv.visitTableSwitchInsn(min, max, dflt, labels);
        }

        public void visitLookupSwitchInsn(Label dflt, int keys[], Label labels[]) {
            flush();
            mv.visitLookupSwitchInsn(dflt, keys, labels);
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

}
