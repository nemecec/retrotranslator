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
package net.sf.retrotranslator.transformer;

import junit.framework.TestCase;

/**
 * @author Taras Puchko
 */
public class InnerClassVisitorTestCase extends TestCase {

    private static final StringBuilder builder = new StringBuilder();

    private String message = "Hello";

    private class InnerClass {

        public InnerClass() {
        }

        public InnerClass(String s) {
            this();
            if (message.equals(s)) {
                throw new IllegalArgumentException();
            }
        }

        public String getMessage() {
            return message;
        }

        public class TwiceInnerClass {
            public String getMessage() {
                return message;
            }
        }
    }

    private class BaseClass {

        public final int n;

        public BaseClass(int n, Object firstObject, Object secondObject) {
            this.n = firstObject != secondObject ? n : 0;
        }

        public String getMessage() {
            return message;
        }
    }

    private class DerivedClass extends BaseClass {

        public final double m;

        public DerivedClass(int n, double m) {
            super(n,
                    new BaseClass(1, new BaseClass(1, "test", new Object()), new Object()),
                    new BaseClass(1, new Object(), "test"));
            this.m = m;
        }

        public String getMessage() {
            return message;
        }
    }

    public void testInnerClass() {
        assertSame(message, new InnerClass().getMessage());
    }

    public void testTwiceInnerClass() {
        assertSame(message, new InnerClass("test").new TwiceInnerClass().getMessage());
    }

    public void testBaseClass() {
        assertSame(message, new BaseClass(7, "test", new Object()).getMessage());
    }

    public void testDerivedClass() {
        assertSame(message, new DerivedClass(7, 0.4).getMessage());
    }

    public void testLocalClass() {
        class LocalClass {
            public String getMessage() {
                return message;
            }
        }
        assertSame(message, new LocalClass().getMessage());
    }

    public void testTwiceLocalClass() {
        class LocalClass {
            class TwiceLocalClass {
                public String getMessage() {
                    return message;
                }
            }
        }
        assertSame(message, new LocalClass().new TwiceLocalClass().getMessage());
    }

    public synchronized void testNonstaticContext() {
        builder.setLength(0);
        final Number a = Integer.valueOf("3");
        new Runnable() {
            public void run() {
                builder.append(a);
            }
        }.run();
        assertEquals("3", builder.toString());
    }

    public synchronized void testStaticContext() {
        executeInStaticMethod();
    }

    private static void executeInStaticMethod() {
        builder.setLength(0);
        final Number a = Integer.valueOf("1");
        final Number b = Integer.valueOf("2");
        new Runnable() {
            public void run() {
                builder.append(a).append(":").append(b);
            }
        }.run();
        assertEquals("1:2", builder.toString());
    }

}