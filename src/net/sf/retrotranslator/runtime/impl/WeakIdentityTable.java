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

import java.lang.ref.*;
import java.util.concurrent.*;

/**
 * @author Taras Puchko
 */
public class WeakIdentityTable<K, V> {

    private final ConcurrentMap<IdentityKey<K>, V> map = new ConcurrentHashMap<IdentityKey<K>, V>();
    private final ReferenceQueue<K> queue = new ReferenceQueue<K>();

    public WeakIdentityTable() {
    }

    public V lookup(K key) {
        return map.get(new StrongKey<K>(key));
    }

    public V obtain(K key) {
        V currentValue = map.get(new StrongKey<K>(key));
        if (currentValue != null) {
            return currentValue;
        }
        cleanup();
        V newValue = initialValue();
        V previousValue = map.putIfAbsent(new WeakKey<K>(key, queue), newValue);
        return previousValue != null ? previousValue : newValue;
    }

    public void putIfAbsent(K key, V value) {
        cleanup();
        map.putIfAbsent(new WeakKey<K>(key, queue), value);
    }

    public int size() {
        cleanup();
        return map.size();
    }

    protected V initialValue() {
        return null;
    }

    private void cleanup() {
        Reference reference;
        while ((reference = queue.poll()) != null) {
            map.remove(reference);
        }
    }

    private interface IdentityKey<T> {
        T get();
    }

    private static class StrongKey<T> implements IdentityKey<T> {

        private T referent;

        public StrongKey(T referent) {
            if (referent == null) {
                throw new NullPointerException();
            }
            this.referent = referent;
        }

        public T get() {
            return referent;
        }

        public int hashCode() {
            return System.identityHashCode(referent);
        }

        public boolean equals(Object obj) {
            return obj instanceof IdentityKey && ((IdentityKey) obj).get() == referent;
        }
    }

    private static class WeakKey<T> extends WeakReference<T> implements IdentityKey<T> {

        private int hashCode;

        public WeakKey(T referent, ReferenceQueue<T> queue) {
            super(referent, queue);
            if (referent == null) {
                throw new NullPointerException();
            }
            hashCode = System.identityHashCode(referent);
        }

        public int hashCode() {
            return hashCode;
        }

        public boolean equals(Object obj) {
            return obj == this || (obj instanceof IdentityKey && ((IdentityKey) obj).get() == get());
        }

    }

}
