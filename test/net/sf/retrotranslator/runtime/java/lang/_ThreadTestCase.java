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
package net.sf.retrotranslator.runtime.java.lang;

import junit.framework.TestCase;
import java.util.Map;

/**
 * @author Taras Puchko
 */
public class _ThreadTestCase extends TestCase {

    private Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler;

    protected void setUp() throws Exception {
        super.setUp();
        defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    protected void tearDown() throws Exception {
        Thread.setDefaultUncaughtExceptionHandler(defaultUncaughtExceptionHandler);
        super.tearDown();
    }

    private static class MyHandler implements Thread.UncaughtExceptionHandler {

        public Thread thread;
        public Throwable exception;

        public void uncaughtException(Thread t, Throwable e) {
            thread = t;
            exception = e;
        }
    }

    private static class MyThread extends Thread {

        public boolean done;

        public void run() {
            try {
                throw new Exception();
            } catch (Exception e) {
                //ok
            }
            done = true;
        }
    }

    public void testGetStackTrace() throws Exception {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String className = this.getClass().getName();
        StackTraceElement element = null;
        for (int i = 1; i < stackTrace.length; i++) {
            element = stackTrace[i];
            if (className.equals(element.getClassName())) break;
        }
        assertNotNull(element);
        assertEquals("testGetStackTrace", element.getMethodName());
        for (StackTraceElement stackTraceElement : stackTrace) {
            assertNotNull(stackTraceElement);
        }
    }

    public void testGetId() throws Exception {
        long currentId = Thread.currentThread().getId();
        assertTrue(currentId > 0);
        Thread thread = new Thread();
        long newId = thread.getId();
        assertTrue(newId > 0);
        assertTrue(currentId != newId);
        assertEquals(currentId, Thread.currentThread().getId());
        assertEquals(newId, thread.getId());
    }

    public void testGetId_Custom() throws Exception {
        class MyThread extends Thread {
            public int hashCode() {
                return 0;
            }

            public boolean equals(Object obj) {
                return true;
            }
        }
        Thread thread1 = new MyThread();
        Thread thread2 = new MyThread();
        assertEquals(thread1.getId(), thread1.getId());
        assertEquals(thread2.getId(), thread2.getId());
        assertTrue(thread1.getId() != Thread.currentThread().getId());
        assertTrue(thread2.getId() != Thread.currentThread().getId());
        assertTrue(thread1.getId() != thread2.getId());
    }

    public void testSetUncaughtExceptionHandler() throws Exception {
        final ThreadGroup group = Thread.currentThread().getThreadGroup();
        final IllegalStateException exception = new IllegalStateException("test");
        Runnable runnable = new Runnable() {
            public void run() {
                throw exception;
            }
        };
        Thread[] threads = {
                new Thread() {
                    public void run() {
                        throw exception;
                    }
                },
                new Thread(runnable),
                new Thread(group, runnable),
                new Thread("name") {
                    public void run() {
                        throw exception;
                    }
                },
                new Thread(group, "name") {
                    public void run() {
                        try {
                            throw new RuntimeException();
                        } catch (RuntimeException e) {
                            throw exception;
                        }
                    }
                },
                new Thread(runnable, "name"),
                new Thread(group, runnable, "name"),
                new Thread(group, runnable, "name", 0),
        };
        for (Thread thread : threads) {
            MyHandler handler = new MyHandler();
            assertSame(thread.getThreadGroup(), thread.getUncaughtExceptionHandler());
            thread.setUncaughtExceptionHandler(handler);
            assertSame(handler, thread.getUncaughtExceptionHandler());
            thread.start();
            thread.join();
            assertSame(thread, handler.thread);
            assertSame(exception, handler.exception);
        }
    }

    public void testSetUncaughtExceptionHandler_NoException() throws Exception {
        MyThread myThread = new MyThread();
        MyHandler myHandler = new MyHandler();
        ((Thread) myThread).setUncaughtExceptionHandler(myHandler);
        assertFalse(myThread.done);
        myThread.start();
        myThread.join();
        assertTrue(myThread.done);
        assertNull(myHandler.thread);
        assertNull(myHandler.exception);
        Thread thread = new Thread() {
            public void run() {
            }
        };
        thread.start();
        thread.join();
    }

    public void testSetDefaultUncaughtExceptionHandler() throws Exception {
        final ThreadGroup group = Thread.currentThread().getThreadGroup();
        final IllegalStateException exception = new IllegalStateException("test");
        Runnable runnable = new Runnable() {
            public void run() {
                throw exception;
            }
        };
        Thread[] threads = {
                new Thread() {
                    public void run() {
                        throw exception;
                    }
                },
                new Thread(runnable),
                new Thread(group, runnable),
                new Thread("name") {
                    public void run() {
                        throw exception;
                    }
                },
                new Thread(group, "name") {
                    public void run() {
                        throw exception;
                    }
                },
                new Thread(runnable, "name"),
                new Thread(group, runnable, "name"),
                new Thread(group, runnable, "name", 0),
        };
        for (Thread thread : threads) {
            MyHandler handler = new MyHandler();
            Thread.setDefaultUncaughtExceptionHandler(handler);
            assertSame(handler, Thread.getDefaultUncaughtExceptionHandler());
            thread.start();
            thread.join();
            assertSame(thread, handler.thread);
            assertSame(exception, handler.exception);
        }
    }

    public void testSetDefaultUncaughtExceptionHandler_NoException() throws Exception {
        MyThread myThread = new MyThread();
        MyHandler myHandler = new MyHandler();
        Thread.setDefaultUncaughtExceptionHandler(myHandler);
        assertFalse(myThread.done);
        myThread.start();
        myThread.join();
        assertTrue(myThread.done);
        assertNull(myHandler.thread);
        assertNull(myHandler.exception);
    }

    public void testGetState() throws Exception {
        assertSame(Thread.State.RUNNABLE, Thread.currentThread().getState());
        Thread thread = new Thread() {
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        };
        assertSame(Thread.State.NEW, thread.getState());
        thread.start();
        assertSame(Thread.State.RUNNABLE, thread.getState());
        thread.join();
        assertSame(Thread.State.TERMINATED, thread.getState());
    }

    public void testGetAllStackTraces() throws Exception {
        Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();
        assertTrue(map.size() > 1);
        StackTraceElement[] elements = map.get(Thread.currentThread());
        assertNotNull(elements);
        assertTrue(elements.length > 0);
    }

}