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
package net.sf.retrotranslator.runtime.format;

import java.util.*;
import net.sf.retrotranslator.tests.BaseTestCase;

/**
 * @author Taras Puchko
 */
public class GeneralConversionTestCase extends BaseTestCase {

    public void testFormat_Boolean() throws Exception {
        assertFormat("     false", "%10b", null, null);
        assertFormat("      FALS", "%10.4B", null, null);
        assertFormat("true", "%b", Boolean.TRUE);
        assertFormat("false", "%b", Boolean.FALSE);
        assertFormat("true", "%b", "abc");
        assertFormatException(FormatFlagsConversionMismatchException.class, "%#b", "abc");
    }

    public void testFormat_Hash() throws Exception {
        assertFormat("     false", "%10b", null, null);
        assertFormat("      FALS", "%10.4B", null, null);
        assertFormat("true", "%b", Boolean.TRUE);
        assertFormat("false", "%b", Boolean.FALSE);
        assertFormat("true", "%b", "abc");
        assertFormatException(FormatFlagsConversionMismatchException.class, "%#b", "abc");
        assertFormatException(FormatFlagsConversionMismatchException.class, "%-#b", "abc");
    }

    static class MyFormattable implements Formattable {
        public void formatTo(Formatter formatter, int flags, int width, int precision) {
            formatter.format("%s", "my[" + flags + "," + width + "," +
                    precision + "," + formatter.locale() + "]");
        }
    }

    public void testFormat_String() throws Exception {
        assertFormat("abc", "%s", "abc");
        assertFormat("        ab", "%10.2s", "abc");
        assertFormat("ab        ", "%-10.2s", "abc");
        assertFormat("abc       ", "%-10.5s", "abc");
        assertFormat("x", "%#s", "x");
        assertFormatException(MissingFormatWidthException.class, "%-s", "x");
        assertFormat("my[0,-1,-1,fr_FR]", "%s", new MyFormattable());
        assertFormat("my[1,5,2,fr_FR]", "%-5.2s", new MyFormattable());
        assertFormat("my[2,1,4,fr_FR]", "%1.4S", new MyFormattable());
        assertFormat("my[4,1,7,fr_FR]", "%#1.7s", new MyFormattable());
    }
}