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
package net.sf.retrotranslator.runtime.java.lang;

import net.sf.retrotranslator.tests.BaseTestCase;
import java.util.EnumSet;

/**
 * @author Taras Puchko
 */
public class Enum_TestCase extends BaseTestCase {

    public void testName() throws Exception {
        assertEquals("GREEN", getName(MyColor.GREEN));
        assertEquals("EAST", getName(CardinalPoint.EAST));
    }

    private static String getName(Enum anEnum) {
        return anEnum.name();
    }

    public void testOrdinal() throws Exception {
        assertEquals(1, MyColor.GREEN.ordinal());
        assertEquals(2, CardinalPoint.SOUTH.ordinal());
    }

    public void testValueOf() throws Exception {
        MyColor color = Enum.valueOf(MyColor.class, "GREEN");
        assertEquals(MyColor.GREEN, color);
        assertSame(MyColor.GREEN, MyColor.valueOf("GREEN"));
        try {
            MyColor.valueOf("WHITE");
            fail("No such color!");
        } catch (IllegalArgumentException e) {
            //ok
        }

        CardinalPoint point = Enum.valueOf(CardinalPoint.class, "WEST");
        assertEquals(CardinalPoint.WEST, point);
        assertSame(CardinalPoint.WEST, CardinalPoint.valueOf("WEST"));
        try {
            CardinalPoint.valueOf("CENTER");
            fail("No such point!");
        } catch (IllegalArgumentException e) {
            //ok
        }
    }

    public void testValues() {
        MyColor[] colors = MyColor.values();
        assertEquals(3, colors.length);
        assertEquals(MyColor.RED, colors[0]);
        assertEquals(MyColor.GREEN, colors[1]);
        assertEquals(MyColor.BLUE, colors[2]);


        CardinalPoint[] points = CardinalPoint.values();
        assertEquals(4, points.length);
        assertEquals(CardinalPoint.NORTH, points[0]);
        assertEquals(CardinalPoint.EAST, points[1]);
        assertEquals(CardinalPoint.SOUTH, points[2]);
        assertEquals(CardinalPoint.WEST, points[3]);
    }

    public void testReadResolve() throws Exception {
        assertSame(MyColor.BLUE, pump(MyColor.BLUE));
        assertSame(CardinalPoint.SOUTH, pump(CardinalPoint.SOUTH));
    }

    enum Letter {
        A, B, C;
        public static Letter DEFAULT = Letter.valueOf("B");
        public static final EnumSet<Letter> SET = EnumSet.complementOf(EnumSet.of(B));
    }

    public void testInitOrder() throws Exception {
        assertEquals("A", Letter.A.name());
        assertEquals("B", Letter.B.name());
        assertEquals("C", Letter.C.name());
        assertEquals("B", Letter.DEFAULT.name());
        assertEquals(2, Letter.SET.size());
        assertTrue(Letter.SET.contains(Letter.A));
        assertFalse(Letter.SET.contains(Letter.B));
        assertTrue(Letter.SET.contains(Letter.C));
    }

}