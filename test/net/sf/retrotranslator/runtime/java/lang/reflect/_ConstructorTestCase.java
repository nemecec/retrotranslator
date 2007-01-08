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
package net.sf.retrotranslator.runtime.java.lang.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import net.sf.retrotranslator.runtime.java.lang.*;
import net.sf.retrotranslator.tests.BaseTestCase;

/**
 * @author Taras Puchko
 */
public class _ConstructorTestCase extends BaseTestCase {

    @MyStyle("bold")
    public _ConstructorTestCase() {
    }

    @MyStyle("italic")
    public _ConstructorTestCase(String string) {
        super(string);
    }

    public _ConstructorTestCase(String string, @MyStyle("glass") int i) throws RuntimeException {
        super(string + i);
    }

    public _ConstructorTestCase(String string, int... numbers) {
        super(string + numbers.length);
    }

    public void testGetAnnotationForNoParam() throws Exception {
        Constructor<_ConstructorTestCase> noParamConstructor = _ConstructorTestCase.class.getConstructor();
        assertEquals("bold", noParamConstructor.getAnnotation(MyStyle.class).value());
    }

    public void testGetAnnotationForOneParam() throws Exception {
        Constructor<_ConstructorTestCase> oneParamConstructor = _ConstructorTestCase.class.getConstructor(String.class);
        assertEquals("italic", oneParamConstructor.getAnnotation(MyStyle.class).value());
    }

    public void testGetAnnotationForTwoParam() throws Exception {
        Constructor<_ConstructorTestCase> twoParamConstructor = _ConstructorTestCase.class.getConstructor(String.class, int.class);
        assertNull(twoParamConstructor.getAnnotation(MyStyle.class));
    }

    public void testGetDeclaredAnnotations() throws Exception {
        Constructor<_ConstructorTestCase> constructor = _ConstructorTestCase.class.getConstructor();
        assertEqualElements(constructor.getAnnotations(), (Object[]) constructor.getDeclaredAnnotations());
    }

    public void testGetGenericExceptionTypes() throws Exception {
        Constructor<_ConstructorTestCase> constructor = _ConstructorTestCase.class.getConstructor(String.class, int.class);
        assertEquals(RuntimeException.class, singleton(constructor.getGenericExceptionTypes()));
    }

    public void testGetGenericParameterTypes() throws Exception {
        Constructor<_ConstructorTestCase> constructor = _ConstructorTestCase.class.getConstructor(String.class);
        assertEquals(String.class, constructor.getGenericParameterTypes()[0]);
    }

    public void testGetParameterAnnotationsForTwoParam() throws Exception {
        Constructor<_ConstructorTestCase> twoParamConstructor = _ConstructorTestCase.class.getConstructor(String.class, int.class);
        Annotation[][] annotations = twoParamConstructor.getParameterAnnotations();
        assertEquals(2, annotations.length);
        assertEquals(0, annotations[0].length);
        assertEquals(1, annotations[1].length);
        assertEquals("glass", ((MyStyle) annotations[1][0]).value());
    }

    public void testGetTypeParameters() throws Exception {
        Constructor<_ConstructorTestCase> constructor = _ConstructorTestCase.class.getConstructor(String.class, int.class);
        assertEquals(0, constructor.getTypeParameters().length);
    }

    public void testIsAnnotationPresent() throws Exception {
        Constructor<_ConstructorTestCase> constructor = _ConstructorTestCase.class.getConstructor();
        assertTrue(constructor.isAnnotationPresent(MyStyle.class));
        assertFalse(constructor.isAnnotationPresent(MyFormatter.class));
    }

    private static class Test {
        private Test() {
        }
    }

    public void testIsSynthetic() throws Exception {
        new Test();
        Constructor[] constructors = Test.class.getDeclaredConstructors();
        assertEquals(2, constructors.length);
        Constructor syntheticConstructor = null;
        for (Constructor constructor : constructors) {
            if (constructor.isSynthetic()) syntheticConstructor = constructor;
        }
        assertNotNull(syntheticConstructor);
        Class parameterType = syntheticConstructor.getParameterTypes()[0];
        assertTrue(parameterType.isSynthetic());
        assertFalse(Test.class.isSynthetic());
        assertFalse(_ConstructorTestCase.class.isSynthetic());
    }

    public void testIsVarArgs() throws Exception {
        assertTrue(_ConstructorTestCase.class.getConstructor(String.class, int[].class).isVarArgs());
        assertFalse(_ConstructorTestCase.class.getConstructor(String.class, int.class).isVarArgs());
    }

    public void testGetGenericString() throws Exception {
        class Test<T extends String, RE extends RuntimeException> {
            public <E extends Number> Test(T t, E e, String[] strings) throws RE, ClassNotFoundException {
            }
        }

        assertEquals("public <E> " +
                this.getClass().getName() +
                "$1Test(T,E,java.lang.String[])" +
                " throws RE,java.lang.ClassNotFoundException",
                Test.class.getConstructors()[0].toGenericString());
    }
}