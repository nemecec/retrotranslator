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

import junit.framework.*;
import java.util.*;

/**
 * @author Taras Puchko
 */
public class _DequeJava6TestCase extends TestCase {

    public void testAddFirst() throws Exception {
        for (Deque deque : new Deque[] {new ArrayDeque(), new LinkedList()}) {
            Collections.addAll(deque, "a", "b", "c");
            deque.addFirst("x");
            assertEquals("[x, a, b, c]", deque.toString());
        }
    }

    public void testAddLast() throws Exception {
        for (Deque deque : new Deque[] {new ArrayDeque(), new LinkedList()}) {
            Collections.addAll(deque, "a", "b", "c");
            deque.addLast("x");
            assertEquals("[a, b, c, x]", deque.toString());
        }
    }

    public void testDescendingIterator() throws Exception {
        for (Deque deque : new Deque[] {new ArrayDeque(), new LinkedList()}) {
            Collections.addAll(deque, "a", "b", "c");
            Iterator iterator = deque.descendingIterator();
            assertTrue(iterator.hasNext());
            assertEquals("c", iterator.next());
            assertTrue(iterator.hasNext());
            assertEquals("b", iterator.next());
            iterator.remove();
            assertTrue(iterator.hasNext());
            assertEquals("a", iterator.next());
            assertFalse(iterator.hasNext());
            assertEquals("[a, c]", deque.toString());
        }
    }

    public void testElement() throws Exception {
        for (Deque deque : new Deque[] {new ArrayDeque(), new LinkedList()}) {
            Collections.addAll(deque, "a", "b", "c");
            assertEquals("a", deque.element());
            assertEquals(3, deque.size());
            deque.clear();
            try {
                deque.element();
                fail();
            } catch (NoSuchElementException e) {
                //ok
            }
        }
    }

    public void testGetFirst() throws Exception {
        for (Deque deque : new Deque[] {new ArrayDeque(), new LinkedList()}) {
            Collections.addAll(deque, "a", "b", "c");
            assertEquals("a", deque.getFirst());
            assertEquals("[a, b, c]", deque.toString());
            deque.clear();
            try {
                deque.getFirst();
                fail();
            } catch (NoSuchElementException e) {
                //ok
            }
        }
    }

    public void testGetLast() throws Exception {
        for (Deque deque : new Deque[] {new ArrayDeque(), new LinkedList()}) {
            Collections.addAll(deque, "a", "b", "c");
            assertEquals("c", deque.getLast());
            assertEquals("[a, b, c]", deque.toString());
            deque.clear();
            try {
                deque.getLast();
                fail();
            } catch (NoSuchElementException e) {
                //ok
            }
        }
    }

    public void testOffer() throws Exception {
        for (Deque deque : new Deque[] {new ArrayDeque(), new LinkedList()}) {
            Collections.addAll(deque, "a", "b", "c");
            assertTrue(deque.offer("x"));
            assertEquals(4, deque.size());
            assertEquals("x", deque.getLast());
        }
    }

    public void testOfferFirst() throws Exception {
        for (Deque deque : new Deque[] {new ArrayDeque(), new LinkedList()}) {
            Collections.addAll(deque, "a", "b", "c");
            assertTrue(deque.offerFirst("x"));
            assertEquals(4, deque.size());
            assertEquals("x", deque.getFirst());
        }
    }

    public void testOfferLast() throws Exception {
        for (Deque deque : new Deque[] {new ArrayDeque(), new LinkedList()}) {
            Collections.addAll(deque, "a", "b", "c");
            assertTrue(deque.offerLast("x"));
            assertEquals(4, deque.size());
            assertEquals("x", deque.getLast());
        }
    }

    public void testPeek() throws Exception {
        for (Deque deque : new Deque[] {new ArrayDeque(), new LinkedList()}) {
            Collections.addAll(deque, "a", "b", "c");
            assertEquals("a", deque.peek());
            assertEquals(3, deque.size());
            deque.clear();
            assertNull(deque.peek());
        }
    }

    public void testPeekFirst() throws Exception {
        for (Deque deque : new Deque[] {new ArrayDeque(), new LinkedList()}) {
            Collections.addAll(deque, "a", "b", "c");
            assertEquals("a", deque.peekFirst());
            assertEquals(3, deque.size());
            deque.clear();
            assertNull(deque.peekFirst());
        }
    }

