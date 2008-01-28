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

/**
 * @author Taras Puchko
 */
public class _Math {

    private static final double LOG10 = Math.log(10);

    public static double cbrt(double a) {
        if (a > 0) {
            return Math.pow(a, 1.0/3);
        }
        if (a < 0) {
            return -Math.pow(Math.abs(a), 1.0/3);
        }
        return a; 
    }

    public static double cosh(double x) {
        return (Math.exp(x) + Math.exp(-x)) / 2;
    }

    public static double expm1(double x) {
        return x == 0 ? x : Math.exp(x) - 1;
    }

    public static double log10(double a) {
        double exactResult = Math.log(a) / LOG10;
        double fixedResult = Math.ceil(exactResult);
        double fixedArgument = Math.pow(10, fixedResult);
        return fixedArgument <= a ? fixedResult : exactResult;
    }

    public static double log1p(double x) {
        return x == 0 ? x : Math.log(1.0 + x);
    }

    public static double signum(double d) {
        return d > 0 ? 1 : d < 0 ? -1 : d == 0 ? d : Double.NaN;
    }

    public static float signum(float f) {
        return f > 0 ? 1 : f < 0 ? -1 : f == 0 ? f : Float.NaN;
    }

    public static double sinh(double x) {
        return x == 0 ? x : (Math.exp(x) - Math.exp(-x)) / 2;
    }

    public static double tanh(double x) {
        if (x == 0) {
            return x;
        }
        if (x == Double.POSITIVE_INFINITY) {
            return 1;
        }
        if (x == Double.NEGATIVE_INFINITY) {
            return -1;
        }
        double p = Math.exp(x);
        double q = Math.exp(-x);
        return (p - q) / (p + q);
    }

}
