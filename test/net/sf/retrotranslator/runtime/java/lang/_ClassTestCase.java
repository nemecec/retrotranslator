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

import java.io.Serializable;
import java.lang.reflect.*;
import java.util.*;
import net.sf.retrotranslator.tests.BaseTestCase;

/**
 * @author Taras Puchko
 */
@MyFormatter(tabPositions = {}, numbers = {1})
public class _ClassTestCase extends BaseTestCase {

    private static final Class<?> PROXY_CLASS = Proxy.getProxyClass(
            _ClassTestCase.class.getClassLoader(), Comparable.class);

    private static Class[] CLASSES_14 = {
            java.util.Collection.class,
            java.util.Set.class,
            java.util.List.class,
            java.util.Queue.class,
            java.util.Map.class,
            java.util.SortedSet.class,
            java.util.SortedMap.class,
            java.util.concurrent.BlockingQueue.class,
            java.util.concurrent.ConcurrentMap.class,
            java.util.HashSet.class,
            java.util.TreeSet.class,
            java.util.LinkedHashSet.class,
            java.util.ArrayList.class,
            java.util.LinkedList.class,
            java.util.PriorityQueue.class,
            java.util.HashMap.class,
            java.util.TreeMap.class,
            java.util.LinkedHashMap.class,
            java.util.Vector.class,
            java.util.Hashtable.class,
            java.util.WeakHashMap.class,
            java.util.IdentityHashMap.class,
            java.util.concurrent.CopyOnWriteArrayList.class,
            java.util.concurrent.CopyOnWriteArraySet.class,
            java.util.EnumSet.class,
            java.util.EnumMap.class,
            java.util.concurrent.ConcurrentLinkedQueue.class,
            java.util.concurrent.LinkedBlockingQueue.class,
            java.util.concurrent.ArrayBlockingQueue.class,
            java.util.concurrent.PriorityBlockingQueue.class,
            java.util.concurrent.DelayQueue.class,
            java.util.concurrent.SynchronousQueue.class,
            java.util.concurrent.ConcurrentHashMap.class,
            java.util.AbstractCollection.class,
            java.util.AbstractSet.class,
            java.util.AbstractList.class,
            java.util.AbstractSequentialList.class,
            java.util.AbstractQueue.class,
            java.util.AbstractMap.class,
            java.util.Enumeration.class,
            java.util.Iterator.class,
            java.util.ListIterator.class,
            java.lang.Comparable.class,
            java.util.Comparator.class
    };

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

