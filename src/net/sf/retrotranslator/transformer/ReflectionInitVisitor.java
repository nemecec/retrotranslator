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

import net.sf.retrotranslator.runtime.asm.*;
import static net.sf.retrotranslator.runtime.asm.Opcodes.*;
import net.sf.retrotranslator.runtime.impl.RuntimeTools;

/**
 * @author Taras Puchko
 */
class ReflectionInitVisitor extends ClassAdapter {

    private static final String CLASS_NAME = Type.getInternalName(Class.class);
    private static final MemberKey SET_ENCODED_METADATA_KEY = new MemberKey(
            true, "setEncodedMetadata", TransformerTools.descriptor(void.class, Class.class, String.class));

    private final byte[] metadata;
    private MemberReplacement replacement;
    private boolean initialized;
    private String className;

    public ReflectionInitVisitor(ClassVisitor visitor, MemberReplacement replacement, byte[] metadata) {
        super(visitor);
        this.metadata = metadata;
        this.replacement = replacement;
    }

    public static MemberReplacement getMethodReplacement(ReplacementLocator locator) {
        ClassReplacement replacement = locator.getReplacement(CLASS_NAME);
        return replacement == null ? null : replacement.getMethodReplacements().get(SET_ENCODED_METADATA_KEY);
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        className = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);
        if (name.equals(RuntimeTools.STATIC_NAME)) {
            initialized = true;
            return new ClassInitMethodVisitor(visitor);
        }
        return visitor;
    }

    public void visitEnd() {
        if (!initialized) {
            MethodVisitor visitor = new ClassInitMethodVisitor(super.visitMethod(ACC_STATIC,
                    RuntimeTools.STATIC_NAME, TransformerTools.descriptor(void.class), null, null));
            visitor.visitCode();
            visitor.visitInsn(RETURN);
            visitor.visitMaxs(0, 0);
            visitor.visitEnd();
        }
        super.visitEnd();
    }

    private String getEncodedMetadata() {
        char[] chars = new char[metadata.length];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char) (127 - metadata[i]);
        }
        return new String(chars);
    }

    private class ClassInitMethodVisitor extends MethodAdapter {

        private ClassInitMethodVisitor(MethodVisitor mv) {
            super(mv);
        }

        public void visitCode() {
            super.visitCode();
            mv.visitInsn(ICONST_0);
            mv.visitTypeInsn(ANEWARRAY, className);
            mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(Object.class),
                    "getClass", TransformerTools.descriptor(Class.class));
            mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(Class.class),
                    "getComponentType", TransformerTools.descriptor(Class.class));
            mv.visitLdcInsn(getEncodedMetadata());
            mv.visitMethodInsn(INVOKESTATIC, replacement.getOwner(), replacement.getName(), replacement.getDesc());
        }

    }

}