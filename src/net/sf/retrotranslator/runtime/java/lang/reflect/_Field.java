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
package net.sf.retrotranslator.runtime.java.lang.reflect;

import net.sf.retrotranslator.runtime.impl.FieldDescriptor;
import net.sf.retrotranslator.runtime.impl.TypeTools;
import org.objectweb.asm.Opcodes;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

/**
 * @author Taras Puchko
 */
public class _Field {

    public static Annotation getAnnotation(Field field, Class annotationType) {
        return FieldDescriptor.getInstance(field).getAnnotation(annotationType);
    }

    public static Annotation[] getAnnotations(Field field) {
        return FieldDescriptor.getInstance(field).getAnnotations();
    }

    public static Annotation[] getDeclaredAnnotations(Field field) {
        return FieldDescriptor.getInstance(field).getDeclaredAnnotations();
    }

    public static Type getGenericType(Field field) {
        Type type = FieldDescriptor.getInstance(field).getGenericType();
        return type != null ? type : field.getType();
    }

    public static boolean isAnnotationPresent(Field field, Class annotationType) {
        return FieldDescriptor.getInstance(field).isAnnotationPresent(annotationType);
    }

    public static boolean isEnumConstant(Field field) {
        return FieldDescriptor.getInstance(field).isAccess(Opcodes.ACC_ENUM);
    }

    public static boolean isSynthetic(Field field) {
        return FieldDescriptor.getInstance(field).isAccess(Opcodes.ACC_SYNTHETIC);
    }

    public static String toGenericString(Field field) {
        StringBuilder builder = new StringBuilder();
        if (field.getModifiers() != 0) {
            builder.append(Modifier.toString(field.getModifiers())).append(' ');
        }
        builder.append(TypeTools.getString(getGenericType(field))).append(' ');
        builder.append(TypeTools.getString(field.getDeclaringClass())).append('.');
        return builder.append(field.getName()).toString();
    }
}
