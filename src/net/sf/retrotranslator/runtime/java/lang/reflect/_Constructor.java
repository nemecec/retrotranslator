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

import net.sf.retrotranslator.runtime.impl.MethodDescriptor;
import net.sf.retrotranslator.runtime.impl.RuntimeTools;
import org.objectweb.asm.Opcodes;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * @author Taras Puchko
 */
public class _Constructor {

    public static Annotation getAnnotation(Constructor constructor, Class annotationType) {
        return MethodDescriptor.getInstance(constructor).getAnnotation(annotationType);
    }

    public static Annotation[] getAnnotations(Constructor constructor) {
        return MethodDescriptor.getInstance(constructor).getAnnotations();
    }

    public static Annotation[] getDeclaredAnnotations(Constructor constructor) {
        return MethodDescriptor.getInstance(constructor).getDeclaredAnnotations();
    }

    public static Type[] getGenericExceptionTypes(Constructor constructor) {
        Type[] types = MethodDescriptor.getInstance(constructor).getGenericExceptionTypes();
        return types != null ? types : constructor.getExceptionTypes();
    }

    public static Type[] getGenericParameterTypes(Constructor constructor) {
        Type[] types = MethodDescriptor.getInstance(constructor).getGenericParameterTypes();
        return types != null ? types : constructor.getParameterTypes();
    }

    public static Annotation[][] getParameterAnnotations(Constructor constructor) {
        return MethodDescriptor.getInstance(constructor).getParameterAnnotations();
    }

    public static TypeVariable[] getTypeParameters(Constructor constructor) {
        return MethodDescriptor.getInstance(constructor).getTypeParameters();
    }

    public static boolean isAnnotationPresent(Constructor constructor, Class annotationType) {
        return MethodDescriptor.getInstance(constructor).isAnnotationPresent(annotationType);
    }

    public static boolean isSynthetic(Constructor constructor) {
        return MethodDescriptor.getInstance(constructor).isAccess(Opcodes.ACC_SYNTHETIC);
    }

    public static boolean isVarArgs(Constructor constructor) {
        return MethodDescriptor.getInstance(constructor).isAccess(Opcodes.ACC_VARARGS);
    }

    public static String toGenericString(Constructor constructor) {
        try {
            return getGenericString(constructor);
        } catch (Exception e) {
            return "<" + e.toString() + ">";
        }
    }

    private static String getGenericString(Constructor constructor) {
        StringBuilder builder = new StringBuilder();
        if (constructor.getModifiers() != 0) {
            builder.append(Modifier.toString(constructor.getModifiers())).append(' ');
        }
        TypeVariable[] typeParameters = getTypeParameters(constructor);
        if (typeParameters.length > 0) {
            RuntimeTools.append(builder.append('<'), typeParameters).append("> ");
        }
        builder.append(RuntimeTools.getString(constructor.getDeclaringClass()));
        RuntimeTools.append(builder.append('('), getGenericParameterTypes(constructor)).append(')');
        Type[] exceptionTypes = getGenericExceptionTypes(constructor);
        if (exceptionTypes.length > 0) {
            RuntimeTools.append(builder.append(" throws "), exceptionTypes);
        }
        return builder.toString();
    }
}
