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
package net.sf.retrotranslator.runtime.java.text;

import java.math.BigDecimal;
import java.text.*;
import java.util.Locale;
import junit.framework.TestCase;

/**
 * @author Taras Puchko
 */
public class _DecimalFormatTestCase extends TestCase {

    public void testIsParseBigDecimal() throws Exception {
        DecimalFormat format = new DecimalFormat();
        assertFalse(format.isParseBigDecimal());
        format.setParseBigDecimal(true);
        assertTrue(format.isParseBigDecimal());
        format.setParseBigDecimal(false);
        assertFalse(format.isParseBigDecimal());
    }

    public void testSetParseBigDecimal() throws Exception {
        DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance(Locale.US);
        assertParse(new Double("1.2345"), format, "1.2345");
        assertSpecial(format);
        format.setParseBigDecimal(true);
        assertParse(new BigDecimal("1.2345"), format, "1.2345");
        assertSpecial(format);
    }

    private void assertSpecial(DecimalFormat format) throws ParseException {
        assertParse(Double.NaN, format, format.getDecimalFormatSymbols().getNaN());
        assertParse(Double.POSITIVE_INFINITY, format, format.getDecimalFormatSymbols().getInfinity());
        assertParse(Double.NEGATIVE_INFINITY, format, "-" + format.getDecimalFormatSymbols().getInfinity());
        assertParse(null, format, "unparsable");
    }

    private void assertParse(Object expected, DecimalFormat format, String source) throws ParseException {
        assertDecimalFormat(expected, format, source);
        assertNumberFormat(expected, format, source);
        assertFormat(expected, format, source);
    }

    private void assertDecimalFormat(Object expected, DecimalFormat format, String source) throws ParseException {
        assertEquals(expected, format.parse(source, new ParsePosition(0)));
        assertEquals(expected, format.parseObject(source, new ParsePosition(0)));
        if (expected != null) {
            assertEquals(expected, format.parse(source));
            assertEquals(expected, format.parseObject(source));
        } else {
            try {
                format.parse(source);
                fail();
            } catch (ParseException e) {
                //ok
            }
            try {
                format.parseObject(source);
                fail();
            } catch (ParseException e) {
                //ok
            }
        }
    }

    private void assertNumberFormat(Object expected, NumberFormat format, String source) throws ParseException {
        assertEquals(expected, format.parse(source, new ParsePosition(0)));
        assertEquals(expected, format.parseObject(source, new ParsePosition(0)));
        if (expected != null) {
            assertEquals(expected, format.parse(source));
            assertEquals(expected, format.parseObject(source));
        } else {
            try {
                format.parse(source);
                fail();
            } catch (ParseException e) {
                //ok
            }
            try {
                format.parseObject(source);
                fail();
            } catch (ParseException e) {
                //ok
            }
        }
    }

    private void assertFormat(Object expected, Format format, String source) throws ParseException {
        assertEquals(expected, format.parseObject(source, new ParsePosition(0)));
        if (expected != null) {
            assertEquals(expected, format.parseObject(source));
        } else {
            try {
                format.parseObject(source);
                fail();
            } catch (ParseException e) {
                //ok
            }
        }
    }

}