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

import net.sf.retrotranslator.runtime.asm.Type;
import net.sf.retrotranslator.runtime.asm.signature.*;

/**
 * @author Taras Puchko
 */
class NameTranslator {

    protected String identifier(String s) {
        return s;
    }

    protected static String fixIdentifier(String s) {
        return s == null ? null : s.replace('+', '$');
    }

    protected String typeName(String s) {
        return s;
    }

    protected final Type type(Type type) {
        if (type == null) {
            return null;
        }
        if (type.getSort() == Type.OBJECT) {
            return TransformerTools.getTypeByInternalName(typeName(type.getInternalName()));
        }
        if (type.getSort() == Type.ARRAY) {
            Type elementType = type.getElementType();
            if (elementType.getSort() == Type.OBJECT) {
                return TransformerTools.getArrayTypeByInternalName(
                        typeName(elementType.getInternalName()), type.getDimensions());
            }
        }
        return type;
    }

    protected final String typeDescriptor(String s) {
        return s == null ? null : type(Type.getType(s)).getDescriptor();
    }

    protected final String methodDescriptor(String s) {
        return s == null ? null : Type.getMethodDescriptor(
                type(Type.getReturnType(s)),
                types(Type.getArgumentTypes(s)));
    }

    protected final String[] typeNames(String[] names) {
        if (names == null) return null;
        for (int i = 0; i < names.length; i++) {
            names[i] = typeName(names[i]);
        }
        return names;
    }

    protected final Type[] types(Type[] types) {
        if (types == null) return null;
        for (int i = 0; i < types.length; i++) {
            types[i] = type(types[i]);
        }
        return types;
    }

    protected final Object typeOrValue(Object object) {
        return object instanceof Type ? type((Type) object) : object;
    }

    protected final String typeNameOrTypeDescriptor(String s) {
        return s != null && s.startsWith("[") ? typeDescriptor(s) : typeName(s);
    }

    protected final String typeSignature(String s) {
        if (s == null) return null;
        SignatureWriter writer = new SignatureWriter();
        new SignatureReader(s).acceptType(new TranslatingSignatureVisitor(writer));
        return writer.toString();
    }

    protected final String declarationSignature(String s) {
        if (s == null) return null;
        SignatureWriter writer = new SignatureWriter();
        new SignatureReader(s).accept(new TranslatingSignatureVisitor(writer));
        return writer.toString();
    }

    private class TranslatingSignatureVisitor extends SignatureAdapter {

        public TranslatingSignatureVisitor(SignatureVisitor visitor) {
            super(visitor);
        }

        protected SignatureVisitor visitStart(SignatureVisitor visitor) {
            return new TranslatingSignatureVisitor(visitor);
        }

        public void visitClassType(String name) {
            super.visitClassType(typeNameOrTypeDescriptor(name));
        }

        public void visitInnerClassType(String name) {
            super.visitInnerClassType(identifier(name));
        }
    }

}
