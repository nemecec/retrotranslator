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

import java.lang.reflect.*;
import net.sf.retrotranslator.tests.TestCaseBase;

/**
 * @author Taras Puchko
 */
public class _FieldNonGenericTestCase extends TestCaseBase {

    class MemberOuterNonGeneric {
        class MemberInnerGeneric<T> {
        }
    }

    class MemberOuterGeneric<T> {
        class MemberInnerNonGeneric {
        }
    }

    public void testLocalNonGeneric() {
        class LocalNonGeneric {
        }
        class Test {
            // null
            // class _FieldNonGenericTestCase$1LocalNonGeneric
            public LocalNonGeneric f;
        }
        Type type = getType(Test.class);
        assertEquals(LocalNonGeneric.class, type);
    }

    public void testMemberNonGeneric() {
        class Test {
            // null
            // class _FieldNonGenericTestCase$MemberOuterNonGeneric
            public MemberOuterNonGeneric f;
        }
        Type type = getType(Test.class);
        assertEquals(MemberOuterNonGeneric.class, type);
    }

    public void testLocalGeneric() {
        class LocalGeneric<T> {
        }
        class Test {
            // _FieldNonGenericTestCase$1LocalGeneric<Ljava/lang/String;>
            // _FieldNonGenericTestCase$1LocalGeneric<java.lang.String>
            public LocalGeneric<String> f;
        }
        ParameterizedType type = getParameterizedType(Test.class);
        assertEquals(LocalGeneric.class, type.getRawType());
        assertEquals(String.class, singleton(type.getActualTypeArguments()));
        assertNull(type.getOwnerType());
    }

    public void testMemberGeneric() {
        class Test {
            // _FieldNonGenericTestCase$MemberOuterGeneric<Ljava/lang/String;>
            // _FieldNonGenericTestCase._FieldNonGenericTestCase$MemberOuterGeneric<java.lang.String>
            public MemberOuterGeneric<String> f;
        }
        ParameterizedType type = getParameterizedType(Test.class);
        assertEquals(MemberOuterGeneric.class, type.getRawType());
        assertEquals(String.class, singleton(type.getActualTypeArguments()));
        assertEquals(this.getClass(), type.getOwnerType());
    }

    public void testLocalInnerGeneric() {
        class LocalNonGeneric {
            class LocalInnerGeneric<T> {
            }
        }
        class Test {
            // _FieldNonGenericTestCase$2LocalNonGeneric$LocalInnerGeneric<Ljava/lang/Integer;>
            // _FieldNonGenericTestCase$2LocalNonGeneric._FieldNonGenericTestCase$2LocalNonGeneric$LocalInnerGeneric<java.lang.Integer>
            public LocalNonGeneric.LocalInnerGeneric<Integer> f;
        }
        ParameterizedType innerType = getParameterizedType(Test.class);
        assertEquals(LocalNonGeneric.LocalInnerGeneric.class, innerType.getRawType());
        assertEquals(Integer.class, singleton(innerType.getActualTypeArguments()));
        assertEquals(LocalNonGeneric.class, innerType.getOwnerType());
    }

    public void testMemberInnerGeneric() {
        class Test {
            // _FieldNonGenericTestCase$MemberOuterNonGeneric$MemberInnerGeneric<Ljava/lang/Integer;>
            // _FieldNonGenericTestCase$MemberOuterNonGeneric._FieldNonGenericTestCase$MemberOuterNonGeneric$MemberInnerGeneric<java.lang.Integer>
            public MemberOuterNonGeneric.MemberInnerGeneric<Integer> f;
        }
        ParameterizedType innerType = getParameterizedType(Test.class);
        assertEquals(MemberOuterNonGeneric.MemberInnerGeneric.class, innerType.getRawType());
        assertEquals(Integer.class, singleton(innerType.getActualTypeArguments()));
        assertEquals(MemberOuterNonGeneric.class, innerType.getOwnerType());
    }

    public void testLocalOuterGeneric() {
        class LocalGeneric<T> {
            class LocalInnerGeneric {
            }
        }
        class Test {
            // _FieldNonGenericTestCase$2LocalGeneric<Ljava/lang/String;>.LocalInnerGeneric
            // _FieldNonGenericTestCase$2LocalGeneric<java.lang.String>.LocalInnerGeneric
            public LocalGeneric<String>.LocalInnerGeneric f;
        }
        ParameterizedType innerType = getParameterizedType(Test.class);
        assertEquals(LocalGeneric.LocalInnerGeneric.class, innerType.getRawType());
        assertEquals(0, innerType.getActualTypeArguments().length);
        ParameterizedType outerType = (ParameterizedType) innerType.getOwnerType();
        assertEquals(LocalGeneric.class, outerType.getRawType());
        assertEquals(String.class, singleton(outerType.getActualTypeArguments()));
        assertNull(outerType.getOwnerType());
    }

    public void testMemberOuterGeneric() {
        class Test {
            // _FieldNonGenericTestCase$MemberOuterGeneric<Ljava/lang/String;>.MemberInnerNonGeneric
            // _FieldNonGenericTestCase._FieldNonGenericTestCase$MemberOuterGeneric<java.lang.String>.MemberInnerNonGeneric
            public MemberOuterGeneric<String>.MemberInnerNonGeneric f;
        }
        ParameterizedType innerType = getParameterizedType(Test.class);
        assertEquals(MemberOuterGeneric.MemberInnerNonGeneric.class, innerType.getRawType());
        assertEquals(0, innerType.getActualTypeArguments().length);
        ParameterizedType outerType = (ParameterizedType) innerType.getOwnerType();
        assertEquals(MemberOuterGeneric.class, outerType.getRawType());
        assertEquals(String.class, singleton(outerType.getActualTypeArguments()));
        assertEquals(this.getClass(), outerType.getOwnerType());
    }

}
