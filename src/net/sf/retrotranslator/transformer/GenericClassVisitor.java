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

import net.sf.retrotranslator.runtime.asm.signature.SignatureReader;
import net.sf.retrotranslator.runtime.asm.signature.SignatureVisitor;
import net.sf.retrotranslator.runtime.asm.signature.SignatureWriter;
import net.sf.retrotranslator.runtime.asm.*;

/**
 * @author Taras Puchko
 */
abstract class GenericClassVisitor implements ClassVisitor {

    private ClassVisitor classVisitor;

    private DescriptorTransformer transformer = new DescriptorTransformer() {
        protected String transformInternalName(String internalName) {
            return internalName(internalName);
        }
    };

    public GenericClassVisitor(ClassVisitor classVisitor) {
        this.classVisitor = classVisitor;
    }

    protected String visitInternalName(String name) {
        return name;
    }

    protected String visitIdentifier(String identifier) {
        return identifier;
    }

    protected void visitFieldRef(int opcode, String owner, String name, String desc) {
    }

    protected void visitMethodRef(int opcode, String owner, String name, String desc) {
    }

    private String internalName(String name) {
        return name == null ? null : visitInternalName(name);
    }

    private String identifier(String identifier) {
        return identifier == null ? null : visitIdentifier(identifier);
    }

    private String[] internalNames(String[] names) {
        if (names == null) return null;
        for (int i = 0; i < names.length; i++) {
            names[i] = internalName(names[i]);
        }
        return names;
    }

    private String internalNameOrDescriptor(String s) {
        return s != null && s.startsWith("[") ? descriptor(s) : internalName(s);
    }

    private String descriptor(String descriptor) {
        return transformer.transformDescriptor(descriptor);
    }

    private String typeSignature(String signature) {
        if (signature == null) return null;
        SignatureWriter writer = new SignatureWriter();
        new SignatureReader(signature).acceptType(new GenericSignatureVisitor(writer));
        return writer.toString();
    }

    private String declarationSignature(String signature) {
        if (signature == null) return null;
        SignatureWriter writer = new SignatureWriter();
        new SignatureReader(signature).accept(new GenericSignatureVisitor(writer));
        return writer.toString();
    }

    private Object type(Object value) {
        return value instanceof Type ? Type.getType(descriptor(((Type) value).getDescriptor())) : value;
    }

    private GenericFieldVisitor wrap(FieldVisitor fv) {
        return fv == null ? null : new GenericFieldVisitor(fv);
    }

    private GenericMethodVisitor wrap(MethodVisitor mv) {
        return mv == null ? null : new GenericMethodVisitor(mv);
    }

    private AnnotationVisitor wrap(AnnotationVisitor av) {
        return av == null ? null : new GenericAnnotationVisitor(av);
    }

