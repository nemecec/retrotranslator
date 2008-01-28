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
package net.sf.retrotranslator.runtime.java.lang;

import junit.framework.*;

/**
 * @author Taras Puchko
 */
public class _MathTestCase extends TestCase {

    public void testCbrt() throws Exception {
        assertEquals(0.4641588833612779, Math.cbrt(0.1));
        assertEquals(-0.4641588833612779, Math.cbrt(-0.1));
        assertEquals(1.0, Math.cbrt(1));
        assertEquals(-1.0, Math.cbrt(-1));
        assertEquals(2.154434690031884, Math.cbrt(10));
        assertEquals(-2.154434690031884, Math.cbrt(-10));
        assertEquals(Double.NaN, Math.cbrt(Double.NaN));
        assertEquals(Double.POSITIVE_INFINITY, Math.cbrt(Double.POSITIVE_INFINITY));
        assertEquals(Double.NEGATIVE_INFINITY, Math.cbrt(Double.NEGATIVE_INFINITY));
        assertEquals(0.0, Math.cbrt(0.0));
        assertEquals(-0.0, Math.cbrt(-0.0));
    }

    public void testCosh() throws Exception {
        assertEquals(1.0050041680558035, Math.cosh(0.1));
        assertEquals(1.0050041680558035, Math.cosh(-0.1));
        assertEquals(1.543080634815244, Math.cosh(1));
        assertEquals(1.543080634815244, Math.cosh(-1));
        assertEquals(11013.232920103324, Math.cosh(10));
        assertEquals(11013.232920103324, Math.cosh(-10));
        assertEquals(Double.NaN, Math.cosh(Double.NaN));
        assertEquals(Double.POSITIVE_INFINITY, Math.cosh(Double.POSITIVE_INFINITY));
        assertEquals(Double.POSITIVE_INFINITY, Math.cosh(Double.NEGATIVE_INFINITY));
        assertEquals(1.0, Math.cosh(0.0));
        assertEquals(1.0, Math.cosh(-0.0));
    }

    public void testExpm1() throws Exception {
        assertEquals(0.10517091807564763, Math.expm1(0.1), 1E-16);
        assertEquals(-0.09516258196404043, Math.expm1(-0.1), 1E-16);
        assertEquals(1.718281828459045, Math.expm1(1), 1E-15);
        assertEquals(-0.6321205588285577, Math.expm1(-1));
        assertEquals(22025.465794806718, Math.expm1(10));
        assertEquals(-0.9999546000702375, Math.expm1(-10));
        assertEquals(Double.NaN, Math.expm1(Double.NaN));
        assertEquals(Double.POSITIVE_INFINITY, Math.expm1(Double.POSITIVE_INFINITY));
        assertEquals(-1.0, Math.expm1(Double.NEGATIVE_INFINITY));
        assertEquals(0.0, Math.expm1(0.0));
        assertEquals(-0.0, Math.expm1(-0.0));
    }

    public void testLog10() throws Exception {
        assertEquals(-1.0, Math.log10(0.1), 1E-15);
        assertEquals(Double.NaN, Math.log10(-0.1));
        assertEquals(0.0, Math.log10(1));
        assertEquals(Double.NaN, Math.log10(-1));
        assertEquals(1.0, Math.log10(10));
        assertEquals(3.0, Math.log10(1000));
        assertTrue(Math.log10(100) <= Math.log10(100.00001));
        assertEquals(Double.NaN, Math.log10(-10));
        assertEquals(4.091491094267951, Math.log10(12345), 1E-15);
        assertEquals(Double.NaN, Math.log10(Double.NaN));
        assertEquals(Double.POSITIVE_INFINITY, Math.log10(Double.POSITIVE_INFINITY));
        assertEquals(Double.NaN, Math.log10(Double.NEGATIVE_INFINITY));
        assertEquals(Double.NEGATIVE_INFINITY, Math.log10(0.0));
        assertEquals(Double.NEGATIVE_INFINITY, Math.log10(-0.0));
    }

