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
package net.sf.retrotranslator.runtime.impl;

import java.io.IOException;
import java.lang.reflect.*;
import junit.framework.TestCase;

/**
 * @author Taras Puchko
 */
public class RuntimeToolsTestCase extends TestCase {

    public void testInvokeMethod() throws Exception {
        StringBuffer buffer = new StringBuffer("abc");
        Class[] parameterTypes = new Class[]{char[].class, int.class, int.class};
        Object[] args = new Object[]{"12345".toCharArray(), 1, 3};
        Object result = RuntimeTools.invokeMethod(buffer, "append", parameterTypes, args);
        assertSame(buffer, result);
        assertEquals("abc234", buffer.toString());
    }

    public void testInvokeMethod_NoSuchMethodException() throws Exception {
        StringBuffer buffer = new StringBuffer("abc");
        Class[] parameterTypes = new Class[]{char[].class, int.class, Integer.class};
        Object[] args = new Object[]{"12345".toCharArray(), 1, 3};
        try {
            RuntimeTools.invokeMethod(buffer, "append", parameterTypes, args);
            fail();
        } catch (NoSuchMethodException e) {
            //ok
        }
    }

    public void testInvokeMethod_InvocationTargetException() throws Throwable {
        StringBuffer buffer = new StringBuffer("abc");
        Class[] parameterTypes = new Class[]{char[].class, int.class, int.class};
        Object[] args = new Object[]{"12345".toCharArray(), -1, 3};
        try {
            RuntimeTools.invokeMethod(buffer, "append", parameterTypes, args);
            fail();
        } catch (InvocationTargetException e) {
            try {
                throw e.getTargetException();
            } catch (IndexOutOfBoundsException ex) {
                //ok
            }
        }
    }

    public void testUnwrap_RuntimeException() throws Exception {
        IllegalStateException exception = new IllegalStateException();
        try {
            RuntimeTools.unwrap(new InvocationTargetException(exception));
            fail();
        } catch (IllegalStateException e) {
            assertSame(exception, e);
        }
    }

    public void testUnwrap_Error() throws Exception {
        InternalError error = new InternalError();
        try {
            RuntimeTools.unwrap(new InvocationTargetException(error));
            fail();
        } catch (InternalError e) {
            assertSame(error, e);
        }
    }

    public void testUnwrap_Exception() throws Exception {
        IOException error = new IOException();
        UndeclaredThrowableException exception = RuntimeTools.unwrap(new InvocationTargetException(error));
        assertSame(error, exception.getCause());
    }

    public void testUnwrap_Throwable() throws Exception {
        Throwable throwable = new Throwable();
        UndeclaredThrowableException exception = RuntimeTools.unwrap(new InvocationTargetException(throwable));
        assertSame(throwable, exception.getCause());
    }

}