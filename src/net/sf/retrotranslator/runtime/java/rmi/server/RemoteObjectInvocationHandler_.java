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
package net.sf.retrotranslator.runtime.java.rmi.server;

import java.io.*;
import java.lang.reflect.*;
import java.rmi.*;
import java.rmi.server.*;
import java.security.*;
import net.sf.retrotranslator.runtime.asm.Type;

/**
 * @author Taras Puchko
 */
public class RemoteObjectInvocationHandler_ extends RemoteObject implements InvocationHandler {

    private static final OutputStream NULL_OUTPUT_STREAM = new OutputStream() {
        public void write(int b) throws IOException {
        }

        public void write(byte b[]) throws IOException {
        }

        public void write(byte b[], int off, int len) throws IOException {
        }
    };

    public RemoteObjectInvocationHandler_(RemoteRef ref) {
        super(ref);
        if (ref == null) {
            throw new NullPointerException();
        }
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            String name = method.getName();
            if (name.equals("hashCode")) {
                return hashCode();
            }
            if (name.equals("equals")) {
                Object arg = args[0];
                return arg == proxy ||
                        arg != null && Proxy.isProxyClass(arg.getClass()) && equals(Proxy.getInvocationHandler(arg));
            }
            if (name.equals("toString")) {
                StringBuilder result = new StringBuilder("Proxy[");
                Class[] interfaces = proxy.getClass().getInterfaces();
                if (interfaces.length > 0) {
                    boolean isFirst = interfaces.length == 1 || interfaces[0] != Remote.class;
                    result.append(getShortName(interfaces[isFirst ? 0 : 1])).append(',');
                }
                return result.append(this).append(']').toString();
            }
        }
        if (proxy instanceof Remote) {
            try {
                return ref.invoke((Remote) proxy, method, args, getOperationNumber(method));
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw isExceptionDeclared(e.getClass(), proxy.getClass(), method)
                        ? e : new UnexpectedException(e.getMessage(), e);
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    public String toString() {
        return ref == null ? getShortName(getClass()) : getShortName(getClass()) + '[' + ref.remoteToString() + ']';
    }

    private static String getShortName(Class aClass) {
        String name = aClass.getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }

    private static long getOperationNumber(Method method) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA");
            DataOutputStream stream = new DataOutputStream(new DigestOutputStream(NULL_OUTPUT_STREAM, digest));
            stream.writeUTF(method.getName() + Type.getMethodDescriptor(method));
            byte[] bytes = digest.digest();
            long result = 0;
            for (int i = 7; i >= 0; i--) {
                result = result << 8 | bytes[i] & 0xFF;
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            SecurityException exception = new SecurityException(e.toString());
            exception.initCause(e);
            throw exception;
        } catch (IOException e) {
            SecurityException exception = new SecurityException(e.toString());
            exception.initCause(e);
            throw exception;
        }
    }

    private static boolean isExceptionDeclared(Class exceptionClass, Class proxyClass, Method method) {
        try {
            method = proxyClass.getMethod(method.getName(), method.getParameterTypes());
        } catch (NoSuchMethodException ignore) {
            //ignore
        }
        for (Class declaredClass : method.getExceptionTypes()) {
            if (declaredClass.isAssignableFrom(exceptionClass)) return true;
        }
        return false;
    }

}
