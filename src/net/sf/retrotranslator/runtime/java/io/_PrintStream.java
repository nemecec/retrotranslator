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
package net.sf.retrotranslator.runtime.java.io;

import java.io.*;
import java.util.*;

/**
 * @author Taras Puchko
 */
public class _PrintStream {

    public static class PrintStreamBuilder {

        private OutputStream out;
        private String encoding;

        protected PrintStreamBuilder(OutputStream out, String encoding) {
            this.out = out;
            this.encoding = encoding;
        }

        public OutputStream argument1() {
            return out;
        }

        public boolean argument2() {
            return false;
        }

        public String argument3() {
            return encoding;
        }
    }

    public static OutputStream convertConstructorArguments(File file) throws FileNotFoundException {
        return new FileOutputStream(file);
    }

    public static OutputStream convertConstructorArguments(String fileName) throws FileNotFoundException {
        return new FileOutputStream(fileName);
    }

    public static PrintStreamBuilder createInstanceBuilder(File file, String csn) throws FileNotFoundException {
        return new PrintStreamBuilder(new FileOutputStream(file), csn);
    }

    public static PrintStreamBuilder createInstanceBuilder(String fileName, String csn) throws FileNotFoundException {
        return new PrintStreamBuilder(new FileOutputStream(fileName), csn);
    }

    public static PrintStream append(PrintStream printStream, CharSequence csq) {
        printStream.print(csq);
        return printStream;
    }

    public static PrintStream append(PrintStream printStream, CharSequence csq, int start, int end) {
        printStream.print(String.valueOf(csq).substring(start, end));
        return printStream;
    }

    public static PrintStream append(PrintStream printStream, char c) {
        printStream.print(c);
        return printStream;
    }

    public static PrintStream format(PrintStream printStream, Locale locale, String format, Object... args) {
        new Formatter(printStream, locale).format(format, args);
        return printStream;
    }

    public static PrintStream format(PrintStream printStream, String format, Object... args) {
        new Formatter(printStream).format(format, args);
        return printStream;
    }

    public static PrintStream printf(PrintStream printStream, Locale locale, String format, Object... args) {
        return format(printStream, locale, format, args);
    }

    public static PrintStream printf(PrintStream printStream, String format, Object... args) {
        return format(printStream, format, args);
    }

}
