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
package net.sf.retrotranslator.runtime13.v14.java.lang;

import java.awt.print.PrinterIOException;
import java.io.*;
import java.lang.reflect.*;
import java.rmi.RemoteException;
import java.rmi.activation.ActivationException;
import java.rmi.server.ServerCloneException;
import java.security.PrivilegedActionException;
import java.util.*;
import javax.naming.NamingException;
import junit.framework.TestCase;

/**
 * @author Taras Puchko
 */
public class _ThrowableTestCase extends TestCase {

    public void testCreateInstanceBuilder_OneArg() throws Exception {
        IOException exception = new IOException("123");
        Throwable throwable = new Throwable(exception);
        assertEquals(exception.toString(), throwable.getMessage());
        assertEquals("123", throwable.getCause().getMessage());
    }

    public void testCreateInstanceBuilder_NullArg() throws Exception {
        IOException exception = null;
        Throwable throwable = new Throwable(exception);
        assertNull(throwable.getMessage());
        assertNull(throwable.getCause());
    }

    public void testCreateInstanceBuilder_TwoArgs() throws Exception {
        Throwable throwable = new Throwable("abc", new IOException("123"));
        assertEquals("abc", throwable.getMessage());
        assertEquals("123", throwable.getCause().getMessage());
    }

    public void testGetCause() throws Exception {
        IOException exception = new IOException("123");
        assertSame(exception, new Throwable(exception).getCause());
        NamingException namingException = new NamingException();
        namingException.initCause(exception);
        assertSame(exception, ((Throwable) namingException).getCause());
        for (Throwable throwable : getSpecialThrowables(exception)) {
            assertSame(exception, throwable.getCause());
        }
    }

    public void testInitCause() throws Exception {
        Throwable throwable = new Throwable("abc");
        assertNull(throwable.getCause());
        try {
            throwable.initCause(throwable);
            fail();
        } catch (IllegalArgumentException e) {
            //ok
        }
        IOException exception = new IOException("123");
        throwable.initCause(exception);
        assertSame(exception, throwable.getCause());
        try {
            throwable.initCause(new Exception("def"));
            fail();
        } catch (IllegalStateException e) {
            //ok
        }
    }

    public void testInitCause_Special() throws Exception {
        IOException exception = new IOException("123");
        for (Throwable specialThrowable : getSpecialThrowables(exception)) {
            try {
                specialThrowable.initCause(new Exception("def"));
                fail();
            } catch (IllegalStateException e) {
                //ok
            }
        }
        Throwable namingException = new NamingException();
        namingException.initCause(exception);
        assertSame(exception, namingException.getCause());
        try {
            namingException.initCause(new Exception("def"));
            fail();
        } catch (IllegalStateException e) {
            //ok
        }
    }

    public void testGetStackTrace() throws Exception {
        StackTraceElement[] elements = new Throwable().getStackTrace();
        assertTrue(elements.length > 1);
        StackTraceElement element = elements[0];
        if (element.getClassName().equals(Throwable.class.getName())) {
            element = elements[1];
        }
        assertEquals(getClass().getName(), element.getClassName());
        assertEquals(getClass().getSimpleName() + ".java", element.getFileName());
        assertTrue(element.getLineNumber() > 0);
        assertEquals("testGetStackTrace", element.getMethodName());
    }

    private static List<Throwable> getSpecialThrowables(IOException exception) {
        return Arrays.asList(
                new ActivationException("", exception),
                new ClassNotFoundException("", exception),
                new ExceptionInInitializerError(exception),
                new InvocationTargetException(exception),
                new PrinterIOException(exception),
                new PrivilegedActionException(exception),
                new RemoteException("", exception),
                new ServerCloneException("", exception),
                new UndeclaredThrowableException(exception),
                new WriteAbortedException("", exception)
        );
    }

}