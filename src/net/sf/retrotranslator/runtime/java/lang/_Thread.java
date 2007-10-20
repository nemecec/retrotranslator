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

import static java.lang.Thread.UncaughtExceptionHandler;
import java.util.*;
import net.sf.retrotranslator.runtime.impl.*;

/**
 * @author Taras Puchko
 */
public class _Thread {

    private static final StackTraceElement[] EMPTY_STACK_TRACE = new StackTraceElement[0];

    private static final WeakIdentityTable<Thread, _Thread> threads =
            new WeakIdentityTable<Thread, _Thread>() {
                protected _Thread initialValue() {
                    return new _Thread();
                }
            };

    private static long lastId;
    private static UncaughtExceptionHandler defaultHandler;

    private volatile long id;
    private volatile boolean started;
    private UncaughtExceptionHandler handler;

    public static class BasicThreadBuilder {

        private ThreadGroup group;
        private Runnable target;

        protected BasicThreadBuilder(ThreadGroup group, Runnable target) {
            this.group = group;
            this.target = target;
        }

        public ThreadGroup argument1() {
            return group;
        }

        public Runnable argument2() {
            return target;
        }
    }

    public static class AdvancedThreadBuilder {

        private ThreadGroup group;
        private Runnable target;
        private String name;
        private long stackSize;

        protected AdvancedThreadBuilder(ThreadGroup group, Runnable target, String name, long stackSize) {
            this.group = group;
            this.target = target;
            this.name = name;
            this.stackSize = stackSize;
        }

        public ThreadGroup argument1() {
            return group;
        }

        public Runnable argument2() {
            return target;
        }

        public String argument3() {
            return name;
        }

        public long argument4() {
            return stackSize;
        }
    }

    private static class RunnableWrapper implements Runnable {

        private Runnable target;

        private RunnableWrapper(Runnable target) {
            this.target = target;
        }

        public void run() {
            try {
                target.run();
            } catch (Throwable e) {
                processException(e);
            }
        }

        protected static Runnable wrap(Runnable target) {
            return target == null || target instanceof RunnableWrapper ? target : new RunnableWrapper(target);
        }
    }

    // Referenced from translated bytecode
    public static void handleUncaughtException(Throwable throwable) {
        processException(throwable);
    }

    protected static void processException(Throwable throwable) {
        if (new Exception().getStackTrace().length <= 3) {
            Thread thread = Thread.currentThread();
            UncaughtExceptionHandler handler = threads.obtain(thread).getHandler();
            if (handler == null) {
                handler = getDefaultUncaughtExceptionHandler();
            }
            if (handler == null) {
                handler = thread.getThreadGroup();
            }
            handler.uncaughtException(thread, throwable);
        } else {
            try {
                throw throwable;
            } catch (RuntimeException e) {
                throw e;
            } catch (Error e) {
                throw e;
            } catch (Throwable t) {
                throw new Error(t);
            }
        }
    }

    @Advanced({"Thread.setDefaultUncaughtExceptionHandler", "Thread.setUncaughtExceptionHandler"})
    public static Runnable convertConstructorArguments(Runnable target) {
        return RunnableWrapper.wrap(target);
    }

    @Advanced({"Thread.setDefaultUncaughtExceptionHandler", "Thread.setUncaughtExceptionHandler"})
    public static BasicThreadBuilder createInstanceBuilder(ThreadGroup group, Runnable target) {
        return new BasicThreadBuilder(group, RunnableWrapper.wrap(target));
    }

    @Advanced({"Thread.setDefaultUncaughtExceptionHandler", "Thread.setUncaughtExceptionHandler"})
    public static AdvancedThreadBuilder createInstanceBuilder(Runnable target, String name) {
        return new AdvancedThreadBuilder(null, RunnableWrapper.wrap(target), name, 0);
    }

    @Advanced({"Thread.setDefaultUncaughtExceptionHandler", "Thread.setUncaughtExceptionHandler"})
    public static AdvancedThreadBuilder createInstanceBuilder(ThreadGroup group, Runnable target, String name) {
        return new AdvancedThreadBuilder(group, RunnableWrapper.wrap(target), name, 0);
    }

    @Advanced({"Thread.setDefaultUncaughtExceptionHandler", "Thread.setUncaughtExceptionHandler"})
    public static AdvancedThreadBuilder createInstanceBuilder(
            ThreadGroup group, Runnable target, String name, long stackSize) {
        return new AdvancedThreadBuilder(group, RunnableWrapper.wrap(target), name, stackSize);
    }

    public static synchronized UncaughtExceptionHandler getDefaultUncaughtExceptionHandler() {
        return defaultHandler;
    }

    @Advanced("Thread.setDefaultUncaughtExceptionHandler")
    public static synchronized void setDefaultUncaughtExceptionHandler(UncaughtExceptionHandler handler) {
        defaultHandler = handler;
    }

    public static long getId(Thread thread) {
        return threads.obtain(thread).getId();
    }

    private long getId() {
        if (id == 0) {
            synchronized (threads) {
                while (id == 0) {
                    id = ++lastId;
                }
            }
        }
        return id;
    }

    public static StackTraceElement[] getStackTrace(Thread thread) {
        return thread == Thread.currentThread() ? getStackTrace() : EMPTY_STACK_TRACE;
    }

    private static StackTraceElement[] getStackTrace() {
        return new Throwable().getStackTrace();
    }

    public static UncaughtExceptionHandler getUncaughtExceptionHandler(Thread thread) {
        UncaughtExceptionHandler handler = threads.obtain(thread).getHandler();
        return handler != null ? handler : thread.getThreadGroup();
    }

    @Advanced("Thread.setUncaughtExceptionHandler")
    public static void setUncaughtExceptionHandler(Thread thread, UncaughtExceptionHandler handler) {
        threads.obtain(thread).setHandler(handler);
    }

    private synchronized UncaughtExceptionHandler getHandler() {
        return handler;
    }

    private synchronized void setHandler(UncaughtExceptionHandler handler) {
        this.handler = handler;
    }

    @Advanced("Thread.getState")
    public static void start(Thread thread) {
        thread.start();
        threads.obtain(thread).started = true;
    }

    @Advanced("Thread.getState")
    public static Thread.State getState(Thread thread) {
        if (thread.isAlive()) {
            return Thread.State.RUNNABLE;
        }
        if (threads.obtain(thread).started) {
            return Thread.State.TERMINATED;
        }
        return Thread.State.NEW;
    }

    public static Map<Thread, StackTraceElement[]> getAllStackTraces() {
        HashMap<Thread, StackTraceElement[]> result = new HashMap<Thread, StackTraceElement[]>();
        Thread currentThread = Thread.currentThread();
        ThreadGroup group = currentThread.getThreadGroup();
        ThreadGroup parent;
        while ((parent = group.getParent()) != null) {
            group = parent;
        }
        Thread[] threads = new Thread[group.activeCount() + 1];
        int count = group.enumerate(threads);
        while (count == threads.length) {
            threads = new Thread[threads.length * 2];
            count = group.enumerate(threads);
        }
        for (int i = 0; i < count; i++) {
            Thread thread = threads[i];
            if (thread.isAlive()) {
                result.put(thread, thread == currentThread ? getStackTrace() : EMPTY_STACK_TRACE);
            }
        }
        return result;
    }

}
