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
package net.sf.retrotranslator.runtime.java.lang.reflect;

import net.sf.retrotranslator.runtime.java.lang.MyColor;
import net.sf.retrotranslator.runtime.java.lang.MyFormatter;
import net.sf.retrotranslator.runtime.java.lang.MyStyle;
import net.sf.retrotranslator.tests.BaseTestCase;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Taras Puchko
 */
public class _FieldTestCase extends BaseTestCase {

    private static Field PROXY_FIELD = getProxyField();

    private static Field getProxyField() {
        try {
            return Proxy.getProxyClass(_FieldTestCase.class.getClassLoader(), Comparable.class).getDeclaredField("m0");
        } catch (NoSuchFieldException e) {
            throw new Error(e);
        }
    }

    @MyFormatter(
            pattern = "aabbcc",
            format = SimpleDateFormat.class,
            backgroundColor = MyColor.BLUE,
            backgroundStyle = @MyStyle("italic"),
            tabPositions = {10, 20, 30},
            keywords = {"my", "formatter"},
            colors = {MyColor.RED, MyColor.GREEN},
            styles = {@MyStyle("bold"), @MyStyle("small")}
    )
    protected String message;

    private Field field;

    protected void setUp() throws Exception {
        super.setUp();
        field = getClass().getDeclaredField("message");
    }

    private MyFormatter getAnnotation() {
        return field.getAnnotation(MyFormatter.class);
    }

    public void testGetAnnotation() {
        MyFormatter formatter = getAnnotation();
        assertEquals("aabbcc", formatter.pattern());
        assertEquals("en", formatter.lang());
        assertEquals(SimpleDateFormat.class, formatter.format());
        assertEquals(MyColor.BLUE, formatter.backgroundColor());
        assertEquals("italic", formatter.backgroundStyle().value());

        int[] tabPositions = formatter.tabPositions();
        assertEquals(3, tabPositions.length);
        assertEquals(10, tabPositions[0]);
        assertEquals(20, tabPositions[1]);
        assertEquals(30, tabPositions[2]);

        String[] keywords = formatter.keywords();
        assertEquals(2, keywords.length);
        assertEquals("my", keywords[0]);
        assertEquals("formatter", keywords[1]);

        MyColor[] colors = formatter.colors();
        assertEquals(2, colors.length);
        assertSame(MyColor.RED, colors[0]);
        assertSame(MyColor.GREEN, colors[1]);

        MyStyle[] styles = formatter.styles();
        assertEquals(2, styles.length);
        assertEquals("bold", styles[0].value());
        assertEquals("small", styles[1].value());
        assertNull(PROXY_FIELD.getAnnotation(MyFormatter.class));
    }

    public void testGetAnnotation_Equals() throws Exception {
        MyFormatter first = getAnnotation();
        MyFormatter second = getAnnotation();
        assertEquals(first, second);
        assertEquals(getAnnotation(), pump(getAnnotation()));
    }

    public void testGetAnnotation_HashCode() {
        int first = getAnnotation().hashCode();
        int second = getAnnotation().hashCode();
        assertEquals(first, second);
    }

    public void testGetAnnotation_ToString() throws Exception {
        StringBuffer buffer = new StringBuffer(getAnnotation().toString());
        delete(buffer, "pattern=aabbcc");
        delete(buffer, "lang=en");
        delete(buffer, "format=class java.text.SimpleDateFormat");
        delete(buffer, "backgroundColor=BLUE");
        delete(buffer, "backgroundStyle=@" + MyStyle.class.getName() + "(value=italic)");
        delete(buffer, "tabPositions=[10, 20, 30]");
        delete(buffer, "keywords=[my, formatter]");
        delete(buffer, "colors=[RED, GREEN]");
        delete(buffer, "styles=[@" + MyStyle.class.getName() + "(value=bold), @" + MyStyle.class.getName() + "(value=small)]");
        delete(buffer, "numbers=[1, 2, 3]");
        delete(buffer, "isPlain=false");
        assertEquals("@" + MyFormatter.class.getName() + "(, , , , , , , , , , )", buffer.toString());
    }

