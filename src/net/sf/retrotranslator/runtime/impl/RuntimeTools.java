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
package net.sf.retrotranslator.runtime.impl;

import net.sf.retrotranslator.runtime.asm.Type;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.util.MissingResourceException;

/**
 * @author Taras Puchko
 */
public class RuntimeTools {

    public static final String CONSTRUCTOR_NAME = "<init>";
    public static final String STATIC_NAME = "<clinit>";
    public static final String CLASS_EXTENSION = ".class";

    public static Class getBaseClass(char type) {
        return getBaseClass(Type.getType(new String(new char[]{type})));
    }

    public static Class getBaseClass(Type type) {
        switch (type.getSort()) {
            case Type.VOID:
                return void.class;
            case Type.BOOLEAN:
                return boolean.class;
            case Type.CHAR:
                return char.class;
            case Type.BYTE:
                return byte.class;
            case Type.SHORT:
                return short.class;
            case Type.INT:
                return int.class;
            case Type.FLOAT:
                return float.class;
            case Type.LONG:
                return long.class;
            case Type.DOUBLE:
                return double.class;
        }
        return null;
    }

    public static String getConstructorDescriptor(final Constructor c) {
        Class[] parameters = c.getParameterTypes();
        StringBuffer buf = new StringBuffer("(");
        for (Class parameter : parameters) {
            buf.append(Type.getDescriptor(parameter));
        }
        return buf.append(")V").toString();
    }

    public static Object cloneNonEmptyArray(Object value) {
        if (!value.getClass().isArray() || Array.getLength(value) == 0) return value;
        if (value instanceof Object[]) return ((Object[]) value).clone();
        if (value instanceof boolean[]) return ((boolean[]) value).clone();
        if (value instanceof byte[]) return ((byte[]) value).clone();
        if (value instanceof char[]) return ((char[]) value).clone();
        if (value instanceof double[]) return ((double[]) value).clone();
        if (value instanceof float[]) return ((float[]) value).clone();
        if (value instanceof int[]) return ((int[]) value).clone();
        if (value instanceof long[]) return ((long[]) value).clone();
        if (value instanceof short[]) return ((short[]) value).clone();
        throw new IllegalStateException();
    }

    public static String getString(java.lang.reflect.Type type) {
        if (!(type instanceof Class)) return type.toString();
        Class aClass = (Class) type;
        int dimensionCount = 0;
        for (; aClass.isArray(); dimensionCount++) {
            aClass = aClass.getComponentType();
        }
        if (dimensionCount == 0) return aClass.getName();
        StringBuilder builder = new StringBuilder();
        builder.append(aClass.getName());
        for (; dimensionCount > 0; dimensionCount--) {
            builder.append("[]");
        }
        return builder.toString();
    }

    public static StringBuilder append(StringBuilder builder, java.lang.reflect.Type[] types) {
        for (int i = 0; i < types.length; i++) {
            if (i > 0) builder.append(',');
            builder.append(getString(types[i]));
        }
        return builder;
    }

    public static byte[] readResourceToByteArray(Class loader, String resourceName) throws MissingResourceException {
        InputStream inputStream = loader.getResourceAsStream(resourceName);
        if (inputStream == null) return null;
        try {
            try {
                byte[] buffer = new byte[0x1000];
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                int count;
                while((count = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, count);
                }
                return outputStream.toByteArray();
            } finally {
                inputStream.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] getBytecode(Class target) {
        if (target.isPrimitive() || target.isArray()) return null;
        String targetName = target.getName();
        int index = targetName.lastIndexOf('.');
        String simpleName = index < 0 ? targetName : targetName.substring(index + 1);
        return readResourceToByteArray(target, simpleName + CLASS_EXTENSION);
    }
}
