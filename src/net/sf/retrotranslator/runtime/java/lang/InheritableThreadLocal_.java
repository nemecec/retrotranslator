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

import java.util.*;
import net.sf.retrotranslator.registry.Advanced;

/**
 * @author Taras Puchko
 */
@Advanced("ThreadLocal.remove")
public class InheritableThreadLocal_ extends ThreadLocal_ {

    private class Key {
        public InheritableThreadLocal_ get() {
            return InheritableThreadLocal_.this;
        }
    }

    private static class Container extends InheritableThreadLocal<Map<Key, Object>> {

        protected Map<Key, Object> initialValue() {
            return new WeakHashMap<Key, Object>();
        }

        protected Map<Key, Object> childValue(Map<Key, Object> parentValue) {
            WeakHashMap<Key, Object> result = new WeakHashMap<Key, Object>(parentValue.size() * 2);
            for (Map.Entry<Key, Object> entry : parentValue.entrySet()) {
                Key key = entry.getKey();
                result.put(key, key.get().childValue(entry.getValue()));
            }
            return result;
        }
    }

    private static final Container container = new Container();

    private final Key key = new Key();

    public InheritableThreadLocal_() {
    }

    public Object get() {
        Map<Key, Object> map = container.get();
        Object value = map.get(key);
        if (value == null && !map.containsKey(key)) {
            map.put(key, value = initialValue());
        }
        return value;
    }

    public void set(Object value) {
        container.get().put(key, value);
    }

    public void remove() {
        container.get().remove(key);
    }

    protected Object childValue(Object parentValue) {
        return parentValue;
    }

}
