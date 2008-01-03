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

import java.lang.ref.*;
import java.util.concurrent.locks.*;
import junit.framework.TestCase;

/**
 * @author Taras Puchko
 */
public class SpecificReplacementVisitorTestCase extends TestCase {

    public void testNanotime() throws Exception {
        long n = System.nanoTime();
        long m = System.currentTimeMillis();
        Thread.sleep(100);
        m = System.currentTimeMillis() - m;
        n = System.nanoTime() - n;
        assertTrue(Math.abs(n / 1000000 - m) <= 100);
    }

    public void testSoftReference() throws Exception {
        class MyReference<T> extends SoftReference<T> {
            public MyReference(T referent, ReferenceQueue<? super T> q) {
                super(referent, q);
            }
        }
        new MyReference<String>("a", null);
        new MyReference<String>("b", new ReferenceQueue<String>());
        new SoftReference<String>("c", null);
        new SoftReference<String>("d", new ReferenceQueue<String>());
    }

    public void testWeakReference() throws Exception {
        class MyReference<T> extends WeakReference<T> {
            public MyReference(T referent, ReferenceQueue<? super T> q) {
                super(referent, q);
            }
        }
        new WeakReference<String>("a", null);
        new MyReference<String>("b", null);
        ReferenceQueue<String> queue1 = new ReferenceQueue<String>();
        ReferenceQueue<String> queue2 = new ReferenceQueue<String>();
        Reference<String> reference1 = new MyReference<String>(new String("c"), queue1);
        Reference<String> reference2 = new WeakReference<String>(new String("d"), queue2);
        gc();
        assertSame(reference1, queue1.poll());
        assertSame(reference2, queue2.poll());
    }

    private static void gc() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            System.gc();
            Thread.sleep(100);
        }
    }

    public void testReentrantReadWriteLock() throws Exception {
        ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();
        ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();
        assertTrue(readLock.tryLock());
        readLock.unlock();
        assertTrue(writeLock.tryLock());
        writeLock.unlock();
    }

    public void testCondition_awaitNanos() throws Exception {
        ReentrantLock lock = new ReentrantLock();
        lock.lock();
        lock.newCondition().awaitNanos(1000);
        lock.unlock();
        try {
            lock.newCondition().awaitNanos(1000);
            fail();
        } catch (IllegalMonitorStateException e) {
            //ok
        }
    }


}