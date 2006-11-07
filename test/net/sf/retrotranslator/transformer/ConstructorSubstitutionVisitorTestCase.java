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

import junit.framework.TestCase;

import java.lang.ref.*;

/**
 * @author Taras Puchko
 */
public class ConstructorSubstitutionVisitorTestCase extends TestCase {

    public void testIllegalArgumentExceptionOneParam() throws Exception {
        IllegalArgumentException exception = new IllegalArgumentException(new ClassNotFoundException("123"));
        assertEquals("java.lang.ClassNotFoundException: 123", exception.getMessage());
        Throwable cause = exception.getCause();
        assertTrue(cause instanceof ClassNotFoundException);
        assertEquals("123", cause.getMessage());
        class Ex extends IllegalArgumentException {
            public Ex(Throwable cause) {
                super(cause);
            }
        }
        Ex ex = new Ex(new IllegalArgumentException());
        assertEquals("java.lang.IllegalArgumentException", ex.getMessage());
        assertTrue(ex.getCause() instanceof IllegalArgumentException);

        IllegalArgumentException nullCausedEx = new IllegalArgumentException((Throwable) null);
        assertNull(nullCausedEx.getMessage());
        assertNull(nullCausedEx.getCause());
        try {
            nullCausedEx.initCause(new Throwable());
            fail();
        } catch (IllegalStateException e) {
            //ok
        }
    }

    public void testIllegalArgumentExceptionTwoParam() throws Exception {
        IllegalArgumentException exception = new IllegalArgumentException("abc", new ClassNotFoundException("123"));
        assertEquals("abc", exception.getMessage());
        Throwable cause = exception.getCause();
        assertTrue(cause instanceof ClassNotFoundException);
        assertEquals("123", cause.getMessage());
        class Ex extends IllegalArgumentException {
            public Ex(String message, Throwable cause) {
                super(message, cause);
            }
        }
        Ex ex = new Ex("qwerty", new IllegalArgumentException());
        assertEquals("qwerty", ex.getMessage());
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
    }

    public void testIllegalStateExceptionOneParam() throws Exception {
        IllegalStateException exception = new IllegalStateException(new ClassNotFoundException("123"));
        assertEquals("java.lang.ClassNotFoundException: 123", exception.getMessage());
        Throwable cause = exception.getCause();
        assertTrue(cause instanceof ClassNotFoundException);
        assertEquals("123", cause.getMessage());
        class Ex extends IllegalStateException {
            public Ex(Throwable cause) {
                super(cause);
            }
        }
        Ex ex = new Ex(new IllegalArgumentException());
        assertEquals("java.lang.IllegalArgumentException", ex.getMessage());
        assertTrue(ex.getCause() instanceof IllegalArgumentException);

        IllegalStateException nullCausedEx = new IllegalStateException((Throwable) null);
        assertNull(nullCausedEx.getMessage());
        assertNull(nullCausedEx.getCause());
        try {
            nullCausedEx.initCause(new Throwable());
            fail();
        } catch (IllegalStateException e) {
            //ok
        }
    }

    public void testIllegalStateExceptionTwoParam() throws Exception {
        IllegalStateException exception = new IllegalStateException("abc", new ClassNotFoundException("123"));
        assertEquals("abc", exception.getMessage());
        Throwable cause = exception.getCause();
        assertTrue(cause instanceof ClassNotFoundException);
        assertEquals("123", cause.getMessage());
        class Ex extends IllegalStateException {
            public Ex(String message, Throwable cause) {
                super(message, cause);
            }
        }
        Ex ex = new Ex("qwerty", new IllegalArgumentException());
        assertEquals("qwerty", ex.getMessage());
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
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
        new MyReference<String>("a", null);
        new MyReference<String>("b", new ReferenceQueue<String>());
        new WeakReference<String>("c", null);
        new WeakReference<String>("d", new ReferenceQueue<String>());
    }

}