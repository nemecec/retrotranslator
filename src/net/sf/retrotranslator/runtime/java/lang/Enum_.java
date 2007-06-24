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

import java.io.*;
import java.lang.ref.WeakReference;
import java.lang.reflect.*;
import java.security.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.sf.retrotranslator.runtime.impl.WeakIdentityTable;

/**
 * @author Taras Puchko
 */
public abstract class Enum_<E extends Enum_<E>> implements Comparable<E>, Serializable {

    private static final WeakIdentityTable<Class, Map<String, WeakReference<Enum_>>> table =
            new WeakIdentityTable<Class, Map<String, WeakReference<Enum_>>>() {
                protected Map<String, WeakReference<Enum_>> initialValue() {
                    return new ConcurrentHashMap<String, WeakReference<Enum_>>();
                }
            };

    private final String name;

    private final int ordinal;

    protected Enum_(String name, int ordinal) {
        this.name = name;
        this.ordinal = ordinal;
        table.obtain(getDeclaringClass()).put(name, new WeakReference<Enum_>(this));
    }

    public final String name() {
        return name;
    }

    public final int ordinal() {
        return ordinal;
    }

    public String toString() {
        return name;
    }

    public final boolean equals(Object other) {
        return this == other;
    }

    public final int hashCode() {
        return super.hashCode();
    }

    protected final Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public final int compareTo(E other) {
        if (this.getClass() != other.getClass() && this.getDeclaringClass() != other.getDeclaringClass()) {
            throw new ClassCastException();
        }
        return this.ordinal - other.ordinal;
    }

    public final Class<E> getDeclaringClass() {
        Class clazz = getClass();
        Class zuper = clazz.getSuperclass();
        return (zuper == Enum_.class) ? clazz : zuper;
    }

    public static <T extends Enum_<T>> T valueOf(Class<T> enumType, String name) {
        if (name == null) {
            throw new NullPointerException("Name is null");
        }
        Map<String, WeakReference<Enum_>> map = getMap(enumType);
        if (map == null) {
            throw new IllegalArgumentException(enumType.getName() + " is not an enum type");
        }
        WeakReference<Enum_> reference = map.get(name);
        if (reference == null) {
            throw new IllegalArgumentException("No enum const " + enumType + "." + name);
        }
        return (T) reference.get();
    }

    protected Object readResolve() throws InvalidObjectException {
        try {
            return valueOf(getDeclaringClass(), name);
        } catch (IllegalArgumentException e) {
            InvalidObjectException exception = new InvalidObjectException(e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    protected static Enum_[] getEnumConstants(Class aClass) {
        Map<String, WeakReference<Enum_>> map = getMap(aClass);
        if (map == null) {
            return null;
        }
        Enum_[] result = (Enum_[]) Array.newInstance(aClass, map.size());
        for (WeakReference<Enum_> reference : map.values()) {
            Enum_ constant = reference.get();
            result[constant.ordinal] = constant;
        }
        return result;
    }

    protected static void setEnumConstants(Class enumType, Enum_[] enumConstants) {
        // for backward compatibility with 1.2.1
    }

    private static Map<String, WeakReference<Enum_>> getMap(final Class enumType) {
        Map<String, WeakReference<Enum_>> map = table.lookup(enumType);
        if (map != null) {
            return map;
        }
        if (enumType.getSuperclass() != Enum_.class) {
            return null;
        }
        return AccessController.doPrivileged(new PrivilegedAction<Map<String, WeakReference<Enum_>>>() {
            public Map<String, WeakReference<Enum_>> run() {
                try {
                    Class.forName(enumType.getName(), true, enumType.getClassLoader());
                    Map<String, WeakReference<Enum_>> result = table.lookup(enumType);
                    if (result != null) {
                        return result;
                    }
                    Method method = enumType.getMethod("values");
                    method.setAccessible(true);
                    method.invoke(null);
                } catch (Exception e) {
                    //ignore
                }
                return table.lookup(enumType);
            }
        });
    }

}