    public void testLog1p() throws Exception {
        assertEquals(0.09531017980432487, Math.log1p(0.1), 1E-16);
        assertEquals(-0.10536051565782631, Math.log1p(-0.1), 1E-16);
        assertEquals(0.6931471805599453, Math.log1p(1));
        assertEquals(Double.NEGATIVE_INFINITY, Math.log1p(-1));
        assertEquals(2.3978952727983707, Math.log1p(10));
        assertEquals(Double.NaN, Math.log1p(-10));
        assertEquals(Double.NaN, Math.log1p(Double.NaN));
        assertEquals(Double.POSITIVE_INFINITY, Math.log1p(Double.POSITIVE_INFINITY));
        assertEquals(Double.NaN, Math.log1p(Double.NEGATIVE_INFINITY));
        assertEquals(0.0, Math.log1p(0.0));
        assertEquals(-0.0, Math.log1p(-0.0));
    }

    public void testSinh() throws Exception {
        assertEquals(0.10016675001984403, Math.sinh(0.1), 1E-16);
        assertEquals(-0.10016675001984403, Math.sinh(-0.1), 1E-16);
        assertEquals(1.1752011936438014, Math.sinh(1), 1E-15);
        assertEquals(-1.1752011936438014, Math.sinh(-1), 1E-15);
        assertEquals(11013.232874703393, Math.sinh(10));
        assertEquals(-11013.232874703393, Math.sinh(-10));
        assertEquals(Double.NaN, Math.sinh(Double.NaN));
        assertEquals(Double.POSITIVE_INFINITY, Math.sinh(Double.POSITIVE_INFINITY));
        assertEquals(Double.NEGATIVE_INFINITY, Math.sinh(Double.NEGATIVE_INFINITY));
        assertEquals(0.0, Math.sinh(0.0));
        assertEquals(-0.0, Math.sinh(-0.0));
    }

    public void testTanh() throws Exception {
        assertEquals(0.09966799462495582, Math.tanh(0.1), 1E-16);
        assertEquals(-0.09966799462495582, Math.tanh(-0.1), 1E-16);
        assertEquals(0.7615941559557649, Math.tanh(1));
        assertEquals(-0.7615941559557649, Math.tanh(-1));
        assertEquals(0.9999999958776927, Math.tanh(10), 1E-15);
        assertEquals(-0.9999999958776927, Math.tanh(-10), 1E-15);
        assertEquals(Double.NaN, Math.tanh(Double.NaN));
        assertEquals(0.0, Math.tanh(0.0));
        assertEquals(-0.0, Math.tanh(-0.0));
        assertEquals(1.0, Math.tanh(Double.POSITIVE_INFINITY));
        assertEquals(-1.0, Math.tanh(Double.NEGATIVE_INFINITY));
    }

    public void testSignum_Double() throws Exception {
        assertEquals(1.0, Math.signum(123d));
        assertEquals(-1.0, Math.signum(-12d));
        assertEquals(Double.NaN, Math.signum(Double.NaN));
        assertEquals(0.0, Math.signum(0.0d));
        assertEquals(-0.0, Math.signum(-0.0d));
        assertEquals(1.0, Math.signum(Double.POSITIVE_INFINITY));
        assertEquals(-1.0, Math.signum(Double.NEGATIVE_INFINITY));
    }

    public void testSignum_Float() throws Exception {
        assertEquals(1.0f, Math.signum(123f));
        assertEquals(-1.0f, Math.signum(-12f));
        assertEquals(Float.NaN, Math.signum(Float.NaN));
        assertEquals(0.0f, Math.signum(0.0f));
        assertEquals(-0.0f, Math.signum(-0.0f));
        assertEquals(1.0f, Math.signum(Float.POSITIVE_INFINITY));
        assertEquals(-1.0f, Math.signum(Float.NEGATIVE_INFINITY));
    }

}
