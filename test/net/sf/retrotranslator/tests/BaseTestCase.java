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
package net.sf.retrotranslator.tests;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.Callable;
import junit.framework.TestCase;

/**
 * @author Taras Puchko
 */
public abstract class BaseTestCase extends TestCase {

    protected static final Locale HINDI = new Locale("hi", "IN");

    protected BaseTestCase() {
    }

    protected BaseTestCase(String string) {
        super(string);
    }

    protected static ParameterizedType getParameterizedType(Class aClass) {
        return (ParameterizedType) getType(aClass);
    }

    protected static Type getType(Class aClass) {
        try {
            return aClass.getField("f").getGenericType();
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    protected static <E> E singleton(E[] array) {
        assertEquals(1, array.length);
        return array[0];
    }

    public static void assertEqualElements(Object[] a, Object... b) {
        Set set = new HashSet<Object>(Arrays.asList(a));
        assertEquals(set.size(), a.length);
        for (Object object : b) {
            assertTrue(set.remove(object));
        }
        assertTrue(set.isEmpty());
    }

    protected Object pump(Object o) throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        new ObjectOutputStream(stream).writeObject(o);
        return new ObjectInputStream(new ByteArrayInputStream(stream.toByteArray())).readObject();
    }

    protected void assertFormat(String expected, String format, Object... argument) {
        assertFormat(Locale.FRANCE, expected, format, argument);
    }

    protected void assertFormat(Locale locale, String expected, String format, Object... argument) {
        assertEquals(expected, new Formatter(Locale.GERMANY).format(locale, format, argument).toString());
    }

    protected void assertFormatException(Class<? extends RuntimeException> expected, String format, Object... argument) {
        try {
            Formatter formatter = new Formatter().format(Locale.FRANCE, format, argument);
            fail("Result: '" + formatter + "', but expected exception: " + expected);
        } catch (RuntimeException e) {
            if (!expected.isInstance(e)) {
                throw e;
            }
        }
    }

    protected static String readLine(File file, String csn) throws Exception {
        FileInputStream stream = new FileInputStream(file);
        BufferedReader reader = new BufferedReader(csn == null
                ? new InputStreamReader(stream) : new InputStreamReader(stream, csn) );
        try {
            return reader.readLine();
        } finally {
            reader.close();
        }
    }

    protected void gc(Callable<Boolean> predicate) throws Exception {
        System.gc();
        try {
            List<long[]> list = new ArrayList<long[]>();
            while (predicate.call() && list.size() < Integer.MAX_VALUE) {
                list.add(new long[1000000]);
            }
        } catch (OutOfMemoryError e) {
            // ok
        }
    }

}
