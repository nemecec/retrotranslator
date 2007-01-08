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

import java.io.*;
import java.util.*;
import net.sf.retrotranslator.tests.BaseTestCase;

/**
 * @author Taras Puchko
 */
public class Formatter_TestCase extends BaseTestCase {

    public void testFormatter_1() throws Exception {
        Formatter formatter = new Formatter();
        assertTrue(formatter.out() instanceof StringBuilder);
        assertEquals(Locale.getDefault(), formatter.locale());
    }

    public void testFormatter_2() throws Exception {
        StringBuffer buffer = new StringBuffer();
        Formatter formatter = new Formatter(buffer);
        assertSame(buffer, formatter.out());
        assertEquals(Locale.getDefault(), formatter.locale());
    }

    public void testFormatter_3() throws Exception {
        Formatter formatter = new Formatter(Locale.CANADA_FRENCH);
        assertTrue(formatter.out() instanceof StringBuilder);
        assertSame(Locale.CANADA_FRENCH, formatter.locale());
    }

    public void testFormatter_4() throws Exception {
        StringBuffer buffer = new StringBuffer();
        Formatter formatter = new Formatter(buffer, Locale.CANADA_FRENCH);
        assertSame(buffer, formatter.out());
        assertSame(Locale.CANADA_FRENCH, formatter.locale());
    }

    public void testFormatter_5() throws Exception {
        File file = getFile();
        Formatter formatter = new Formatter(file.getPath());
        assertEquals(Locale.getDefault(), formatter.locale());
        assertToFile(formatter, file);
    }

    public void testFormatter_6() throws Exception {
        File file = getFile();
        Formatter formatter = new Formatter(file.getPath(), "UTF-8");
        assertEquals(Locale.getDefault(), formatter.locale());
        assertToFile(formatter, file);
    }

    public void testFormatter_7() throws Exception {
        File file = getFile();
        Formatter formatter = new Formatter(file.getPath(), "UTF-8", Locale.CANADA_FRENCH);
        assertSame(Locale.CANADA_FRENCH, formatter.locale());
        assertToFile(formatter, file);
    }

    public void testFormatter_8() throws Exception {
        File file = getFile();
        Formatter formatter = new Formatter(file);
        assertEquals(Locale.getDefault(), formatter.locale());
        assertToFile(formatter, file);
    }

    public void testFormatter_9() throws Exception {
        File file = getFile();
        Formatter formatter = new Formatter(file, "UTF-8");
        assertEquals(Locale.getDefault(), formatter.locale());
        assertToFile(formatter, file);
    }

    public void testFormatter_10() throws Exception {
        File file = getFile();
        Formatter formatter = new Formatter(file, "UTF-8", Locale.CANADA_FRENCH);
        assertSame(Locale.CANADA_FRENCH, formatter.locale());
        assertToFile(formatter, file);
    }

    public void testFormatter_11() throws Exception {
        File file = getFile();
        Formatter formatter = new Formatter(new PrintStream(new FileOutputStream(file)));
        assertEquals(Locale.getDefault(), formatter.locale());
        assertToFile(formatter, file);
    }

    public void testFormatter_12() throws Exception {
        File file = getFile();
        Formatter formatter = new Formatter(new FileOutputStream(file));
        assertEquals(Locale.getDefault(), formatter.locale());
        assertToFile(formatter, file);
    }

    public void testFormatter_13() throws Exception {
        File file = getFile();
        Formatter formatter = new Formatter(new FileOutputStream(file), "UTF-8");
        assertEquals(Locale.getDefault(), formatter.locale());
        assertToFile(formatter, file);
    }

    public void testFormatter_14() throws Exception {
        File file = getFile();
        Formatter formatter = new Formatter(new FileOutputStream(file), "UTF-8", Locale.CANADA_FRENCH);
        assertSame(Locale.CANADA_FRENCH, formatter.locale());
        assertToFile(formatter, file);
    }

    private File getFile() throws IOException {
        File file = File.createTempFile("fmt", "tmp");
        file.deleteOnExit();
        return file;
    }

    private void assertToFile(Formatter formatter, File file) throws Exception {
        try {
            formatter.format("%s", "Hello");
        } finally {
            formatter.close();
        }
        BufferedReader reader = new BufferedReader(new FileReader(file));
        try {
            assertEquals("Hello", reader.readLine());
        } finally {
            reader.close();
        }
    }

    static class MyWriter extends StringWriter {

        private boolean flushed;
        private boolean closed;

        public void flush() {
            flushed = true;
            super.flush();
        }

        public void close() throws IOException {
            closed = true;
            super.close();
        }
    }

    public void testFlush() throws Exception {
        new Formatter().flush();
        MyWriter writer = new MyWriter();
        Formatter formatter = new Formatter(writer);
        formatter.format("%s", "x");
        assertFalse(writer.flushed);
        formatter.flush();
        assertTrue(writer.flushed);
        assertFalse(writer.closed);
    }

    public void testClose() throws Exception {
        new Formatter().close();
        MyWriter writer = new MyWriter();
        Formatter formatter = new Formatter(writer);
        formatter.format("%s", "x");
        assertFalse(writer.closed);
        formatter.close();
        assertTrue(writer.closed);
        assertFalse(writer.flushed);
    }

    public void testIoException() throws Exception {
        Formatter formatter = new Formatter(new Appendable() {

            public Appendable append(CharSequence csq) throws IOException {
                throw new IOException("test");
            }

            public Appendable append(CharSequence csq, int start, int end) throws IOException {
                throw new IOException("test");
            }

            public Appendable append(char c) throws IOException {
                throw new IOException("test");
            }
        });
        formatter.format("%s", "Hello");
        assertEquals("test", formatter.ioException().getMessage());
    }

    public void testClosed() throws Exception {
        Formatter formatter = new Formatter();
        formatter.close();
        try {
            formatter.locale();
            fail();
        } catch (FormatterClosedException e) {
            //ok
        }
        try {
            formatter.out();
            fail();
        } catch (FormatterClosedException e) {
            //ok
        }
        try {
            formatter.toString();
            fail();
        } catch (FormatterClosedException e) {
            //ok
        }
        try {
            formatter.flush();
            fail();
        } catch (FormatterClosedException e) {
            //ok
        }
        try {
            formatter.format("");
            fail();
        } catch (FormatterClosedException e) {
            //ok
        }
    }

    public void testFormat() throws Exception {
        assertFormat("AxByC", "A%sB%sC", "x", "y");
        assertFormat("AxBxC", "A%sB%<sC", "x");
        assertFormat("AyByC", "A%2$sB%<sC", "x", "y");
        assertFormat("AyBxCyD", "A%2$sB%sC%sD", "x", "y");
        assertFormat("AyBxC", "A%2$sB%1$sC", "x", "y");
        assertFormatException(MissingFormatWidthException.class, "%-%", "x");
        assertFormatException(IllegalFormatPrecisionException.class, "%1$10.4%", "x");
        assertFormatException(IllegalFormatPrecisionException.class, "%1$.3n", "x");
        assertFormatException(IllegalFormatWidthException.class, "%1$10n", "x");
        assertFormatException(UnknownFormatConversionException.class, "A%qB", "x");
        assertFormatException(UnknownFormatConversionException.class, "A%", "x");
        assertFormatException(MissingFormatArgumentException.class, "A%<sB", "x");
        assertFormatException(MissingFormatArgumentException.class, "A%3$sB", "x");
    }
}