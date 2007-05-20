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
package net.sf.retrotranslator.runtime.java.text;

import java.math.BigDecimal;
import java.text.*;
import net.sf.retrotranslator.runtime.impl.*;

/**
 * @author Taras Puchko
 */
public class _DecimalFormat {

    private static final WeakIdentityTable<DecimalFormat, _DecimalFormat> formats =
            new WeakIdentityTable<DecimalFormat, _DecimalFormat>() {
                protected _DecimalFormat initialValue() {
                    return new _DecimalFormat();
                }
            };

    private boolean parseBigDecimal;

    protected _DecimalFormat() {
    }

    public static boolean isParseBigDecimal(DecimalFormat format) {
        _DecimalFormat value = formats.lookup(format);
        return value != null && value.parseBigDecimal;
    }

    @Advanced("DecimalFormat.setParseBigDecimal")
    public static void setParseBigDecimal(DecimalFormat format, boolean newValue) {
        formats.obtain(format).parseBigDecimal = newValue;
    }

    @Advanced("DecimalFormat.setParseBigDecimal")
    public static Number parse(DecimalFormat format, String source) throws ParseException {
        return correctNumber(format, format.parse(source));
    }

    @Advanced("DecimalFormat.setParseBigDecimal")
    public static Number parse(DecimalFormat format, String source, ParsePosition parsePosition) {
        return correctNumber(format, format.parse(source, parsePosition));
    }

    @Advanced("DecimalFormat.setParseBigDecimal")
    public static Object parseObject(DecimalFormat format, String source) throws ParseException {
        return correctObject(format, format.parseObject(source));
    }

    @Advanced("DecimalFormat.setParseBigDecimal")
    public static Object parseObject(DecimalFormat format, String source, ParsePosition pos) {
        return correctObject(format, format.parseObject(source, pos));
    }

    private static Number correctNumber(DecimalFormat format, Number number) {
        if (number == null || !isParseBigDecimal(format)) {
            return number;
        }
        if (number instanceof Double) {
            Double result = (Double) number;
            if (result.isInfinite() || result.isNaN()) {
                return result;
            }
        }
        return new BigDecimal(number.toString());
    }

    private static Object correctObject(DecimalFormat format, Object object) {
        return object instanceof Number ? correctNumber(format, (Number) object) : object;
    }

    protected static Number fixNumber(Format format, Number number) {
        return format instanceof DecimalFormat ? correctNumber((DecimalFormat) format, number) : number;
    }

    protected static Object fixObject(Format format, Object object) {
        return object instanceof Number ? fixNumber(format, (Number) object) : object;
    }

}