    private static void delete(StringBuffer buffer, String substring) {
        int index = buffer.indexOf(substring);
        if (index == -1) {
            substring = substring.replaceAll("\\s", "");
            index = buffer.indexOf(substring);
        }
        assertFalse("Cannot find: " + substring + " in " + buffer, index == -1);
        buffer.delete(index, index + substring.length());
    }

    public void testGetAnnotations() throws Exception {
        Annotation[] annotations = field.getAnnotations();
        assertEquals(1, annotations.length);
        assertEquals(getAnnotation(), annotations[0]);
        assertEquals(0, PROXY_FIELD.getAnnotations().length);
    }

    public void testGetDeclaredAnnotations() throws Exception {
        assertTrue(Arrays.equals(field.getDeclaredAnnotations(), field.getAnnotations()));
        assertEquals(0, PROXY_FIELD.getDeclaredAnnotations().length);
    }

    public void testGetGenericType() throws Exception {
        class Outer {
            class Top<A> {
                class Middle<B> {
                    class Bottom {
                    }
                }
            }
        }

        class Test<T> {
            public Outer.Top<Comparable<? super Integer>>.Middle<List<?>>.Bottom f;
        }
        Field field = Test.class.getField("f");
        ParameterizedType bottom = (ParameterizedType) field.getGenericType();
        assertEquals(Outer.Top.Middle.Bottom.class, bottom.getRawType());
        assertEquals(0, bottom.getActualTypeArguments().length);
        ParameterizedType middle = (ParameterizedType) bottom.getOwnerType();
        assertEquals(Outer.Top.Middle.class, middle.getRawType());

        ParameterizedType list = (ParameterizedType) singleton(middle.getActualTypeArguments());
        assertEquals(List.class, list.getRawType());
        assertNull(list.getOwnerType());
        WildcardType listParam = (WildcardType) singleton(list.getActualTypeArguments());
        assertEquals(Object.class, singleton(listParam.getUpperBounds()));
        assertEquals(0, listParam.getLowerBounds().length);

        ParameterizedType top = (ParameterizedType) middle.getOwnerType();
        assertEquals(Outer.Top.class, top.getRawType());
        ParameterizedType comparable = (ParameterizedType) singleton(top.getActualTypeArguments());
        assertEquals(Comparable.class, comparable.getRawType());
        assertNull(comparable.getOwnerType());

        WildcardType comparableParam = (WildcardType) singleton(comparable.getActualTypeArguments());
        assertEquals(Object.class, singleton(comparableParam.getUpperBounds()));
        assertEquals(Integer.class, singleton(comparableParam.getLowerBounds()));
        assertEquals(Outer.class, top.getOwnerType());
        assertEquals(PROXY_FIELD.getType(), PROXY_FIELD.getGenericType());
    }

    public void testIsAnnotationPresent() throws Exception {
        assertTrue(field.isAnnotationPresent(MyFormatter.class));
        assertFalse(field.isAnnotationPresent(MyStyle.class));
        assertFalse(PROXY_FIELD.isAnnotationPresent(MyStyle.class));
    }

    public void testIsEnumConstant() throws Exception {
        assertTrue(MyColor.class.getField(MyColor.BLUE.name()).isEnumConstant());
        assertFalse(field.isEnumConstant());
        assertFalse(PROXY_FIELD.isEnumConstant());
    }

    public void testIsSynthetic() throws Exception {
        class Test {
        }
        assertTrue(Test.class.getDeclaredFields()[0].isSynthetic());
        assertFalse(PROXY_FIELD.isSynthetic());
    }

    static class Test<T extends Map> {
        public Comparable<T>[] c;
        public static boolean b;
        public static Test<HashMap>.Inner i;

        public class Inner {}
    }

    public void testToGenericString() throws Exception {
        String name = this.getClass().getName();
        assertEquals("public java.lang.Comparable<T>[] " + name + "$Test.c", 
                Test.class.getField("c").toGenericString());
        assertEquals("public static boolean " + name + "$Test.b",
                Test.class.getField("b").toGenericString());
        assertEquals("public static " + name + "." + name + "$Test<java.util.HashMap>.Inner " + name + "$Test.i",
                Test.class.getField("i").toGenericString());
        assertEquals(PROXY_FIELD.toString(), PROXY_FIELD.toGenericString());
    }
}