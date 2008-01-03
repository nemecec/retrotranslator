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

/**
 * @author Taras Puchko
 */
public class _Character {

    private static final int SURROGATE_MASK = 0x3FF;
    private static final Character[] cache = new Character[128];

    static {
        for (int i = 0; i < cache.length; i++) {
            cache[i] = new Character((char) i);
        }
    }

    public static int charCount(int codePoint) {
        return codePoint < Character.MIN_SUPPLEMENTARY_CODE_POINT ? 1 : 2;
    }

    public static int codePointAt(char[] chars, int index) {
        return codePointAt(chars, index, chars.length);
    }

    public static int codePointAt(char[] chars, int index, int limit) {
        if (index >= limit || limit < 0 || limit > chars.length) {
            throw new IndexOutOfBoundsException();
        }
        char highChar = chars[index];
        if (isHighSurrogate(highChar) && ++index < limit) {
            char lowChar = chars[index];
            if (isLowSurrogate(lowChar)) {
                return toCodePoint(highChar, lowChar);
            }
        }
        return highChar;
    }

    public static int codePointAt(CharSequence sequence, int index) {
        char highChar = sequence.charAt(index);
        if (isHighSurrogate(highChar) && ++index < sequence.length()) {
            char lowChar = sequence.charAt(index);
            if (isLowSurrogate(lowChar)) {
                return toCodePoint(highChar, lowChar);
            }
        }
        return highChar;
    }

    public static int codePointBefore(char[] chars, int index) {
        return codePointBefore(chars, index, 0);
    }

    public static int codePointBefore(char[] chars, int index, int start) {
        if (index <= start || start < 0 || start >= chars.length) {
            throw new IndexOutOfBoundsException();
        }
        char lowChar = chars[--index];
        if (isLowSurrogate(lowChar) && index > start) {
            char highChar = chars[index - 1];
            if (isHighSurrogate(highChar)) {
                return toCodePoint(highChar, lowChar);
            }
        }
        return lowChar;
    }

    public static int codePointBefore(CharSequence sequence, int index) {
        char lowChar = sequence.charAt(--index);
        if (isLowSurrogate(lowChar) && index > 0) {
            char highChar = sequence.charAt(index - 1);
            if (isHighSurrogate(highChar)) {
                return toCodePoint(highChar, lowChar);
            }
        }
        return lowChar;
    }

    public static int codePointCount(char[] chars, int offset, int count) {
        int endIndex = offset + count;
        if (offset < 0 || offset > endIndex || endIndex > chars.length) {
            throw new IndexOutOfBoundsException();
        }
        int result = 0;
        for (int i = offset; i < endIndex; result++) {
            if (isHighSurrogate(chars[i++]) && i < endIndex && isLowSurrogate(chars[i])) {
                i++;
            }
        }
        return result;
    }

    public static int codePointCount(CharSequence sequence, int beginIndex, int endIndex) {
        if (beginIndex < 0 || beginIndex > endIndex || endIndex > sequence.length()) {
            throw new IndexOutOfBoundsException();
        }
        int result = 0;
        for (int i = beginIndex; i < endIndex; result++) {
            if (isHighSurrogate(sequence.charAt(i++)) && i < endIndex && isLowSurrogate(sequence.charAt(i))) {
                i++;
            }
        }
        return result;
    }

    public static int digit(int codePoint, int radix) {
        return isBasic(codePoint) ? Character.digit((char) codePoint, radix) : -1;
    }

    public static byte getDirectionality(int codePoint) {
        return isBasic(codePoint) ? Character.getDirectionality((char) codePoint) : Character.DIRECTIONALITY_UNDEFINED;
    }

    public static int getNumericValue(int codePoint) {
        return isBasic(codePoint) ? Character.getNumericValue((char) codePoint) : -1;
    }

    public static int getType(int codePoint) {
        return isBasic(codePoint) ? Character.getType((char) codePoint) : Character.UNASSIGNED;
    }

    public static boolean isDefined(int codePoint) {
        return isBasic(codePoint) && Character.isDefined((char) codePoint);
    }

    public static boolean isDigit(int codePoint) {
        return isBasic(codePoint) && Character.isDigit((char) codePoint);
    }

    public static boolean isHighSurrogate(char aChar) {
        return aChar >= Character.MIN_HIGH_SURROGATE && aChar <= Character.MAX_HIGH_SURROGATE;
    }

    public static boolean isIdentifierIgnorable(int codePoint) {
        return isBasic(codePoint) && Character.isIdentifierIgnorable((char) codePoint);
    }

    public static boolean isISOControl(int codePoint) {
        return isBasic(codePoint) && Character.isISOControl((char) codePoint);
    }

    public static boolean isJavaIdentifierPart(int codePoint) {
        return isBasic(codePoint) && Character.isJavaIdentifierPart((char) codePoint);
    }

    public static boolean isJavaIdentifierStart(int codePoint) {
        return isBasic(codePoint) && Character.isJavaIdentifierStart((char) codePoint);
    }

    public static boolean isLetter(int codePoint) {
        return isBasic(codePoint) && Character.isLetter((char) codePoint);
    }

    public static boolean isLetterOrDigit(int codePoint) {
        return isBasic(codePoint) && Character.isLetterOrDigit((char) codePoint);
    }

    public static boolean isLowerCase(int codePoint) {
        return isBasic(codePoint) && Character.isLowerCase((char) codePoint);
    }

