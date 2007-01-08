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
package net.sf.retrotranslator.runtime.format;

import java.text.DecimalFormatSymbols;

/**
 * @author Taras Puchko
 */
abstract class NumericConversion extends Conversion {

    protected static void appendNumber(StringBuilder builder, String number,
                                       boolean groupingUsed, DecimalFormatSymbols symbols) {
        for (int i = 0; i < number.length(); i++) {
            if (groupingUsed && i > 0 && (number.length() - i) % 3 == 0) {
                builder.append(symbols.getGroupingSeparator());
            }
            builder.append((char) (symbols.getZeroDigit() - '0' + number.charAt(i)));
        }
    }

    protected static boolean printSpecialNumber(FormatContext context, double argument) {
        if (Double.isNaN(argument)) {
            context.writePadded("NaN");
            return true;
        }
        if (argument == Double.POSITIVE_INFINITY) {
            context.writePadded(context.isFlag('+') ? "+Infinity" : "Infinity");
            return true;
        }
        if (argument == Double.NEGATIVE_INFINITY) {
            context.writePadded(context.isFlag('(') ? "(Infinity)" : "-Infinity");
            return true;
        }
        return false;
    }

    protected static void printNumber(FormatContext context, boolean negative, String prefix,
                                      StringBuilder argument, DecimalFormatSymbols symbols) {
        StringBuilder builder = new StringBuilder();
        if (negative) {
            if (context.isFlag('(')) {
                builder.append('(');
                argument.append(')');
            } else {
                builder.append('-');
            }
        } else {
            if (context.isFlag('+')) {
                builder.append('+');
            } else if (context.isFlag(' ')) {
                builder.append(' ');
            }
        }
        if (prefix != null) {
            builder.append(prefix);
        }
        if (context.isFlag('0')) {
            int count = context.getWidth() - builder.length() - argument.length();
            for (int i = 0; i < count; i++) {
                builder.append(symbols.getZeroDigit());
            }
        }
        context.writePadded(builder.append(argument).toString());
    }

}
