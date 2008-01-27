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

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import net.sf.retrotranslator.runtime.java.lang.*;
import net.sf.retrotranslator.tests.TestCaseBase;

/**
 * @author Taras Puchko
 */
public class _MethodTestCase extends TestCaseBase {

    private static final String NAME = "doAction";

    private static Method PROXY_METHOD = getProxyMethod();

    private static Method getProxyMethod() {
        try {
            return Proxy.getProxyClass(_MethodTestCase.class.getClassLoader(),
                    Comparable.class) .getMethod("compareTo", Object.class);
        } catch (NoSuchMethodException e) {
            throw new Error(e);
        }
    }

    @MyStyle("bold")
    public void doAction() {
    }

    @MyStyle("italic")
    public void doAction(String string) {
    }

    public void doAction(@MyStyle("glass") String string, int i) {
    }

    public void testGetAnnotationForAbsent() throws Exception {
        assertNull(PROXY_METHOD.getAnnotation(MyStyle.class));
    }

    public void testGetAnnotationForNoParam() throws Exception {
        Method noParamMethod = _MethodTestCase.class.getMethod(NAME);
        assertEquals("bold", noParamMethod.getAnnotation(MyStyle.class).value());
    }

    public void testGetAnnotationForOneParam() throws Exception {
        Method oneParamMethod = _MethodTestCase.class.getMethod(NAME, String.class);
        assertEquals("italic", oneParamMethod.getAnnotation(MyStyle.class).value());
    }

    public void testGetAnnotationForTwoParam() throws Exception {
        Method twoParamMethod = _MethodTestCase.class.getMethod(NAME, String.class, int.class);
        assertNull(twoParamMethod.getAnnotation(MyStyle.class));
    }

    public void testGetAnnotations() throws Exception {
        Method noParamMethod = _MethodTestCase.class.getMethod(NAME);
        assertEquals("bold", ((MyStyle) noParamMethod.getAnnotations()[0]).value());
        assertEquals(0, PROXY_METHOD.getAnnotations().length);
    }

    public void testGetDeclaredAnnotations() throws Exception {
        Method oneParamMethod = _MethodTestCase.class.getMethod(NAME, String.class);
        assertEquals("italic", ((MyStyle) oneParamMethod.getDeclaredAnnotations()[0]).value());
        assertEquals(0, PROXY_METHOD.getDeclaredAnnotations().length);
    }

    public void testGetDefaultValue() throws Exception {
        assertEquals(MyColor.RED, MyFormatter.class.getMethod("backgroundColor").getDefaultValue());
        assertNull(MyStyle.class.getMethod("value").getDefaultValue());
        assertNull(_MethodTestCase.class.getMethod(NAME).getDefaultValue());
    }

    public void testGetGenericExceptionTypes() throws Exception {
        class Test<T extends RuntimeException> {
            public <X extends Throwable> void m() throws T, X {
            }
        }
        Type[] types = Test.class.getMethod("m").getGenericExceptionTypes();

        TypeVariable variableT = (TypeVariable) types[0];
        assertEquals("T", variableT.getName());
        assertEquals(Test.class, variableT.getGenericDeclaration());
        assertEquals(RuntimeException.class, singleton(variableT.getBounds()));

        TypeVariable variableX = (TypeVariable) types[1];
        assertEquals("X", variableX.getName());
        assertEquals(Test.class.getMethod("m"), variableX.getGenericDeclaration());
        assertEquals(Throwable.class, singleton(variableX.getBounds()));
    }

    public void testGetGenericExceptionTypes_NotGeneric() throws Exception {
        class Test<T extends RuntimeException> {
            public void m() throws RuntimeException {
            }
        }
        assertEquals(RuntimeException.class, singleton(Test.class.getMethod("m").getGenericExceptionTypes()));
        assertEqualElements(PROXY_METHOD.getExceptionTypes(), (Object[]) PROXY_METHOD.getGenericExceptionTypes());
    }

    public void testGetGenericParameterTypes() throws Exception {
        class Test<T extends RuntimeException> {
            public <E> void m(T t, E e, Comparable<? super Integer> c, String s) throws RuntimeException {
            }
        }
        Method method = Test.class.getMethod("m", RuntimeException.class, Object.class, Comparable.class, String.class);
        Type[] types = method.getGenericParameterTypes();
        assertEquals(4, types.length);
        TypeVariable t = (TypeVariable) types[0];
        TypeVariable e = (TypeVariable) types[1];
        ParameterizedType c = (ParameterizedType) types[2];
        Class s = (Class) types[3];

        assertEquals("T", t.getName());
        assertEquals(Test.class, t.getGenericDeclaration());
        assertEquals(RuntimeException.class, singleton(t.getBounds()));

        assertEquals("E", e.getName());
        assertEquals(method, e.getGenericDeclaration());
        assertEquals(Object.class, singleton(e.getBounds()));

        assertEquals(Comparable.class, c.getRawType());
        assertNull(c.getOwnerType());
        WildcardType argument = (WildcardType) singleton(c.getActualTypeArguments());
        assertEquals(Integer.class, singleton(argument.getLowerBounds()));
        assertEquals(Object.class, singleton(argument.getUpperBounds()));

        assertEquals(String.class, s);
        assertEqualElements(PROXY_METHOD.getParameterTypes(), (Object[]) PROXY_METHOD.getGenericParameterTypes());
    }

