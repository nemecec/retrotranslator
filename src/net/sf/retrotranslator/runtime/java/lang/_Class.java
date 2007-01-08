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
package net.sf.retrotranslator.runtime.java.lang;

import java.lang.reflect.*;
import java.util.Arrays;
import net.sf.retrotranslator.runtime.asm.Opcodes;
import net.sf.retrotranslator.runtime.impl.*;
import net.sf.retrotranslator.runtime.java.lang.annotation.Annotation_;

/**
 * @author Taras Puchko
 */
public class _Class {

    public static Class asSubclass(Class aClass, Class superclass) {
        if (superclass.isAssignableFrom(aClass)) return aClass;
        throw new ClassCastException(aClass.toString());
    }

    public static Object cast(Class aClass, Object obj) {
        if (obj == null || aClass.isInstance(obj)) return obj;
        throw new ClassCastException(aClass.toString());
    }

    public static Annotation_ getAnnotation(Class aClass, Class annotationType) {
        return ClassDescriptor.getInstance(aClass).getAnnotation(annotationType);
    }

    public static Annotation_[] getAnnotations(final Class aClass) {
        return ClassDescriptor.getInstance(aClass).getAnnotations();
    }

    public static String getCanonicalName(Class aClass) {
        if (aClass.isArray()) {
            String name = getCanonicalName(aClass.getComponentType());
            return name == null ? null : name + "[]";
        }
        if (ClassDescriptor.getInstance(aClass).isLocalOrAnonymous()) return null;
        Class declaringClass = aClass.getDeclaringClass();
        return declaringClass == null ? aClass.getName()
                : getCanonicalName(declaringClass) + "." + getSimpleName(aClass);
    }

    public static Annotation_[] getDeclaredAnnotations(Class aClass) {
        return ClassDescriptor.getInstance(aClass).getDeclaredAnnotations();
    }

    @Advanced
    public static Method getDeclaredMethod(Class aClass, String name, Class... parameterTypes)
            throws NoSuchMethodException, SecurityException {
        Method method = findMethod(aClass.getDeclaredMethods(), name, parameterTypes);
        return method != null ? method : aClass.getDeclaredMethod(name, parameterTypes);
    }

    public static Class getEnclosingClass(Class aClass) {
        MethodDescriptor descriptor = ClassDescriptor.getInstance(aClass).getEnclosingMethodDescriptor();
        return descriptor == null ? aClass.getDeclaringClass() : descriptor.getClassDescriptor().getTarget();
    }

    public static Constructor getEnclosingConstructor(Class aClass) {
        MethodDescriptor descriptor = ClassDescriptor.getInstance(aClass).getEnclosingMethodDescriptor();
        return descriptor == null ? null : descriptor.getConstructor();
    }

    public static Method getEnclosingMethod(Class aClass) {
        MethodDescriptor descriptor = ClassDescriptor.getInstance(aClass).getEnclosingMethodDescriptor();
        return descriptor == null ? null : descriptor.getMethod();
    }

    public static Object[] getEnumConstants(Class aClass) {
        Object[] constants = Enum_.getEnumConstants(aClass);
        return constants == null ? null : constants.clone();
    }

    public static Type[] getGenericInterfaces(Class aClass) {
        Type[] interfaces = ClassDescriptor.getInstance(aClass).getGenericInterfaces();
        return interfaces != null ? interfaces : aClass.getInterfaces();
    }

    public static Type getGenericSuperclass(Class aClass) {
        Type genericSuperclass = ClassDescriptor.getInstance(aClass).getGenericSuperclass();
        return genericSuperclass != null ? genericSuperclass : aClass.getSuperclass();
    }

    @Advanced
    public static Method getMethod(Class aClass, String name, Class... parameterTypes)
            throws NoSuchMethodException, SecurityException {
        Method method = findMethod(aClass.getMethods(), name, parameterTypes);
        return method != null ? method : aClass.getMethod(name, parameterTypes);
    }

    public static String getSimpleName(Class aClass) {
        if (aClass.isArray()) return getSimpleName(aClass.getComponentType()) + "[]";
        String thisName = aClass.getName();
        Class enclosingClass = getEnclosingClass(aClass);
        if (enclosingClass == null) return thisName.substring(thisName.lastIndexOf('.') + 1);
        String enclosingName = enclosingClass.getName();
        if (!thisName.startsWith(enclosingName)) throw new InternalError();
        String name = thisName.substring(enclosingName.length());
        if (!name.startsWith("$")) throw new InternalError();
        for (int i = 1; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c < '0' || c > '9') return name.substring(i);
        }
        return "";
    }

    public static TypeVariable[] getTypeParameters(Class aClass) {
        return ClassDescriptor.getInstance(aClass).getTypeParameters();
    }

    public static boolean isAnnotation(Class aClass) {
        return ClassDescriptor.getInstance(aClass).isAccess(Opcodes.ACC_ANNOTATION);
    }

    public static boolean isAnnotationPresent(Class aClass, Class annotationType) {
        return ClassDescriptor.getInstance(aClass).isAnnotationPresent(annotationType);
    }

    public static boolean isAnonymousClass(Class aClass) {
        return "".equals(getSimpleName(aClass));
    }

    public static boolean isEnum(Class aClass) {
        return aClass.getSuperclass() == Enum.class && ClassDescriptor.getInstance(aClass).isAccess(Opcodes.ACC_ENUM);
    }

    public static boolean isLocalClass(Class aClass) {
        return ClassDescriptor.getInstance(aClass).isLocalOrAnonymous() && !isAnonymousClass(aClass);
    }

    public static boolean isMemberClass(Class aClass) {
        return aClass.getDeclaringClass() != null;
    }

    public static boolean isSynthetic(Class aClass) {
        return ClassDescriptor.getInstance(aClass).isAccess(Opcodes.ACC_SYNTHETIC);
    }

    private static Method findMethod(Method[] methods, String name, Class... parameterTypes) {
        Method result = null;
        for (Method method : methods) {
            if (method.getName().equals(name) && Arrays.equals(method.getParameterTypes(), parameterTypes) &&
                    (result == null || result.getReturnType().isAssignableFrom(method.getReturnType()))) {
                result = method;
            }
        }
        return result;
    }
}
