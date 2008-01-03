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
package net.sf.retrotranslator.runtime.java.io;

import java.io.*;
import java.util.*;

/**
 * @author Taras Puchko
 */
public class _PrintWriter {

    public static Writer convertConstructorArguments(File file) throws FileNotFoundException {
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
    }

    public static Writer convertConstructorArguments(File file, String csn)
            throws FileNotFoundException, UnsupportedEncodingException {
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), csn));
    }

    public static Writer convertConstructorArguments(String fileName) throws FileNotFoundException {
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName)));
    }

    public static Writer convertConstructorArguments(String fileName, String csn)
            throws FileNotFoundException, UnsupportedEncodingException {
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), csn));
    }

    public static PrintWriter append(PrintWriter writer, CharSequence csq) {
        writer.write(String.valueOf(csq));
        return writer;
    }

    public static PrintWriter append(PrintWriter writer, CharSequence csq, int start, int end) {
        writer.write(String.valueOf(csq).substring(start, end));
        return writer;
    }

    public static PrintWriter append(PrintWriter writer, char c) {
        writer.write(c);
        return writer;
    }

    public static PrintWriter format(PrintWriter printWriter, Locale locale, String format, Object... args) {
        new Formatter(printWriter, locale).format(format, args);
        printWriter.flush();
        return printWriter;
    }

    public static PrintWriter format(PrintWriter printWriter, String format, Object... args) {
        new Formatter(printWriter).format(format, args);
        printWriter.flush();
        return printWriter;
    }

    public static PrintWriter printf(PrintWriter printWriter, Locale locale, String format, Object... args) {
        return format(printWriter, locale, format, args);
    }

    public static PrintWriter printf(PrintWriter printWriter, String format, Object... args) {
        return format(printWriter, format, args);
    }

}
