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
package net.sf.retrotranslator.runtime.java.math;

import net.sf.retrotranslator.tests.BaseTestCase;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * @author Taras Puchko
 */
public class _BigDecimalTestCase extends BaseTestCase {

    public void testConstants() throws Exception {
        assertEquals(0, BigDecimal.ZERO.intValue());
        assertEquals(1, BigDecimal.ONE.intValue());
        assertEquals(10, BigDecimal.TEN.intValue());
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

    public void testToPlainString() throws Exception {
        assertEquals("1230", new BigDecimal(1230).toPlainString());
        assertEquals("1.23", new BigDecimal(BigInteger.valueOf(123), 2).toPlainString());
    }

    public void testValueOf() throws Exception {
        assertEquals(1.23, BigDecimal.valueOf(1.23).doubleValue());
        assertEquals(123, BigDecimal.valueOf(123).longValue());
    }
}