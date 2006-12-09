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
package net.sf.retrotranslator.runtime.java.util.concurrent.locks;

import junit.framework.*;

import java.util.concurrent.locks.LockSupport;

/**
 * @author Taras Puchko
 */
public class LockSupport_TestCase extends TestCase {

    private static final int SLEEP_TIME = 100;

    private abstract class ExecutingThread extends Thread {

        private Throwable result;

        protected abstract void execute() throws Throwable;

        public final void run() {
            try {
                execute();
            } catch (Throwable throwable) {
                result = throwable;
                throwable.printStackTrace();
            }
        }

        public void await() throws Throwable {
            join();
            if (result != null) {
                throw result;
            }
        }

    }

    public void testPark_ParkUnpark() throws Throwable {
        ExecutingThread thread = new ExecutingThread() {
            protected void execute() throws Throwable {
                LockSupport.park();
            }
        };
        thread.start();
        Thread.sleep(SLEEP_TIME);
        LockSupport.unpark(thread);
        thread.await();
    }

    public void testPark_UnparkPark() throws Throwable {
        ExecutingThread thread = new ExecutingThread() {
            protected void execute() throws Throwable {
                Thread.sleep(SLEEP_TIME);
                LockSupport.park();
            }
        };
        thread.start();
        LockSupport.unpark(thread);
        thread.await();
    }

    public void testPark_ParkInterrupt() throws Throwable {
        ExecutingThread thread = new ExecutingThread() {
            protected void execute() throws Throwable {
                LockSupport.park();
            }
        };
        thread.start();
        Thread.sleep(SLEEP_TIME);
        thread.interrupt();
        thread.await();
    }

    public void testPark_InterruptPark() throws Throwable {
        ExecutingThread thread = new ExecutingThread() {
            protected void execute() throws Throwable {
                Thread.currentThread().interrupt();
                LockSupport.park();
            }
        };
        thread.start();
        thread.await();
    }

    public void testParkNanos() throws Throwable {
        ExecutingThread thread = new ExecutingThread() {
            protected void execute() throws Throwable {
                Thread.currentThread().interrupt();
                LockSupport.parkNanos(1001000);
            }
        };
        thread.start();
        thread.await();
    }

    public void testParkUntil() throws Throwable {
        ExecutingThread thread = new ExecutingThread() {
            protected void execute() throws Throwable {
                Thread.currentThread().interrupt();
                LockSupport.parkUntil(System.currentTimeMillis() + 500);
            }
        };
        thread.start();
        thread.await();
    }

}