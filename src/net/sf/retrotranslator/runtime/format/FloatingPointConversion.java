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

import java.math.*;
import java.text.DecimalFormatSymbols;

/**
 * @author Taras Puchko
 */
abstract class FloatingPointConversion extends NumericConversion {

    protected abstract void printf(FormatContext context, boolean negative, BigDecimal argument);

    protected void printf(FormatContext context) {
        Object argument = context.getArgument();
        if (argument instanceof Double) {
            printf(context, (Double) argument);
        } else if (argument instanceof Float) {
            printf(context, (Float) argument);
        } else if (argument == null) {
            context.writeRestricted(String.valueOf(argument));
        } else if (argument instanceof BigDecimal) {
            BigDecimal bigDecimal = (BigDecimal) argument;
            printf(context, bigDecimal.signum() < 0, bigDecimal.abs());
        } else {
            throw context.getConversionException();
        }
    }

    private void printf(FormatContext context, double argument) {
        if (!printSpecialNumber(context, argument)) {
            printf(context, Double.doubleToLongBits(argument) < 0, BigDecimal.valueOf(Math.abs(argument)));
        }
    }

    protected static void printComputerizedScientificNumber(FormatContext context, boolean negative,
                                                            BigDecimal argument, int precision) {
        String unscaled = argument.unscaledValue().toString();
        StringBuilder builder = new StringBuilder();
        builder.append(unscaled.charAt(0));
        if (precision > 0 || context.isFlag('#')) {
            builder.append('.');
        }
        if (precision < unscaled.length()) {
            builder.append(unscaled.substring(1, precision + 1));
        } else {
            builder.append(unscaled.substring(1));
            appendZeros(builder, precision + 1 - unscaled.length());
        }
        int exponent = unscaled.equals("0") ? 0 : unscaled.length() - argument.scale() - 1;
        builder.append('e').append(exponent < 0 ? '-' : '+');
        int absoluteExponent = Math.abs(exponent);
        if (absoluteExponent < 10) {
            builder.append('0');
        }
        builder.append(absoluteExponent);
        printNumber(context, negative, null, builder, context.getSymbols(false));
    }

    protected static void printDecimalNumber(FormatContext context, boolean negative,
                                             BigDecimal argument, boolean localized) {
        String unscaled = argument.unscaledValue().toString();
        String integerPart = "0";
        String fractionPart = "";
        int separatorIndex = unscaled.length() - argument.scale();
        if (separatorIndex < 0) {
            fractionPart = appendZeros(new StringBuilder(), -separatorIndex).append(unscaled).toString();
        } else if (separatorIndex == 0) {
            fractionPart = unscaled;
        } else if (separatorIndex < unscaled.length()) {
            integerPart = unscaled.substring(0, separatorIndex);
            fractionPart = unscaled.substring(separatorIndex);
        } else if (separatorIndex == unscaled.length()) {
            integerPart = unscaled;
        } else {
            integerPart = appendZeros(new StringBuilder(unscaled), separatorIndex - unscaled.length()).toString();
        }
        StringBuilder builder = new StringBuilder();
        DecimalFormatSymbols symbols = context.getSymbols(localized);
        appendNumber(builder, integerPart, context.isFlag(','), symbols);
        if (fractionPart.length() > 0 || context.isFlag('#')) {
            builder.append(symbols.getDecimalSeparator());
        }
        appendNumber(builder, fractionPart, false, symbols);
        printNumber(context, negative, null, builder, symbols);
    }

    protected static StringBuilder appendZeros(StringBuilder builder, int count) {
        for (int i = 0; i < count; i++) {
            builder.append('0');
        }
        return builder;
    }

    public static class ComputerizedScientificConversion extends FloatingPointConversion {

        public void format(FormatContext context) {
            context.checkFlags();
            context.assertNoFlag(',');
            printf(context);
        }

        protected void printf(FormatContext context, boolean negative, BigDecimal argument) {
            int shift = argument.unscaledValue().toString().length() - context.getNumberPrecision() - 1;
            BigDecimal roundedArgument = argument.setScale(argument.scale() - shift, BigDecimal.ROUND_HALF_UP);
            printComputerizedScientificNumber(context, negative, roundedArgument, context.getNumberPrecision());
        }
    }

    public static class DecimalConversion extends FloatingPointConversion {

        public void format(FormatContext context) {
            context.checkWidth();
            context.checkFlags();
            printf(context);
        }

        protected void printf(FormatContext context, boolean negative, BigDecimal argument) {
            BigDecimal roundedArgument = argument.setScale(context.getNumberPrecision(), BigDecimal.ROUND_HALF_UP);
            printDecimalNumber(context, negative, roundedArgument, true);
        }
    }

    public static class GeneralScientificConversion extends FloatingPointConversion {

        public void format(FormatContext context) {
            context.checkFlags();
            context.assertNoFlag('#');
            printf(context);
        }

        protected void printf(FormatContext context, boolean negative, BigDecimal argument) {
            int precision = Math.max(context.getNumberPrecision(), 1);
            int shift = argument.unscaledValue().toString().length() - precision;
            BigDecimal roundedArgument = argument.setScale(argument.scale() - shift, BigDecimal.ROUND_HALF_UP);
            if (roundedArgument.compareTo(new BigDecimal(BigInteger.ONE, 4)) >= 0 &&
                    roundedArgument.compareTo(BigDecimal.ONE.movePointRight(precision)) < 0) {
                printDecimalNumber(context, negative, roundedArgument, context.getArgument() instanceof BigDecimal);
            } else {
                printComputerizedScientificNumber(context, negative, roundedArgument, precision - 1);
            }
        }
    }

}
