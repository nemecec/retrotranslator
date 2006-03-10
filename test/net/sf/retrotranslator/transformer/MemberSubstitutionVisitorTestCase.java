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
package net.sf.retrotranslator.transformer;

import junit.framework.*;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.DelayQueue;

/**
 * @author Taras Puchko
 */
public class MemberSubstitutionVisitorTestCase extends TestCase {

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

    public void testNanotime() throws Exception {
        long n = System.nanoTime();
        long m = System.currentTimeMillis();
        Thread.sleep(100);
        m = System.currentTimeMillis() - m;
        n = System.nanoTime() - n;
        assertTrue(Math.abs(n / 1000000 - m) <= 100);
    }
}