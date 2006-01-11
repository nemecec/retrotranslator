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
package net.sf.retrotranslator.runtime.java.lang;

import net.sf.retrotranslator.tests.BaseTestCase;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * @author Taras Puchko
 */
@MyFormatter(tabPositions = {}, numbers = {1})
public class _ClassTestCase extends BaseTestCase {

    @MyStyle("bold")
    @MyFormatter(lang = "uk")
    private static class A<T> {
    }

    private static class B<T> extends A {
    }

    @MyStyle("italic")
    private static class C extends A {
    }

    public void testAsSubclass() throws Exception {
        B.class.asSubclass(A.class);
        try {
            A.class.asSubclass(B.class);
            fail();
        } catch (ClassCastException e) {
            //ok
        }
        assertEquals(Integer[].class, Integer[].class.asSubclass(Number[].class));
        assertEquals(void.class, void.class.asSubclass(void.class));
        assertEquals(boolean[].class, boolean[].class.asSubclass(boolean[].class));
    }

    public void testCast() throws Exception {
        String str = "a";
        assertSame(str, String.class.cast(str));
        try {
            Integer.class.cast(str);
            fail();
        } catch (ClassCastException e) {
            //ok
        }
        try {
            int.class.cast(5);
            fail();
        } catch (ClassCastException e) {
            //ok
        }

        Integer[].class.cast(new Integer[] {});
        try {
            boolean[].class.cast(boolean[].class);
            fail();
        } catch (ClassCastException e) {
            //ok
        }
    }

    public void testGetAnnotation() throws Exception {
        assertEquals(0, _ClassTestCase.class.getAnnotation(MyFormatter.class).tabPositions().length);

        assertEquals("bold", A.class.getAnnotation(MyStyle.class).value());
        assertEquals("uk", A.class.getAnnotation(MyFormatter.class).lang());

        assertEquals("bold", B.class.getAnnotation(MyStyle.class).value());
        assertNull(B.class.getAnnotation(MyFormatter.class));
        assertEquals("italic", C.class.getAnnotation(MyStyle.class).value());
        assertNull(C.class.getAnnotation(MyFormatter.class));

        assertNull(int.class.getAnnotation(MyFormatter.class));
        assertNull(boolean[].class.getAnnotation(MyFormatter.class));
        assertNull(String[][].class.getAnnotation(MyFormatter.class));
    }

    public void testGetAnnotations() throws Exception {
        assertEqualElements(A.class.getAnnotations(), A.class.getAnnotation(MyStyle.class), A.class.getAnnotation(MyFormatter.class));
        assertEqualElements(B.class.getAnnotations(), B.class.getAnnotation(MyStyle.class));
        assertEqualElements(C.class.getAnnotations(), C.class.getAnnotation(MyStyle.class));
        assertEquals(0, void.class.getAnnotations().length);
    }

    public void testGetCanonicalName() throws Exception {
        class Test {
        }
        Serializable anonymous = new Serializable() { };
        assertEquals("net.sf.retrotranslator.runtime.java.lang._ClassTestCase", this.getClass().getCanonicalName());
        assertEquals("net.sf.retrotranslator.runtime.java.lang._ClassTestCase.A", A.class.getCanonicalName());
        assertNull(Test.class.getCanonicalName());
        assertNull(anonymous.getClass().getCanonicalName());
        assertEquals("java.lang.String[]", String[].class.getCanonicalName());
        assertNull(Test[].class.getCanonicalName());
        assertEquals("void", void.class.getCanonicalName());
        assertEquals("boolean[][]", boolean[][].class.getCanonicalName());
    }

    public void testGetDeclaredAnnotations() throws Exception {
        assertEqualElements(A.class.getDeclaredAnnotations(), A.class.getAnnotation(MyStyle.class), A.class.getAnnotation(MyFormatter.class));
        assertEquals(0, B.class.getDeclaredAnnotations().length);
        assertEqualElements(C.class.getAnnotations(), C.class.getAnnotation(MyStyle.class));
        assertEquals(0, int[][].class.getDeclaredAnnotations().length);
    }

