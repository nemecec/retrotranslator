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
import net.sf.retrotranslator.tests.BaseTestCase;

/**
 * @author Taras Puchko
 */
public class DecimalIntegralConversionTestCase extends BaseTestCase {

    public void testFormat() throws Exception {
        assertFormat("5", "%d", 5);
        assertFormat("-5", "%d", -5);
        assertFormat("   5", "%4d", 5);
        assertFormat("  -5", "%4d", -5);
        assertFormat("5   ", "%-4d", 5);
        assertFormat("-5  ", "%-4d", -5);
        assertFormat("  +5", "%+4d", 5);
        assertFormat("  -5", "%+4d", -5);
        assertFormat("   5", "% 4d", 5);
        assertFormat("  -5", "% 4d", -5);
        assertFormat("   5", "%(4d", 5);
        assertFormat(" (5)", "%(4d", -5);
        assertFormat("0005", "%04d", 5);
        assertFormat("-005", "%04d", -5);
        assertFormat("-005", "%04d", BigInteger.valueOf(-5));

        assertFormat("1234567890", "%d", 1234567890);
        assertFormat("1\u00a0234\u00a0567\u00a0890", "%,d", 1234567890);

        assertFormat(HINDI, "\u0967,\u0968\u0969\u096a,\u096b\u096c\u096d,\u096e\u096f\u0966", "%,d", 1234567890);

        assertFormat("1\u00a0234", "%,d", BigInteger.valueOf(1234));
        assertFormat(" null", "%5d", (Object) null);
        assertFormat("(5) ", "%-(4d", -5);
        assertFormat("-128", "%d", Byte.MIN_VALUE);
        assertFormat("-32768", "%d", Short.MIN_VALUE);
        assertFormat("-2147483648", "%d", Integer.MIN_VALUE);
        assertFormat("-9223372036854775808", "%d", Long.MIN_VALUE);
        assertFormat("-1134474760533137424384", "%d",
                BigInteger.valueOf(Long.MIN_VALUE).multiply(BigInteger.valueOf(123)));
        assertFormat("10", "%d", BigInteger.valueOf(10));

        assertFormatException(IllegalFormatPrecisionException.class, "%4.1d", 5);
        assertFormatException(FormatFlagsConversionMismatchException.class, "%#4d", 5);
        assertFormatException(UnknownFormatConversionException.class, "%D", 5);
        assertFormatException(IllegalFormatFlagsException.class, "%+ d", 5);
        assertFormatException(MissingFormatWidthException.class, "%-0d", 5);
        assertFormatException(IllegalFormatFlagsException.class, "%-05d", 5);
    }

}