    public static boolean isLowSurrogate(char aChar) {
        return aChar >= Character.MIN_LOW_SURROGATE && aChar <= Character.MAX_LOW_SURROGATE;
    }

    public static boolean isMirrored(int codePoint) {
        return isBasic(codePoint) && Character.isMirrored((char) codePoint);
    }

    public static boolean isSpaceChar(int codePoint) {
        return isBasic(codePoint) && Character.isSpaceChar((char) codePoint);
    }

    public static boolean isSupplementaryCodePoint(int codePoint) {
        return codePoint >= Character.MIN_SUPPLEMENTARY_CODE_POINT && codePoint <= Character.MAX_CODE_POINT;
    }

    public static boolean isSurrogatePair(char highChar, char lowChar) {
        return isHighSurrogate(highChar) && isLowSurrogate(lowChar);
    }

    public static boolean isTitleCase(int codePoint) {
        return isBasic(codePoint) && Character.isTitleCase((char) codePoint);
    }

    public static boolean isUnicodeIdentifierPart(int codePoint) {
        return isBasic(codePoint) && Character.isUnicodeIdentifierPart((char) codePoint);
    }

    public static boolean isUnicodeIdentifierStart(int codePoint) {
        return isBasic(codePoint) && Character.isUnicodeIdentifierStart((char) codePoint);
    }

    public static boolean isUpperCase(int codePoint) {
        return isBasic(codePoint) && Character.isUpperCase((char) codePoint);
    }

    public static boolean isValidCodePoint(int codePoint) {
        return codePoint >= Character.MIN_CODE_POINT && codePoint <= Character.MAX_CODE_POINT;
    }

    public static boolean isWhitespace(int codePoint) {
        return isBasic(codePoint) && Character.isWhitespace((char) codePoint);
    }

    public static int offsetByCodePoints(char[] chars, int start, int count, int index, int codePointOffset) {
        int endIndex = start + count;
        if (start < 0 || start > endIndex || endIndex > chars.length || index < start || index > endIndex) {
            throw new IndexOutOfBoundsException();
        }
        if (codePointOffset >= 0) {
            for (int i = 0; i < codePointOffset; i++) {
                if (index >= endIndex) {
                    throw new IndexOutOfBoundsException();
                }
                if (isHighSurrogate(chars[index++]) && index < endIndex && isLowSurrogate(chars[index])) {
                    index++;
                }
            }
        } else {
            for (int i = codePointOffset; i < 0; i++) {
                if (index <= start) {
                    throw new IndexOutOfBoundsException();
                }
                if (isLowSurrogate(chars[--index]) && index > start && isHighSurrogate(chars[index - 1])) {
                    index--;
                }
            }
        }
        return index;
    }

    public static int offsetByCodePoints(CharSequence sequence, int index, int codePointOffset) {
        if (index < 0 || index > sequence.length()) {
            throw new IndexOutOfBoundsException();
        }
        if (codePointOffset >= 0) {
            for (int i = 0; i < codePointOffset; i++) {
                if (isHighSurrogate(sequence.charAt(index++)) &&
                        index < sequence.length() && isLowSurrogate(sequence.charAt(index))) {
                    index++;
                }
            }
        } else {
            for (int i = codePointOffset; i < 0; i++) {
                if (isLowSurrogate(sequence.charAt(--index)) &&
                        index > 0 && isHighSurrogate(sequence.charAt(index - 1))) {
                    index--;
                }
            }
        }
        return index;
    }

    public static char reverseBytes(char ch) {
        return (char) (ch >> 8 & 0xFF | ch << 8);
    }

    public static char[] toChars(int codePoint) {
        char[] chars = new char[charCount(codePoint)];
        toChars(codePoint, chars, 0);
        return chars;
    }

    public static int toChars(int codePoint, char[] chars, int index) {
        if (!isValidCodePoint(codePoint)) {
            throw new IllegalArgumentException();
        }
        if (codePoint < Character.MIN_SUPPLEMENTARY_CODE_POINT) {
            chars[index] = (char) codePoint;
            return 1;
        } else {
            chars[index] = (char) (((codePoint - Character.MIN_SUPPLEMENTARY_CODE_POINT) >>> 10) |
                    Character.MIN_HIGH_SURROGATE);
            chars[index + 1] = (char) ((codePoint & SURROGATE_MASK) | Character.MIN_LOW_SURROGATE);
            return 2;
        }
    }

    public static int toCodePoint(char highChar, char lowChar) {
        return ((highChar & SURROGATE_MASK) << 10 | (lowChar & SURROGATE_MASK)) +
                Character.MIN_SUPPLEMENTARY_CODE_POINT;
    }

    public static int toLowerCase(int codePoint) {
        return isBasic(codePoint) ? Character.toLowerCase((char) codePoint) : codePoint;
    }

    public static int toTitleCase(int codePoint) {
        return isBasic(codePoint) ? Character.toTitleCase((char) codePoint) : codePoint;
    }

    public static int toUpperCase(int codePoint) {
        return isBasic(codePoint) ? Character.toUpperCase((char) codePoint) : codePoint;
    }

    public static Character valueOf(char c) {
        return c <= 127 ? cache[c] : new Character(c);
    }

    private static boolean isBasic(int codePoint) {
        return codePoint >= 0 && codePoint < Character.MIN_SUPPLEMENTARY_CODE_POINT;
    }

}
