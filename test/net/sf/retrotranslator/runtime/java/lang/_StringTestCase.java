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

import java.util.Locale;
import junit.framework.TestCase;

/**
 * @author Taras Puchko
 */
public class _StringTestCase extends TestCase {

    public void testConvertConstructorArguments() throws Exception {
        assertEquals("b\uD834\uDD1Ec", new String(new int[]{'a', 'b', 0x1D11E, 'c', 'd'}, 1, 3));
        try {
            new String(new int[]{-1, 'b'}, 0, 1);
            fail();
        } catch (IllegalArgumentException e) {
            //ok
        }
        try {
            new String(new int[]{'a', 'b'}, -1, 1);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        try {
            new String(new int[]{'a', 'b'}, 1, -1);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        try {
            new String(new int[]{'a', 'b'}, 1, 2);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
    }

    public void testCodePointAt() throws Exception {
        assertEquals('b', "ab".codePointAt(1));
        assertEquals(0x1D11E, "b\uD834\uDD1Ec".codePointAt(1));
        assertEquals(0xD834, "b\uD834".codePointAt(1));
        try {
            "abc".codePointAt(-1);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        try {
            "abc".codePointAt(3);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
    }

    public void testCodePointBefore() throws Exception {
        assertEquals('a', "ab".codePointBefore(1));
        assertEquals(0x1D11E, "b\uD834\uDD1Ec".codePointBefore(3));
        assertEquals(0xDD1E, "\uDD1E".codePointBefore(1));
        try {
            "abc".codePointBefore(0);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        try {
            "abc".codePointBefore(4);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
    }

    public void testCodePointCount() throws Exception {
        assertEquals(2, "abcd".codePointCount(1, 3));
        assertEquals(3, "b\uD834\uDD1Ec".codePointCount(0, 4));
        assertEquals(2, "b\uD834\uDD1Ec".codePointCount(0, 2));
        assertEquals(2, "b\uD834".codePointCount(0, 2));
        try {
            "abc".codePointCount(-1, 1);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        try {
            "abc".codePointCount(1, 0);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        try {
            "abc".codePointCount(5, 5);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        try {
            "abc".codePointCount(1, 5);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
    }

    public void testContains() throws Exception {
        assertTrue("abcd".contains("bc"));
        assertFalse("abcd".contains("xy"));
    }

    public void testContentEquals() throws Exception {
        assertTrue("abcd".contentEquals("abcd"));
        assertFalse("abcd".contentEquals("xy"));
    }

    public void testFormat() throws Exception {
        assertEquals("1234", String.format("%d", 1234));
        assertEquals("1.234", String.format(Locale.GERMAN, "%,d", 1234));
    }

    public void testOffsetByCodePoints() throws Exception {
        assertEquals(3, "abc".offsetByCodePoints(1, 2));
        assertEquals(1, "abc".offsetByCodePoints(1, 0));
        assertEquals(3, "b\uD834\uDD1Ec".offsetByCodePoints(0, 2));
        try {
            "abc".offsetByCodePoints(-1, 1);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        try {
            "abc".offsetByCodePoints(10, 0);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        try {
            "abc".offsetByCodePoints(0, 5);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        try {
            "abc".offsetByCodePoints(2, -5);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
    }

    public void testReplace() throws Exception {
        assertEquals("axydxy", "abcdbc".replace("bc", "xy"));
        assertEquals("abc", "abc*".replace("*", ""));
        assertEquals("XaXbXcX", "abc".replace("", "X"));
        assertEquals("ac", "abc".replace("b", ""));
        assertEquals("abc", "abc".replace("", ""));
    }

}