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
public class _LinkedListTestCase extends TestCase {

    LinkedList list = new LinkedList();

    protected void setUp() throws Exception {
        super.setUp();
        list.add("a");
        list.add("b");
    }

    public void testElement() throws Exception {
        assertEquals("a", list.element());
    }

    public void testOffer() throws Exception {
        assertTrue(list.offer("c"));
        assertEquals("c", list.getLast());
    }

    public void testPeek() throws Exception {
        assertEquals("a", list.peek());
        list.clear();
        assertNull(list.peek());
    }

    public void testPoll() throws Exception {
        assertEquals("a", list.poll());
        assertEquals("b", list.getFirst());
        assertEquals(1, list.size());
        list.clear();
        assertNull(list.poll());
    }

    public void testRemove() throws Exception {
        assertEquals("a", list.remove());
        assertEquals("b", list.getFirst());
        assertEquals(1, list.size());
        list.clear();
        try {
            list.remove();
            fail();
        } catch (NoSuchElementException e) {
            //ok
        }
    }
}