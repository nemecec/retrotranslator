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

import junit.framework.TestCase;
import net.sf.retrotranslator.runtime.java.lang.MyStyle;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Taras Puchko
 */
public class _AnnotatedElementTestCase extends TestCase {

    @Retention(RetentionPolicy.CLASS)
    private @interface Invisible {}

    @Invisible
    private Class aClass = getClass();

    public void test() throws Exception {
        List<AnnotatedElement> elements = new ArrayList<AnnotatedElement>();
        elements.add(aClass.getPackage());
        elements.add(aClass);
        elements.add(aClass.getConstructors()[0]);
        elements.add(aClass.getDeclaredFields()[0]);
        elements.add(aClass.getMethods()[0]);
        for (AnnotatedElement element : elements) {
            assertFalse(element.isAnnotationPresent(MyStyle.class));
            assertNull(element.getAnnotation(MyStyle.class));
            assertEquals(0, element.getAnnotations().length);
            assertEquals(0, element.getDeclaredAnnotations().length);
        }
        if (!System.getProperty("java.vm.version").startsWith("R26.0.0")) {
            Method method = aClass.getDeclaredMethod("methodWithParameter", String.class);
            Annotation[] annotations = method.getParameterAnnotations()[0];
            assertNotNull(annotations);
            assertEquals(0, annotations.length);
        }
    }

    protected void methodWithParameter(@Invisible String s) {
        //empty
    }
}