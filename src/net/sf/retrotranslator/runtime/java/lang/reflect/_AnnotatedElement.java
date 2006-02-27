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

import net.sf.retrotranslator.runtime.java.lang._Class;
import net.sf.retrotranslator.runtime.java.lang._Package;
import net.sf.retrotranslator.runtime.impl.Derived;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author Taras Puchko
 */
@Derived({Class.class, Constructor.class, Field.class, Method.class, Package.class})
public class _AnnotatedElement {

    public static Annotation getAnnotation(AnnotatedElement annotatedElement, Class annotationType) {
        if (annotatedElement instanceof Class) {
            return _Class.getAnnotation((Class) annotatedElement, annotationType);
        }
        if (annotatedElement instanceof Constructor) {
            return _Constructor.getAnnotation((Constructor) annotatedElement, annotationType);
        }
        if (annotatedElement instanceof Field) {
            return _Field.getAnnotation((Field) annotatedElement, annotationType);
        }
        if (annotatedElement instanceof Method) {
            return _Method.getAnnotation((Method) annotatedElement, annotationType);
        }
        if (annotatedElement instanceof Package) {
            return _Package.getAnnotation((Package) annotatedElement, annotationType);
        }
        return annotatedElement.getAnnotation(annotationType);
    }

    public static Annotation[] getAnnotations(AnnotatedElement annotatedElement) {
        if (annotatedElement instanceof Class) {
            return _Class.getAnnotations((Class) annotatedElement);
        }
        if (annotatedElement instanceof Constructor) {
            return _Constructor.getAnnotations((Constructor) annotatedElement);
        }
        if (annotatedElement instanceof Field) {
            return _Field.getAnnotations((Field) annotatedElement);
        }
        if (annotatedElement instanceof Method) {
            return _Method.getAnnotations((Method) annotatedElement);
        }
        if (annotatedElement instanceof Package) {
            return _Package.getAnnotations((Package) annotatedElement);
        }
        return annotatedElement.getAnnotations();
    }

    public static Annotation[] getDeclaredAnnotations(AnnotatedElement annotatedElement) {
        if (annotatedElement instanceof Class) {
            return _Class.getDeclaredAnnotations((Class) annotatedElement);
        }
        if (annotatedElement instanceof Constructor) {
            return _Constructor.getDeclaredAnnotations((Constructor) annotatedElement);
        }
        if (annotatedElement instanceof Field) {
            return _Field.getDeclaredAnnotations((Field) annotatedElement);
        }
        if (annotatedElement instanceof Method) {
            return _Method.getDeclaredAnnotations((Method) annotatedElement);
        }
        if (annotatedElement instanceof Package) {
            return _Package.getDeclaredAnnotations((Package) annotatedElement);
        }
        return annotatedElement.getDeclaredAnnotations();
    }

    public static boolean isAnnotationPresent(AnnotatedElement annotatedElement, Class annotationType) {
        if (annotatedElement instanceof Class) {
            return _Class.isAnnotationPresent((Class) annotatedElement, annotationType);
        }
        if (annotatedElement instanceof Constructor) {
            return _Constructor.isAnnotationPresent((Constructor) annotatedElement, annotationType);
        }
        if (annotatedElement instanceof Field) {
            return _Field.isAnnotationPresent((Field) annotatedElement, annotationType);
        }
        if (annotatedElement instanceof Method) {
            return _Method.isAnnotationPresent((Method) annotatedElement, annotationType);
        }
        if (annotatedElement instanceof Package) {
            return _Package.isAnnotationPresent((Package) annotatedElement, annotationType);
        }
        return annotatedElement.isAnnotationPresent(annotationType);
    }

}
