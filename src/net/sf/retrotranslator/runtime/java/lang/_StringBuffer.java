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

import net.sf.retrotranslator.runtime.impl.Advanced;

/**
 * @author Taras Puchko
 */
public class _StringBuffer {

    public static String convertConstructorArguments(CharSequence s) {
        return s == null ? null : s.toString();
    }

    public static StringBuffer append(StringBuffer buffer, CharSequence s) {
        return s instanceof StringBuffer ? buffer.append((StringBuffer) s) : buffer.append((Object) s);
    }

    public static StringBuffer append(StringBuffer buffer, CharSequence s, int start, int end) {
        return append(buffer, s.subSequence(start, end));
    }

    public static StringBuffer appendCodePoint(StringBuffer buffer, int codePoint) {
        buffer.append(Character.toChars(codePoint));
        return buffer;
    }

    public static int codePointAt(StringBuffer buffer, int index) {
        synchronized (buffer) {
            return Character.codePointAt(buffer, index);
        }
    }

    public static int codePointBefore(StringBuffer buffer, int index) {
        synchronized (buffer) {
            return Character.codePointBefore(buffer, index);
        }
    }

    public static int codePointCount(StringBuffer buffer, int beginIndex, int endIndex) {
        synchronized (buffer) {
            return Character.codePointCount(buffer, beginIndex, endIndex);
        }
    }

    public static StringBuffer insert(StringBuffer buffer, int dstOffset, CharSequence s) {
        return buffer.insert(dstOffset, (Object) s);
    }

    public static StringBuffer insert(StringBuffer buffer, int dstOffset, CharSequence s, int start, int end) {
        return insert(buffer, dstOffset, s.subSequence(start, end));
    }

    public static int offsetByCodePoints(StringBuffer buffer, int index, int codePointOffset) {
        synchronized (buffer) {
            return Character.offsetByCodePoints(buffer, index, codePointOffset);
        }
    }

    @Advanced("StringBuffer.trimToSize")
    public static void trimToSize(StringBuffer buffer) {
        //do nothing
    }

}
