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

import net.sf.retrotranslator.tests.TestCaseBase;
import java.io.*;

/**
 * @author Taras Puchko
 */
public class _PrintStreamJava14TestCase extends TestCaseBase {

    static class MyPrintStream extends PrintStream {
        public MyPrintStream(String fileName, String csn)
                throws FileNotFoundException, UnsupportedEncodingException {
            super(fileName, csn);
        }
    }

    public void testConstructors() throws Exception {
        File file = File.createTempFile("_PrintStreamTestCase", ".tmp");
        try {
            writeAndClose(new PrintStream(file, "UTF-16"), "test2");
            assertEquals("test2", readLine(file, "UTF-16"));

            writeAndClose(new PrintStream(file.getPath(), "UTF-16"), "test4");
            assertEquals("test4", readLine(file, "UTF-16"));

            writeAndClose(new _PrintStreamJava14TestCase.MyPrintStream(file.getPath(), "UTF-16"), "test6");
            assertEquals("test6", readLine(file, "UTF-16"));
        } finally {
            file.delete();
        }
    }

    private static void writeAndClose(PrintStream writer, String s) throws Exception {
        try {
            writer.print(s);
        } finally {
            writer.close();
        }
    }

}
