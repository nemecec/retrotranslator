/***
 * Retrotranslator: a Java bytecode transformer that translates Java classes
 * compiled with JDK 5.0 into classes that can be run on JVM 1.4.
 * 
 * Copyright (c) 2005, 2006 Taras Puchko
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
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.util.FormatFlagsConversionMismatchException;
import java.util.IllegalFormatConversionException;

/**
 * @author Taras Puchko
 */
public abstract class FloatingPointConversion extends NumericConversion {

    public void format(FormatContext context) {
        checkFlags(context);
        Object argument = context.getArgument();
        if (argument instanceof BigDecimal) {
            format(context, (BigDecimal) argument);
        } else if (argument instanceof Float) {
            format(context, (Float) argument);
        } else if (argument instanceof Double) {
            format(context, (Double) argument);
        } else if (argument == null) {
            context.writeRestricted(String.valueOf(argument));
        } else {
            throw new IllegalFormatConversionException(context.getConversionType(), argument.getClass());
        }
    }

    protected abstract void checkFlags(FormatContext context);

    protected abstract void format(FormatContext context, BigDecimal argument);

    protected abstract void format(FormatContext context, double argument);


    public static class ComputerizedScientificConversion extends FloatingPointConversion {

        protected void checkFlags(FormatContext context) {
            if (context.isFlag(',')) {
                throw new FormatFlagsConversionMismatchException(context.flags, context.getConversionType());
            }
        }

        protected void format(FormatContext context, BigDecimal argument) {
            writeScientificNumber(context, getFormat(context).format(argument, new StringBuffer(), new FieldPosition(0)));
        }

        protected void format(FormatContext context, double argument) {
            writeScientificNumber(context, getFormat(context).format(argument, new StringBuffer(), new FieldPosition(0)));
        }

        private void writeScientificNumber(FormatContext context, StringBuffer buffer) {
            int index = buffer.indexOf(context.getExponentialSymbol());
            buffer.setCharAt(index, 'e');
            if (buffer.charAt(index + 1) == context.getSymbols().getMinusSign()) {
                buffer.setCharAt(index + 1, '-');
            } else {
                buffer.insert(index + 1, '+');
            }
            writeNumber(context, buffer);
        }

        private DecimalFormat getFormat(FormatContext context) {
            DecimalFormat format = context.getDecimalFormat();
            StringBuffer pattern = new StringBuffer();
            pattern.append('0');
            int precision = context.getNumberPrecision();
            if (precision > 0) pattern.append('.');
            while (precision-- > 0) pattern.append('0');
            pattern.append("E00");
            format.applyPattern(pattern.toString());
            return format;
        }

    }

    public static class DecimalConversion extends FloatingPointConversion {

        protected void checkFlags(FormatContext context) {
        }

        protected void format(FormatContext context, BigDecimal argument) {
            writeNumber(context, getFormat(context).format(argument, new StringBuffer(), new FieldPosition(0)));
        }

        protected void format(FormatContext context, double argument) {
            writeNumber(context, getFormat(context).format(argument, new StringBuffer(), new FieldPosition(0)));
        }

        private DecimalFormat getFormat(FormatContext context) {
            DecimalFormat format = context.getDecimalFormat();
            format.setMinimumFractionDigits(context.getNumberPrecision());
            format.setMaximumFractionDigits(context.getNumberPrecision());
            format.setGroupingUsed(context.isFlag(','));
            return format;
        }

    }

    public static class GeneralScientificConversion extends FloatingPointConversion {

        protected void checkFlags(FormatContext context) {
        }

        protected void format(FormatContext context, BigDecimal argument) {
            
        }

        protected void format(FormatContext context, double argument) {
        }
    }

    public static class HexadecimalExponentialConversion extends FloatingPointConversion {

        protected void checkFlags(FormatContext context) {
        }

        protected void format(FormatContext context, BigDecimal argument) {
        }

        protected void format(FormatContext context, double argument) {
        }
    }

    private static void writeNumber(FormatContext context, StringBuffer buffer) {
        replaceFirst(buffer, context.getSymbols().getInfinity(), "Infinity");
        replaceFirst(buffer, context.getSymbols().getNaN(), "NaN");
        int position = 0;
        if (buffer.charAt(0) == context.getSymbols().getMinusSign()) {
            position = 1;
            if (context.isFlag('(')) {
                buffer.setCharAt(0, '(');
                buffer.append(')');
            } else {
                buffer.setCharAt(0, '-');
            }
        } else {
            if (context.isFlag('+')) {
                position = 1;
                buffer.insert(0, '+');
            } else if (context.isFlag(' ')) {
                position = 1;
                buffer.insert(0, ' ');
            }
        }
        if (context.isFlag('0')) {
            while (context.width > buffer.length()) buffer.insert(position, context.getSymbols().getZeroDigit());
        }
        context.writePadded(buffer.toString());
    }

    private static void replaceFirst(StringBuffer buffer, String target, String replacement) {
        int i = buffer.indexOf(target);
        if (i >= 0) buffer.replace(i, i + target.length(), replacement);
    }

}
