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
package net.sf.retrotranslator.runtime.impl;

import java.util.*;
import java.util.concurrent.*;
import junit.framework.TestCase;
import net.sf.retrotranslator.tests.BaseTestCase;

/**
 * @author Taras Puchko
 */
public class WeakIdentityTableTestCase extends BaseTestCase {

    private static class StringWeakIdentityTable extends WeakIdentityTable<String, String> {
        protected String initialValue() {
            return new String();
        }
    }

    public void testSequential() throws Exception {
        StringWeakIdentityTable table = new StringWeakIdentityTable();
        String k1 = new String("k");
        assertNull(table.lookup(k1));
        assertEquals(0, table.size());
        String v1 = table.obtain(k1);
        assertNotNull(v1);
        assertEquals(1, table.size());
        assertSame(v1, table.lookup(k1));
        assertSame(v1, table.obtain(k1));
        assertEquals(1, table.size());
        String v1x = "";
        table.putIfAbsent(k1, v1x);
        assertEquals(1, table.size());
        assertSame(v1, table.lookup(k1));
        assertNotSame(v1, v1x);

        String k2 = new String("k");
        assertNull(table.lookup(k2));
        assertEquals(1, table.size());
        String v2 = "";
        table.putIfAbsent(k2, v2);
        assertEquals(2, table.size());
        assertSame(v2, table.lookup(k2));
        assertSame(v2, table.obtain(k2));
        assertEquals(2, table.size());

        System.gc();
        assertSame(v1, table.lookup(k1));
        assertSame(v2, table.lookup(k2));
        assertNotSame(v1, v2);
        k1 = null;
        gc(table, 1);
        assertEquals(1, table.size());
        assertSame(v2, table.lookup(k2));
        k2 = null;
        gc(table, 0);
        assertEquals(0, table.size());
    }

    public void testParallel() throws Exception {
        final StringWeakIdentityTable table = new StringWeakIdentityTable();
        ExecutorService service = Executors.newCachedThreadPool();
        List<Future> list = new ArrayList<Future>();
        final int iterations = 1000;
        final CyclicBarrier barrier = new CyclicBarrier(20);
        for (int i = 0; i < barrier.getParties(); i++) {
            list.add(service.submit(new Callable() {
                public Object call() throws Exception {
                    barrier.await();
                    for (int j = 0; j < iterations; j++) {
                        String k = new String("k");
                        assertNull(table.lookup(k));
                        String v = table.obtain(k);
                        assertNotNull(v);
                        table.putIfAbsent(k, new String("x"));
                        assertSame(v, table.lookup(k));
                        assertSame(v, table.obtain(k));
                        String k2 = new String("k2");
                        String v2 = "v2";
                        table.putIfAbsent(k2, v2);
                        assertSame(v2, table.lookup(k2));
                    }
                    return null;
                }
            }));
        }
        for (Future future : list) {
            future.get();
        }
        gc(table, 0);
        int size = table.size();
        assertTrue("Table must be empty but contains " + size + " entries.", size < 10);
    }

    private void gc(final WeakIdentityTable table, final int size) throws Exception {
        gc(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return table.size() > size;
            }
        });
    }

}