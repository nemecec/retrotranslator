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
import net.sf.retrotranslator.runtime.impl.WeakIdentityTable;

/**
 * @author Taras Puchko
 */
public class _Throwable {

    private static final String TAB_AT_SPACE = "\tat ";
    private static final Throwable NULL = new Throwable();

    private static final WeakIdentityTable<Throwable, Throwable> causeTable =
            new WeakIdentityTable<Throwable, Throwable>();

    public static class ThrowableBuilder {

        private final String message;
        private final Throwable cause;

        protected ThrowableBuilder(String message, Throwable cause) {
            this.message = message;
            this.cause = cause;
        }

        public String argument1() {
            return message;
        }

        public void initialize(Throwable throwable) {
            initCause(throwable, cause);
        }

    }

    public static ThrowableBuilder createInstanceBuilder(String message, Throwable cause) {
        return new ThrowableBuilder(message, cause);
    }

    public static ThrowableBuilder createInstanceBuilder(Throwable cause) {
        return new ThrowableBuilder(cause == null ? null : cause.toString(), cause);
    }

    public static Throwable getCause(Throwable throwable) {
        if (throwable instanceof ActivationException) {
            return ((ActivationException) throwable).detail;
        }
        if (throwable instanceof ClassNotFoundException) {
            return ((ClassNotFoundException) throwable).getException();
        }
        if (throwable instanceof ExceptionInInitializerError) {
            return ((ExceptionInInitializerError) throwable).getException();
        }
        if (throwable instanceof InvocationTargetException) {
            return ((InvocationTargetException) throwable).getTargetException();
        }
        if (throwable instanceof PrinterIOException) {
            return ((PrinterIOException) throwable).getIOException();
        }
        if (throwable instanceof PrivilegedActionException) {
            return ((PrivilegedActionException) throwable).getException();
        }
        if (throwable instanceof RemoteException) {
            return ((RemoteException) throwable).detail;
        }
        if (throwable instanceof ServerCloneException) {
            return ((ServerCloneException) throwable).detail;
        }
        if (throwable instanceof UndeclaredThrowableException) {
            return ((UndeclaredThrowableException) throwable).getUndeclaredThrowable();
        }
        if (throwable instanceof WriteAbortedException) {
            return ((WriteAbortedException) throwable).detail;
        }
        if (throwable instanceof NamingException) {
            return ((NamingException) throwable).getRootCause();
        }
        Throwable result = causeTable.lookup(throwable);
        return result == NULL ? null : result;
    }

    public static Throwable initCause(Throwable throwable, Throwable cause) {
        if (throwable instanceof ActivationException ||
                throwable instanceof ClassNotFoundException ||
                throwable instanceof ExceptionInInitializerError ||
                throwable instanceof InvocationTargetException ||
                throwable instanceof PrinterIOException ||
                throwable instanceof PrivilegedActionException ||
                throwable instanceof RemoteException ||
                throwable instanceof ServerCloneException ||
                throwable instanceof UndeclaredThrowableException ||
                throwable instanceof WriteAbortedException) {
            throw new IllegalStateException();
        }
        if (throwable instanceof NamingException) {
            saveCause(throwable, null);
            ((NamingException) throwable).setRootCause(cause);
        } else {
            saveCause(throwable, cause);
        }
        return throwable;
    }

    private static void saveCause(Throwable throwable, Throwable cause) {
        synchronized (throwable) {
            if (causeTable.lookup(throwable) != null) {
                throw new IllegalStateException();
            }
            if (throwable == cause) {
                throw new IllegalArgumentException();
            }
            causeTable.putIfAbsent(throwable, cause == null ? NULL : cause);
        }
    }

    public static StackTraceElement_[] getStackTrace(Throwable throwable) {
        List<StackTraceElement_> result = new ArrayList<StackTraceElement_>();
        StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        BufferedReader reader = new BufferedReader(new StringReader(writer.toString()));
        try {
            String s;
            while ((s = reader.readLine()) != null) {
                StackTraceElement_ element = !s.startsWith(TAB_AT_SPACE) ? null :
                        StackTraceElement_.valueOf(s.substring(TAB_AT_SPACE.length()));
                if (element != null) {
                    result.add(element);
                } else if (!result.isEmpty()) {
                    break;
                }
            }
        } catch (IOException e) {
            throw new Error(e.getMessage());
        }
        return result.toArray(new StackTraceElement_[result.size()]);
    }

}
