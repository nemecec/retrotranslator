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

/**
 * @author Taras Puchko
 */
class HexadecimalExponentialConversion extends NumericConversion {

    public void format(FormatContext context) {
        context.checkFlags();
        context.assertNoFlag('(');
        context.assertNoFlag(',');
        Object argument = context.getArgument();
        if (argument instanceof Double) {
            printf(context, (Double) argument);
        } else if (argument instanceof Float) {
            printf(context, (Float) argument);
        } else if (argument == null) {
            context.writeRestricted(String.valueOf(argument));
        } else {
            throw context.getConversionException();
        }
    }

    private static void printf(FormatContext context, double argument) {
        if (!printSpecialNumber(context, argument)) {
            context.writePadded(toHex(context, argument));
        }
    }

    private static String toHex(FormatContext context, double argument) {
        StringBuilder builder = new StringBuilder();
        long bits = Double.doubleToLongBits(argument);
        if (bits < 0) {
            builder.append('-');
        } else if (context.isFlag('+')) {
            builder.append('+');
        }
        int exponent = getExponent(bits);
        int precision = context.getPrecision();
        if (argument != 0 && precision >= 0 && precision <= 12) {
            if (exponent == 0) {
                bits = Double.doubleToLongBits(argument * (1L << 52));
                exponent = getExponent(bits) - 52;
            }
            double value = Double.longBitsToDouble(getSignificand(bits));
            double factor = 1L << (52 - 4 * Math.max(precision, 1));
            bits = Double.doubleToLongBits(value / factor * factor);
            exponent += getExponent(bits) - 1;
        }
        builder.append(exponent == 0 ? "0x0." : "0x1.");
        appendSignificand(builder, bits, precision);
        return builder.append('p').append(argument == 0 ? 0 :
                exponent == 0 ? -1022 : exponent - 1023).toString();
    }

    private static int getExponent(long bits) {
        return (int) (bits << 1 >>> 53);
    }

    private static long getSignificand(long bits) {
        return bits & ((1L << 52) - 1) | 1L << 52;
    }

    private static void appendSignificand(StringBuilder builder, long bits, int precision) {
        String s = Long.toHexString(getSignificand(bits));
        int endIndex = s.length();
        while (endIndex > 2 && s.charAt(endIndex - 1) == '0') {
            endIndex--;
        }
        builder.append(s.substring(1, endIndex));
        for (int i = endIndex; i <= precision; i++) {
            builder.append('0');
        }
    }

}
