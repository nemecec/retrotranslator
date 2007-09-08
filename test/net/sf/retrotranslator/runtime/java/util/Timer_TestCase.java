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
import java.lang.ref.WeakReference;
import junit.framework.TestCase;

/**
 * @author Taras Puchko
 */
public class Timer_TestCase extends TestCase {

    class MyTimerTask extends TimerTask {
        public volatile int count;
        public volatile boolean error;

        public void run() {
            count++;
            if (error) {
                throw new RuntimeException();
            }
        }
    }

    class HalfSecondTimerTask extends TimerTask {
        public void run() {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    public void testTimer() throws Exception {
        new Timer().cancel();
        new Timer(true).cancel();
        Timer firstTimer = new Timer("FirstTimer");
        Thread firstThread = getThread("FirstTimer");
        assertTrue(firstThread.isAlive());
        assertFalse(firstThread.isDaemon());
        firstTimer.cancel();
        Timer secondTimer = new Timer("SecondTimer", true);
        Thread secondThread = getThread("SecondTimer");
        assertTrue(secondThread.isAlive());
        assertTrue(secondThread.isDaemon());
        secondTimer.cancel();
    }

    public void testCancel_1() throws Exception {
        Timer timer = new Timer();
        MyTimerTask task = new MyTimerTask();
        timer.schedule(task, 100, 100);
        timer.cancel();
        Thread.sleep(300);
        assertEquals(0, task.count);
        assertTrue(task.cancel());
        timer.cancel();
    }

    public void testCancel_2() throws Exception {
        Timer timer = new Timer();
        MyTimerTask task = new MyTimerTask();
        timer.schedule(task, 200, 200);
        Thread.sleep(500);
        assertEquals(2, task.count);
        timer.cancel();
        Thread.sleep(500);
        assertEquals(2, task.count);
    }

    public void testPurge() throws Exception {
        Timer timer = new Timer();
        MyTimerTask firstTask = new MyTimerTask();
        MyTimerTask secondTask = new MyTimerTask();
        MyTimerTask thirdTask = new MyTimerTask();
        timer.schedule(firstTask, 0, 10);
        timer.schedule(secondTask, 200, 200);
        timer.schedule(thirdTask, 0, 200);
        firstTask.cancel();
        WeakReference<TimerTask> firstReference = new WeakReference<TimerTask>(firstTask);
        firstTask = null;
        Thread.sleep(10);
        System.gc();
        Thread.sleep(10);
        assertNull(firstReference.get());
        secondTask.cancel();
        thirdTask.cancel();
        assertEquals(2, timer.purge());
    }

    public void testSchedule_Once_Delay_1() throws Exception {
        Timer timer = new Timer();
        MyTimerTask task = new MyTimerTask();
        timer.schedule(task, 200);
        Thread.sleep(100);
        assertEquals(0, task.count);
        Thread.sleep(200);
        assertEquals(1, task.count);
        assertFalse(task.cancel());
    }

    public void testSchedule_Once_Delay_2() throws Exception {
        Timer timer = new Timer();
        try {
            timer.schedule(new MyTimerTask(), -100);
            fail();
        } catch (IllegalArgumentException e) {
            //ok
        }
        try {
            timer.schedule(new MyTimerTask(), Long.MAX_VALUE);
            fail();
        } catch (IllegalArgumentException e) {
            //ok
        }
        MyTimerTask task = new MyTimerTask();
        timer.schedule(task, 0);
        try {
            timer.schedule(task, 0);
            fail();
        } catch (IllegalStateException e) {
            //ok
        }
        task = new MyTimerTask();
        task.cancel();
        try {
            timer.schedule(task, 0);
            fail();
        } catch (IllegalStateException e) {
            //ok
        }
        timer.cancel();
        try {
            timer.schedule(new MyTimerTask(), 0);
            fail();
        } catch (IllegalStateException e) {
            //ok
        }
    }

    public void testSchedule_Once_Date_1() throws Exception {
        Timer timer = new Timer();
        MyTimerTask task = new MyTimerTask();
        timer.schedule(task, new Date(System.currentTimeMillis() + 200));
        Thread.sleep(100);
        assertEquals(0, task.count);
        Thread.sleep(200);
        assertEquals(1, task.count);
        assertFalse(task.cancel());
    }

    public void testSchedule_Once_Date_2() throws Exception {
        Timer timer = new Timer();
        MyTimerTask task = new MyTimerTask();
        timer.schedule(task, new Date(System.currentTimeMillis() - 200));
        Thread.sleep(100);
        assertEquals(1, task.count);
        try {
            timer.schedule(new MyTimerTask(), new Date(-200));
            fail();
        } catch (IllegalArgumentException e) {
            //ok
        }
        try {
            timer.schedule(task, new Date());
            fail();
        } catch (IllegalStateException e) {
            //ok
        }
        task = new MyTimerTask();
        task.cancel();
        try {
            timer.schedule(task, new Date());
            fail();
        } catch (IllegalStateException e) {
            //ok
        }
        timer.cancel();
        try {
            timer.schedule(new MyTimerTask(), new Date());
            fail();
        } catch (IllegalStateException e) {
            //ok
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                throw new ThreadDeath();
            }
        }, 0);
        Thread.sleep(50);
        try {
            timer.schedule(new MyTimerTask(), new Date());
            fail();
        } catch (IllegalStateException e) {
            //ok
        }
    }

