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
package net.sf.retrotranslator.runtime.java.math;

import java.math.*;
import net.sf.retrotranslator.runtime.impl.Advanced;

/**
 * @author Taras Puchko
 */
public class _BigDecimal {

    public static final BigDecimal ZERO = BigDecimal.valueOf(0);
    public static final BigDecimal ONE = BigDecimal.valueOf(1);
    public static final BigDecimal TEN = BigDecimal.valueOf(10);

    public static BigInteger convertConstructorArguments(int value) {
        return BigInteger.valueOf(value);
    }

    public static BigInteger convertConstructorArguments(long value) {
        return BigInteger.valueOf(value);
    }

    public static String convertConstructorArguments(char[] in, int offset, int len) {
        return new String(in, offset, len);
    }

    public static String convertConstructorArguments(char[] in) {
        return new String(in);
    }

    public static BigDecimal[] divideAndRemainder(BigDecimal dividend, BigDecimal divisor) {
        BigDecimal[] result = new BigDecimal[2];
        BigDecimal quotient = divideToIntegralValue(dividend, divisor);
        result[0] = quotient;
        result[1] = dividend.subtract(quotient.multiply(divisor));
        return result;
    }

    public static BigDecimal divideToIntegralValue(BigDecimal dividend, BigDecimal divisor) {
        BigDecimal quotient = dividend.divide(divisor, 0, BigDecimal.ROUND_DOWN);
        if (dividend.scale() > divisor.scale()) {
            quotient = quotient.setScale(dividend.scale() - divisor.scale());
        }
        return quotient;
    }

    public static BigDecimal pow(BigDecimal bigDecimal, int n) {
        if (n < 0 || n > 999999999) {
            throw new ArithmeticException("Invalid operation");
        }
        BigDecimal result = ONE;
        while (n-- > 0) {
            result = result.multiply(bigDecimal);
        }
        return result;
    }

    public static BigDecimal remainder(BigDecimal dividend, BigDecimal divisor) {
        return dividend.subtract(divideToIntegralValue(dividend, divisor).multiply(divisor));
    }

    @Advanced("BigDecimal.setScale")
    public static BigDecimal setScale(BigDecimal bigDecimal, int newScale, int roundingMode) {
        if (newScale >= 0) {
            return bigDecimal.setScale(newScale, roundingMode);
        }
        return bigDecimal.movePointRight(newScale).setScale(0, roundingMode).movePointLeft(newScale);
    }

    public static String toPlainString(BigDecimal bigDecimal) {
        return bigDecimal.toString();
    }

    public static BigDecimal valueOf(double val) {
        return new BigDecimal(Double.toString(val));
    }

    public static BigDecimal valueOf(long val) {
        return BigDecimal.valueOf(val, 0);
    }


}
