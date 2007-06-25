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
package net.sf.retrotranslator.runtime.java.math;

import java.math.*;
import net.sf.retrotranslator.tests.BaseTestCase;

/**
 * @author Taras Puchko
 */
public class _BigDecimalTestCase extends BaseTestCase {

    public void testConstants() throws Exception {
        assertEquals(0, BigDecimal.ZERO.intValue());
        assertEquals(1, BigDecimal.ONE.intValue());
        assertEquals(10, BigDecimal.TEN.intValue());
    }

    public void testConvertConstructorArguments() throws Exception {
        assertEquals(10L, new BigDecimal(10L).longValue());
        assertEquals(20, new BigDecimal(20).intValue());
        assertEquals(30, new BigDecimal("a30b".toCharArray(), 1, 2).intValue());
        assertEquals(40, new BigDecimal("40".toCharArray()).intValue());
        class MyDecimal extends BigDecimal {
            public MyDecimal(int val) {
                super(val);
            }

            public MyDecimal(long val) {
                super(val);
            }

            public MyDecimal(char[] in, int offset, int len) {
                super(in, offset, len);
            }

            public MyDecimal(char[] in) {
                super(in);
            }
        }
        assertEquals(10L, new MyDecimal(10L).longValue());
        assertEquals(20, new MyDecimal(20).intValue());
        assertEquals(30, new MyDecimal("a30b".toCharArray(), 1, 2).intValue());
        assertEquals(40, new MyDecimal("40".toCharArray()).intValue());
    }

    public void testDivideAndRemainder() throws Exception {
        BigDecimal dividend = BigDecimal.valueOf(123.4567);
        assertEquals(4, dividend.scale());
        BigDecimal divisor = BigDecimal.valueOf(8.9);
        assertEquals(1, divisor.scale());
        BigDecimal[] result = dividend.divideAndRemainder(divisor);
        BigDecimal quotient = result[0];
        BigDecimal remainder = result[1];
        assertEquals(13.0, quotient.doubleValue());
        assertEquals(3, quotient.scale());
        assertEquals(7.7567, remainder.doubleValue());
        assertEquals(4, remainder.scale());

    }

    public void testDivideToIntegralValue_scale2() throws Exception {
        BigDecimal dividend = BigDecimal.valueOf(123.456);
        assertEquals(3, dividend.scale());
        BigDecimal divisor = BigDecimal.valueOf(7.8);
        assertEquals(1, divisor.scale());
        BigDecimal quotient = dividend.divideToIntegralValue(divisor);
        assertEquals(15.0, quotient.doubleValue());
        assertEquals(2, quotient.scale());
    }

    public void testDivideToIntegralValue_scale0() throws Exception {
        BigDecimal dividend = BigDecimal.valueOf(123.4);
        assertEquals(1, dividend.scale());
        BigDecimal divisor = BigDecimal.valueOf(5.678);
        assertEquals(3, divisor.scale());
        BigDecimal quotient = dividend.divideToIntegralValue(divisor);
        assertEquals(21.0, quotient.doubleValue());
        assertEquals(0, quotient.scale());
    }

    public void testDivideToIntegralValue_zeroDividend() throws Exception {
        BigDecimal dividend = BigDecimal.valueOf(0, 10);
        assertEquals(10, dividend.scale());
        BigDecimal divisor = BigDecimal.valueOf(1, 5);
        assertEquals(5, divisor.scale());
        BigDecimal quotient = dividend.divideToIntegralValue(divisor);
        assertEquals(0.0, quotient.doubleValue());
        assertEquals(5, quotient.scale());
    }

    public void testDivideToIntegralValue_zeroDivider() throws Exception {
        BigDecimal dividend = BigDecimal.valueOf(1);
        BigDecimal divisor = BigDecimal.valueOf(0);
        try {
            dividend.divideToIntegralValue(divisor);
            fail();
        } catch (ArithmeticException e) {
            //ok
        }
    }

    public void testPow() throws Exception {
        assertEquals(1, new BigDecimal(10).pow(0).intValue());
        assertEquals(10, new BigDecimal(10).pow(1).intValue());
        assertEquals(100, new BigDecimal(10).pow(2).intValue());
        assertEquals(100000, new BigDecimal(10).pow(5).intValue());
        try {
            new BigDecimal(10).pow(-10);
            fail();
        } catch (ArithmeticException e) {
            //ok
        }
    }

    public void testSetScale() throws Exception {
        assertEquals("123.5", new BigDecimal(123.45).setScale(1, BigDecimal.ROUND_HALF_EVEN).toPlainString());
        assertEquals("120", new BigDecimal(123).setScale(-1, BigDecimal.ROUND_HALF_EVEN).toPlainString());
    }

    public void testRemainder() throws Exception {
        BigDecimal dividend = BigDecimal.valueOf(12.3);
        assertEquals(1, dividend.scale());
        BigDecimal divisor = BigDecimal.valueOf(4.5678);
        assertEquals(4, divisor.scale());
        BigDecimal remainder = dividend.remainder(divisor);
        assertEquals(3.1644, remainder.doubleValue());
        assertEquals(4, remainder.scale());
    }

    public void testToPlainString() throws Exception {
        assertEquals("1230", new BigDecimal(1230).toPlainString());
        assertEquals("1.23", new BigDecimal(BigInteger.valueOf(123), 2).toPlainString());
    }

    public void testValueOf() throws Exception {
        assertEquals(1.23, BigDecimal.valueOf(1.23).doubleValue());
        assertEquals(123, BigDecimal.valueOf(123).longValue());
    }

}