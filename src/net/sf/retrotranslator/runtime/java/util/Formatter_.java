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
package net.sf.retrotranslator.runtime.java.util;

import java.io.*;
import java.util.*;
import net.sf.retrotranslator.runtime.format.FormatContext;

/**
 * @author Taras Puchko
 */
public class Formatter_ implements Closeable, Flushable {

    private Appendable out;
    private Locale locale;
    private IOException ioException;

    public Formatter_() {
        this(null, Locale.getDefault());
    }

    public Formatter_(Appendable a) {
        this(a, Locale.getDefault());
    }

    public Formatter_(Locale l) {
        this(null, l);
    }

    public Formatter_(Appendable a, Locale l) {
        out = a != null ? a : new StringBuilder();
        locale = l;
    }

    public Formatter_(String fileName) throws FileNotFoundException {
        this(new FileOutputStream(fileName));
    }

    public Formatter_(String fileName, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        this(fileName, csn, Locale.getDefault());
    }

    public Formatter_(String fileName, String csn, Locale l) throws FileNotFoundException, UnsupportedEncodingException {
        this(new FileOutputStream(fileName), csn, l);
    }

    public Formatter_(File file) throws FileNotFoundException {
        this(new FileOutputStream(file));
    }

    public Formatter_(File file, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        this(file, csn, Locale.getDefault());
    }

    public Formatter_(File file, String csn, Locale l) throws FileNotFoundException, UnsupportedEncodingException {
        this(new FileOutputStream(file), csn, l);
    }

    public Formatter_(PrintStream ps) {
        this(assertNotNull(ps), Locale.getDefault());
    }

    public Formatter_(OutputStream os) {
        this(new BufferedWriter(new OutputStreamWriter(os)));
    }

    public Formatter_(OutputStream os, String csn) throws UnsupportedEncodingException {
        this(os, csn, Locale.getDefault());
    }

    public Formatter_(OutputStream os, String csn, Locale l) throws UnsupportedEncodingException {
        this(new BufferedWriter(new OutputStreamWriter(os, csn)), l);
    }

    public Locale locale() {
        assertOpen();
        return locale;
    }

    public Appendable out() {
        assertOpen();
        return out;
    }

    public String toString() {
        assertOpen();
        return out.toString();
    }

    public void flush() {
        assertOpen();
        if (out instanceof Flushable) {
            try {
                ((Flushable) out).flush();
            } catch (IOException e) {
                ioException = e;
            }
        }
    }

    public void close() {
        if (out == null) return;
        if (out instanceof Closeable) {
            try {
                ((Closeable) out).close();
            } catch (IOException e) {
                ioException = e;
            }
        }
        out = null;
    }

    public IOException ioException() {
        return ioException;
    }

    public Formatter_ format(String format, Object... args) {
        return format(locale, format, args);
    }

    public Formatter_ format(Locale locale, String format, Object... args) {
        assertOpen();
        new FormatterContext(locale).printf(format, args);
        return this;
    }

    private static Appendable assertNotNull(Appendable appendable) {
        if (appendable == null) throw new NullPointerException();
        return appendable;
    }

    private void assertOpen() {
        if (out == null) throw new FormatterClosedException();
    }

    private class FormatterContext extends FormatContext {

        public FormatterContext(Locale locale) {
            super(locale);
        }

        public void append(char c) {
            try {
                out.append(c);
            } catch (IOException e) {
                ioException = e;
            }
        }

        public void append(String s) {
            try {
                out.append(s);
            } catch (IOException e) {
                ioException = e;
            }
        }

        public void append(String s, int start, int end) {
            try {
                out.append(s, start, end);
            } catch (IOException e) {
                ioException = e;
            }
        }

        public boolean writeFormattable() {
            if (!(getArgument() instanceof Formattable_)) return false;
            Formatter_ formatter = Formatter_.this;
            if (getLocale() != formatter.locale) formatter = new Formatter_(out, getLocale());
            int formatFlags = 0;
            if (isFlag('-')) formatFlags |= FormattableFlags_.LEFT_JUSTIFY;
            if (isUpperCase()) formatFlags |= FormattableFlags_.UPPERCASE;
            if (isFlag('#')) formatFlags |= FormattableFlags_.ALTERNATE;
            ((Formattable_) getArgument()).formatTo(formatter, formatFlags, getWidth(), getPrecision());
            return true;
        }

    }

}
