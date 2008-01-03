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
import java.text.DecimalFormatSymbols;

/**
 * @author Taras Puchko
 */
class DecimalIntegralConversion extends NumericConversion {

    public void format(FormatContext context) {
        context.assertNoPrecision();
        context.assertNoFlag('#');
        context.checkWidth();
        context.checkFlags();
        Object argument = context.getArgument();
        if (argument instanceof Byte) {
            printf(context, (Byte) argument);
        } else if (argument instanceof Short) {
            printf(context, (Short) argument);
        } else if (argument instanceof Integer) {
            printf(context, (Integer) argument);
        } else if (argument instanceof Long) {
            printf(context, (Long) argument);
        } else if (argument instanceof BigInteger) {
            printf(context, (BigInteger) argument);
        } else if (argument == null) {
            context.writePadded(String.valueOf(argument));
        } else {
            throw context.getConversionException();
        }
    }

    private void printf(FormatContext context, long argument) {
        printf(context, BigInteger.valueOf(argument));
    }

    private void printf(FormatContext context, BigInteger argument) {
        DecimalFormatSymbols symbols = context.getSymbols(true);
        StringBuilder builder = new StringBuilder();
        appendNumber(builder, argument.abs().toString(), context.isFlag(','), symbols);
        printNumber(context, argument.signum() < 0, null, builder, symbols);
    }

}
