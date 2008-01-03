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
package net.sf.retrotranslator.runtime.java.lang;

import junit.framework.*;

/**
 * @author Taras Puchko
 */
public class _Thread_UncaughtExceptionHandlerTestCase extends TestCase {

    private static class MyHandler implements Thread.UncaughtExceptionHandler {

        public Thread thread;
        public Throwable exception;

        public void uncaughtException(Thread t, Throwable e) {
            thread = t;
            exception = e;
        }
    }

    private static class MyGroup extends ThreadGroup {

        public Thread thread;
        public Throwable exception;

        public MyGroup() {
            super("test");
        }

        public void uncaughtException(Thread t, Throwable e) {
            thread = t;
            exception = e;
        }
    }

    public void testExecuteInstanceOfInstruction() throws Exception {
        assertTrue(new MyHandler() instanceof Thread.UncaughtExceptionHandler);
        assertTrue(new MyGroup() instanceof Thread.UncaughtExceptionHandler);
        assertTrue(new ThreadGroup("test") instanceof Thread.UncaughtExceptionHandler);
        assertFalse(new Object() instanceof Thread.UncaughtExceptionHandler);
    }

    public void testExecuteCheckCastInstruction() throws Exception {
        Object handler = new MyHandler();
        ((Thread.UncaughtExceptionHandler) handler).uncaughtException(null, null);
        Object group = new MyGroup();
        ((Thread.UncaughtExceptionHandler) group).uncaughtException(null, null);
        Object string = "test";
        try {
            ((Thread.UncaughtExceptionHandler) string).uncaughtException(null, null);
            fail();
        } catch (ClassCastException e) {
            //ok
        }
    }

    public void testUncaughtException_Handler() throws Exception {
        MyHandler handler = new MyHandler();
        Thread thread = new Thread();
        Exception exception = new Exception();
        dispatchException(handler, thread, exception);
        assertSame(thread, handler.thread);
        assertSame(exception, handler.exception);
    }

    public void testUncaughtException_Group() throws Exception {
        MyGroup group = new MyGroup();
        Thread thread = new Thread();
        Exception exception = new Exception();
        dispatchException(group, thread, exception);
        assertSame(thread, group.thread);
        assertSame(exception, group.exception);
    }

    private void dispatchException(Thread.UncaughtExceptionHandler handler, Thread thread, Exception exception) {
        handler.uncaughtException(thread, exception);
    }

}