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
import java.util.Locale;
import net.sf.retrotranslator.tests.TestCaseBase;

/**
 * @author Taras Puchko
 */
public class _PrintWriterTestCase extends TestCaseBase {

    public void testConstructors() throws Exception {
        File file = File.createTempFile("_PrintWriterTestCase", ".tmp");
        try {
            writeAndClose(new PrintWriter(file), "test1");
            assertEquals("test1", readLine(file, null));

            writeAndClose(new PrintWriter(file, "UTF-16"), "test2");
            assertEquals("test2", readLine(file, "UTF-16"));

            writeAndClose(new PrintWriter(file.getPath()), "test3");
            assertEquals("test3", readLine(file, null));

            writeAndClose(new PrintWriter(file.getPath(), "UTF-16"), "test4");
            assertEquals("test4", readLine(file, "UTF-16"));
        } finally {
            file.delete();
        }
    }

    private static void writeAndClose(PrintWriter writer, String s) throws Exception {
        try {
            writer.write(s);
        } finally {
            writer.close();
        }
    }

    public void testFormat() throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(stream, true);
        writer.format("Hello, %s!", "World");
        writer.format(Locale.FRANCE, " - %f", 1.2);
        assertEquals("Hello, World! - 1,200000", stream.toString());
    }

    public void testPrintf() throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(stream, true);
        writer.printf("Hello, %s!", "World");
        writer.printf(Locale.FRANCE, " - %,d", 1000000);
        assertEquals("Hello, World! - 1\u00a0000\u00a0000", stream.toString());
    }
}