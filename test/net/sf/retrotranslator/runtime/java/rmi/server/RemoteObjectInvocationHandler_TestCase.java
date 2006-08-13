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
package net.sf.retrotranslator.runtime.java.rmi.server;

import junit.framework.TestCase;

import javax.naming.NamingException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.UnexpectedException;
import java.rmi.server.*;
import java.util.Arrays;

/**
 * @author Taras Puchko
 */
public class RemoteObjectInvocationHandler_TestCase extends TestCase {

    private MockRemoteRef ref = new MockRemoteRef(new MyRemoteImpl());
    private MyRemote myRemote = (MyRemote) Proxy.newProxyInstance(MyRemote.class.getClassLoader(),
            new Class[]{MyRemote.class}, new RemoteObjectInvocationHandler(ref));

    public void testRemoteObjectInvocationHandler_() throws Exception {
        try {
            new RemoteObjectInvocationHandler(null);
            fail();
        } catch (NullPointerException e) {
            //ok
        }
    }

    public void testInvoke_hashcode() {
        assertEquals(1234567890, myRemote.hashCode());
        assertEquals("remoteHashCode", ref.log());
    }

    public void testInvoke_equals() {
        assertTrue(myRemote.equals(myRemote));
        assertEquals("", ref.log());

        assertTrue(myRemote.equals(Proxy.newProxyInstance(MyRemote.class.getClassLoader(),
                new Class[]{MyRemote.class}, new RemoteObjectInvocationHandler(ref))));
        assertEquals("remoteEquals", ref.log());

        assertFalse(myRemote.equals(Proxy.newProxyInstance(MyRemote.class.getClassLoader(),
                new Class[]{MyRemote.class}, new RemoteObjectInvocationHandler(new MockRemoteRef()))));
        assertEquals("remoteEquals", ref.log());

        assertFalse(myRemote.equals(new MyRemoteImpl()));
        assertEquals("", ref.log());

        assertFalse(myRemote.equals(null));
        assertEquals("", ref.log());
    }

    public void testInvoke_toString() {
        String handlerName = RemoteObjectInvocationHandler.class.getSimpleName();

        assertEquals("Proxy[RemoteObjectInvocationHandler_TestCase$MyRemote," + handlerName + "[<Mock>]]",
                myRemote.toString());
        assertEquals("remoteToString", ref.log());

        Object noInterface = Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class[]{}, new RemoteObjectInvocationHandler(ref));
        assertEquals("Proxy[" + handlerName + "[<Mock>]]", noInterface.toString());

        Object remoteInterface = Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class[]{Remote.class}, new RemoteObjectInvocationHandler(ref));
        assertEquals("Proxy[Remote," + handlerName + "[<Mock>]]", remoteInterface.toString());

        Object comparableInterface = Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class[]{Remote.class, Comparable.class}, new RemoteObjectInvocationHandler(ref));
        assertEquals("Proxy[Comparable," + handlerName + "[<Mock>]]", comparableInterface.toString());

    }

    public void testInvoke_nonRemote() {
        Runnable runnable = (Runnable) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class[]{Runnable.class}, new RemoteObjectInvocationHandler(new MockRemoteRef(new Thread())));
        try {
            runnable.run();
            fail();
        } catch (IllegalArgumentException e) {
            //ok
        }
    }

    public void testInvoke_remote() throws Exception {
        assertEquals("Hello, World!", myRemote.hello("World"));
        assertEquals("invoke: hello [World] -5976794856777945295", ref.log());

        assertEquals(3, myRemote.add(1, 2));
        assertEquals("invoke: add [1, 2] -7734458262622125146", ref.log());
    }

    public void testInvoke_exceptions() throws Exception {
        InternalError internalError = new InternalError();
        try {
            myRemote.exception(internalError);
            fail();
        } catch (InternalError e) {
            assertSame(e, internalError);
        }
        assertEquals("invoke: exception [java.lang.InternalError] 4977354682513654574", ref.log());
        ArithmeticException arithmeticException = new ArithmeticException();
        try {
            myRemote.exception(arithmeticException);
            fail();
        } catch (ArithmeticException e) {
            assertSame(e, arithmeticException);
        }
        assertEquals("invoke: exception [java.lang.ArithmeticException] 4977354682513654574", ref.log());
        FileNotFoundException fileNotFoundException = new FileNotFoundException();
        try {
            myRemote.exception(fileNotFoundException);
            fail();
        } catch (FileNotFoundException e) {
            assertSame(e, fileNotFoundException);
        }
        assertEquals("invoke: exception [java.io.FileNotFoundException] 4977354682513654574", ref.log());
        NamingException namingException = new NamingException();
        try {
            myRemote.exception(namingException);
            fail();
        } catch (UnexpectedException e) {
            assertSame(e.getCause(), namingException);
        }
        assertEquals("invoke: exception [javax.naming.NamingException] 4977354682513654574", ref.log());
    }

    private interface MyRemote extends Remote {

        String hello(String name) throws RemoteException;

        int add(int a, int b) throws RemoteException;

        void exception(Throwable t) throws IOException;
    }

    private static class Thrower {

        public static ThreadLocal<Throwable> result = new ThreadLocal<Throwable>();

        public Thrower() throws Throwable {
            Throwable throwable = result.get();
            result.set(null);
            throw throwable;
        }

        public static void rethrow(Throwable t) {
            try {
                Thrower.result.set(t);
                Thrower.class.newInstance();
                throw new Error(t);
            } catch (InstantiationException e) {
                throw new Error(e);
            } catch (IllegalAccessException e) {
                throw new Error(e);
            }
        }
    }

    private class MyRemoteImpl implements MyRemote {

        public String hello(String name) {
            return "Hello, " + name + "!";
        }

        public int add(int a, int b) {
            return a + b;
        }

        public void exception(Throwable t) throws IOException {
            Thrower.rethrow(t);
        }
    }

    private class MockRemoteRef implements RemoteRef {

        private Object delegate;
        private StringBuilder log = new StringBuilder();

        public MockRemoteRef() {
        }

        public MockRemoteRef(Object delegate) {
            this.delegate = delegate;
        }

        public String log() {
            try {
                return log.toString();
            } finally {
                log.setLength(0);
            }
        }

        public Object invoke(Remote obj, Method method, Object[] params, long opnum) throws Exception {
            log.append("invoke: ").append(method.getName()).append(' ').
                    append(Arrays.toString(params)).append(' ').append(opnum);
            try {
                return method.invoke(delegate, params);
            } catch (InvocationTargetException e) {
                Thrower.rethrow(e.getTargetException());
                throw new Error(e);
            }
        }

        public RemoteCall newCall(RemoteObject obj, Operation[] op, int opnum, long hash) throws RemoteException {
            throw new UnsupportedOperationException();
        }

        public void invoke(RemoteCall call) throws Exception {
            throw new UnsupportedOperationException();
        }

        public void done(RemoteCall call) throws RemoteException {
            throw new UnsupportedOperationException();
        }

        public String getRefClass(ObjectOutput out) {
            throw new UnsupportedOperationException();
        }

        public int remoteHashCode() {
            log.append("remoteHashCode");
            return 1234567890;
        }

        public boolean remoteEquals(RemoteRef obj) {
            log.append("remoteEquals");
            return obj == this;
        }

        public String remoteToString() {
            log.append("remoteToString");
            return "<Mock>";
        }

        public void writeExternal(ObjectOutput out) throws IOException {
            throw new UnsupportedOperationException();
        }

        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            throw new UnsupportedOperationException();
        }
    }
}