    public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
        classVisitor.visit(version, access, internalName(name), declarationSignature(signature), internalName(superName), internalNames(interfaces));
    }

    public void visitSource(final String source, final String debug) {
        classVisitor.visitSource(source, debug);
    }

    public void visitOuterClass(final String owner, final String name, final String desc) {
        classVisitor.visitOuterClass(internalName(owner), identifier(name), descriptor(desc));
    }

    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        return wrap(classVisitor.visitAnnotation(descriptor(desc), visible));
    }

    public void visitAttribute(final Attribute attr) {
        classVisitor.visitAttribute(attr);
    }

    public void visitInnerClass(final String name, final String outerName, final String innerName, final int access) {
        classVisitor.visitInnerClass(internalName(name), internalName(outerName), identifier(innerName), access);
    }

    public FieldVisitor visitField(final int access, final String name, final String desc, final String signature, final Object value) {
        return wrap(classVisitor.visitField(access, identifier(name), descriptor(desc), typeSignature(signature), value));
    }

    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        return wrap(classVisitor.visitMethod(access, identifier(name), descriptor(desc), declarationSignature(signature), internalNames(exceptions)));
    }

    public void visitEnd() {
        classVisitor.visitEnd();
    }

    private class GenericAnnotationVisitor implements AnnotationVisitor {

        private AnnotationVisitor annotationVisitor;

        public GenericAnnotationVisitor(AnnotationVisitor annotationVisitor) {
            this.annotationVisitor = annotationVisitor;
        }

        public void visit(String name, Object value) {
            annotationVisitor.visit(identifier(name), type(value));
        }

        public void visitEnum(String name, String desc, String value) {
            annotationVisitor.visitEnum(identifier(name), descriptor(desc), value);
        }

        public AnnotationVisitor visitAnnotation(String name, String desc) {
            return wrap(annotationVisitor.visitAnnotation(identifier(name), descriptor(desc)));
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
            return wrap(fieldVisitor.visitAnnotation(descriptor(desc), visible));
        }

        public void visitAttribute(Attribute attr) {
            fieldVisitor.visitAttribute(attr);
        }

        public void visitEnd() {
            fieldVisitor.visitEnd();
        }
    }

    private class GenericMethodVisitor implements MethodVisitor {

        private MethodVisitor methodVisitor;

        public GenericMethodVisitor(final MethodVisitor methodVisitor) {
            this.methodVisitor = methodVisitor;
        }

        public AnnotationVisitor visitAnnotationDefault() {
            return wrap(methodVisitor.visitAnnotationDefault());
        }

        public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
            return wrap(methodVisitor.visitAnnotation(descriptor(desc), visible));
        }

        public AnnotationVisitor visitParameterAnnotation(final int parameter, final String desc, final boolean visible) {
            return wrap(methodVisitor.visitParameterAnnotation(parameter, descriptor(desc), visible));
        }

        public void visitTypeInsn(final int opcode, final String desc) {
            methodVisitor.visitTypeInsn(opcode, internalNameOrDescriptor(desc));
        }

        public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
            visitFieldRef(opcode, owner, name, desc);
            methodVisitor.visitFieldInsn(opcode, internalName(owner), identifier(name), descriptor(desc));
        }

        public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc) {
            visitMethodRef(opcode, owner, name, desc);
            methodVisitor.visitMethodInsn(opcode, internalNameOrDescriptor(owner), identifier(name), descriptor(desc));
        }

        public void visitLdcInsn(final Object cst) {
            methodVisitor.visitLdcInsn(type(cst));
        }

        public void visitMultiANewArrayInsn(final String desc, final int dims) {
            methodVisitor.visitMultiANewArrayInsn(descriptor(desc), dims);
        }

        public void visitTryCatchBlock(final Label start, final Label end, final Label handler, final String type) {
            methodVisitor.visitTryCatchBlock(start, end, handler, internalName(type));
        }

        public void visitLocalVariable(final String name, final String desc, final String signature, final Label start, final Label end, final int index) {
            methodVisitor.visitLocalVariable(identifier(name), descriptor(desc), typeSignature(signature), start, end, index);
        }

        public void visitAttribute(Attribute attr) {
            methodVisitor.visitAttribute(attr);
        }

        public void visitCode() {
            methodVisitor.visitCode();
        }

        public void visitInsn(int opcode) {
            methodVisitor.visitInsn(opcode);
        }

        public void visitIntInsn(int opcode, int operand) {
            methodVisitor.visitIntInsn(opcode, operand);
        }

        public void visitVarInsn(int opcode, int var) {
            methodVisitor.visitVarInsn(opcode, var);
        }

        public void visitJumpInsn(int opcode, Label label) {
            methodVisitor.visitJumpInsn(opcode, label);
        }

        public void visitLabel(Label label) {
            methodVisitor.visitLabel(label);
        }

        public void visitIincInsn(int var, int increment) {
            methodVisitor.visitIincInsn(var, increment);
        }

        public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {
            methodVisitor.visitTableSwitchInsn(min, max, dflt, labels);
        }

        public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
            methodVisitor.visitLookupSwitchInsn(dflt, keys, labels);
        }

        public void visitLineNumber(int line, Label start) {
            methodVisitor.visitLineNumber(line, start);
        }

        public void visitMaxs(int maxStack, int maxLocals) {
            methodVisitor.visitMaxs(maxStack, maxLocals);
        }

        public void visitEnd() {
            methodVisitor.visitEnd();
        }
    }

    private class GenericSignatureVisitor implements SignatureVisitor {

        final private SignatureVisitor visitor;

        public GenericSignatureVisitor(final SignatureVisitor visitor) {
            this.visitor = visitor;
        }

        public void visitFormalTypeParameter(String name) {
            visitor.visitFormalTypeParameter(name);
        }

        public SignatureVisitor visitClassBound() {
            return new GenericSignatureVisitor(visitor.visitClassBound());
        }

        public SignatureVisitor visitInterfaceBound() {
            return new GenericSignatureVisitor(visitor.visitInterfaceBound());
        }

        public SignatureVisitor visitSuperclass() {
            return new GenericSignatureVisitor(visitor.visitSuperclass());
        }

        public SignatureVisitor visitInterface() {
            return new GenericSignatureVisitor(visitor.visitInterface());
        }

        public SignatureVisitor visitParameterType() {
            return new GenericSignatureVisitor(visitor.visitParameterType());
        }

        public SignatureVisitor visitReturnType() {
            return new GenericSignatureVisitor(visitor.visitReturnType());
        }

        public SignatureVisitor visitExceptionType() {
            return new GenericSignatureVisitor(visitor.visitExceptionType());
        }

        public void visitBaseType(char descriptor) {
            visitor.visitBaseType(descriptor);
        }

        public void visitTypeVariable(String name) {
            visitor.visitTypeVariable(name);
        }

        public SignatureVisitor visitArrayType() {
            return new GenericSignatureVisitor(visitor.visitArrayType());
        }

        public void visitClassType(String name) {
            visitor.visitClassType(internalNameOrDescriptor(name));
        }

        public void visitInnerClassType(String name) {
            visitor.visitInnerClassType(identifier(name));
        }

        public void visitTypeArgument() {
            visitor.visitTypeArgument();
        }

        public SignatureVisitor visitTypeArgument(char wildcard) {
            return new GenericSignatureVisitor(visitor.visitTypeArgument(wildcard));
        }

        public void visitEnd() {
            visitor.visitEnd();
        }
    }

}