    class TestGetEnclosingClass {
        class Inner {
        }
    }
    public void testGetEnclosingClass() throws Exception {
        class Test {
            class Inner {
            }
        }
        assertEquals(this.getClass(), Test.class.getEnclosingClass());
        assertEquals(Test.class, Test.Inner.class.getEnclosingClass());
        assertEquals(this.getClass(), TestGetEnclosingClass.class.getEnclosingClass());
        assertEquals(TestGetEnclosingClass.class, TestGetEnclosingClass.Inner.class.getEnclosingClass());

        Object anonymous = new Object() {};
        assertEquals(this.getClass(), anonymous.getClass().getEnclosingClass());
        assertNull(void.class.getEnclosingClass());
        assertNull(int[][].class.getEnclosingClass());
    }

    public void testGetEnclosingConstructor() throws Exception {
        class Test {
            public Test() {
                class Inner {
                }
                assertEquals(Test.class.getName(), Inner.class.getEnclosingConstructor().getName());
            }
        }
        new Test();
        assertNull(float.class.getEnclosingConstructor());
    }

    public void testGetEnclosingMethod() throws Exception {
        class Inner {}
        assertEquals("testGetEnclosingMethod", Inner.class.getEnclosingMethod().getName());
        Object anonymous = new Object() {};
        assertEquals("testGetEnclosingMethod", anonymous.getClass().getEnclosingMethod().getName());
        assertNull(double.class.getEnclosingMethod());
    }


    public void testGetEnumConstants() throws Exception {
        MyColor[] constants = MyColor.class.getEnumConstants();
        assertEqualElements(constants, MyColor.RED, MyColor.GREEN, MyColor.BLUE);
        assertNull(void.class.getEnumConstants());
        assertNull(int[].class.getEnumConstants());
    }

    public void testGetGenericInterfaces_Parameterized() throws Exception {
        class Test implements Comparable<String> {
            public int compareTo(String o) {
                return 0;
            }
        }
        ParameterizedType type = (ParameterizedType) singleton(Test.class.getGenericInterfaces());
        assertEquals(Comparable.class, type.getRawType());
        assertNull(type.getOwnerType());
        assertEquals(String.class, singleton(type.getActualTypeArguments()));
    }

    public void testGetGenericInterfaces_NotParameterized() throws Exception {
        class Test implements Comparable {
            public int compareTo(Object o) {
                return 0;
            }
        }
        assertEquals(Comparable.class, singleton(Test.class.getGenericInterfaces()));
        assertEquals(0, void.class.getGenericInterfaces().length);
    }

    public void testGetGenericSuperclass_FullyParameterized() throws Exception {
        class Test extends ThreadLocal<Comparable<String>> {
        }
        ParameterizedType threadLocal = (ParameterizedType) Test.class.getGenericSuperclass();
        assertEquals(ThreadLocal.class, threadLocal.getRawType());
        assertNull(threadLocal.getOwnerType());
        ParameterizedType comparable = (ParameterizedType) singleton(threadLocal.getActualTypeArguments());
        assertEquals(Comparable.class, comparable.getRawType());
        assertNull(comparable.getOwnerType());
        assertEquals(String.class, singleton(comparable.getActualTypeArguments()));
    }

    public void testGetGenericSuperclass_PartlyParameterized() throws Exception {
        class Test extends ThreadLocal<Comparable> {
        }
        ParameterizedType threadLocal = (ParameterizedType) Test.class.getGenericSuperclass();
        assertEquals(ThreadLocal.class, threadLocal.getRawType());
        assertNull(threadLocal.getOwnerType());
        assertEquals(Comparable.class, singleton(threadLocal.getActualTypeArguments()));
    }

    public void testGetSimpleName() throws Exception {
        class Test {
        }
        Serializable anonymous = new Serializable() { };
        assertEquals("_ClassTestCase", this.getClass().getSimpleName());
        assertEquals("A", A.class.getSimpleName());
        assertEquals("Test", Test.class.getSimpleName());
        assertEquals("", anonymous.getClass().getSimpleName());
        assertEquals("String[]", String[].class.getSimpleName());
        assertEquals("void", void.class.getSimpleName());
        assertEquals("boolean[]", boolean[].class.getSimpleName());
    }

