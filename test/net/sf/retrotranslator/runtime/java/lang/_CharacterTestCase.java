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
public class _CharacterTestCase extends TestCase {

    public void testCharCount() throws Exception {
        assertEquals(1, Character.charCount('a'));
        assertEquals(2, Character.charCount(0x1D11E));
    }

    public void testCodePointAt() throws Exception {
        assertEquals('b', Character.codePointAt("abc", 1));
        assertEquals(0x20001, Character.codePointAt("a\uD840\uDC01c", 1));
        assertEquals(0xD840, Character.codePointAt("a\uD840", 1));
        assertEquals(0x1D11E, Character.codePointAt("\uD834\uDD1E".toCharArray(), 0));
        assertEquals(0xD834, Character.codePointAt("\uD834\uDD1Ex".toCharArray(), 0, 1));
        assertEquals(0xDD1E, Character.codePointAt("x\uDD1E".toCharArray(), 1));
        try {
            Character.codePointAt("abc", -1);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        try {
            Character.codePointAt("abc".toCharArray(), -1);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        try {
            Character.codePointAt("abc", 10);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        try {
            Character.codePointAt("abc".toCharArray(), 10);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        try {
            Character.codePointAt("abc".toCharArray(), 2, 1);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        try {
            Character.codePointAt("abc".toCharArray(), 0, -1);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        try {
            Character.codePointAt("abc".toCharArray(), 0, 10);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
    }

    public void testCodePointBefore() throws Exception {
        assertEquals('a', Character.codePointBefore("abc", 1));
        assertEquals('c', Character.codePointBefore("abc", 3));
        assertEquals(0x20001, Character.codePointBefore("a\uD840\uDC01c", 3));
        assertEquals(0xDC01, Character.codePointBefore("a\uDC01", 2));
        assertEquals(0x1D11E, Character.codePointBefore("\uD834\uDD1E".toCharArray(), 2));
        assertEquals(0xDD1E, Character.codePointBefore("\uD834\uDD1Ex".toCharArray(), 2, 1));
        assertEquals(0xDD1E, Character.codePointBefore("x\uDD1E", 2));
        try {
            Character.codePointBefore("abc", 0);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        try {
            Character.codePointBefore("abc".toCharArray(), 0);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        try {
            Character.codePointBefore("abc", 4);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        try {
            Character.codePointBefore("abc".toCharArray(), 4);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        try {
            Character.codePointBefore("abc".toCharArray(), 1, 2);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        try {
            Character.codePointBefore("abc".toCharArray(), 0, -1);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        try {
            Character.codePointBefore("abc".toCharArray(), 0, 10);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
    }

    public void testCodePointCount() throws Exception {
        assertEquals(4, Character.codePointCount("abcdef", 1, 5));
        assertEquals(3, Character.codePointCount("ab\uD834\uDD1Eef", 1, 5));
        assertEquals(5, Character.codePointCount("ab\uD834x\uDD1Eef", 1, 6));
        assertEquals(4, Character.codePointCount("abcdef".toCharArray(), 1, 4));
        assertEquals(3, Character.codePointCount("ab\uD834\uDD1Eef".toCharArray(), 1, 4));
        assertEquals(5, Character.codePointCount("ab\uD834x\uDD1Eef".toCharArray(), 1, 5));
        try {
            Character.codePointCount("abc", -1, 2);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        try {
            Character.codePointCount("abc", 1, 5);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        try {
            Character.codePointCount("abc", 2, 1);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        try {
            Character.codePointCount("abc".toCharArray(), -1, 2);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        try {
            Character.codePointCount("abc".toCharArray(), 1, 3);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        try {
            Character.codePointCount("abc".toCharArray(), 2, -1);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
    }

    public void testDigit() throws Exception {
        assertEquals(5, Character.digit((int) '5', 10));
        assertEquals(-1, Character.digit((int) 'z', 10));
    }

    public void testGetNumericValue() throws Exception {
        assertEquals(5, Character.getNumericValue((int) '5'));
    }

    public void testGetType() throws Exception {
        assertEquals(Character.DECIMAL_DIGIT_NUMBER, Character.getType((int) '5'));
    }

    public void testIsDefined() throws Exception {
        assertTrue(Character.isDefined((int) 'a'));
        assertFalse(Character.isDefined(0x50000));
    }

    public void testIsDigit() throws Exception {
        assertTrue(Character.isDigit((int) '5'));
        assertFalse(Character.isDigit((int) 'a'));
    }

    public void testIsHighSurrogate() throws Exception {
        assertTrue(Character.isHighSurrogate('\uD834'));
        assertFalse(Character.isHighSurrogate('a'));
    }

    public void testIsIdentifierIgnorable() throws Exception {
        assertTrue(Character.isIdentifierIgnorable(0x0085));
        assertFalse(Character.isIdentifierIgnorable((int) 'a'));
    }

    public void testIsISOControl() throws Exception {
        assertTrue(Character.isISOControl(9));
        assertFalse(Character.isISOControl((int) 'a'));
    }

    public void testIsJavaIdentifierPart() throws Exception {
        assertTrue(Character.isJavaIdentifierPart((int) 'a'));
        assertFalse(Character.isJavaIdentifierPart((int) '+'));
    }

    public void testIsJavaIdentifierStart() throws Exception {
        assertTrue(Character.isJavaIdentifierStart((int) 'a'));
        assertFalse(Character.isJavaIdentifierStart((int) '1'));
    }

    public void testIsLetter() throws Exception {
        assertTrue(Character.isLetter((int) 'a'));
        assertFalse(Character.isLetter((int) '1'));
    }

    public void testIsLetterOrDigit() throws Exception {
        assertTrue(Character.isLetterOrDigit((int) 'a'));
        assertTrue(Character.isLetterOrDigit((int) '5'));
        assertFalse(Character.isLetterOrDigit((int) '.'));
    }

    public void testIsLowerCase() throws Exception {
        assertTrue(Character.isLowerCase((int) 'a'));
        assertFalse(Character.isLowerCase((int) 'A'));
    }

    public void testIsLowSurrogate() throws Exception {
        assertTrue(Character.isLowSurrogate('\uDD1E'));
        assertFalse(Character.isLowSurrogate('\uD834'));
        assertFalse(Character.isLowSurrogate('a'));
    }

    public void testIsSpaceChar() throws Exception {
        assertTrue(Character.isSpaceChar((int) ' '));
        assertFalse(Character.isSpaceChar((int) 'x'));
    }

    public void testIsSupplementaryCodePoint() throws Exception {
        assertTrue(Character.isSupplementaryCodePoint(0x1D11E));
        assertFalse(Character.isSupplementaryCodePoint((int) 'a'));
        assertFalse(Character.isSupplementaryCodePoint(-100));
    }

    public void testIsSurrogatePair() throws Exception {
        assertTrue(Character.isSurrogatePair('\uD834', '\uDD1E'));
        assertFalse(Character.isSurrogatePair('\uDD1E', '\uD834'));
        assertFalse(Character.isSurrogatePair('a', 'b'));
    }

    public void testIsTitleCase() throws Exception {
        assertTrue(Character.isTitleCase(453));
        assertFalse(Character.isTitleCase((int) 'A'));
    }

    public void testIsUnicodeIdentifierPart() throws Exception {
        assertTrue(Character.isUnicodeIdentifierPart((int) '_'));
        assertFalse(Character.isUnicodeIdentifierPart((int) '.'));
    }

    public void testIsUnicodeIdentifierStart() throws Exception {
        assertTrue(Character.isUnicodeIdentifierStart((int) 'a'));
        assertFalse(Character.isUnicodeIdentifierStart((int) '.'));
    }

    public void testIsUpperCase() throws Exception {
        assertTrue(Character.isUpperCase((int) 'A'));
        assertFalse(Character.isUpperCase((int) 'a'));
    }

    public void testIsValidCodePoint() throws Exception {
        assertTrue(Character.isValidCodePoint((int) 'A'));
        assertFalse(Character.isValidCodePoint(0x1000000));
        assertFalse(Character.isValidCodePoint(-1));
    }

    public void testIsWhitespace() throws Exception {
        assertTrue(Character.isWhitespace(9));
        assertFalse(Character.isWhitespace((int) 'x'));
    }

    public void testOffsetByCodePoints() throws Exception {
        assertEquals(5, Character.offsetByCodePoints("abcdef", 2, 3));
        assertEquals(6, Character.offsetByCodePoints("ab\uD834\uDD1Eef", 2, 3));
        assertEquals(2, Character.offsetByCodePoints("ab\uD834\uDD1Eef", 4, -1));
        assertEquals(0, Character.offsetByCodePoints("\uDD1Eef", 3, -3));
        assertEquals(3, Character.offsetByCodePoints("ab\uD834", 1, 2));
        assertEquals(3, Character.offsetByCodePoints("abcdef".toCharArray(), 1, 3, 2, 1));
        assertEquals(1, Character.offsetByCodePoints("abc".toCharArray(), 1, 1, 2, -1));
        assertEquals(1, Character.offsetByCodePoints("ab\uD834\uDD1Eef".toCharArray(), 1, 4, 5, -3));
        try {
            Character.offsetByCodePoints("abc", -1, 3);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        try {
            Character.offsetByCodePoints("abc", 5, 0);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        try {
            Character.offsetByCodePoints("abc", 1, 3);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        try {
            Character.offsetByCodePoints("abc", 1, -2);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        try {
            Character.offsetByCodePoints("abc".toCharArray(), -1, 0, 1, 0);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        try {
            Character.offsetByCodePoints("abc".toCharArray(), 1, -1, 1, 0);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        try {
            Character.offsetByCodePoints("abc".toCharArray(), 1, 3, 1, 0);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        try {
            Character.offsetByCodePoints("abc".toCharArray(), 1, 2, 0, 0);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        try {
            Character.offsetByCodePoints("abc".toCharArray(), 1, 1, 1, 2);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        try {
            Character.offsetByCodePoints("abc".toCharArray(), 1, 1, 3, 0);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        try {
            Character.offsetByCodePoints("abc".toCharArray(), 1, 1, 2, -2);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
    }

    public void testReverseBytes() throws Exception {
        assertEquals(0x3412, Character.reverseBytes((char) 0x1234));
    }

    public void testToChars() throws Exception {
        assertEquals("a", new String(Character.toChars('a')));
        assertEquals("\uD840\uDC01", new String(Character.toChars(0x20001)));
        char[] chars = new char[10];
        assertEquals(1, Character.toChars('a', chars, 5));
        assertEquals('a', chars[5]);
        assertEquals(2, Character.toChars(0x20001, chars, 7));
        assertEquals('\uD840', chars[7]);
        assertEquals('\uDC01', chars[8]);
        try {
            Character.toChars(-100);
            fail();
        } catch (IllegalArgumentException e) {
            //ok
        }
        try {
            Character.toChars(0x1000000, chars, 0);
            fail();
        } catch (IllegalArgumentException e) {
            //ok
        }
    }

    public void testToCodePoint() throws Exception {
        assertEquals(0x1D11E, Character.toCodePoint('\uD834', '\uDD1E'));
        assertEquals(0x20001, Character.toCodePoint('\uD840', '\uDC01'));
    }

    public void testToLowerCase() throws Exception {
        assertEquals('a', Character.toLowerCase((int) 'A'));
        assertEquals('5', Character.toLowerCase((int) '5'));
    }

    public void testToTitleCase() throws Exception {
        assertEquals('A', Character.toTitleCase((int) 'a'));
        assertEquals('Z', Character.toTitleCase((int) 'Z'));
        assertEquals('5', Character.toTitleCase((int) '5'));
        assertEquals(453, Character.toTitleCase(454));
    }

    public void testToUpperCase() throws Exception {
        assertEquals('A', Character.toUpperCase((int) 'a'));
        assertEquals('Z', Character.toUpperCase((int) 'Z'));
        assertEquals(452, Character.toUpperCase(453));
        assertEquals(452, Character.toUpperCase(454));
    }

    public void testValueOf() throws Exception {
        Character character = 'a';
        assertEquals("a", String.valueOf(character));
    }

}