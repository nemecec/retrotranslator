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
public class _FieldGenericTestCase<Z> extends TestCaseBase {

    class MemberOuterNonGeneric {
    }

    class MemberOuterGeneric<T> {
        class MemberInnerNonGeneric {
        }
    }

    static class StaticMemberOuterNonGeneric {
    }

    static class StaticMemberOuterGeneric<T> {
        class MemberInnerNonGeneric {
        }
    }

    public void testVariable() {
        class Test {
            // TZ;
            // Z
            public Z f;
        }
        Type type = getType(Test.class);
        assertVariable(type);
    }

    public void testMemberNonGeneric() {
        class Test {
            // _FieldGenericTestCase<TZ;>.MemberOuterNonGeneric
            // _FieldGenericTestCase<Z>.MemberOuterNonGeneric
            public MemberOuterNonGeneric f;
        }
        ParameterizedType outerType = getParameterizedType(Test.class);
        assertEquals(MemberOuterNonGeneric.class, outerType.getRawType());
        assertEquals(0, outerType.getActualTypeArguments().length);
        assertThisType(outerType.getOwnerType());
    }

    public void testStaticMemberNonGeneric() {
        class Test {
            // null
            // class _FieldGenericTestCase$StaticMemberOuterNonGeneric
            public StaticMemberOuterNonGeneric f;
        }
        Type type = getType(Test.class);
        assertEquals(StaticMemberOuterNonGeneric.class, type);
    }

    public void testMemberGeneric() {
        class Test {
            // _FieldGenericTestCase<TZ;>.MemberOuterGeneric<-[TZ;>
            // _FieldGenericTestCase<Z>.MemberOuterGeneric<? super Z[]>
            public MemberOuterGeneric<? super Z[]> f;
        }
        ParameterizedType outerType = getParameterizedType(Test.class);
        assertEquals(MemberOuterGeneric.class, outerType.getRawType());
        WildcardType wildcardType = (WildcardType) singleton(outerType.getActualTypeArguments());
        assertEquals(Object.class, singleton(wildcardType.getUpperBounds()));
        GenericArrayType arrayType = (GenericArrayType) singleton(wildcardType.getLowerBounds());
        assertVariable(arrayType.getGenericComponentType());
        assertThisType(outerType.getOwnerType());
    }

    public void testStaticMemberGeneric() {
        class Test {
            // _FieldGenericTestCase$StaticMemberOuterGeneric<Ljava/lang/String;>
            // _FieldGenericTestCase._FieldGenericTestCase$StaticMemberOuterGeneric<java.lang.String>
            public StaticMemberOuterGeneric<String> f;
        }
        ParameterizedType type = getParameterizedType(Test.class);
        assertEquals(StaticMemberOuterGeneric.class, type.getRawType());
        assertEquals(String.class, singleton(type.getActualTypeArguments()));
        assertEquals(this.getClass(), type.getOwnerType());
    }

    public void testMemberOuterGeneric() {
        class Test {
            // _FieldGenericTestCase<TZ;>.MemberOuterGeneric<Ljava/lang/String;>.MemberInnerNonGeneric
            // _FieldGenericTestCase<Z>.MemberOuterGeneric<java.lang.String>.MemberInnerNonGeneric
            public MemberOuterGeneric<String>.MemberInnerNonGeneric f;
        }
        ParameterizedType innerType = getParameterizedType(Test.class);
        assertEquals(MemberOuterGeneric.MemberInnerNonGeneric.class, innerType.getRawType());
        assertEquals(0, innerType.getActualTypeArguments().length);
        ParameterizedType outerType = (ParameterizedType) innerType.getOwnerType();
        assertEquals(MemberOuterGeneric.class, outerType.getRawType());
        assertEquals(String.class, singleton(outerType.getActualTypeArguments()));
        assertThisType(outerType.getOwnerType());
    }

    public void testStaticMemberOuterGeneric() {
        class Test {
            // _FieldGenericTestCase$StaticMemberOuterGeneric<Ljava/lang/String;>.MemberInnerNonGeneric
            // _FieldGenericTestCase._FieldGenericTestCase$StaticMemberOuterGeneric<java.lang.String>.MemberInnerNonGeneric
            public StaticMemberOuterGeneric<String>.MemberInnerNonGeneric f;
        }
        ParameterizedType innerType = getParameterizedType(Test.class);
        assertEquals(StaticMemberOuterGeneric.MemberInnerNonGeneric.class, innerType.getRawType());
        assertEquals(0, innerType.getActualTypeArguments().length);
        ParameterizedType outerType = (ParameterizedType) innerType.getOwnerType();
        assertEquals(StaticMemberOuterGeneric.class, outerType.getRawType());
        assertEquals(String.class, singleton(outerType.getActualTypeArguments()));
        assertEquals(this.getClass(), outerType.getOwnerType());
    }

    private void assertThisType(Type thisType) {
        ParameterizedType type = (ParameterizedType) thisType;
        assertEquals(this.getClass(), type.getRawType());
        assertNull(type.getOwnerType());
        assertVariable(singleton(type.getActualTypeArguments()));
    }

    private void assertVariable(Type type) {
        TypeVariable variable = (TypeVariable) type;
        assertEquals("Z", variable.getName());
        assertEquals(this.getClass(), variable.getGenericDeclaration());
        assertEquals(Object.class, singleton(variable.getBounds()));
    }
}