    public void testGetTypeParameters() throws Exception {
        class Test <A, B extends String & Comparable<String>> {
        }
        TypeVariable<Class<Test>>[] parameters = Test.class.getTypeParameters();
        assertEquals(2, parameters.length);

        TypeVariable<Class<Test>> a = parameters[0];
        assertEquals("A", a.getName());
        assertEquals(Test.class, a.getGenericDeclaration());
        assertEquals(Object.class, singleton(a.getBounds()));

        TypeVariable<Class<Test>> b = parameters[1];
        assertEquals("B", b.getName());
        assertEquals(Test.class, b.getGenericDeclaration());
        Type[] bBounds = b.getBounds();
        assertEquals(2, bBounds.length);
        assertEquals(String.class, bBounds[0]);
        ParameterizedType comparable = (ParameterizedType) bBounds[1];
        assertEquals(Comparable.class, comparable.getRawType());
        assertNull(comparable.getOwnerType());
        assertEquals(String.class, singleton(comparable.getActualTypeArguments()));
    }

    public void testGetTypeParameters_NoParams() throws Exception {
        class Test {
        }
        assertEquals(0, Test.class.getTypeParameters().length);
        assertEquals(0, boolean[].class.getTypeParameters().length);
    }

    public void testIsAnnotation() throws Exception {
        assertTrue(MyStyle.class.isAnnotation());
        assertFalse(MyColor.class.isAnnotation());
        assertFalse(void.class.isAnnotation());
        assertFalse(int[].class.isAnnotation());
    }

    public void testIsAnnotationPresent() throws Exception {
        assertTrue(A.class.isAnnotationPresent(MyStyle.class));
        assertTrue(A.class.isAnnotationPresent(MyFormatter.class));

        assertTrue(B.class.isAnnotationPresent(MyStyle.class));
        assertFalse(B.class.isAnnotationPresent(MyFormatter.class));

        assertTrue(C.class.isAnnotationPresent(MyStyle.class));
        assertFalse(C.class.isAnnotationPresent(MyFormatter.class));

        assertFalse(void.class.isAnnotationPresent(MyStyle.class));
        assertFalse(long[].class.isAnnotationPresent(MyFormatter.class));
    }

    public void testIsAnonymousClass() throws Exception {
        class Test {
        }
        Serializable anonymous = new Serializable() { };
        assertTrue(anonymous.getClass().isAnonymousClass());
        assertFalse(Test.class.isAnonymousClass());
        assertFalse(A.class.isAnonymousClass());
        assertFalse(_ClassTestCase.class.isAnonymousClass());
        assertFalse(void.class.isAnonymousClass());
        assertFalse(short[].class.isAnonymousClass());
    }

    enum Case {
        UPPER {
            String toCase() {
                return this.name().toUpperCase();
            }
        },
        LOWER {
            String toCase() {
                return this.name().toLowerCase();
            }
        };
        abstract String toCase();
    }
    public void testIsEnum() throws Exception {
        assertTrue(MyColor.class.isEnum());
        assertTrue(MyColor.BLUE.getClass().isEnum());
        assertFalse(MyStyle.class.isEnum());
        assertFalse(void.class.isEnum());
        assertFalse(short[].class.isEnum());
        assertTrue(Case.class.isEnum());
        assertFalse(Case.UPPER.getClass().isEnum());
    }

    public void testIsLocalClass() throws Exception {
        class Test {
        }
        Serializable anonymous = new Serializable() { };
        assertTrue(Test.class.isLocalClass());
        assertFalse(anonymous.getClass().isLocalClass());
        assertFalse(A.class.isLocalClass());
        assertFalse(_ClassTestCase.class.isLocalClass());
        assertFalse(void.class.isLocalClass());
        assertFalse(short[].class.isLocalClass());
    }

    public void testIsMemberClass() throws Exception {
        class Test {
        }
        Serializable anonymous = new Serializable() { };
        assertTrue(A.class.isMemberClass());
        assertFalse(Test.class.isMemberClass());
        assertFalse(anonymous.getClass().isMemberClass());
        assertFalse(_ClassTestCase.class.isMemberClass());
        assertFalse(void.class.isMemberClass());
        assertFalse(short[].class.isMemberClass());
    }
}