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

import java.math.BigDecimal;
import java.util.*;
import net.sf.retrotranslator.tests.BaseTestCase;

/**
 * @author Taras Puchko
 */
public class HexadecimalExponentialConversionTestCase extends BaseTestCase {

    public void testFormat() throws Exception {
        assertFormat("0x0.0p0", "%a", 0.0);
        assertFormat("+0x0.0p0", "%+a", 0.0);
        assertFormat("-0x0.0p0", "%a", -0.0);
        assertFormat("0x1.8p0", "%a", 1.5);
        assertFormat("+0x1.8p0", "%+a", 1.5);
        assertFormat("-0x1.8p0", "%a", -1.5);
        assertFormat("+0x1.8p0", "%+a", 1.5f);
        assertFormat("    0x0.00000p0", "%15.5a", 0.0);

        assertFormat("       NaN", "%10.2a", Double.NaN);
        assertFormat("Infinity", "%a", Double.POSITIVE_INFINITY);
        assertFormat("+Infinity", "%+a", Double.POSITIVE_INFINITY);
        assertFormat(" -Infinity", "%10.2a", Double.NEGATIVE_INFINITY);
        assertFormat("null", "%a", (Double) null);
        assertFormat(" n", "%2.1a", (Double) null);
        assertFormat("0X1.ABCDEFP1", "%A", 0x1.abcdef0p1);

        assertFormat("0x1.2345p1", "%a", 0x1.2345p1);
        assertFormat("0x1.2345p-1", "%a", 0x1.2345p-1);
        assertFormat("0x1.2p1", "%1.0a", 0x1.23456789p1);
        assertFormat("0x1.2p1", "%1.0a", 0x1.23456789p1);
        assertFormat("0x1.23456p1", "%1.5a", 0x1.23456789p1);
        assertFormat("0x1.2345679p1", "%1.7a", 0x1.23456789p1);
        assertFormat("-0x1.2345679p1", "%1.7a", -0x1.23456789p1);

        assertFormat("0x1.2345678p1", "%1.7a", 0x1.234567871p1);
        assertFormat("0x1.2345678p1", "%1.7a", 0x1.23456788p1);
        assertFormat("0x1.2345679p1", "%1.7a", 0x1.2345678801p1);
        assertFormat("-0x1.2345678p1", "%1.7a", -0x1.23456788p1);
        assertFormat("-0x1.2345678p1", "%1.7a", -0x1.23456788p1);
        assertFormat("-0x1.2345679p1", "%1.7a", -0x1.234567881p1);
        assertFormat("0x0.12345p-1022", "%a", 0x0.12345p-1022);
        assertFormat("0x1.23p-1026", "%1.2a", 0x0.12345p-1022);
        assertFormat("0x1.2p-1030", "%#1.0a", 0x0.012345p-1022);
        assertFormat("0x0.123450000000000p-1022", "%1.15a", 0x0.12345p-1022);
        assertFormat("     0x0.12345p-1022", "%20a", 0x0.12345p-1022);

        assertFormat("0x1.fffffffffffffp1023", "%a", Double.MAX_VALUE);
        assertFormat("0x1.0p1024", "%1.0a", Double.MAX_VALUE);
        assertFormat("-0x1.00000p1024", "%1.5a", -Double.MAX_VALUE);
        assertFormatException(FormatFlagsConversionMismatchException.class, "%(a");
        assertFormatException(FormatFlagsConversionMismatchException.class, "%,a");
        assertFormatException(IllegalFormatConversionException.class, "%a", BigDecimal.ONE);
        assertFormatException(IllegalFormatFlagsException.class, "%-012.3a");
        assertFormatException(IllegalFormatFlagsException.class, "%+ 12.3a");
    }

}