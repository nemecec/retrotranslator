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
package net.sf.retrotranslator.transformer;

import java.lang.reflect.AnnotatedElement;
import java.util.*;
import java.util.concurrent.*;
import junit.framework.TestCase;

public class GeneralReplacementVisitorTestCase extends TestCase {

    private String hello = "Hello,";
    private String world = "World!";
    private StringBuilder builder = new StringBuilder();

    public void testImplicit() {
        assertEquals("Hello, World!", hello + " " + world);
    }

    public void testExplicit() {
        assertEquals("Hello, World!", builder.append(hello).append(" ").append(world).toString());
    }

    public void testCallable() throws Exception {
        final String hello = "Hello, World";
        Callable callable = new Callable() {
            public Object call() throws Exception {
                return hello;
            }
        };
        assertSame(hello, callable.call());
    }

    public void testConcurrentMap() throws Exception {
        ConcurrentMap<String, String> map = new ConcurrentHashMap<String, String>();
        map.put("hi", "Hello");
        map.put("bye", "Good-bye");
        assertEquals("Hello", map.get("hi"));
    }

    public void testDelayQueue() throws Exception {
        class MyDelayed implements Delayed {
            public long getDelay(TimeUnit unit) {
                return 0;
            }

            public int compareTo(Delayed o) {
                return 0;
            }
        }
        DelayQueue<MyDelayed> delayQueue = new DelayQueue<MyDelayed>();
        delayQueue.offer(new MyDelayed());
        delayQueue.put(new MyDelayed());
        delayQueue.offer(new MyDelayed(), 0, TimeUnit.SECONDS);
        delayQueue.add(new MyDelayed());
        assertNotNull(delayQueue.take());
        assertNotNull(delayQueue.poll(0, TimeUnit.SECONDS));
        assertNotNull(delayQueue.poll());
        assertNotNull(delayQueue.peek());
    }

    public static Queue getQueue() {
        return new LinkedList();
    }

    public void testMethod() {
        assertNotNull(getQueue());
    }

    public void testCast() {
        try {
            String s = (String) new Object();
            fail();
        } catch (ClassCastException e) {
            //ok
        }
        Queue queue;
        Object linkedList = new LinkedList();
        queue = (Queue) linkedList;
        queue.peek();
        Object priorityQueue = new PriorityQueue();
        queue = (Queue) priorityQueue;
        Object string = "String";
        try {
            queue = (Queue) string;
            fail();
        } catch (ClassCastException e) {
            //ok
        }
        queue.peek();
    }

    public void testArray() {
        Queue[][][][] array = new Queue[1][][][];
        array[0] = new Queue[1][1][];
        array[0][0][0] = new Queue[1];
        array[0][0][0][0] = new LinkedList();
        Object object = array;
        ((Queue[][][][]) object)[0][0][0][0].peek();
    }

    public void testInstanceOf() throws Exception {
        assertTrue(new PriorityQueue() instanceof Queue);
        assertTrue(new LinkedList() instanceof Queue);
        assertFalse(new Object() instanceof Queue);
        assertTrue(new Integer(1) instanceof Number);
        assertFalse(new Object() instanceof Number);
        assertTrue(this.getClass() instanceof AnnotatedElement);
        assertFalse((Object) "Hello" instanceof AnnotatedElement);
        assertTrue(new String[0] instanceof String[]);
        assertFalse(((Object) new Integer[0]) instanceof String[]);
    }

}