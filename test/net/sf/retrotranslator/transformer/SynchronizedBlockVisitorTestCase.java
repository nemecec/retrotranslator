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

import junit.framework.TestCase;

/**
 * @author Taras Puchko
 */
public class SynchronizedBlockVisitorTestCase extends TestCase {

    protected final Object monitor = this;
    private int counter;

    protected void increment() {
        counter++;
    }

    protected synchronized void synchIncrement() {
        increment();
    }

    public void testConstant() throws InterruptedException {
        increment();
        synchronized(monitor) {
            increment();
        }
        increment();
        Thread thread = new Thread() {
            public void run() {
                increment();
                synchIncrement();
                increment();
                synchronized (monitor) {
                    increment();
                }
                increment();
            }
        };
        thread.start();
        thread.join();
        synchIncrement();
        assertEquals(9, counter);
    }

    public void testSeveralBlocks() throws InterruptedException {
        final Object firstLock = new Object();
        final Object secondLock = new Object();
        Thread thread = new Thread() {
            public void run() {
                synchronized (firstLock) {
                    synchronized(secondLock) {
                        increment();
                    }
                    synchronized(firstLock) {
                        increment();
                    }
                }
                increment();
                synchronized(firstLock) {
                    increment();
                }
            }
        };
        thread.start();
        thread.join();
        assertEquals(4, counter);
    }

}