    public void testPeekLast() throws Exception {
        for (Deque deque : new Deque[] {new ArrayDeque(), new LinkedList()}) {
            Collections.addAll(deque, "a", "b", "c");
            assertEquals("c", deque.peekLast());
            assertEquals(3, deque.size());
            deque.clear();
            assertNull(deque.peekLast());
        }
    }

    public void testPoll() throws Exception {
        for (Deque deque : new Deque[] {new ArrayDeque(), new LinkedList()}) {
            Collections.addAll(deque, "a", "b", "c");
            assertEquals("a", deque.poll());
            assertEquals(2, deque.size());
            deque.clear();
            assertNull(deque.poll());
        }
    }

    public void testPollFirst() throws Exception {
        for (Deque deque : new Deque[] {new ArrayDeque(), new LinkedList()}) {
            Collections.addAll(deque, "a", "b", "c");
            assertEquals("a", deque.pollFirst());
            assertEquals(2, deque.size());
            deque.clear();
            assertNull(deque.pollFirst());
        }
    }

    public void testPollLast() throws Exception {
        for (Deque deque : new Deque[] {new ArrayDeque(), new LinkedList()}) {
            Collections.addAll(deque, "a", "b", "c");
            assertEquals("c", deque.pollLast());
            assertEquals(2, deque.size());
            deque.clear();
            assertNull(deque.pollLast());
        }
    }

    public void testPop() throws Exception {
        for (Deque deque : new Deque[] {new ArrayDeque(), new LinkedList()}) {
            Collections.addAll(deque, "a", "b", "c");
            assertEquals("a", deque.pop());
            assertEquals(2, deque.size());
            deque.clear();
            try {
                deque.pop();
                fail();
            } catch (NoSuchElementException e) {
                //ok
            }
        }
    }

    public void testPush() throws Exception {
        for (Deque deque : new Deque[] {new ArrayDeque(), new LinkedList()}) {
            Collections.addAll(deque, "a", "b", "c");
            deque.push("x");
            assertEquals(4, deque.size());
            assertEquals("x", deque.getFirst());
        }
    }

    public void testRemove() throws Exception {
        for (Deque deque : new Deque[] {new ArrayDeque(), new LinkedList()}) {
            Collections.addAll(deque, "a", "b", "c");
            assertEquals("a", deque.remove());
            assertEquals(2, deque.size());
            deque.clear();
            try {
                deque.remove();
                fail();
            } catch (NoSuchElementException e) {
                //ok
            }
        }
    }

    public void testRemoveFirst() throws Exception {
        for (Deque deque : new Deque[] {new ArrayDeque(), new LinkedList()}) {
            Collections.addAll(deque, "a", "b", "c");
            assertEquals("a", deque.removeFirst());
            assertEquals("[b, c]", deque.toString());
            deque.clear();
            try {
                deque.removeFirst();
                fail();
            } catch (NoSuchElementException e) {
                //ok
            }
        }
    }

    public void testRemoveLast() throws Exception {
        for (Deque deque : new Deque[] {new ArrayDeque(), new LinkedList()}) {
            Collections.addAll(deque, "a", "b", "c");
            assertEquals("c", deque.removeLast());
            assertEquals("[a, b]", deque.toString());
            deque.clear();
            try {
                deque.removeLast();
                fail();
            } catch (NoSuchElementException e) {
                //ok
            }
        }
    }

    public void testRemoveFirstOccurrence() throws Exception {
        for (Deque deque : new Deque[] {new ArrayDeque(), new LinkedList()}) {
            Collections.addAll(deque, "a", "b", "c", "b", "d");
            assertTrue(deque.removeFirstOccurrence("b"));
            assertEquals(4, deque.size());
            assertEquals("[a, c, b, d]", deque.toString());
            assertFalse(deque.removeFirstOccurrence("x"));
        }
    }

    public void testRemoveLastOccurrence() throws Exception {
        for (Deque deque : new Deque[] {new ArrayDeque(), new LinkedList()}) {
            Collections.addAll(deque, "a", "b", "c", "b", "d");
            assertTrue(deque.removeLastOccurrence("b"));
            assertEquals(4, deque.size());
            assertEquals("[a, b, c, d]", deque.toString());
            assertFalse(deque.removeLastOccurrence("x"));
        }
    }

}