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

import junit.framework.TestCase;

/**
 * @author Taras Puchko
 */
public class _LongTestCase extends TestCase {

    public void testValueOf() throws Exception {
        Long aLong = -100L;
        assertEquals(-100L, aLong.longValue());
    }

    public void testSignum() throws Exception {
        assertEquals(1, Long.signum(123));
        assertEquals(0, Long.signum(0));
        assertEquals(-1, Long.signum(-345));
    }

    public void testHighestOneBit() throws Exception {
        assertEquals(0, Long.highestOneBit(0));
        assertEquals(1, Long.highestOneBit(1));
        assertEquals(2, Long.highestOneBit(2));
        assertEquals(2, Long.highestOneBit(3));
        assertEquals(0x8000000000000000L, Long.highestOneBit(-1));
        assertEquals(0x8000000000000000L, Long.highestOneBit(-2));
        assertEquals(0x8000000000000000L, Long.highestOneBit(-3));
        assertEquals(0x0800000000000000L, Long.highestOneBit(0x0FFFFFFFFFFFFFFFL));
        assertEquals(0x0800000000000000L, Long.highestOneBit(0x0F0F0F0404040404L));
    }

    public void testLowestOneBit() throws Exception {
        assertEquals(0, Long.lowestOneBit(0));
        assertEquals(1, Long.lowestOneBit(1));
        assertEquals(2, Long.lowestOneBit(2));
        assertEquals(1, Long.lowestOneBit(3));
        assertEquals(1, Long.lowestOneBit(-1));
        assertEquals(2, Long.lowestOneBit(-2));
        assertEquals(1, Long.lowestOneBit(-3));
        assertEquals(1, Long.lowestOneBit(0x0FFFFFFFFFFFFFFFL));
        assertEquals(4, Long.lowestOneBit(0x0F0F0F0404040404L));
    }

}