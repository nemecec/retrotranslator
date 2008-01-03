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
package net.sf.retrotranslator.runtime.java.lang;

import junit.framework.TestCase;

/**
 * @author Taras Puchko
 */
public class _StringBufferTestCase extends TestCase {

    public void testConvertConstructorArguments() throws Exception {
        CharSequence sequence = "abc";
        assertEquals("abc", new StringBuffer(sequence).toString());
        sequence = null;
        try {
            new StringBuilder(sequence);
            fail();
        } catch (NullPointerException e) {
            //ok
        }
    }

    public void testAppend() throws Exception {
        CharSequence sequence = "xyz";
        assertEquals("abcxyz", new StringBuffer("abc").append(sequence).toString());
        assertEquals("abcy", new StringBuilder("abc").append(sequence, 1, 2).toString());
    }

    public void testAppendCodePoint() throws Exception {
        assertEquals("abc", new StringBuffer("a").appendCodePoint('b').append('c').toString());
        assertEquals("a\uD834\uDD1Eb", new StringBuffer("a").appendCodePoint(0x1D11E).append('b').toString());
        try {
            new StringBuffer().appendCodePoint(-1);
            fail();
        } catch (IllegalArgumentException e) {
            //ok
        }
    }

    public void testCodePointAt() throws Exception {
        assertEquals('b', new StringBuffer("ab").codePointAt(1));
        assertEquals(0x1D11E, new StringBuffer("b\uD834\uDD1Ec").codePointAt(1));
        assertEquals(0xD834, new StringBuffer("b\uD834").codePointAt(1));
        try {
            new StringBuffer("abc").codePointAt(-1);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        try {
            new StringBuffer("abc").codePointAt(3);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
    }

    public void testCodePointBefore() throws Exception {
        assertEquals('a', new StringBuffer("ab").codePointBefore(1));
        assertEquals(0x1D11E, new StringBuffer("b\uD834\uDD1Ec").codePointBefore(3));
        assertEquals(0xDD1E, new StringBuffer("\uDD1E").codePointBefore(1));
        try {
            new StringBuffer("abc").codePointBefore(0);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        try {
            new StringBuffer("abc").codePointBefore(4);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
    }

    public void testCodePointCount() throws Exception {
        assertEquals(2, new StringBuffer("abcd").codePointCount(1, 3));
        assertEquals(3, new StringBuffer("b\uD834\uDD1Ec").codePointCount(0, 4));
        assertEquals(2, new StringBuffer("b\uD834\uDD1Ec").codePointCount(0, 2));
        assertEquals(2, new StringBuffer("b\uD834").codePointCount(0, 2));
        try {
            new StringBuffer("abc").codePointCount(-1, 1);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        try {
            new StringBuffer("abc").codePointCount(1, 0);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        try {
            new StringBuffer("abc").codePointCount(5, 5);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        try {
            new StringBuffer("abc").codePointCount(1, 5);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
    }

    public void testInsert() throws Exception {
        CharSequence sequence = "xyz";
        assertEquals("axyzbc", new StringBuffer("abc").insert(1, sequence).toString());
        assertEquals("aybc", new StringBuilder("abc").insert(1, sequence, 1, 2).toString());
    }

    public void testOffsetByCodePoints() throws Exception {
        assertEquals(3, new StringBuffer("abc").offsetByCodePoints(1, 2));
        assertEquals(1, new StringBuffer("abc").offsetByCodePoints(1, 0));
        assertEquals(3, new StringBuffer("b\uD834\uDD1Ec").offsetByCodePoints(0, 2));
        try {
            new StringBuffer("abc").offsetByCodePoints(-1, 1);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        try {
            new StringBuffer("abc").offsetByCodePoints(10, 0);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        try {
            new StringBuffer("abc").offsetByCodePoints(0, 5);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        try {
            new StringBuffer("abc").offsetByCodePoints(2, -5);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
    }

    public void testTrimToSize() throws Exception {
        StringBuffer buffer = new StringBuffer(100);
        buffer.append("abc");
        buffer.trimToSize();
    }

}