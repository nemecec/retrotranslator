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

import net.sf.retrotranslator.runtime.impl.Advanced;
import edu.emory.mathcs.backport.java.util.concurrent.*;

/**
 * @author Taras Puchko
 */
@Advanced("Timer.All")
public abstract class TimerTask_ implements Runnable {

    final TimerCommand command = new TimerCommand();

    protected TimerTask_() {
    }

    public abstract void run();

    public boolean cancel() {
        return command.cancel();
    }

    public long scheduledExecutionTime() {
        return command.getExecutionTime();
    }

    class TimerCommand implements Runnable {

        private boolean periodic;
        private boolean cancelled;
        private long executionTime;
        private ScheduledFuture future;
        private ScheduledThreadPoolExecutor executor;

        public void run() {
            try {
                saveExecutionTime();
                TimerTask_.this.run();
            } catch (ThreadDeath e) {
                shutdownExecutor();
                throw e;
            }
        }

        private synchronized void saveExecutionTime() {
            executionTime = System.currentTimeMillis() + future.getDelay(TimeUnit.MILLISECONDS);
        }

        private synchronized void shutdownExecutor() {
            executor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
            executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
            executor.shutdown();
        }

        synchronized long getExecutionTime() {
            return executionTime;
        }

        synchronized boolean cancel() {
            cancelled = true;
            if (future == null) {
                return false;
            }
            if (!periodic && future.getDelay(TimeUnit.MILLISECONDS) <= 0) {
                future.cancel(false);
                return false;
            }
            return future.cancel(false);
        }

        synchronized void runOnce(ScheduledThreadPoolExecutor executor, long delay) {
            check();
            init(false, executor, executor.schedule(this, delay, TimeUnit.MILLISECONDS));
        }

        synchronized void runWithFixedDelay(ScheduledThreadPoolExecutor executor, long delay, long period) {
            check();
            init(true, executor, executor.scheduleWithFixedDelay(this, delay, period, TimeUnit.MILLISECONDS));
        }

        synchronized void runAtFixedRate(ScheduledThreadPoolExecutor executor, long delay, long period) {
            check();
            init(true, executor, executor.scheduleAtFixedRate(this, delay, period, TimeUnit.MILLISECONDS));
        }

        private void check() {
            if (cancelled || executor != null) {
                throw new IllegalStateException();
            }
        }

        private void init(boolean periodic, ScheduledThreadPoolExecutor executor, ScheduledFuture future) {
            this.periodic = periodic;
            this.executor = executor;
            this.future = future;
        }
    }

}
