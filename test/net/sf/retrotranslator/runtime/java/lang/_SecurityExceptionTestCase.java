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

import junit.framework.TestCase;

/**
 * @author Taras Puchko
 */
public class _SecurityExceptionTestCase extends TestCase {

    public void testSecurityException_oneParam() throws Exception {
        SecurityException exception = new SecurityException(new ClassNotFoundException("123"));
        assertEquals("java.lang.ClassNotFoundException: 123", exception.getMessage());
        Throwable cause = exception.getCause();
        assertTrue(cause instanceof ClassNotFoundException);
        assertEquals("123", cause.getMessage());
        class Ex extends SecurityException {
            public Ex(Throwable cause) {
                super(cause);
            }
        }
        Ex ex = new Ex(new SecurityException());
        assertEquals("java.lang.SecurityException", ex.getMessage());
        assertTrue(ex.getCause() instanceof SecurityException);

        SecurityException nullCausedEx = new SecurityException((Throwable) null);
        assertNull(nullCausedEx.getMessage());
        assertNull(nullCausedEx.getCause());
        try {
            nullCausedEx.initCause(new Throwable());
            fail();
        } catch (IllegalStateException e) {
            //ok
        }
    }

    public void testSecurityException_twoParams() throws Exception {
        SecurityException exception = new SecurityException("abc", new ClassNotFoundException("123"));
        assertEquals("abc", exception.getMessage());
        Throwable cause = exception.getCause();
        assertTrue(cause instanceof ClassNotFoundException);
        assertEquals("123", cause.getMessage());
        class Ex extends SecurityException {
            public Ex(String message, Throwable cause) {
                super(message, cause);
            }
        }
        Ex ex = new Ex("qwerty", new SecurityException());
        assertEquals("qwerty", ex.getMessage());
        assertTrue(ex.getCause() instanceof SecurityException);
    }

}