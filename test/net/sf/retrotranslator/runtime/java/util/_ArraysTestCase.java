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
package net.sf.retrotranslator.runtime.java.util;

import java.util.Arrays;
import junit.framework.TestCase;

/**
 * @author Taras Puchko
 */
public class _ArraysTestCase extends TestCase {

    public void testDeepEqualsTrue() {
        Object[] a = {"a", "b", new Object[] {"c", null}};
        Object[] b = {"a", "b", new Object[] {"c", null}};
        assertTrue(Arrays.deepEquals(a, b));
        assertTrue(Arrays.deepEquals(null, null));
    }

    public void testDeepEqualsFalse() {
        Object[] a = {"a", "b", new Object[] {"c", null}};
        Object[] b = {"a", "b", new Object[] {"x", null}};
        assertFalse(Arrays.deepEquals(a, b));
        assertFalse(Arrays.deepEquals(null, new Object[0]));
        assertFalse(Arrays.deepEquals(new Object[0], null));
    }

    public void testDeepHashCode() throws Exception {
        Object[] a = {"a", "b", new Object[] {"c", null}};
        assertEquals(130076, Arrays.deepHashCode(a));
    }

    public void testDeepToString() {
        Object[] a = {"a", "b", new Object[] {"c", null}};
        assertEquals("[a, b, [c, null]]", Arrays.deepToString(a));
        assertEquals("null", Arrays.deepToString(null));
        assertEquals("[]", Arrays.deepToString(new Object[0]));
    }

    public void testToStringBoolean() {
        boolean[] a = {true, false, true};
        assertEquals("[true, false, true]", Arrays.toString(a));
        assertEquals("null", Arrays.toString((boolean[]) null));
        assertEquals("[]", Arrays.toString(new boolean[0]));
    }

    public void testToStringByte() {
        byte[] a = {1, -1, 10};
        assertEquals("[1, -1, 10]", Arrays.toString(a));
        assertEquals("null", Arrays.toString((byte[]) null));
        assertEquals("[]", Arrays.toString(new byte[0]));
    }

    public void testToStringChar() {
        char[] a = {'a', 'b', 'c'};
        assertEquals("[a, b, c]", Arrays.toString(a));
        assertEquals("null", Arrays.toString((char[]) null));
        assertEquals("[]", Arrays.toString(new char[0]));
    }

    public void testToStringDouble() {
        double[] a = {1.1, -0.5, 0.8};
        assertEquals("[1.1, -0.5, 0.8]", Arrays.toString(a));
        assertEquals("null", Arrays.toString((double[]) null));
        assertEquals("[]", Arrays.toString(new double[0]));
    }

    public void testToStringFloat() {
        float[] a = {1.17f, -0.51f, 0.87f};
        assertEquals("[1.17, -0.51, 0.87]", Arrays.toString(a));
        assertEquals("null", Arrays.toString((float[]) null));
        assertEquals("[]", Arrays.toString(new float[0]));
    }

    public void testToStringInt() {
        int[] a = {0, -10, 54};
        assertEquals("[0, -10, 54]", Arrays.toString(a));
        assertEquals("null", Arrays.toString((int[]) null));
        assertEquals("[]", Arrays.toString(new int[0]));
    }

    public void testToStringLong() {
        long[] a = {0, -1000, 657};
        assertEquals("[0, -1000, 657]", Arrays.toString(a));
        assertEquals("null", Arrays.toString((long[]) null));
        assertEquals("[]", Arrays.toString(new long[0]));
    }

    public void testToStringObject() {
        Object[] nested = new Object[]{"c", null};
        Object[] a = {"a", "b", null, nested};
        assertEquals("[a, b, null, " + nested.toString() + "]", Arrays.toString(a));
        assertEquals("null", Arrays.toString((Object[]) null));
        assertEquals("[]", Arrays.toString(new Object[0]));
    }

    public void testHashCodeBoolean() throws Exception {
        boolean[] a = {true, false, true};
        assertEquals(1252360, Arrays.hashCode(a));
    }

    public void testHashCodeByte() throws Exception {
        byte[] a = {1, -1, 10};
        assertEquals(30731, Arrays.hashCode(a));
    }

    public void testHashCodeChar() throws Exception {
        char[] a = {'a', 'b', 'c'};
        assertEquals(126145, Arrays.hashCode(a));
    }

    public void testHashCodeDouble() throws Exception {
        double[] a = {1.1, -0.5, 0.8};
        assertEquals(-1896317019, Arrays.hashCode(a));
    }

    public void testHashCodeFloat() throws Exception {
        float[] a = {1.17f, -0.51f, 0.87f};
        assertEquals(299754404, Arrays.hashCode(a));
    }

    public void testHashCodeInt() throws Exception {
        int[] a = {0, -10, 54};
        assertEquals(29535, Arrays.hashCode(a));
    }

    public void testHashCodeLong() throws Exception {
        long[] a = {0, -1000, 657};
        assertEquals(61417, Arrays.hashCode(a));
    }

    public void testHashCodeObject() throws Exception {
        Object[] a = {"a", "b", null, Integer.valueOf("1")};
        assertEquals(3907427, Arrays.hashCode(a));
    }

}
