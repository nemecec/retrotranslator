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
package net.sf.retrotranslator.transformer;

import junit.framework.TestCase;

public class ClassLiteralVisitorTestCase extends TestCase {

    public static Class CONST = Float.class;

    public static interface InterfaceInClass {
        public Class CONST = Integer.class;
    }

    public static class StaticClassInClass {
        public static Class CONST = Float.class;
    }

    public class ClassInClass {
        public Class getConst() {
            return Float.class;
        }
    }

    public static class MyConstantsImpl implements MyConstants {
    }

    public void testClasses() {
        assertTrue(Iterable.class.isInterface());
        assertEquals(ClassLiteralVisitorTestCase.class, ClassLiteralVisitorTestCase[].class.getComponentType());
        assertEquals(InterfaceInClass.class, InterfaceInClass[].class.getComponentType());
        assertEquals(StaticClassInClass.class, StaticClassInClass[].class.getComponentType());
        assertEquals(ClassInClass.class, ClassInClass[].class.getComponentType());
        assertEquals(MyConstantsImpl.class, MyConstantsImpl[].class.getComponentType());
    }

    public void testConst() {
        assertNotNull(CONST);
        assertNotNull(InterfaceInClass.CONST);
        assertNotNull(StaticClassInClass.CONST);
        assertNotNull(new ClassInClass().getConst());
        assertSame(MyConstantsImpl.CONST[1], Integer.class);
        assertSame(MyConstants.CONST[2], String.class);
        assertNotNull(MyConstants.InterfaceInInterface.CONST);
        assertNotNull(MyConstants.ClassInInterface.CONST);
    }

    public void testArrays() {
        assertEquals(Integer.class, Integer[].class.getComponentType());
        assertEquals(Integer[].class, Integer[][].class.getComponentType());
        assertEquals(Integer[][].class, Integer[][][].class.getComponentType());
        assertEquals(boolean.class, boolean[].class.getComponentType());
        assertEquals(boolean[].class, boolean[][].class.getComponentType());
        assertEquals(boolean[][].class, boolean[][][].class.getComponentType());

        assertEquals(char[].class, char[][].class.getComponentType());
        assertEquals(float[].class, float[][].class.getComponentType());
        assertEquals(double[].class, double[][].class.getComponentType());
        assertEquals(byte[].class, byte[][].class.getComponentType());
        assertEquals(short[].class, short[][].class.getComponentType());
        assertEquals(int[].class, int[][].class.getComponentType());
        assertEquals(long[].class, long[][].class.getComponentType());
    }
}