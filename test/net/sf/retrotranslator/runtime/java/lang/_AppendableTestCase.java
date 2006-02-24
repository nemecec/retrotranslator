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

import junit.framework.*;

import java.util.List;
import java.util.Arrays;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.io.IOException;
import java.nio.CharBuffer;

/**
 * @author Taras Puchko
 */
public class _AppendableTestCase extends TestCase {

    private static class AppendableWrapper implements Appendable {

        private Appendable appendable;

        public AppendableWrapper(Appendable appendable) {
            this.appendable = appendable;
        }

        public Appendable append(CharSequence csq) throws IOException {
            appendable.append(csq);
            return this;
        }

        public Appendable append(CharSequence csq, int start, int end) throws IOException {
            appendable.append(csq, start, end);
            return this;
        }

        public Appendable append(char c) throws IOException {
            appendable.append(c);
            return this;
        }

        public String toString() {
            return appendable.toString();
        }
    }

    public void testAppend() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Appendable> list = Arrays.asList(new Appendable[] {
                new StringBuffer(),
                new PrintStream(out),
                new StringWriter(),
                CharBuffer.wrap(new char[10]),
                new AppendableWrapper(new StringBuffer())
        });
        for (Appendable appendable : list) {
            assertSame(appendable, appendable.append("abc").append("xyz", 1, 2).append('0'));
            String s;
            if (appendable instanceof PrintStream) {
                s = out.toString();
            } else if (appendable instanceof CharBuffer) {
                CharBuffer buffer = ((CharBuffer) appendable);
                buffer.limit(buffer.position());
                buffer.position(0);
                s = buffer.toString();
            } else {
                s = appendable.toString();
            }
            assertEquals("abcy0", s);
        }
    }
}