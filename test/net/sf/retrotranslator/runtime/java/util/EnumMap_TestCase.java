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
package net.sf.retrotranslator.runtime.java.util;

import junit.framework.TestCase;

import java.util.*;
import java.lang.annotation.ElementType;

/**
 * @author Taras Puchko
 */
public class EnumMap_TestCase extends TestCase {

    private static enum Day {
        SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIEDAY, SATURDAY
    }

    public void testClear() {
        EnumMap<Day, String> map = getSampleMap();
        assertFalse(map.isEmpty());
        assertEquals(3, map.size());
        map.clear();
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
    }

    public void testClone() {
        EnumMap<Day, String> clone = getSampleMap().clone();
        assertTrue(clone.equals(getSampleMap()));
    }

    public void testConstructors() {
        EnumMap<Day, String> map = getSampleMap();
        assertEquals(3, new EnumMap<Day, String>(map).size());
        assertEquals(3, new EnumMap<Day, String>((Map<Day, String>) map).size());
        assertEquals(3, new EnumMap<Day, String>(new HashMap<Day, String>(map)).size());
        try {
            new EnumMap(String.class);
            fail();
        } catch (NullPointerException e) {
            //ok
        }
        try {
            new EnumMap<Day, String>(new HashMap<Day, String>());
            fail();
        } catch (IllegalArgumentException e) {
            //ok
        }
        try {
            HashMap m = new HashMap();
            m.put("a", "b");
            new EnumMap(m);
            fail();
        } catch (ClassCastException e) {
            //ok
        }
    }

    public void testEntrySet() {
        EnumMap<Day, String> map = getSampleMap();
        Set<Map.Entry<Day, String>> entries = map.entrySet();
        assertEquals(3, entries.size());
        assertFalse(entries.contains("a"));
        assertTrue(entries.contains(new MapEntry(Day.SUNDAY, "Sunday")));
        assertFalse(entries.contains(new MapEntry(Day.WEDNESDAY, "Frieday")));
        Iterator<Map.Entry<Day, String>> iterator = entries.iterator();
        Map.Entry<Day, String> first = iterator.next();
        assertEquals(Day.SUNDAY, first.getKey());
        assertEquals("Sunday", first.getValue());
        Map.Entry<Day, String> second = iterator.next();
        assertEquals(Day.WEDNESDAY, second.getKey());
        assertEquals("Wednesday", second.getValue());
        iterator.remove();
        Map.Entry<Day, String> third = iterator.next();
        assertEquals(Day.FRIEDAY, third.getKey());
        assertEquals("Frieday", third.getValue());
        assertFalse(iterator.hasNext());
        assertFalse(entries.remove("a"));
        assertFalse(entries.remove(new MapEntry(Day.FRIEDAY, "Sunday")));
        assertTrue(entries.remove(new MapEntry(Day.SUNDAY, "Sunday")));
        assertEquals(1, entries.size());
        assertEquals(1, map.size());
    }

    public void testEquals() {
        assertTrue(getSampleMap().equals(getSampleMap()));
        assertTrue(getSampleMap().equals(new HashMap(getSampleMap())));
    }

    public void testGet() {
        assertEquals("Frieday", getSampleMap().get(Day.FRIEDAY));
        assertEquals(null, getSampleMap().get("s"));
        assertEquals(null, getSampleMap().get(null));
    }

    public void testKeySet() {
        EnumMap<Day, String> map = getSampleMap();
        Set<Day> days = map.keySet();
        assertFalse(days.isEmpty());
        assertEquals(3, days.size());
        assertTrue(days.contains(Day.SUNDAY));
        assertFalse(days.contains(Day.MONDAY));
        assertFalse(days.contains(Day.TUESDAY));
        assertTrue(days.contains(Day.WEDNESDAY));
        assertFalse(days.contains(Day.THURSDAY));
        assertTrue(days.contains(Day.FRIEDAY));
        assertFalse(days.contains(Day.SATURDAY));
        Iterator<Day> iterator = days.iterator();
        assertEquals(Day.SUNDAY, iterator.next());
        assertEquals(Day.WEDNESDAY, iterator.next());
        iterator.remove();
        assertEquals(Day.FRIEDAY, iterator.next());
        assertFalse(iterator.hasNext());
        assertTrue(days.remove(Day.FRIEDAY));
        assertEquals(1, days.size());
        assertEquals(1, map.size());
    }

    public void testPut() {
        assertEquals("Sunday", getSampleMap().put(Day.SUNDAY, "v"));
        try {
            ((Map) getSampleMap()).put("k", "v");
            fail();
        } catch (ClassCastException e) {
            //ok
        }
        try {
            getSampleMap().put(null, "null");
            fail();
        } catch (NullPointerException e) {
            //ok
        }
    }

    public void testPutAll() {
        EnumMap<Day, String> map = new EnumMap<Day, String>(Day.class);
        map.putAll(getSampleMap());
        assertEquals(3, map.size());
        map.clear();
        map.putAll(new HashMap<Day, String>(getSampleMap()));
        assertEquals(3, map.size());
        EnumMap strings = new EnumMap(ElementType.class);
        map.putAll(strings);
        strings.put(ElementType.ANNOTATION_TYPE, "a");
        try {
            map.putAll(strings);
            fail();
        } catch (ClassCastException e) {
            //ok
        }
    }

    public void testRemove() {
        assertEquals("Wednesday", getSampleMap().remove(Day.WEDNESDAY));
        assertNull(getSampleMap().remove(Day.MONDAY));
        assertNull(getSampleMap().remove(null));
        assertNull(getSampleMap().remove("k"));
    }

    public void testValues() {
        EnumMap<Day, String> map = getSampleMap();
        Collection<String> values = map.values();
        assertEquals(3, values.size());
        assertTrue(values.contains("Sunday"));
        assertFalse(values.contains("Saturday"));
        Iterator<String> iterator = values.iterator();
        assertEquals("Sunday", iterator.next());
        assertEquals("Wednesday", iterator.next());
        iterator.remove();
        assertEquals("Frieday", iterator.next());
        assertFalse(iterator.hasNext());
        values.remove("Sunday");
        assertEquals(1, values.size());
        assertEquals(1, map.size());
    }

    private EnumMap<Day, String> getSampleMap() {
        EnumMap<Day, String> map = new EnumMap<Day, String>(Day.class);
        assertNull(map.put(Day.SUNDAY, "Sunday"));
        assertNull(map.put(Day.FRIEDAY, "Frieday"));
        assertNull(map.put(Day.WEDNESDAY, "Wednesday"));
        return map;
    }

    private static class MapEntry implements Map.Entry {

        private Object key;
        private Object value;

        public MapEntry(Object key, Object value) {
            this.key = key;
            this.value = value;
        }

        public Object getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }

        public Object setValue(Object value) {
            Object previous = this.value;
            this.value = value;
            return previous;
        }
    }
}
