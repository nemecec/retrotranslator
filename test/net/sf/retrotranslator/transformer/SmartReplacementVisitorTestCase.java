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
package net.sf.retrotranslator.transformer;

import junit.framework.TestCase;
import net.sf.retrotranslator.transformer.smart.*;
import java.math.*;

/**
 * @author Taras Puchko
 */
public class SmartReplacementVisitorTestCase extends TestCase {

    public void testField() {
        assertEquals("Bye", FullDerivedClass.FIELD);
        Object baseValue = BackportedClass.FIELD;
        Object derivedValue = EmptyDerivedClass.FIELD;
        if (isSmart()) {
            assertEquals("Hi", baseValue);
            assertEquals("Hi", derivedValue);
        } else {
            assertEquals("Hello", baseValue);
            assertEquals("Hello", derivedValue);
        }
    }

    public void testStaticMethod() {
        assertEquals(21, FullDerivedClass.add(2, 5));
        int baseValue = BackportedClass.add(2, 5);
        int derivedValue = EmptyDerivedClass.add(2, 5);
        if (isSmart()) {
            assertEquals(14, baseValue);
            assertEquals(14, derivedValue);
        } else {
            assertEquals(7, baseValue);
            assertEquals(7, derivedValue);
        }
    }

    public void testInstanceMethod() {
        assertEquals(210, new FullDerivedClass().multiply(7));
        int baseValue = new BackportedClass().multiply(7);
        int derivedValue = new EmptyDerivedClass().multiply(7);
        if (isSmart()) {
            assertEquals(140, baseValue);
            assertEquals(140, derivedValue);
        } else {
            assertEquals(70, baseValue);
            assertEquals(70, derivedValue);
        }
    }

    public void testConstructor() {
        Exception exception1 = new IllegalArgumentException();
        Exception exception2 = new IllegalArgumentException("test2");
        Exception exception3 = new IllegalArgumentException("test3", new Exception("cause3"));
        Exception exception4 = new IllegalArgumentException(new Exception("cause4"));
        assertNull(exception1.getMessage());
        assertNull(exception1.getCause());
        assertEquals("test2", exception2.getMessage());
        assertNull(exception2.getCause());
        if (isSmart()) {
            assertEquals("message and cause", exception3.getMessage());
            assertNull(exception3.getCause());
            assertEquals("cause", exception4.getMessage());
            assertNull(exception4.getCause());
        } else {
            assertEquals("test3", exception3.getMessage());
            assertEquals("cause3", exception3.getCause().getMessage());
            assertEquals("java.lang.Exception: cause4", exception4.getMessage());
            assertEquals("cause4", exception4.getCause().getMessage());
        }
    }

    private boolean isSmart() {
        return Boolean.getBoolean("net.sf.retrotranslator.tests.smart-test");
    }

    private boolean isJava5() {
        String version = System.getProperty("java.version");
        return version.length() > 3 && version.substring(0, 3).compareTo("1.5") >= 0;
    }

    public void testBigDecimal() {
        if (isSmart() || isJava5()) {
            class MyDecimal extends BigDecimal {
                public MyDecimal(int val) {
                    super(val);
                }
            }
            MyDecimal myDecimal = new MyDecimal(123);
            assertEquals(0, myDecimal.scale());
            BigDecimal decimal = myDecimal.setScale(-1, MyDecimal.ROUND_DOWN);
            assertEquals(120, decimal.intValue());
        }
    }


}