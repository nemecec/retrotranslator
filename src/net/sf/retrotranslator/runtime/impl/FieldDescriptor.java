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
package net.sf.retrotranslator.runtime.impl;

import java.lang.reflect.*;
import net.sf.retrotranslator.runtime.asm.signature.SignatureReader;
import net.sf.retrotranslator.runtime.java.lang.annotation.Annotation_;

/**
 * @author Taras Puchko
 */
public class FieldDescriptor extends AnnotatedElementDescriptor implements MemberDescriptor {

    private String name;
    private String desc;
    private ClassDescriptor classDescriptor;
    private TypeDescriptor typeDescriptor;

    public FieldDescriptor(ClassDescriptor classDescriptor, int access, String name, String desc, String signature) {
        this.classDescriptor = classDescriptor;
        this.access = access;
        this.name = name;
        this.desc = desc;
        if (signature != null) {
            typeDescriptor = new TypeDescriptor();
            new SignatureReader(signature).accept(typeDescriptor);
        }
    }

    public FieldDescriptor(ClassDescriptor classDescriptor, Field field) {
        this(classDescriptor, field.getModifiers(), field.getName(),
                net.sf.retrotranslator.runtime.asm.Type.getDescriptor(field.getType()), null);
    }

    public static FieldDescriptor getInstance(Field field) {
        ClassDescriptor classDescriptor = ClassDescriptor.getInstance(field.getDeclaringClass());
        FieldDescriptor fieldDescriptor = classDescriptor.getFieldDescriptor(field.getName());
        return fieldDescriptor != null ? fieldDescriptor : new FieldDescriptor(classDescriptor, field);
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public Type getGenericType() {
        return createType(typeDescriptor);
    }

    public ClassDescriptor getClassDescriptor() {
        return classDescriptor;
    }

    public String getInfo() {
        return RuntimeTools.getFieldInfo(classDescriptor.getName(), name);
    }

    protected TypeVariable findTypeVariable(String name) {
        return classDescriptor.findTypeVariable(name);
    }

    protected Annotation_[] createAnnotations(Annotation_[] declaredAnnotations) {
        return declaredAnnotations;
    }

}
