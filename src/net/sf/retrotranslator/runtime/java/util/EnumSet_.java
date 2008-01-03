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
package net.sf.retrotranslator.runtime.java.util;

import java.util.*;

/**
 * @author Taras Puchko
 */
public class EnumSet_<E extends Enum<E>> extends HashSet<E> {

    private static final long serialVersionUID = 7684628957901243852L;

    private Class<E> elementType;

    private EnumSet_(Class<E> elementType) {
        if (!Enum.class.isAssignableFrom(elementType)) {
            throw new ClassCastException();
        }
        this.elementType = elementType;
    }

    public static <E extends Enum<E>> EnumSet_<E> allOf(Class<E> elementType) {
        EnumSet_<E> result = new EnumSet_<E>(elementType);
        for (E e : elementType.getEnumConstants()) {
            result.add(e);
        }
        return result;
    }

    public static <E extends Enum<E>> EnumSet_<E> complementOf(EnumSet_<E> enumSet) {
        Class<E> elementType = enumSet.elementType;
        EnumSet_<E> result = new EnumSet_<E>(elementType);
        for (E e : elementType.getEnumConstants()) {
            if (!enumSet.contains(e)) {
                result.add(e);
            }
        }
        return result;
    }

    public static <E extends Enum<E>> EnumSet_<E> copyOf(Collection<E> collection) {
        if (collection instanceof EnumSet_) {
            return copyOf((EnumSet_<E>) collection);
        }
        Iterator<E> iterator = collection.iterator();
        if (!iterator.hasNext()) {
            throw new IllegalArgumentException();
        }
        EnumSet_<E> result = EnumSet_.of(iterator.next());
        while (iterator.hasNext()) {
            result.add(iterator.next());
        }
        return result;
    }

    public static <E extends Enum<E>> EnumSet_<E> copyOf(EnumSet_<E> enumSet) {
        return enumSet.clone();
    }

    public static <E extends Enum<E>> EnumSet_<E> noneOf(Class<E> elementType) {
        return new EnumSet_<E>(elementType);
    }

    public static <E extends Enum<E>> EnumSet_<E> of(E e) {
        EnumSet_<E> result = new EnumSet_<E>(e.getDeclaringClass());
        result.add(e);
        return result;
    }

    public static <E extends Enum<E>> EnumSet_<E> of(E e1, E e2) {
        EnumSet_<E> result = of(e1);
        result.add(e2);
        return result;
    }

    public static <E extends Enum<E>> EnumSet_<E> of(E e1, E e2, E e3) {
        EnumSet_<E> result = of(e1);
        result.add(e2);
        result.add(e3);
        return result;
    }

    public static <E extends Enum<E>> EnumSet_<E> of(E e1, E e2, E e3, E e4) {
        EnumSet_<E> result = of(e1);
        result.add(e2);
        result.add(e3);
        result.add(e4);
        return result;
    }

    public static <E extends Enum<E>> EnumSet_<E> of(E e1, E e2, E e3, E e4, E e5) {
        EnumSet_<E> result = of(e1);
        result.add(e2);
        result.add(e3);
        result.add(e4);
        result.add(e5);
        return result;
    }

    public static <E extends Enum<E>> EnumSet_<E> of(E first, E... rest) {
        EnumSet_<E> result = of(first);
        for (E e : rest) {
            result.add(e);
        }
        return result;
    }

    public static <E extends Enum<E>> EnumSet_<E> range(E from, E to) {
        int fromIndex = from.ordinal();
        int toIndex = to.ordinal();
        if (fromIndex > toIndex) throw new IllegalArgumentException();
        Class<E> elementType = from.getDeclaringClass();
        E[] enumConstants = elementType.getEnumConstants();
        EnumSet_<E> result = new EnumSet_<E>(elementType);
        for (int i = fromIndex; i <= toIndex; i++) {
            E enumConstant = enumConstants[i];
            result.add(enumConstant);
        }
        return result;
    }

    public boolean add(E o) {
        if (o == null) {
            throw new NullPointerException();
        }
        return super.add(elementType.cast(o));
    }

    public Iterator<E> iterator() {
        TreeSet<E> treeSet = new TreeSet<E>(ENUM_COMPARATOR);
        Iterator<E> iterator = super.iterator();
        while (iterator.hasNext()) {
            treeSet.add(iterator.next());
        }
        return treeSet.iterator();
    }

    private static final Comparator<Enum> ENUM_COMPARATOR = new Comparator<Enum>() {
        public int compare(Enum o1, Enum o2) {
            return o1.ordinal() - o2.ordinal();
        }
    };

    public EnumSet_<E> clone() {
        return (EnumSet_<E>) super.clone();
    }

}
