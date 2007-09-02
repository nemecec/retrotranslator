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

import junit.framework.*;
import java.util.*;

/**
 * @author Taras Puchko
 */
public class TimerTask_TestCase extends TestCase {

    class MyTimerTask extends TimerTask {
        public volatile long lastTime;
        public volatile int count;

        public synchronized void run() {
            lastTime = scheduledExecutionTime();
            count++;
            notifyAll();
        }
    }

    public void testCancel_1() throws Exception {
        Timer timer = new Timer();
        MyTimerTask task = new MyTimerTask();
        assertFalse(task.cancel());
        try {
            timer.schedule(task, 0);
            fail();
        } catch (IllegalStateException e) {
            //ok
        }
        Thread.sleep(50);
        assertEquals(0, task.count);
    }

    public void testCancel_2() throws Exception {
        Timer timer = new Timer();
        MyTimerTask task = new MyTimerTask();
        timer.schedule(task, 200);
        assertTrue(task.cancel());
        Thread.sleep(200);
        assertEquals(0, task.count);
    }

    public void testCancel_3() throws Exception {
        Timer timer = new Timer();
        MyTimerTask task = new MyTimerTask();
        timer.schedule(task, 0);
        Thread.sleep(100);
        assertEquals(1, task.count);
        assertFalse(task.cancel());
    }

    public void testCancel_4() throws Exception {
        Timer timer = new Timer();
        MyTimerTask task = new MyTimerTask();
        timer.schedule(task, 400, 400);
        Thread.sleep(1000);
        assertEquals(2, task.count);
        assertTrue(task.cancel());
        assertFalse(task.cancel());
        Thread.sleep(500);
        assertEquals(2, task.count);
    }

    public void testScheduledExecutionTime_1() throws Exception {
        Timer timer = new Timer();
        MyTimerTask task = new MyTimerTask();
        long delay = 200;
        long period = 300;
        long time = System.currentTimeMillis();
        timer.scheduleAtFixedRate(task, delay, period);
        synchronized (task) {
            task.wait();
        }
        long lastTime = task.lastTime;
        assertTime(time + delay, lastTime);
        Thread.sleep(50);
        assertEquals(lastTime, task.scheduledExecutionTime());
        synchronized (task) {
            task.wait();
        }
        lastTime = task.lastTime;
        assertTime(time + delay + period, lastTime);
        Thread.sleep(50);
        assertEquals(lastTime, task.scheduledExecutionTime());
        synchronized (task) {
            task.wait();
        }
        lastTime = task.lastTime;
        assertTime(time + delay + 2 * period, lastTime);
        timer.cancel();
    }

    public void testScheduledExecutionTime_2() throws Exception {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new ThreadDeath();
                }
            }
        }, 0);
        MyTimerTask task = new MyTimerTask();
        long delay = 200;
        long time = System.currentTimeMillis();
        timer.scheduleAtFixedRate(task, delay, 1000);
        synchronized (task) {
            task.wait();
        }
        assertTime(time + delay, task.lastTime);
        timer.cancel();
    }

    public void testScheduledExecutionTime_3() throws Exception {
        Timer timer = new Timer();
        MyTimerTask task = new MyTimerTask();
        long delay = 200;
        long time = System.currentTimeMillis();
        timer.schedule(task, delay);
        synchronized (task) {
            task.wait();
        }
        assertTime(time + delay, task.lastTime);
        timer.cancel();
    }

    private void assertTime(long expected, long found) {
        long delta = Math.abs(expected - found);
        if (delta > 75) {
            throw new AssertionFailedError("Expected: " + expected +
                    ", but found: " + found + ", delta: " + delta + ".");
        }
    }

}