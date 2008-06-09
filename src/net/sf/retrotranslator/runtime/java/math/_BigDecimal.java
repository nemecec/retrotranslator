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
import net.sf.retrotranslator.registry.Advanced;

/**
 * @author Taras Puchko
 */
public class _BigDecimal {

    public static final BigDecimal ZERO = BigDecimal.valueOf(0);
    public static final BigDecimal ONE = BigDecimal.valueOf(1);
    public static final BigDecimal TEN = BigDecimal.valueOf(10);

    private static final BigInteger[] FIVE_POWERS = new BigInteger[32];

    static {
        FIVE_POWERS[0] = BigInteger.valueOf(5);
        for (int i = 1; i < FIVE_POWERS.length; i++) {
            FIVE_POWERS[i] = FIVE_POWERS[i - 1].multiply(FIVE_POWERS[0]);
        }
    }

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

    public static BigDecimal divide(BigDecimal dividend, BigDecimal divisor) {
        BigInteger p = dividend.unscaledValue();
        BigInteger q = divisor.unscaledValue();
        if (q.signum() == 0) {
            throw new ArithmeticException("Division by zero");
        }
        long preferredScale = (long) dividend.scale() - divisor.scale();
        if (p.signum() == 0) {
            return getZero(preferredScale);
        }
        BigInteger gcd = p.gcd(q);
        p = p.divide(gcd);
        q = q.divide(gcd);
        if (q.signum() < 0) {
            p = p.negate();
            q = q.negate();
        }
        int x = q.getLowestSetBit();
        int y = log5(q.shiftRight(x));
        BigInteger value = x > y ? multiplyBy5Power(p, x - y) : p.shiftLeft(y - x);
        int scale = castScaleToInt(preferredScale + Math.max(x, y));
        return scale >= 0 ? new BigDecimal(value, scale) : new BigDecimal(value, 0).movePointLeft(scale);
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
        if (n == 0) {
            return ONE;
        }
        if (n < 0 || n > 999999999) {
            throw new ArithmeticException("Invalid operation");
        }
        long scale = bigDecimal.scale() * (long) n;
        return bigDecimal.signum() == 0 ? getZero(scale) :
                new BigDecimal(bigDecimal.unscaledValue().pow(n), castScaleToInt(scale));
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

    public static BigDecimal stripTrailingZeros(BigDecimal bigDecimal) {
        long perfectScale = 0;
        while (bigDecimal.scale() > perfectScale) {
            long newScale = (bigDecimal.scale() + perfectScale) / 2;
            try {
                bigDecimal = bigDecimal.setScale((int) newScale);
            } catch (ArithmeticException e) {
                perfectScale = newScale + 1;
            }
        }
        return bigDecimal;
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

    private static int castScaleToInt(long scale) {
        if (scale > Integer.MAX_VALUE) {
            throw new ArithmeticException("Underflow");
        }
        if (scale < Integer.MIN_VALUE) {
            throw new ArithmeticException("Overflow");
        }
        return (int) scale;
    }

    private static BigDecimal getZero(long scale) {
        return BigDecimal.valueOf(0, (int) Math.max(0, Math.min(Integer.MAX_VALUE, scale)));
    }

    private static int log5(BigInteger x) {
        int result = 0;
        int power = 1;
        while (true) {
            BigInteger[] quotientAndReminder = x.divideAndRemainder(FIVE_POWERS[power - 1]);
            if (quotientAndReminder[1].signum() == 0) {
                x = quotientAndReminder[0];
                result += power;
                power = Math.min(power + 1, FIVE_POWERS.length);
            } else if (power > 1) {
                power /= 2;
            } else if (x.bitLength() == 1) {
                return result;
            } else {
                throw new ArithmeticException("Non-terminating decimal expansion");
            }
        }
    }

    private static BigInteger multiplyBy5Power(BigInteger x, int power) {
        return power <= FIVE_POWERS.length ?
                x.multiply(FIVE_POWERS[power - 1]) :
                x.multiply(FIVE_POWERS[0].pow(power));
    }

}
