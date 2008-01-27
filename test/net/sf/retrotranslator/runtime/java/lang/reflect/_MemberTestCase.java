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
package net.sf.retrotranslator.runtime.java.lang.reflect;

import junit.framework.*;
import java.lang.reflect.*;

/**
 * @author Taras Puchko
 */
public class _MemberTestCase extends TestCase {

    public static class CTest {
        private CTest() {
        }
    }

    public void testIsSynthetic_constructor() throws Exception {
        new CTest();
        Member[] constructors = CTest.class.getDeclaredConstructors();
        assertEquals(2, constructors.length);
        if (constructors[0].isSynthetic()) {
            assertFalse(constructors[1].isSynthetic());
        } else {
            assertTrue(constructors[1].isSynthetic());
        }
    }

    public void testIsSynthetic_field() throws Exception {
        class FTest {
            public int f;
        }
        Member[] fields = FTest.class.getDeclaredFields();
        assertEquals(2, fields.length);
        if (fields[0].isSynthetic()) {
            assertFalse(fields[1].isSynthetic());
        } else {
            assertTrue(fields[1].isSynthetic());
        }
    }

    public void testIsSynthetic_method() throws Exception {
        class MTest implements Comparable<String> {
            public int compareTo(String o) {
                return 0;
            }
        }
        Member syntheticMember = MTest.class.getMethod("compareTo", Object.class);
        Member declaredMember = MTest.class.getMethod("compareTo", String.class);
        assertTrue(syntheticMember.isSynthetic());
        assertFalse(declaredMember.isSynthetic());
    }

    public void testIsSynthetic_other() throws Exception {
        Member member = new Member() {
            public Class<?> getDeclaringClass() {
                return null;
            }

            public String getName() {
                return null;
            }

            public int getModifiers() {
                return 0;
            }

            public boolean isSynthetic() {
                return false;
            }
        };
        assertFalse(member.isSynthetic());
    }

}