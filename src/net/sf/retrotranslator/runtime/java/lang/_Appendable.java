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

import java.io.*;
import java.nio.CharBuffer;
import net.sf.retrotranslator.runtime.java.io.*;
import net.sf.retrotranslator.runtime.java.nio._CharBuffer;

/**
 * @author Taras Puchko
 */
public class _Appendable {

    public static boolean executeInstanceOfInstruction(Object object) {
        return object instanceof StringBuffer ||
                object instanceof PrintStream ||
                object instanceof Writer ||
                object instanceof CharBuffer ||
                object instanceof Appendable_;
    }

    public static Object executeCheckCastInstruction(Object object) {
        if (object instanceof StringBuffer) {
            return (StringBuffer) object;
        }
        if (object instanceof PrintStream) {
            return (PrintStream) object;
        }
        if (object instanceof Writer) {
            return (Writer) object;
        }
        if (object instanceof CharBuffer) {
            return (CharBuffer) object;
        }
        return (Appendable_) object;
    }

    public static Object append(Object object, CharSequence csq) throws IOException {
        if (object instanceof StringBuffer) {
            return _StringBuffer.append((StringBuffer) object, csq);
        }
        if (object instanceof PrintStream) {
            return _PrintStream.append((PrintStream) object, csq);
        }
        if (object instanceof Writer) {
            return _Writer.append((Writer) object, csq);
        }
        if (object instanceof CharBuffer) {
            return _CharBuffer.append((CharBuffer) object, csq);
        }
        return ((Appendable_) object).append(csq);
    }

    public static Object append(Object object, CharSequence csq, int start, int end) throws IOException {
        if (object instanceof StringBuffer) {
            return _StringBuffer.append((StringBuffer) object, csq, start, end);
        }
        if (object instanceof PrintStream) {
            return _PrintStream.append((PrintStream) object, csq, start, end);
        }
        if (object instanceof Writer) {
            return _Writer.append((Writer) object, csq, start, end);
        }
        if (object instanceof CharBuffer) {
            return _CharBuffer.append((CharBuffer) object, csq, start, end);
        }
        return ((Appendable_) object).append(csq, start, end);
    }

    public static Object append(Object object, char c) throws IOException {
        if (object instanceof StringBuffer) {
            return ((StringBuffer) object).append(c);
        }
        if (object instanceof PrintStream) {
            return _PrintStream.append((PrintStream) object, c);
        }
        if (object instanceof Writer) {
            return _Writer.append((Writer) object, c);
        }
        if (object instanceof CharBuffer) {
            return _CharBuffer.append((CharBuffer) object, c);
        }
        return ((Appendable_) object).append(c);
    }

}
