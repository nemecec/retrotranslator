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

import java.math.BigInteger;
import java.util.*;
import net.sf.retrotranslator.tests.TestCaseBase;

/**
 * @author Taras Puchko
 */
public class NondecimalIntegralConversionTestCase extends TestCaseBase {

    public void testFormat_Hex() throws Exception {
        assertFormat("64", "%x", 100);
        assertFormat("ffffff9c", "%x", -100);
        assertFormat("64", "%x", (byte) 100);
        assertFormat("9c", "%x", (byte) -100);
        assertFormat("64", "%x", (short) 100);
        assertFormat("ff9c", "%x", (short) -100);
        assertFormat("64", "%x", 100L);
        assertFormat("ffffffffffffff9c", "%x", -100L);
        assertFormat("f4240", "%x", BigInteger.valueOf(1000000));
        assertFormat("-f4240", "%x", BigInteger.valueOf(-1000000));

        assertFormat("00064", "%05x", 100);
        assertFormat("0x064", "%#05x", 100);
        assertFormat("0xffffff9c", "%#x", -100);
        assertFormat("0xffffffffffffff9c", "%#x", -100L);
        assertFormat("0x00ffffffffffffff9c", "%0#20x", -100L);
        assertFormat("ffffffffffffff9c    ", "%-20x", -100L);
        assertFormat("-0xf4240", "%#x", BigInteger.valueOf(-1000000));
        assertFormat("-0x00f4240", "%#010x", BigInteger.valueOf(-1000000));
        assertFormat("+0x00f4240", "%#+010x", BigInteger.valueOf(1000000));
        assertFormat(" 0x00f4240", "%# 010x", BigInteger.valueOf(1000000));
        assertFormat("  -0xf4240", "%#10x", BigInteger.valueOf(-1000000));
        assertFormat("-0xf4240  ", "%-#10x", BigInteger.valueOf(-1000000));
        assertFormat("(0xf4240)      ", "%-(#15x", BigInteger.valueOf(-1000000));
        assertFormat("       +0xf4240", "%+#15x", BigInteger.valueOf(1000000));
        assertFormat(" f4240", "% x", BigInteger.valueOf(1000000));
        assertFormat("-0XF4240", "%#X", BigInteger.valueOf(-1000000));

        assertFormatException(IllegalFormatPrecisionException.class, "%,01.2x");
        assertFormatException(MissingFormatWidthException.class, "%,-x");
        assertFormatException(MissingFormatWidthException.class, "%,0x");
        assertFormatException(FormatFlagsConversionMismatchException.class, "%,x");
        assertFormatException(MissingFormatArgumentException.class, "%(x");
        assertFormatException(IllegalFormatConversionException.class, "%(x", "z");
        assertFormatException(FormatFlagsConversionMismatchException.class, "%(x", 5);
        assertFormatException(FormatFlagsConversionMismatchException.class, "%+x", 5);
        assertFormatException(FormatFlagsConversionMismatchException.class, "% x", 5);
        assertFormatException(FormatFlagsConversionMismatchException.class, "%,x", 5);
        assertFormatException(FormatFlagsConversionMismatchException.class, "%,x", BigInteger.valueOf(5));
    }

    public void testFormat_Octal() throws Exception {
        assertFormat("144", "%o", 100);
        assertFormat("37777777634", "%o", -100);
        assertFormat("144", "%o", (byte) 100);
        assertFormat("234", "%o", (byte) -100);
        assertFormat("144", "%o", (short) 100);
        assertFormat("177634", "%o", (short) -100);
        assertFormat("144", "%o", 100L);
        assertFormat("1777777777777777777634", "%o", -100L);
        assertFormat("3641100", "%o", BigInteger.valueOf(1000000));
        assertFormat("-3641100", "%o", BigInteger.valueOf(-1000000));

        assertFormat("00144", "%05o", 100);
        assertFormat("0144", "%#o", 100);
        assertFormat("037777777634", "%#o", -100);
        assertFormat("01777777777777777777634", "%#o", -100L);
        assertFormat("0001777777777777777777634", "%0#25o", -100L);
        assertFormat("1777777777777777777634   ", "%-25o", -100L);
        assertFormat("-03641100", "%#o", BigInteger.valueOf(-1000000));
        assertFormat("-003641100", "%#010o", BigInteger.valueOf(-1000000));
        assertFormat("+003641100", "%#+010o", BigInteger.valueOf(1000000));
        assertFormat(" 003641100", "%# 010o", BigInteger.valueOf(1000000));
        assertFormat(" -03641100", "%#10o", BigInteger.valueOf(-1000000));
        assertFormat("-03641100 ", "%-#10o", BigInteger.valueOf(-1000000));
        assertFormat("(03641100)     ", "%-(#15o", BigInteger.valueOf(-1000000));
        assertFormat("      +03641100", "%+#15o", BigInteger.valueOf(1000000));
        assertFormat(" 3641100", "% o", BigInteger.valueOf(1000000));

        assertFormatException(UnknownFormatConversionException.class, "%#O");
        assertFormatException(IllegalFormatPrecisionException.class, "%,01.2o");
        assertFormatException(MissingFormatWidthException.class, "%,-o");
        assertFormatException(MissingFormatWidthException.class, "%,0o");
        assertFormatException(FormatFlagsConversionMismatchException.class, "%,o");
        assertFormatException(MissingFormatArgumentException.class, "%(o");
        assertFormatException(IllegalFormatConversionException.class, "%(o", "z");
        assertFormatException(FormatFlagsConversionMismatchException.class, "%(o", 5);
        assertFormatException(FormatFlagsConversionMismatchException.class, "%+o", 5);
        assertFormatException(FormatFlagsConversionMismatchException.class, "% o", 5);
        assertFormatException(FormatFlagsConversionMismatchException.class, "%,o", 5);
        assertFormatException(FormatFlagsConversionMismatchException.class, "%,o", BigInteger.valueOf(5));
    }

}