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
package net.sf.retrotranslator.runtime.java.lang;

import net.sf.retrotranslator.runtime.java.io._Writer;
import net.sf.retrotranslator.runtime.java.io._PrintStream;
import net.sf.retrotranslator.runtime.java.nio._CharBuffer;

import java.io.IOException;
import java.io.Writer;
import java.io.PrintStream;
import java.nio.CharBuffer;

/**
 * @author Taras Puchko
 */
public class _Appendable {

    public static Appendable append(Appendable appendable, CharSequence csq) throws IOException {
        if (appendable instanceof StringBuffer) {
            return _StringBuffer.append((StringBuffer) appendable, csq);
        }
        if (appendable instanceof PrintStream) {
            return _PrintStream.append((PrintStream) appendable, csq);
        }
        if (appendable instanceof Writer) {
            return _Writer.append((Writer) appendable, csq);
        }
        if (appendable instanceof CharBuffer) {
            return _CharBuffer.append((CharBuffer) appendable, csq);
        }
        return appendable.append(csq);
    }

    public static Appendable append(Appendable appendable, CharSequence csq, int start, int end) throws IOException {
        if (appendable instanceof StringBuffer) {
            return _StringBuffer.append((StringBuffer) appendable, csq, start, end);
        }
        if (appendable instanceof PrintStream) {
            return _PrintStream.append((PrintStream) appendable, csq, start, end);
        }
        if (appendable instanceof Writer) {
            return _Writer.append((Writer) appendable, csq, start, end);
        }
        if (appendable instanceof CharBuffer) {
            return _CharBuffer.append((CharBuffer) appendable, csq, start, end);
        }
        return appendable.append(csq, start, end);
    }

    public static Appendable append(Appendable appendable, char c) throws IOException {
        if (appendable instanceof StringBuffer) {
            return ((StringBuffer) appendable).append(c);
        }
        if (appendable instanceof PrintStream) {
            return _PrintStream.append((PrintStream) appendable, c);
        }
        if (appendable instanceof Writer) {
            return _Writer.append((Writer) appendable, c);
        }
        if (appendable instanceof CharBuffer) {
            return _CharBuffer.append((CharBuffer) appendable, c);
        }
        return appendable.append(c);
    }

}
