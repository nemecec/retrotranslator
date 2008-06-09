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
import net.sf.retrotranslator.tests.TestCaseBase;

/**
 * @author Taras Puchko
 */
public class FloatingPointConversionTestCase extends TestCaseBase {

    public void testFormat_ComputerizedScientific() throws Exception {
        assertFormat("0.000000e+00", "%e", 0.0);
        assertFormat("-0.000000e+00", "%e", -0.0);
        assertFormat("0.000000e+00", "%e", BigDecimal.ZERO);
        assertFormat("1.234500e+02", "%e", 123.45);
        assertFormat("-1.234500e+02", "%e", -123.45);
        assertFormat("1.234500e+02", "%e", BigDecimal.valueOf(123.45));
        assertFormat("-1.234500e+02", "%e", BigDecimal.valueOf(-123.45));

        assertFormat("1.5e-01", "%1.1e", 0.145);
        assertFormat("1.5E-01", "%1.1E", 0.145);
        assertFormat("-1.5e-01", "%1.1e", -0.145);
        assertFormat("1.5e-01", "%1.1e", BigDecimal.valueOf(0.145));
        assertFormat("-1.5e-01", "%1.1e", BigDecimal.valueOf(-0.145));
        assertFormat("1.234500e+02", "%e", 123.45f);
        assertFormat("0.0e+00", "%.1e", 0f);
        assertFormat("0e+00", "%.0e", 0f);
        assertFormat("1.230000e+02", "%e", 123f);
        assertFormat("1.230000e-01", "%e", 0.123);

        assertFormat("1.234568e+242", "%e", 123456789e234);
        assertFormat("  1.235e+242", "%12.3e", 123456789e234);
        assertFormat("      1e+242", "%12.0e", 123456789e234);
        assertFormat("1e+242      ", "%-12.0e", 123456789e234);
        assertFormat("001.235e+242", "%012.3e", 123456789e234);
        assertFormat("-01.235e+242", "%012.3e", -123456789e234);
        assertFormat("(0001.235e+242)", "%(015.3e", -123456789e234);
        assertFormat("+00001.235e+242", "%+015.3e", 123456789e234);
        assertFormat("+00001.235e+242", "%+(015.3e", 123456789e234);
        assertFormat(" 00001.235e+242", "% 015.3e", 123456789e234);
        assertFormat(HINDI, "1.234568e+06", "%e", 1234567.8f);

        assertFormat("2.76701161105643274210e+19", "%1.20e",
                BigDecimal.valueOf(Long.MAX_VALUE).multiply(BigDecimal.valueOf(3)));
        assertFormat("1.e+00", "%#.0e", 1f);

        assertFormat("            NaN", "% 015.2e", Double.NaN);
        assertFormat("       Infinity", "% 015.2e", Double.POSITIVE_INFINITY);
        assertFormat("      -Infinity", "% 015.2e", Double.NEGATIVE_INFINITY);
        assertFormat("             nu", "% 015.2e", (Object) null);
        assertFormatException(FormatFlagsConversionMismatchException.class, "%,e");
        assertFormatException(IllegalFormatConversionException.class, "%e", "x");
        assertFormatException(IllegalFormatFlagsException.class, "%-012.3e");
        assertFormatException(IllegalFormatFlagsException.class, "%+ 12.3e");
        assertFormatException(IllegalFormatFlagsException.class, "%+ ,12.3e");
    }

    public void testFormat_Decimal() throws Exception {
        assertFormat("0,000000", "%f", 0.0);
        assertFormat("-0,000000", "%f", -0.0);
        assertFormat("0,000000", "%f", BigDecimal.ZERO);
        assertFormat("12,345000", "%f", 12.345);
        assertFormat("-12,345000", "%f", -12.345);
        assertFormat("12,345000", "%f", BigDecimal.valueOf(12.345));
        assertFormat("-12,345000", "%f", BigDecimal.valueOf(-12.345));
        assertFormat("0,001230", "%f", BigDecimal.valueOf(0.00123));

        if (isJava14AtLeast()) {
            assertFormat("1230000000000", "%1.0f", new BigDecimal("123e10"));
        }
        
        assertFormat("0,0", "%.1f", 0f);
        assertFormat("0", "%.0f", 0f);
        assertFormat("0,", "%#.0f", 0f);
        assertFormat("12,35", "%1.2f", 12.345);
        assertFormat("    +12,35", "%+10.2f", 12.345);
        assertFormat("    -12,35", "%+10.2f", -12.345);
        assertFormat(" 12,35", "% 1.2f", 12.345);
        assertFormat("-12,35", "% 1.2f", -12.345);
        assertFormat("0000012,35", "%010.2f", 12.345);
        assertFormat("-000012,35", "%010.2f", -12.345);
        assertFormat("     12,35", "%(10.2f", 12.345);
        assertFormat("   (12,35)", "%(10.2f", -12.345);
        assertFormat("(12,35)   ", "%(-10.2f", -12.345);
        assertFormat("-12,35    ", "%- 10.2f", -12.345);
        assertFormat("+000012,35", "%+010.2f", 12.345);
        assertFormat("-000012,35", "%+010.2f", -12.345);

        assertFormat("1234567936,000000", "%f", 1234567890f);
        assertFormat("1\u00a0234\u00a0567\u00a0936,000000", "%,f", 1234567890f);
        assertFormat("1\u00a0234\u00a0567\u00a0936,000000", "%,f", BigDecimal.valueOf(1234567890f));
        assertFormat("-1\u00a0234\u00a0567\u00a0890,123457", "%,f", -1234567890.123456789d);
        assertFormat("27670116110564327421,000000", "%f",
                BigDecimal.valueOf(Long.MAX_VALUE).multiply(BigDecimal.valueOf(3)));

        if (isJava14AtLeast()) {
            assertFormat(HINDI, "\u0967,\u0968\u0969\u096a,\u096b\u096c\u096d.\u096e", "%,1.1f", 1234567.8f);
        }

        assertFormat("NaN            ", "%-15.2f", Double.NaN);
        assertFormat("Infinity       ", "%-15.2f", Double.POSITIVE_INFINITY);
        assertFormat("-Infinity      ", "%-15.2f", Double.NEGATIVE_INFINITY);
        assertFormat("(Infinity)", "%(f", Double.NEGATIVE_INFINITY);
        assertFormat("nu             ", "%-15.2f", (Object) null);
        assertFormatException(MissingFormatWidthException.class, "%0f");
        assertFormatException(MissingFormatWidthException.class, "%-f");
        assertFormatException(MissingFormatWidthException.class, "%-0f");
        assertFormatException(UnknownFormatConversionException.class, "%F");
        assertFormatException(IllegalFormatConversionException.class, "%f", "x");
        assertFormatException(IllegalFormatFlagsException.class, "%-012.3f");
        assertFormatException(IllegalFormatFlagsException.class, "%+ 12.3f");

        assertFormat("1234567936,000", "%.3f", 1234567890f);
        assertFormatException(UnknownFormatConversionException.class, "%.f");
    }

