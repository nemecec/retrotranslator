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
package net.sf.retrotranslator.transformer;

import java.lang.reflect.*;
import junit.framework.TestCase;

/**
 * @author Taras Puchko
 */
public class MemoryModelVisitorTestCase extends TestCase {

    private static final int DURATION = 100;

    public static class FinalA implements Comparable<FinalB> {

        public final String messageA;

        public FinalA(String messageA) {
            this.messageA = messageA;
        }

        public int compareTo(FinalB o) {
            return messageA.compareTo(o.messageB);
        }
    }

    public static class FinalB implements Comparable<FinalA> {

        protected final String messageB;

        public FinalB(String messageB) {
            this.messageB = messageB;
        }

        public int compareTo(FinalA o) {
            return messageB.compareTo(o.messageA);
        }
    }

    public static class VolatileA implements Comparable<VolatileB> {

        volatile int countA;

        public int compareTo(VolatileB o) {
            return countA - o.countB;
        }
    }

    public static class VolatileB implements Comparable<VolatileA> {

        private volatile int countB;

        public int compareTo(VolatileA o) {
            return countB - o.countA;
        }
    }

    public static class StaticA implements Comparable<StaticB> {

        protected static volatile int zeroA;

        public int compareTo(StaticB o) {
            return zeroA - StaticB.zeroB;
        }
    }

    public static class StaticB implements Comparable<StaticA> {

        public static volatile int zeroB;

        public int compareTo(StaticA o) {
            return zeroB - StaticA.zeroA;
        }
    }

    public void testFinal() throws Exception {
        FinalA finalA = new FinalA("A");
        FinalB finalB = new FinalB("B");
        assertTrue(finalA.compareTo(finalB) < 0);
        assertTrue(finalB.compareTo(finalA) > 0);
        if (Boolean.getBoolean("net.sf.retrotranslator.tests.syncfinal")) {
            Object lockA = getLock(finalA, "messageA$lock", Modifier.PUBLIC);
            Object lockB = getLock(finalB, "messageB$lock", Modifier.PROTECTED);
            checkSync(lockA, lockB, finalA, finalB);
            checkSync(lockA, lockB, finalB, finalA);
            checkSync(lockB, lockA, finalA, finalB);
            checkSync(lockB, lockA, finalB, finalA);
        } else {
            assertEquals(1, finalA.getClass().getDeclaredFields().length);
            assertEquals(1, finalB.getClass().getDeclaredFields().length);
        }
    }

    public void testVolatile() throws Exception {
        VolatileA volatileA = new VolatileA();
        volatileA.countA = 1;
        VolatileB volatileB = new VolatileB();
        volatileB.countB = 2;
        assertTrue(volatileA.compareTo(volatileB) < 0);
        assertTrue(volatileB.compareTo(volatileA) > 0);
        if (Boolean.getBoolean("net.sf.retrotranslator.tests.syncvolatile")) {
            Object lockA = getLock(volatileA, "countA$lock", 0);
            Object lockB = getLock(volatileB, "countB$lock", Modifier.PRIVATE);
            checkSync(lockA, lockB, volatileA, volatileB);
            checkSync(lockA, lockB, volatileB, volatileA);
            checkSync(lockB, lockA, volatileA, volatileB);
            checkSync(lockB, lockA, volatileB, volatileA);
        } else {
            assertEquals(1, volatileA.getClass().getDeclaredFields().length);
            assertEquals(1, volatileB.getClass().getDeclaredFields().length);
        }
    }

    public void testStaticVolatile() throws Exception {
        StaticA staticA = new StaticA();
        StaticB staticB = new StaticB();
        assertEquals(0, staticA.compareTo(staticB));
        assertEquals(0, staticB.compareTo(staticA));
        if (Boolean.getBoolean("net.sf.retrotranslator.tests.syncvolatile")) {
            Object lockA = getLock(staticA, "zeroA$lock", Modifier.PROTECTED);
            Object lockB = getLock(staticB, "zeroB$lock", Modifier.PUBLIC);
            checkSync(lockA, lockB, staticA, staticB);
            checkSync(lockA, lockB, staticB, staticA);
            checkSync(lockB, lockA, staticA, staticB);
            checkSync(lockB, lockA, staticB, staticA);
        } else {
            assertEquals(1, staticA.getClass().getDeclaredFields().length);
            assertEquals(1, staticB.getClass().getDeclaredFields().length);
        }
    }

    public void testStaticFinal() throws Exception {
        assertEquals(1, this.getClass().getDeclaredFields().length);
    }

    private static Object getLock(Object o, String name, int modifier) throws Exception {
        Field field = o.getClass().getDeclaredField(name);
        field.setAccessible(true);
        assertTrue((field.getModifiers() & modifier) == modifier);
        return field.get(o);
    }

    private static void checkSync(Object firstLock, Object secondLock,
                                  final Comparable first, final Comparable second) throws Exception {
        Thread thread;
        final boolean[] finished = new boolean[1];
        synchronized (firstLock) {
            synchronized (secondLock) {
                thread = new Thread() {
                    public void run() {
                        first.compareTo(second);
                        finished[0] = true;
                    }
                };
                thread.start();
                Thread.sleep(DURATION);
            }
            Thread.sleep(DURATION);
            assertFalse(finished[0]);
        }
        thread.join();
        assertTrue(finished[0]);
    }

}