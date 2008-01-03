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
import java.util.concurrent.*;

/**
 * @author Taras Puchko
 */
public class InheritableThreadLocal_TestCase extends TestCase {

    private ExecutorService service;

    private static class TestInheritableThreadLocal extends InheritableThreadLocal<String> {
        protected String initialValue() {
            return "a";
        }

        protected String childValue(String parentValue) {
            return "(" + parentValue + ")";
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
        service = Executors.newSingleThreadExecutor();
    }

    protected void tearDown() throws Exception {
        service.shutdown();
        super.tearDown();
    }

    private void execute(Runnable runnable) throws Exception {
        try {
            service.submit(runnable).get();
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception) throw (Exception) cause;
            if (cause instanceof Error) throw (Error) cause;
            throw e;
        }
    }

    public void test_Inheritance() throws Exception {
        final InheritableThreadLocal<String> threadLocal = new TestInheritableThreadLocal();
        assertEquals("a", threadLocal.get());
        threadLocal.set("b");
        execute(new Runnable() {
            public void run() {
                assertEquals("(b)", threadLocal.get());
                threadLocal.set("x");
            }
        });
        assertEquals("b", threadLocal.get());
        threadLocal.set(null);
        assertNull(threadLocal.get());
        threadLocal.remove();
        execute(new Runnable() {
            public void run() {
                assertEquals("x", threadLocal.get());
            }
        });
        assertEquals("a", threadLocal.get());
    }

    public void test_NoInheritance() throws Exception {
        final InheritableThreadLocal<String> threadLocal = new TestInheritableThreadLocal();
        execute(new Runnable() {
            public void run() {
                assertEquals("a", threadLocal.get());
                threadLocal.set("x");
            }
        });
        assertEquals("a", threadLocal.get());
        threadLocal.set("b");
        execute(new Runnable() {
            public void run() {
                assertEquals("x", threadLocal.get());
            }
        });
        assertEquals("b", threadLocal.get());
    }

    public void test_Removed() throws Exception {
        final InheritableThreadLocal<String> threadLocal = new TestInheritableThreadLocal();
        assertEquals("a", threadLocal.get());
        threadLocal.remove();
        execute(new Runnable() {
            public void run() {
                assertEquals("a", threadLocal.get());
                threadLocal.set("x");
            }
        });
        assertEquals("a", threadLocal.get());
        threadLocal.set("b");
        execute(new Runnable() {
            public void run() {
                assertEquals("x", threadLocal.get());
            }
        });
        assertEquals("b", threadLocal.get());
    }

}