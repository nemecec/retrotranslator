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
package net.sf.retrotranslator.tests;

import junit.framework.TestCase;

import java.util.UnknownFormatFlagsException;
import java.util.DuplicateFormatFlagsException;
import java.util.Arrays;
import java.math.BigDecimal;

/**
 * @author Taras Puchko
 */
public class CustomBackportTestCase extends TestCase {

    public void testReplacement() {
        if (isCustomBackport()) {
            Exception exception = new UnknownFormatFlagsException("abc");
            assertEquals(DuplicateFormatFlagsException.class, exception.getClass().getSuperclass());
        }
    }

    public void testFields() {
        assertEquals(1, BigDecimal.ONE.intValue());
        if (isCustomBackport()) {
            assertEquals(1000, BigDecimal.ZERO.intValue());
        }
    }

    public void testMethods() {
        assertEquals("[1, 2, 3]", Arrays.toString(new int[] {1, 2, 3}));
        if (isCustomBackport()) {
            assertEquals("[TRUE, FALSE, TRUE]", Arrays.toString(new boolean[] {true, false, true}));
        }
    }

    public void testConvertors() {
        assertEquals(10, new BigDecimal(10L).intValue());
        if (isCustomBackport()) {
            assertEquals(11, new BigDecimal(10).intValue());
        }
    }

    public void testBuilders_1() {
        TestBean_ bean = new TestBean_(true, 'L', (byte) 12, (short) 10, 1234567, 0.5f, 12345670L, 123.45, "XYZ");
        assertEquals(true, bean.isVisible());
        assertEquals('L', bean.getSign());
        assertEquals((byte) 12, bean.getCode());
        assertEquals((short) 10, bean.getWidth());
        assertEquals(1234567, bean.getHeight());
        assertEquals(0.5f, bean.getOpacity());
        assertEquals(12345670L, bean.getArea());
        assertEquals(123.45, bean.getWeight());
        assertEquals("XYZ", bean.getName());
        if (isCustomBackport()) {
            assertEquals("reverse", bean.getDirection());
            assertEquals("initialized", bean.getState());
        } else {
            assertEquals("right", bean.getDirection());
            assertNull(bean.getState());
        }
    }

    public void testBuilders_2() {
        TestBean_ bean = new TestBean_("abc");
        assertEquals("abc", bean.getState());
        if (isCustomBackport()) {
            assertEquals("B", bean.getDirection());
        } else {
            assertEquals("A", bean.getDirection());
        }
    }

    private boolean isCustomBackport() {
        return Boolean.getBoolean("net.sf.retrotranslator.custom-backport");
    }

}