    public void testFormat_GeneralScientific() throws Exception {
        assertFormat("12.3450", "%g", 12.345f);
        assertFormat("12.3450", "%g", 12.345d);
        assertFormat("12,3450", "%g", BigDecimal.valueOf(12.345d));
        assertFormat(HINDI, "12.3450", "%g", 12.345d);
        if (isJava14AtLeast()) {
            assertFormat(HINDI, "\u0967\u0968.\u0969\u096a\u096b\u0966", "%g", BigDecimal.valueOf(12.345d));
        }

        assertFormat("1", "%1.0g", 1.2345f);
        assertFormat("1e+01", "%1.0g", 12.345f);
        assertFormat("1e+01", "%1.1g", 12.345f);
        assertFormat("12", "%1.2g", 12.345f);
        assertFormat("12.3", "%1.3g", 12.345f);
        assertFormat("+12.3", "%+1.3g", 12.345f);
        assertFormat("-12.3", "%+1.3g", -12.345f);

        assertFormat("1234567890", "%1.10g", 1234567890d);
        assertFormat("1,234,567,890", "%,1.10g", 1234567890d);
        assertFormat("+1,234,567,890", "%+,1.10g", 1234567890d);
        assertFormat(" 1,234,567,890", "% ,1.10g", 1234567890d);
        assertFormat("1.23456789e+09", "%,1.9g", 1234567890d);
        assertFormat("(1234567890)", "%(1.10g", -1234567890d);
        assertFormat("(1.23456789e+09)", "%(1.9g", -1234567890d);

        assertFormat("12345679", "%1.8g", 12345678.5d);
        assertFormat("1.234568e+07", "%1.7g", 12345678.5d);
        assertFormat("1.234568E+07", "%1.7G", 12345678.5d);
        assertFormat("1.2300000e-05", "%1.8g", 0.0000123);
        assertFormat("0.00012300000", "%1.8g", 0.000123);
        assertFormat("0.00012", "%1.2g", 0.000123);
        assertFormat("0.0001", "%1.0g", 0.000123);
        assertFormat("    0.0001", "%10.0g", 0.000123);
        assertFormat("0.0001    ", "%-10.0g", 0.000123);
        assertFormat("1.23e+04", "%1.3g", BigDecimal.valueOf(12345.6789));
        assertFormat(HINDI, "1.23e+04", "%1.3g", BigDecimal.valueOf(12345.6789));

        assertFormat("12\u00a0345,68", "%,1.7g", BigDecimal.valueOf(12345.6789));
        assertFormat("12,345.68", "%,1.7g", 12345.6789);
        assertFormat("2.767011611056432742e+19", "%1.19g",
                BigDecimal.valueOf(Long.MAX_VALUE).multiply(BigDecimal.valueOf(3)));
        assertFormat("27670116110564327421", "%1.20g",
                BigDecimal.valueOf(Long.MAX_VALUE).multiply(BigDecimal.valueOf(3)));

        assertFormat("NAN", "%1.1G", Double.NaN);
        assertFormat("INFINITY", "%1.1G", Double.POSITIVE_INFINITY);
        assertFormat("+Infinity", "%+1.1g", Double.POSITIVE_INFINITY);
        assertFormat("-Infinity", "%1.1g", Double.NEGATIVE_INFINITY);
        assertFormat("(Infinity)", "%(1.1g", Double.NEGATIVE_INFINITY);
        assertFormat("   NULL", "%7.5G", (Object) null);
        assertFormatException(IllegalFormatConversionException.class, "%g", "x");
        assertFormatException(IllegalFormatFlagsException.class, "%-012.3g");
        assertFormatException(IllegalFormatFlagsException.class, "%+ 12.3g");
        assertFormatException(IllegalFormatFlagsException.class, "%#+ 12.3g", 0d);
        assertFormatException(FormatFlagsConversionMismatchException.class, "%#12.3g", 0d);
    }

}