    public void testSchedule_WithFixedDelay_Delay_1() throws Exception {
        Timer timer = new Timer();
        MyTimerTask task = new MyTimerTask();
        timer.schedule(new HalfSecondTimerTask(), 0);
        timer.schedule(task, 100, 100);
        Thread.sleep(450);
        assertEquals(0, task.count);
        Thread.sleep(100);
        assertEquals(1, task.count);
        Thread.sleep(100);
        assertEquals(2, task.count);
        assertTrue(task.cancel());
    }

    public void testSchedule_WithFixedDelay_Delay_2() throws Exception {
        Timer timer = new Timer();
        MyTimerTask errorTask = new MyTimerTask();
        errorTask.error = true;
        timer.schedule(errorTask, 100, 100);
        Thread.sleep(50);
        assertEquals(0, errorTask.count);
        Thread.sleep(100);
        assertEquals(1, errorTask.count);
        Thread.sleep(200);
        assertEquals(1, errorTask.count);
    }

    public void testSchedule_WithFixedDelay_Delay_3() throws Exception {
        Timer timer = new Timer();
        try {
            timer.schedule(new MyTimerTask(), 200, 0);
            fail();
        } catch (IllegalArgumentException e) {
            //ok
        }
        try {
            timer.schedule(new MyTimerTask(), 200, -100);
            fail();
        } catch (IllegalArgumentException e) {
            //ok
        }
        try {
            timer.schedule(new MyTimerTask(), -200, 100);
            fail();
        } catch (IllegalArgumentException e) {
            //ok
        }
        try {
            timer.schedule(new MyTimerTask(), Long.MAX_VALUE, 100);
            fail();
        } catch (IllegalArgumentException e) {
            //ok
        }
        TimerTask task = new MyTimerTask();
        timer.schedule(task, 10, 10);
        try {
            timer.schedule(task, 10, 10);
            fail();
        } catch (IllegalStateException e) {
            //ok
        }
        task = new MyTimerTask();
        task.cancel();
        try {
            timer.schedule(task, 10, 10);
            fail();
        } catch (IllegalStateException e) {
            //ok
        }
        timer.cancel();
        try {
            timer.schedule(new MyTimerTask(), 10, 10);
            fail();
        } catch (IllegalStateException e) {
            //ok
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                throw new ThreadDeath();
            }
        }, 0);
        Thread.sleep(50);
        try {
            timer.schedule(new MyTimerTask(), 10, 10);
            fail();
        } catch (IllegalStateException e) {
            //ok
        }
    }

    public void testSchedule_WithFixedDelay_Date_1() throws Exception {
        Timer timer = new Timer();
        MyTimerTask task = new MyTimerTask();
        timer.schedule(new HalfSecondTimerTask(), 0);
        timer.schedule(task, new Date(System.currentTimeMillis() + 100), 100);
        Thread.sleep(450);
        assertEquals(0, task.count);
        Thread.sleep(100);
        assertEquals(1, task.count);
        Thread.sleep(100);
        assertEquals(2, task.count);
        assertTrue(task.cancel());
    }

    public void testSchedule_WithFixedDelay_Date_2() throws Exception {
        Timer timer = new Timer();
        MyTimerTask task = new MyTimerTask();
        timer.schedule(task, new Date(System.currentTimeMillis() - 400), 100);
        Thread.sleep(50);
        assertEquals(1, task.count);
        Thread.sleep(100);
        assertEquals(2, task.count);
        Thread.sleep(100);
        assertEquals(3, task.count);
        assertTrue(task.cancel());
    }

    public void testSchedule_WithFixedDelay_Date_3() throws Exception {
        Timer timer = new Timer();
        try {
            timer.schedule(new MyTimerTask(), new Date(), 0);
            fail();
        } catch (IllegalArgumentException e) {
            //ok
        }
        try {
            timer.schedule(new MyTimerTask(), new Date(), -100);
            fail();
        } catch (IllegalArgumentException e) {
            //ok
        }
        try {
            timer.schedule(new MyTimerTask(), new Date(-200), 100);
            fail();
        } catch (IllegalArgumentException e) {
            //ok
        }
        timer.schedule(new MyTimerTask(), new Date(System.currentTimeMillis() - 200), 100);
        timer.schedule(new MyTimerTask(), new Date(Long.MAX_VALUE), 100);
        TimerTask task = new MyTimerTask();
        timer.schedule(task, new Date(), 10);
        try {
            timer.schedule(task, new Date(), 10);
            fail();
        } catch (IllegalStateException e) {
            //ok
        }
        task = new MyTimerTask();
        task.cancel();
        try {
            timer.schedule(task, new Date(), 10);
            fail();
        } catch (IllegalStateException e) {
            //ok
        }
        timer.cancel();
        try {
            timer.schedule(new MyTimerTask(), new Date(), 10);
            fail();
        } catch (IllegalStateException e) {
            //ok
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                throw new ThreadDeath();
            }
        }, new Date(), 10);
        Thread.sleep(50);
        try {
            timer.schedule(new MyTimerTask(), new Date(), 10);
            fail();
        } catch (IllegalStateException e) {
            //ok
        }
    }

    public void testScheduleAtFixedRate_Delay_1() throws Exception {
        Timer timer = new Timer();
        MyTimerTask task = new MyTimerTask();
        timer.schedule(new HalfSecondTimerTask(), 0);
        timer.scheduleAtFixedRate(task, 200, 200);
        Thread.sleep(380);
        assertEquals(0, task.count);
        Thread.sleep(300);
        assertEquals(3, task.count);
        Thread.sleep(200);
        assertEquals(4, task.count);
        assertTrue(task.cancel());
    }

    public void testScheduleAtFixedRate_Delay_2() throws Exception {
        Timer timer = new Timer();
        try {
            timer.scheduleAtFixedRate(new MyTimerTask(), 200, 0);
            fail();
        } catch (IllegalArgumentException e) {
            //ok
        }
        try {
            timer.scheduleAtFixedRate(new MyTimerTask(), 200, -100);
            fail();
        } catch (IllegalArgumentException e) {
            //ok
        }
        try {
            timer.scheduleAtFixedRate(new MyTimerTask(), -200, 100);
            fail();
        } catch (IllegalArgumentException e) {
            //ok
        }
        try {
            timer.scheduleAtFixedRate(new MyTimerTask(), Long.MAX_VALUE, 100);
            fail();
        } catch (IllegalArgumentException e) {
            //ok
        }
        TimerTask task = new MyTimerTask();
        timer.scheduleAtFixedRate(task, 10, 10);
        try {
            timer.scheduleAtFixedRate(task, 10, 10);
            fail();
        } catch (IllegalStateException e) {
            //ok
        }
        task = new MyTimerTask();
        task.cancel();
        try {
            timer.scheduleAtFixedRate(task, 10, 10);
            fail();
        } catch (IllegalStateException e) {
            //ok
        }
        timer.cancel();
        try {
            timer.scheduleAtFixedRate(new MyTimerTask(), 10, 10);
            fail();
        } catch (IllegalStateException e) {
            //ok
        }
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                throw new ThreadDeath();
            }
        }, 0, 10);
        Thread.sleep(50);
        try {
            timer.scheduleAtFixedRate(new MyTimerTask(), 10, 10);
            fail();
        } catch (IllegalStateException e) {
            //ok
        }
    }

    public void testScheduleAtFixedRate_Date_1() throws Exception {
        Timer timer = new Timer();
        MyTimerTask task = new MyTimerTask();
        timer.schedule(new HalfSecondTimerTask(), 0);
        timer.scheduleAtFixedRate(task, new Date(System.currentTimeMillis() + 200), 200);
        Thread.sleep(400);
        assertEquals(0, task.count);
        Thread.sleep(300);
        assertEquals(3, task.count);
        Thread.sleep(200);
        assertEquals(4, task.count);
        assertTrue(task.cancel());
    }

    public void testScheduleAtFixedRate_Date_3() throws Exception {
        Timer timer = new Timer();
        try {
            timer.scheduleAtFixedRate(new MyTimerTask(), new Date(), 0);
            fail();
        } catch (IllegalArgumentException e) {
            //ok
        }
        try {
            timer.scheduleAtFixedRate(new MyTimerTask(), new Date(), -100);
            fail();
        } catch (IllegalArgumentException e) {
            //ok
        }
        try {
            timer.scheduleAtFixedRate(new MyTimerTask(), new Date(-200), 100);
            fail();
        } catch (IllegalArgumentException e) {
            //ok
        }
        timer.scheduleAtFixedRate(new MyTimerTask(), new Date(System.currentTimeMillis() - 200), 100);
        timer.scheduleAtFixedRate(new MyTimerTask(), new Date(Long.MAX_VALUE), 100);
        TimerTask task = new MyTimerTask();
        timer.scheduleAtFixedRate(task, new Date(), 10);
        try {
            timer.scheduleAtFixedRate(task, new Date(), 10);
            fail();
        } catch (IllegalStateException e) {
            //ok
        }
        task = new MyTimerTask();
        task.cancel();
        try {
            timer.scheduleAtFixedRate(task, new Date(), 10);
            fail();
        } catch (IllegalStateException e) {
            //ok
        }
        timer.cancel();
        try {
            timer.scheduleAtFixedRate(new MyTimerTask(), new Date(), 10);
            fail();
        } catch (IllegalStateException e) {
            //ok
        }
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                throw new ThreadDeath();
            }
        }, new Date(), 10);
        Thread.sleep(50);
        try {
            timer.scheduleAtFixedRate(new MyTimerTask(), new Date(), 10);
            fail();
        } catch (IllegalStateException e) {
            //ok
        }
    }

    public void testFinalize() throws Exception {
        MyTimerTask task = new MyTimerTask();
        new Timer("MyTimer").schedule(task, 400, 400);
        Thread thread = getThread("MyTimer");
        assertTrue(thread.isAlive());
        Thread.sleep(1000);
        assertTrue(thread.isAlive());
        assertEquals(2, task.count);
        task.cancel();
        for (int i = 0; thread.isAlive() && i < 10; i++) {
            System.gc();
            Thread.sleep(200);
        }
        assertFalse(thread.isAlive());
    }

    private Thread getThread(String name) {
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            if (thread.getName().equals(name)) {
                return thread;
            }
        }
        throw new IllegalArgumentException("Thread not found: " + name);
    }

}