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
package net.sf.retrotranslator.runtime.java.util.concurrent.locks;

import net.sf.retrotranslator.runtime.impl.WeakIdentityTable;

/**
 * @author Taras Puchko
 */
public class LockSupport_ {

    private static final WeakIdentityTable<Thread, ThreadLock> table =
            new WeakIdentityTable<Thread, ThreadLock>() {
                protected ThreadLock initialValue() {
                    return new ThreadLock();
                }
            };

    private LockSupport_() {
    }

    private static class ThreadLock {

        private boolean permit;

        public ThreadLock() {
        }

        public synchronized void park() throws InterruptedException {
            if (permit) {
                permit = false;
            } else {
                wait();
            }
        }

        public synchronized void parkNanos(long nanos) throws InterruptedException {
            if (permit) {
                permit = false;
            } else {
                wait(nanos / 1000000, (int) (nanos % 1000000));
            }
        }

        public synchronized void parkUntil(long deadline) throws InterruptedException {
            if (permit) {
                permit = false;
            } else {
                long millis = deadline - System.currentTimeMillis();
                if (millis > 0) {
                    wait(millis);
                }
            }
        }

        public synchronized void unpark() {
            if (!permit) {
                permit = true;
                notify();
            }
        }

    }

    public static void park() {
        try {
            table.obtain(Thread.currentThread()).park();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void parkNanos(long nanos) {
        try {
            table.obtain(Thread.currentThread()).parkNanos(nanos);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void parkUntil(long deadline) {
        try {
            table.obtain(Thread.currentThread()).parkUntil(deadline);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void unpark(Thread thread) {
        table.obtain(thread).unpark();
    }

}