    public void testGetGenericReturnType() throws Exception {
        class Test<T extends RuntimeException> {
            class Inner {
                public T m() {
                    return null;
                }
            }
        }
        TypeVariable variable = (TypeVariable) Test.Inner.class.getMethod("m").getGenericReturnType();
        assertEquals("T", variable.getName());
        assertEquals(Test.class, variable.getGenericDeclaration());
        assertEquals(RuntimeException.class, singleton(variable.getBounds()));
        assertEquals(PROXY_METHOD.getReturnType(), PROXY_METHOD.getGenericReturnType());
    }

    public void testGetParameterAnnotationsForTwoParam() throws Exception {
        Method twoParamMethod = _MethodTestCase.class.getMethod(NAME, String.class, int.class);
        Annotation[][] annotations = twoParamMethod.getParameterAnnotations();
        assertEquals(2, annotations.length);
        assertEquals(1, annotations[0].length);
        assertEquals(0, annotations[1].length);
        assertEquals("glass", ((MyStyle) annotations[0][0]).value());
        if (!System.getProperty("java.vm.version").startsWith("R26.0.0")) {
            assertEquals(0, singleton(PROXY_METHOD.getParameterAnnotations()).length);
        }
    }

    public void testGetTypeParameters() throws Exception {
        class Test<T extends String> {
            public <E extends Number> void m(T t, E e) throws RuntimeException {
            }
        }
        Method method = Test.class.getMethod("m", String.class, Number.class);
        TypeVariable<Method> variable = singleton(method.getTypeParameters());
        assertEquals("E", variable.getName());
        assertEquals(Number.class, singleton(variable.getBounds()));
        assertEquals(method, variable.getGenericDeclaration());
        assertEquals(0, PROXY_METHOD.getTypeParameters().length);
    }

    public void testIsAnnotationPresent() throws Exception {
        assertTrue(_MethodTestCase.class.getMethod(NAME).isAnnotationPresent(MyStyle.class));
        assertFalse(_MethodTestCase.class.getMethod(NAME).isAnnotationPresent(MyFormatter.class));
        assertFalse(PROXY_METHOD.isAnnotationPresent(MyFormatter.class));
    }

    public void testIsBridge() throws Exception {
        class Test implements Comparable<String> {
            public int compareTo(String o) {
                return 0;
            }
        }
        assertTrue(Test.class.getDeclaredMethod("compareTo", Object.class).isBridge());
        assertFalse(Test.class.getDeclaredMethod("compareTo", String.class).isBridge());
        assertFalse(PROXY_METHOD.isBridge());
    }

    public void testIsSynthetic() throws Exception {
        class Test implements Comparable<String> {
            public int compareTo(String o) {
                return 0;
            }
        }
        assertTrue(Test.class.getMethod("compareTo", Object.class).isSynthetic());
        assertFalse(Test.class.getMethod("compareTo", String.class).isSynthetic());
        assertFalse(PROXY_METHOD.isSynthetic());
    }

    interface VarargsInterface {
        void m1(String... s);

        void m2(String s);
    }

    public void testIsVarArgs() throws Exception {
        abstract class Test {
            public void m1(String... s) {
            }

            public void m2(String s) {
            }
        }
        assertTrue(Test.class.getMethod("m1", String[].class).isVarArgs());
        assertFalse(Test.class.getMethod("m2", String.class).isVarArgs());

        assertTrue(VarargsInterface.class.getMethod("m1", String[].class).isVarArgs());
        assertFalse(VarargsInterface.class.getMethod("m2", String.class).isVarArgs());

        assertFalse(PROXY_METHOD.isVarArgs());
    }

    class Test<T extends String, RE extends RuntimeException> {
        public <E extends Number> void m(T t, E e, String[] strings) throws RE, ClassNotFoundException {
        }
    }

    public void testGetGenericString() throws Exception {
        assertEquals("public <E> void " +
                this.getClass().getName() +
                "$Test.m(T,E,java.lang.String[])" +
                " throws RE,java.lang.ClassNotFoundException",
                Test.class.getDeclaredMethods()[0].toGenericString());
        assertEquals(PROXY_METHOD.toString(), PROXY_METHOD.toGenericString());
    }
}