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
public class ObjectMethodsVisitorTestCase extends TestCase {

    public interface BaseInterface {

        Object clone() throws CloneNotSupportedException;

        boolean equals(Object obj);

        void finalize() throws Throwable;

        int hashCode();

        void method();

        String toString();
    }

    public interface DerivedInterface extends BaseInterface {
    }

    public static class ClonableClass implements DerivedInterface, Cloneable {

        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

        public void finalize() throws Throwable {
            super.finalize();
        }

        public int hashCode() {
            return super.hashCode();
        }

        public void method() {
        }

    }

    public void testObjectMethods() throws Throwable {
        DerivedInterface ref = new ClonableClass();
        ref.method();
        ref.hashCode();
        ref.equals("");
        ref.toString();
        ref.clone();
        ref.finalize();
    }

    public void testArrayClone() throws Throwable {
        int[] a = {10, 20};
        int[] b = a.clone();
        assertNotSame(a, b);
        assertEquals(2, b.length);
        assertEquals(10, b[0]);
        assertEquals(20, b[1]);
    }

}