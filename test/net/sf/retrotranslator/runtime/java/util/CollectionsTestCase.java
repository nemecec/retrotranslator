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
package net.sf.retrotranslator.runtime.java.util;

import java.util.*;
import junit.framework.TestCase;

/**
 * @author Taras Puchko
 */
public class CollectionsTestCase extends TestCase {

    public void testCheckedCollection() throws Exception {
        Collection strings = Collections.checkedCollection(new ArrayList(), String.class);
        strings.add("abc");
        try {
            strings.add(Boolean.TRUE);
            fail();
        } catch (ClassCastException e) {
            //ok
        }
    }

    public void testCheckedSet() throws Exception {
        Set strings = Collections.checkedSet(new HashSet(), String.class);
        strings.add("abc");
        try {
            strings.add(Boolean.TRUE);
            fail();
        } catch (ClassCastException e) {
            //ok
        }
    }

    public void testCheckedSortedSet() throws Exception {
        SortedSet strings = Collections.checkedSortedSet(new TreeSet(), String.class);
        strings.add("abc");
        try {
            strings.add(Boolean.TRUE);
            fail();
        } catch (ClassCastException e) {
            //ok
        }
    }

    public void testCheckedList() throws Exception {
        List strings = Collections.checkedList(new ArrayList(), String.class);
        strings.add("abc");
        try {
            strings.add(Boolean.TRUE);
            fail();
        } catch (ClassCastException e) {
            //ok
        }
    }

    public void testCheckedMap() throws Exception {
        Map strings = Collections.checkedMap(new HashMap(), String.class, Integer.class);
        strings.put("abc", Integer.MIN_VALUE);
        try {
            strings.put(Boolean.TRUE, Integer.MIN_VALUE);
            fail();
        } catch (ClassCastException e) {
            //ok
        }
        try {
            strings.put("abc", Boolean.TRUE);
            fail();
        } catch (ClassCastException e) {
            //ok
        }
        Set set = strings.entrySet();
        Map.Entry entry1 = (Map.Entry) set.iterator().next();
        entry1.setValue(Integer.MAX_VALUE);
        try {
            entry1.setValue("fail");
            fail();
        } catch (ClassCastException e) {
            //ok
        }
        Map.Entry entry2 = (Map.Entry) set.toArray()[0];
        entry2.setValue(Integer.MAX_VALUE);
        try {
            entry2.setValue("fail");
            fail();
        } catch (ClassCastException e) {
            //ok
        }
    }

    public void testCheckedSortedMap() throws Exception {
        Map strings = Collections.checkedSortedMap(new TreeMap(), String.class, Integer.class);
        strings.put("abc", Integer.MIN_VALUE);
        try {
            strings.put(Boolean.TRUE, Integer.MIN_VALUE);
            fail();
        } catch (ClassCastException e) {
            //ok
        }
        try {
            strings.put("abc", Boolean.TRUE);
            fail();
        } catch (ClassCastException e) {
            //ok
        }
        Set set = strings.entrySet();
        Map.Entry entry1 = (Map.Entry) set.iterator().next();
        entry1.setValue(Integer.MAX_VALUE);
        try {
            entry1.setValue("fail");
            fail();
        } catch (ClassCastException e) {
            //ok
        }
        Map.Entry entry2 = (Map.Entry) set.toArray()[0];
        entry2.setValue(Integer.MAX_VALUE);
        try {
            entry2.setValue("fail");
            fail();
        } catch (ClassCastException e) {
            //ok
        }
    }

    public void testEmptySet() throws Exception {
        assertSame(Collections.EMPTY_SET, Collections.emptySet());
    }

    public void testEmptyList() throws Exception {
        assertSame(Collections.EMPTY_LIST, Collections.emptyList());
    }

    public void testEmptyMap() throws Exception {
        assertSame(Collections.EMPTY_MAP, Collections.emptyMap());
    }

    public void testReverseOrder() throws Exception {
        String[] strings = {"a", "b"};
        Arrays.sort(strings, String.CASE_INSENSITIVE_ORDER);
        assertEquals("a", strings[0]);
        assertEquals("b", strings[1]);
        Arrays.sort(strings, Collections.reverseOrder(String.CASE_INSENSITIVE_ORDER));
        assertEquals("b", strings[0]);
        assertEquals("a", strings[1]);
    }

    public void testFrequency() throws Exception {
        assertEquals(2, Collections.frequency(Arrays.asList("a", "b", "c", "b", "a"), "b"));
    }

    public void testDisjoint() throws Exception {
        assertTrue(Collections.disjoint(Arrays.asList("a", "b", "c"), Arrays.asList("x", "y", "z")));
        assertFalse(Collections.disjoint(Arrays.asList("a", "b", "c"), Arrays.asList("x", "c", "z")));
    }

    public void testAddAll() throws Exception {
        List<String> list = new ArrayList<String>(Arrays.asList("a", "b", "c"));
        Collections.addAll(list, "x", "y");
        assertEquals(5, list.size());
        assertEquals("x", list.get(3));
    }

    public void testExisting_singletonList() throws Exception {
        List<String> list = Collections.singletonList("a");
        assertEquals(1, list.size());
        assertEquals("a", list.get(0));
    }

    public void testExisting_synchronizedMap() throws Exception {
        Map map = new HashMap();
        Map syncMap = Collections.synchronizedMap(map);
        syncMap.put("a", "b");
        assertEquals(1, map.size());
        assertEquals("b", map.get("a"));
    }

}
