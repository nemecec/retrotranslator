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
package net.sf.retrotranslator.runtime.format;

import java.util.*;
import net.sf.retrotranslator.tests.BaseTestCase;

/**
 * @author Taras Puchko
 */
public class CharacterConversionTestCase extends BaseTestCase {

    public void testFormat() throws Exception {
        assertFormat("x", "%c", 'x');
        assertFormat("y", "%c", (byte) 'y');
        assertFormat("z", "%c", (short) 'z');
        assertFormat("i", "%c", (int) 'i');
        assertFormat("\uF123", "%c", '\uF123');
        assertFormat("\uD800\uDF00", "%c", 0x10300);
        if (!System.getProperty("java.vm.version").startsWith("1.5")) {
            assertFormatException(IllegalFormatCodePointException.class, "%c", (byte) '\u00F1');
            assertFormatException(IllegalFormatCodePointException.class, "%c", (short) '\uF123');
            assertFormatException(IllegalFormatCodePointException.class, "%c", 0x500000);
        }
        assertFormat("   null null", "%7c %1c", null, null);
        assertFormat("   x", "%4c", 'x');
        assertFormat("y  ", "%-3c", 'y');
        assertFormat("  z", "%3c", 'z');
        assertFormatException(MissingFormatWidthException.class, "%-c", 'x');
        assertFormatException(FormatFlagsConversionMismatchException.class, "%-#c", 'x');
        assertFormatException(FormatFlagsConversionMismatchException.class, "%#c", 'x');
        assertFormatException(IllegalFormatPrecisionException.class, "%#10.2c", 'x');
        assertFormatException(IllegalFormatPrecisionException.class, "%10.2c", 'x');
    }

}