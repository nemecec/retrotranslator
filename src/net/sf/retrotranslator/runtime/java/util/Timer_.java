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
package net.sf.retrotranslator.runtime.java.util;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Date;
import net.sf.retrotranslator.registry.Advanced;

/**
 * @author Taras Puchko
 */
@Advanced("Timer.All")
public class Timer_ {

    private static AtomicInteger counter = new AtomicInteger();

    private final ScheduledThreadPoolExecutor executor;

    private final Object finalizer = new Object() {
        protected void finalize() throws Throwable {
            executor.shutdown();
        }
    };

    public Timer_() {
        this(false);
    }

    public Timer_(boolean isDaemon) {
        this("Timer-" + counter.getAndIncrement(), isDaemon);
    }

    public Timer_(String name) {
        this(name, false);
    }

    public Timer_(final String name, final boolean isDaemon) {
        executor = new ScheduledThreadPoolExecutor(1, new TimerThreadFactory(name, isDaemon));
        executor.setRejectedExecutionHandler(new TimerRejectedExecutionHandler());
        executor.setContinueExistingPeriodicTasksAfterShutdownPolicy(true);
        executor.prestartCoreThread();
    }

    public void cancel() {
        executor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        executor.shutdown();
    }

    public int purge() {
        int count = 0;
        BlockingQueue queue = executor.getQueue();
        for (Object object : queue.toArray()) {
            if (object instanceof Future && ((Future) object).isCancelled()) {
                if (queue.remove(object)) {
                    count++;
                }
            }
        }
        return count;
    }

    public void schedule(TimerTask_ task, Date time) {
        long delay = checkDelay(time);
        task.command.runOnce(executor, delay);
    }

    public void schedule(TimerTask_ task, Date firstTime, long period) {
        checkPeriod(period);
        long delay = checkDelay(firstTime);
        task.command.runWithFixedDelay(executor, delay, period);
    }

    public void scheduleAtFixedRate(TimerTask_ task, Date firstTime, long period) {
        checkPeriod(period);
        long delay = checkDelay(firstTime);
        task.command.runAtFixedRate(executor, delay, period);
    }

    public void schedule(TimerTask_ task, long delay) {
        checkDelay(delay);
        task.command.runOnce(executor, delay);
    }

    public void schedule(TimerTask_ task, long delay, long period) {
        checkDelay(delay);
        checkPeriod(period);
        task.command.runWithFixedDelay(executor, delay, period);
    }

    public void scheduleAtFixedRate(TimerTask_ task, long delay, long period) {
        checkDelay(delay);
        checkPeriod(period);
        task.command.runAtFixedRate(executor, delay, period);
    }

    private static long checkDelay(Date date) {
        long time = date.getTime();
        if (time < 0) {
            throw new IllegalArgumentException();
        }
        return time - System.currentTimeMillis();
    }

    private static void checkDelay(long delay) {
        if (delay < 0 || delay + System.currentTimeMillis() < 0) {
            throw new IllegalArgumentException();
        }
    }

    private static void checkPeriod(long period) {
        if (period <= 0) {
            throw new IllegalArgumentException();
        }
    }

    private static class TimerThreadFactory implements ThreadFactory {
        private final String name;
        private final boolean daemon;

        public TimerThreadFactory(String name, boolean daemon) {
            this.name = name;
            this.daemon = daemon;
        }

        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setName(name);
            thread.setDaemon(daemon);
            return thread;
        }
    }

    private static class TimerRejectedExecutionHandler implements RejectedExecutionHandler {
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            throw new IllegalStateException();
        }
    }

}
