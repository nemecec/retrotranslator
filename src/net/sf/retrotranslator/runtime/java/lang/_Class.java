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
package net.sf.retrotranslator.runtime.java.lang;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.security.*;
import net.sf.retrotranslator.registry.Advanced;
import net.sf.retrotranslator.runtime.asm.Opcodes;
import net.sf.retrotranslator.runtime.impl.*;

/**
 * @author Taras Puchko
 */
public class _Class {

    private static final String[] RUNTIME_PREFIXES = getPrefixes();

    public static Class asSubclass(Class aClass, Class superclass) {
        if (superclass.isAssignableFrom(aClass)) return aClass;
        throw new ClassCastException(aClass.toString());
    }

    public static Object cast(Class aClass, Object obj) {
        if (obj == null || aClass.isInstance(obj)) return obj;
        throw new ClassCastException(aClass.toString());
    }

    @Advanced("Class.forName")
    public static Class forName(String name) throws ClassNotFoundException {
        return forName(name, true, getCallerClassLoader());
    }

    @Advanced("Class.forName")
    public static Class forName(String name, boolean initialize, ClassLoader loader) throws ClassNotFoundException {
        try {
            return Class.forName(name, initialize, loader);
        } catch (ClassNotFoundException e) {
            for (String prefix : RUNTIME_PREFIXES) {
                try {
                    return Class.forName(prefix + name + "_", initialize, loader);
                } catch (ClassNotFoundException ex) {
                    // ignore
                }
            }
            try {
                return Class.forName(RuntimeTools.CONCURRENT_PREFIX + name, initialize, loader);
            } catch (ClassNotFoundException ex) {
                // ignore
            }
            if (name.equals("java.lang.StringBuilder")) {
                return StringBuffer.class;
            }
            throw e;
        }
    }

    public static Annotation getAnnotation(Class aClass, Class annotationType) {
        return ClassDescriptor.getInstance(aClass).getAnnotation(annotationType);
    }

    public static Annotation[] getAnnotations(Class aClass) {
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

    public static Annotation[] getDeclaredAnnotations(Class aClass) {
        return ClassDescriptor.getInstance(aClass).getDeclaredAnnotations();
    }

    @Advanced("Class.getDeclaredMethod")
    public static Method getDeclaredMethod(Class aClass, String name, Class... parameterTypes)
            throws NoSuchMethodException, SecurityException {
        Method method = findMethod(aClass.getDeclaredMethods(), name, parameterTypes);
        if (method != null) {
            return method;
        }
        method = findBackportedMethod(aClass, name, parameterTypes);
        if (method != null) {
            return method;
        }
        return aClass.getDeclaredMethod(name, parameterTypes);
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
        return Enum_.getEnumConstants(aClass);
    }

    public static Type[] getGenericInterfaces(Class aClass) {
        return RuntimeTools.getTypes(aClass.getInterfaces(),
                ClassDescriptor.getInstance(aClass).getGenericInterfaces());
    }

    public static Type getGenericSuperclass(Class aClass) {
        return RuntimeTools.getType(aClass.getSuperclass(),
                ClassDescriptor.getInstance(aClass).getGenericSuperclass());
    }

    @Advanced("Class.getMethod")
    public static Method getMethod(Class aClass, String name, Class... parameterTypes)
            throws NoSuchMethodException, SecurityException {
        Method method = findMethod(aClass.getMethods(), name, parameterTypes);
        if (method != null) {
            return method;
        }
        method = findBackportedMethod(aClass, name, parameterTypes);
        if (method != null) {
            return method;
        }
        return aClass.getMethod(name, parameterTypes);
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

    // Referenced from translated bytecode
    public static void setEncodedMetadata(Class aClass, String metadata) {
        ClassDescriptor.setEncodedMetadata(aClass, metadata);
    }

    private static String[] getPrefixes() {
        String p = RuntimeTools.getPrefix("java.lang.Iterable_", Iterable_.class);
        if (p == null) {
            return new String[0];
        }
        if (p.endsWith(".v15.")) {
            return new String[] {p, p.substring(0, p.length() - 5) + ".v14."};
        } else {
            return new String[] {p};
        }
    }

    private static ClassLoader getCallerClassLoader() {
        final Class thisClass = _Class.class;
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            public ClassLoader run() {
                try {
                    Class[] context = new SecurityManager() {
                        protected Class[] getClassContext() {
                            return super.getClassContext();
                        }
                    }.getClassContext();
                    boolean found = false;
                    for (Class aClass : context) {
                        if (aClass == thisClass) {
                            found = true;
                        } else if (found) {
                            return aClass.getClassLoader();
                        }
                    }
                } catch (Exception e) {
                    // ignore
                }
                return thisClass.getClassLoader();
            }
        });
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

    private static Method findBackportedMethod(Class aClass, String name, Class... parameterTypes) {
        for (String prefix : RUNTIME_PREFIXES) {
            String s = prefix + aClass.getName();
            int index = s.lastIndexOf('.');
            String className = s.substring(0, index + 1) + "_" + s.substring(index + 1);
            Method method = findStaticMethod(className, name, parameterTypes);
            if (method != null) {
                return method;
            }
        }
        return findStaticMethod(RuntimeTools.CONCURRENT_PREFIX + aClass.getName(), name, parameterTypes);
    }

    private static Method findStaticMethod(String className, String methodName, Class... parameterTypes) {
        try {
            Method method = Class.forName(className).getMethod(methodName, parameterTypes);
            if (Modifier.isStatic(method.getModifiers())) {
                return method;
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

}