        Integer[].class.cast(new Integer[]{});
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
        assertNull(PROXY_CLASS.getAnnotation(MyFormatter.class));
    }

    public void testGetAnnotations() throws Exception {
        assertEqualElements(A.class.getAnnotations(), A.class.getAnnotation(MyStyle.class), A.class.getAnnotation(MyFormatter.class));
        assertEqualElements(B.class.getAnnotations(), B.class.getAnnotation(MyStyle.class));
        assertEqualElements(C.class.getAnnotations(), C.class.getAnnotation(MyStyle.class));
        assertEquals(0, void.class.getAnnotations().length);
        assertEquals(0, PROXY_CLASS.getAnnotations().length);
    }

    public void testGetCanonicalName() throws Exception {
        class Test {
        }
        Serializable anonymous = new Serializable() {
        };
        assertEquals(this.getClass().getName(), this.getClass().getCanonicalName());
        assertEquals(this.getClass().getName() + ".A", A.class.getCanonicalName());
        assertNull(Test.class.getCanonicalName());
        assertNull(anonymous.getClass().getCanonicalName());
        assertEquals("java.lang.String[]", String[].class.getCanonicalName());
        assertNull(Test[].class.getCanonicalName());
        assertEquals("void", void.class.getCanonicalName());
        assertEquals("boolean[][]", boolean[][].class.getCanonicalName());
        assertEquals(PROXY_CLASS.getName(), PROXY_CLASS.getCanonicalName());
    }

    public void testGetDeclaredAnnotations() throws Exception {
        assertEqualElements(A.class.getDeclaredAnnotations(), A.class.getAnnotation(MyStyle.class), A.class.getAnnotation(MyFormatter.class));
        assertEquals(0, B.class.getDeclaredAnnotations().length);
        assertEqualElements(C.class.getAnnotations(), C.class.getAnnotation(MyStyle.class));
        assertEquals(0, int[][].class.getDeclaredAnnotations().length);
        assertEquals(0, PROXY_CLASS.getDeclaredAnnotations().length);
    }

    public void testGetDeclaredMethod() throws Exception {
        abstract class Getter<T> {
            abstract T get();
        }
        class GetterImpl extends Getter<String> {
            public String get() {
                throw new UnsupportedOperationException();
            }
        }
        assertEquals(String.class, GetterImpl.class.getDeclaredMethod("get").getReturnType());
        try {
            GetterImpl.class.getDeclaredMethod("get", int.class);
            fail();
        } catch (NoSuchMethodException e) {
            //ok
        }
        assertEquals(int.class, PROXY_CLASS.getDeclaredMethod("compareTo", Object.class).getReturnType());
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
        assertNull(PROXY_CLASS.getEnclosingClass());
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
        assertNull(PROXY_CLASS.getEnclosingConstructor());
    }

    public void testGetEnclosingMethod() throws Exception {
        class Inner {}
        assertEquals("testGetEnclosingMethod", Inner.class.getEnclosingMethod().getName());
        Object anonymous = new Object() {};
        assertEquals("testGetEnclosingMethod", anonymous.getClass().getEnclosingMethod().getName());
        assertNull(double.class.getEnclosingMethod());
        assertNull(PROXY_CLASS.getEnclosingMethod());
    }

    public void testGetEnumConstants() throws Exception {
        MyColor[] constants = MyColor.class.getEnumConstants();
        assertEqualElements(constants, MyColor.RED, MyColor.GREEN, MyColor.BLUE);
        assertNull(void.class.getEnumConstants());
        assertNull(int[].class.getEnumConstants());
        assertNull(PROXY_CLASS.getEnumConstants());
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
        assertEquals(Comparable.class, singleton(PROXY_CLASS.getGenericInterfaces()));
    }

    public void testGetGenericInterfaces_Classes14() throws Exception {
        for (Class aClass : CLASSES_14) {
            Class[] interfaces = aClass.getInterfaces();
            Type[] genericInterfaces = aClass.getGenericInterfaces();
            assertEquals(aClass.getName(), interfaces.length, genericInterfaces.length);
            for (int i = 0; i < interfaces.length; i++) {
                assertEqualClasses(aClass, interfaces[i], genericInterfaces[i]);
            }
        }
    }

    public void testGetGenericSuperclass_Classes14() throws Exception {
        for (Class aClass : CLASSES_14) {
            Class superclass = aClass.getSuperclass();
            Type genericSuperclass = aClass.getGenericSuperclass();
            if (superclass != null || genericSuperclass != null) {
                assertEqualClasses(aClass, superclass, genericSuperclass);
            }
        }
    }

    private void assertEqualClasses(Class owner, Class rawClass, Type genericType) {
        if (genericType instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) genericType).getRawType();
            assertSame(owner.getName(), rawClass, rawType);
        } else {
            assertSame(owner.getName(), rawClass, genericType);
            assertTrue(rawClass.getName(), rawClass == Object.class || rawClass == Cloneable.class ||
                    rawClass == Serializable.class || rawClass == RandomAccess.class);
        }
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
        assertEquals(Proxy.class, PROXY_CLASS.getGenericSuperclass());
    }

    public void testGetMethod() throws Exception {
        class StringList extends ArrayList<String> {
            public String get(int index) {
                return super.get(index);
            }
        }
        if (!System.getProperty("java.vm.version").equals("1.4.1-b21")) {
            //fails on JDK 1.4.1-b21
            assertEquals(String.class, StringList.class.getMethod("get", int.class).getReturnType());
        }
        try {
            StringList.class.getMethod("get", long.class);
            fail();
        } catch (NoSuchMethodException e) {
            //ok
        }
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
        assertEquals(PROXY_CLASS.getName(), PROXY_CLASS.getSimpleName());
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
        assertEquals(0, PROXY_CLASS.getTypeParameters().length);
    }

    public void testIsAnnotation() throws Exception {
        assertTrue(MyStyle.class.isAnnotation());
        assertFalse(MyColor.class.isAnnotation());
        assertFalse(void.class.isAnnotation());
        assertFalse(int[].class.isAnnotation());
        assertFalse(PROXY_CLASS.isAnnotation());
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
        assertFalse(PROXY_CLASS.isAnnotationPresent(MyFormatter.class));
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
        assertFalse(PROXY_CLASS.isAnonymousClass());
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
        assertFalse(PROXY_CLASS.isEnum());
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
        assertFalse(PROXY_CLASS.isLocalClass());
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
        assertFalse(PROXY_CLASS.isMemberClass());
    }

}