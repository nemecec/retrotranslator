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

import java.io.Serializable;
import java.util.*;

/**
 * @author Taras Puchko
 */
public class EnumMap_<K extends Enum<K>, V> extends TreeMap<K, V> {

    private static final long serialVersionUID = 3267103726949236459L;

    private Class<K> keyType;

    public EnumMap_(Class<K> keyType) {
        super(new EnumComparator());
        if (!Enum.class.isAssignableFrom(keyType)) {
            throw new NullPointerException();
        }
        this.keyType = keyType;
    }

    public EnumMap_(EnumMap_<K, ? extends V> map) {
        super(map);
        this.keyType = map.keyType;
    }

    public EnumMap_(Map<K, ? extends V> map) {
        this(getKeyType(map));
        putAll(map);
    }

    public V put(K key, V value) {
        Class<? extends Enum> aClass = key.getClass();
        if (aClass != keyType && aClass.getSuperclass() != keyType) {
            throw new ClassCastException(aClass.getName());
        }
        return super.put(key, value);
    }

    public void putAll(Map<? extends K, ? extends V> map) {
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public EnumMap_<K, V> clone() {
        return (EnumMap_<K, V>) super.clone();
    }

    private static <K extends Enum<K>, V> Class<K> getKeyType(Map<K, ? extends V> map) {
        if (map instanceof EnumMap_) {
            return ((EnumMap_<K, ? extends V>) map).keyType;
        }
        if (map.isEmpty()) {
            throw new IllegalArgumentException();
        }
        return map.keySet().iterator().next().getDeclaringClass();
    }

    private static class EnumComparator implements Comparator, Serializable {
        public int compare(Object o1, Object o2) {
            if (o1 instanceof Enum) {
                return o2 instanceof Enum ? ((Enum) o1).ordinal() - ((Enum) o2).ordinal() : 1;
            } else {
                return o2 instanceof Enum ? -1 : 0;
            }
        }
    }

}
