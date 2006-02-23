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

import java.math.BigInteger;
import java.util.FormatFlagsConversionMismatchException;
import java.util.IllegalFormatConversionException;
import java.util.IllegalFormatFlagsException;

/**
 * @author Taras Puchko
 */
public abstract class IntegralConversion extends NumericConversion {

    public void format(FormatContext context) {
        context.denyPrecision();
        Object argument = context.getArgument();
        if (argument == null) {
            context.writePadded(String.valueOf(argument));
        } else if (argument instanceof BigInteger) {
            format(context, (BigInteger) argument);
        } else {
            format(context, parse(context, argument));
        }
    }

    private long parse(FormatContext context, Object argument) {
        if (argument instanceof Byte) return fixNegative((Byte) argument, 8);
        if (argument instanceof Short) return fixNegative(((Short) argument), 16);
        if (argument instanceof Integer) return fixNegative(((Integer) argument), 32);
        if (argument instanceof Long) return fixNegative(((Long) argument), 64);
        throw new IllegalFormatConversionException(context.getConversionType(), argument.getClass());
    }

    protected abstract long fixNegative(long argument, int size);

    protected abstract void format(FormatContext context, long argument);

    protected abstract void format(FormatContext context, BigInteger argument);

    public static class DecimalConversion extends IntegralConversion {

        protected long fixNegative(long argument, int size) {
            return argument;
        }

        protected void format(FormatContext context, long argument) {
            format(context, Long.toString(argument));
        }

        protected void format(FormatContext context, BigInteger argument) {
            format(context, argument.toString());
        }

        private void format(FormatContext context, String value) {
            if (context.isFlag('#')) {
                throw new FormatFlagsConversionMismatchException(context.flags, context.getConversionType());
            }
            StringBuilder builder = new StringBuilder();
            if (value.charAt(0) == '-') {
                value = value.substring(1);
                if (context.isFlag('(')) {
                    builder.append('(');
                } else if (context.isFlag('-')) {
                    builder.append('-');
                }
            } else {
                if (context.isFlag(' ')) {
                    if (context.isFlag('+')) throw new IllegalFormatFlagsException(context.flags);
                    builder.append(' ');
                } else if (context.isFlag('+')) {
                    builder.append('+');
                }
            }
        }

    }

    public static class NonDecimalConversion extends IntegralConversion {

        private int radix;
        private String radixIndicator;

        protected NonDecimalConversion(int radix, String radixIndicator) {
            this.radix = radix;
            this.radixIndicator = radixIndicator;
        }

        protected long fixNegative(long argument, int size) {
            return argument >= 0 ? argument : argument + (1L << size);//todo
        }

        protected void format(FormatContext context, long argument) {
            if (context.isFlag('(') || context.isFlag(' ') || context.isFlag('+') || context.isFlag(',')) {
                throw new FormatFlagsConversionMismatchException(context.flags, context.getConversionType());
            }
            format(context, new StringBuilder(), Long.toString(argument, radix));
        }

        protected void format(FormatContext context, BigInteger argument) {
            if (context.isFlag(',')) {
                throw new FormatFlagsConversionMismatchException(context.flags, context.getConversionType());
            }
            format(context, new StringBuilder(argument.signum() >= 0 ? "+" : ""), argument.toString(radix));
        }

        private void format(FormatContext context, StringBuilder builder, String value) {
            if (context.isFlag('#')) builder.append(radixIndicator);
            if (context.isFlag('0')) {
                int padding = context.width - value.length() - builder.length();
                while (padding-- > 0) builder.append('0');
            }
            context.writePadded(builder.append(value).toString());
        }

    }

}
