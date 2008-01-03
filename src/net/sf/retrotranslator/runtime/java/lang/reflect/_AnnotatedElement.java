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
import net.sf.retrotranslator.runtime.java.lang.*;
import net.sf.retrotranslator.runtime.java.lang.annotation.Annotation_;

/**
 * @author Taras Puchko
 */
public class _AnnotatedElement {

    public static boolean executeInstanceOfInstruction(Object object) {
        return object instanceof Class ||
                object instanceof Constructor ||
                object instanceof Field ||
                object instanceof Method ||
                object instanceof Package ||
                object instanceof AnnotatedElement_;
    }

    public static Object executeCheckCastInstruction(Object object) {
        if (object instanceof Class) {
            return (Class) object;
        }
        if (object instanceof Constructor) {
            return (Constructor) object;
        }
        if (object instanceof Field) {
            return (Field) object;
        }
        if (object instanceof Method) {
            return (Method) object;
        }
        if (object instanceof Package) {
            return (Package) object;
        }
        return (AnnotatedElement_) object;
    }

    public static Annotation_ getAnnotation(Object object, Class<? extends Annotation_> annotationType) {
        if (object instanceof Class) {
            return _Class.getAnnotation((Class) object, annotationType);
        }
        if (object instanceof Constructor) {
            return _Constructor.getAnnotation((Constructor) object, annotationType);
        }
        if (object instanceof Field) {
            return _Field.getAnnotation((Field) object, annotationType);
        }
        if (object instanceof Method) {
            return _Method.getAnnotation((Method) object, annotationType);
        }
        if (object instanceof Package) {
            return _Package.getAnnotation((Package) object, annotationType);
        }
        return ((AnnotatedElement_) object).getAnnotation(annotationType);
    }

    public static Annotation_[] getAnnotations(Object object) {
        if (object instanceof Class) {
            return _Class.getAnnotations((Class) object);
        }
        if (object instanceof Constructor) {
            return _Constructor.getAnnotations((Constructor) object);
        }
        if (object instanceof Field) {
            return _Field.getAnnotations((Field) object);
        }
        if (object instanceof Method) {
            return _Method.getAnnotations((Method) object);
        }
        if (object instanceof Package) {
            return _Package.getAnnotations((Package) object);
        }
        return ((AnnotatedElement_) object).getAnnotations();
    }

    public static Annotation_[] getDeclaredAnnotations(Object object) {
        if (object instanceof Class) {
            return _Class.getDeclaredAnnotations((Class) object);
        }
        if (object instanceof Constructor) {
            return _Constructor.getDeclaredAnnotations((Constructor) object);
        }
        if (object instanceof Field) {
            return _Field.getDeclaredAnnotations((Field) object);
        }
        if (object instanceof Method) {
            return _Method.getDeclaredAnnotations((Method) object);
        }
        if (object instanceof Package) {
            return _Package.getDeclaredAnnotations((Package) object);
        }
        return ((AnnotatedElement_) object).getDeclaredAnnotations();
    }

    public static boolean isAnnotationPresent(Object object, Class<? extends Annotation_> annotationType) {
        if (object instanceof Class) {
            return _Class.isAnnotationPresent((Class) object, annotationType);
        }
        if (object instanceof Constructor) {
            return _Constructor.isAnnotationPresent((Constructor) object, annotationType);
        }
        if (object instanceof Field) {
            return _Field.isAnnotationPresent((Field) object, annotationType);
        }
        if (object instanceof Method) {
            return _Method.isAnnotationPresent((Method) object, annotationType);
        }
        if (object instanceof Package) {
            return _Package.isAnnotationPresent((Package) object, annotationType);
        }
        return ((AnnotatedElement_) object).isAnnotationPresent(annotationType);
    }

}
