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
package net.sf.retrotranslator.runtime.impl;

import java.lang.reflect.*;
import java.util.*;
import net.sf.retrotranslator.runtime.asm.*;
import net.sf.retrotranslator.runtime.asm.Type;
import net.sf.retrotranslator.runtime.java.lang.annotation.*;
import net.sf.retrotranslator.runtime.java.lang.Enum_;

/**
 * @author Taras Puchko
 */
public abstract class AnnotatedElementDescriptor extends EmptyVisitor {

    private static final Annotation_[] EMPTY = new Annotation_[0];
    protected static final EmptyVisitor EMPTY_VISITOR = new EmptyVisitor();

    protected int access;

    private LazyList<AnnotationValue, Annotation_> declaredAnnotations = new LazyList<AnnotationValue, Annotation_>() {
        protected Annotation_ resolve(AnnotationValue input) {
            return createAnnotation(input);
        }

        protected Annotation_[] newArray(int size) {
            return new Annotation_[size];
        }
    };

    private LazyValue<LazyList<AnnotationValue, Annotation_>, Annotation_[]> annotations
            = new LazyValue<LazyList<AnnotationValue, Annotation_>, Annotation_[]>(declaredAnnotations) {
        protected Annotation_[] resolve(LazyList<AnnotationValue, Annotation_> input) {
            return createAnnotations(input.getLive());
        }
    };

    public boolean isAccess(int mask) {
        return (access & mask) != 0;
    }

    public boolean isAnnotationPresent(Class annotationType) {
        for (Annotation_ annotation : annotations.get()) {
            if (annotationType.isInstance(annotation)) return true;
        }
        return false;
    }

    public Annotation_ getAnnotation(Class annotationType) {
        for (Annotation_ annotation : annotations.get()) {
            if (annotationType.isInstance(annotation)) return annotation;
        }
        return null;
    }

    public Annotation_[] getAnnotations() {
        Annotation_[] result = annotations.get();
        return result.length == 0 ? result : result.clone();
    }

    public Annotation_[] getDeclaredAnnotations() {
        return declaredAnnotations.getClone();
    }

    public abstract ClassDescriptor getClassDescriptor();

    protected abstract TypeVariable findTypeVariable(String name);

    protected abstract Annotation_[] createAnnotations(Annotation_[] declaredAnnotations);

    protected Annotation_[] createAnnotations(List<AnnotationValue> values) {
        if (values == null) return EMPTY;
        Annotation_[] result = new Annotation_[values.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = createAnnotation(values.get(i));
        }
        return result;
    }

    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (!visible) return EMPTY_VISITOR;
        AnnotationValue value = new AnnotationValue(desc);
        declaredAnnotations.add(value);
        return value;
    }

    private ClassLoader getClassLoader() {
        return getClassDescriptor().getTarget().getClassLoader();
    }

    protected Class getClassByInternalName(String name) {
        name = name.replace('/', '.');
        try {
            return Class.forName(name, false, getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new TypeNotPresentException(name, e);
        }
    }

    protected Class getClassByType(net.sf.retrotranslator.runtime.asm.Type type) {
        Class baseClass = RuntimeTools.getBaseClass(type);
        if (baseClass != null) return baseClass;
        return getClassByInternalName(type.getSort() == Type.ARRAY ? type.getDescriptor() : type.getInternalName());
    }

    private Class getClassByDesc(String desc) {
        return getClassByType(net.sf.retrotranslator.runtime.asm.Type.getType(desc));
    }

    public java.lang.reflect.Type[] createTypes(List<TypeDescriptor> descriptors) {
        if (descriptors == null) return null;
        java.lang.reflect.Type[] result = new java.lang.reflect.Type[descriptors.size()];
        Iterator<TypeDescriptor> iterator = descriptors.iterator();
        for (int i = 0; i < result.length; i++) {
            result[i] = createType(iterator.next());
        }
        return result;
    }

    public java.lang.reflect.Type createType(TypeDescriptor descriptor) {
        if (descriptor == null) return null;
        if (descriptor.arrayType != null) {
            return new GenericArrayTypeImpl(createType(descriptor.arrayType));
        }
        if (descriptor.typeVariable != null) {
            return findTypeVariable(descriptor.typeVariable);
        }
        LinkedList<ClassTypeElement> elements = descriptor.elements;
        if (elements != null) {
            return createClassType(elements.toArray(new ClassTypeElement[elements.size()]));
        }
        return RuntimeTools.getBaseClass(descriptor.baseType);
    }

    private java.lang.reflect.Type createClassType(ClassTypeElement[] typeElements) {
        class Element {
            Class rawType;
            List<TypeArgument> arguments;

            public Element(Class rawType, List<TypeArgument> arguments) {
                this.rawType = rawType;
                this.arguments = arguments;
            }
        }
        String className = null;
        for (ClassTypeElement typeElement : typeElements) {
            className = (className == null ? "" : className + "$") + typeElement.getName();
        }
        LinkedList<Element> elements = new LinkedList<Element>();
        Class currentClass = getClassByInternalName(className);
        for (int i = typeElements.length - 1; i >= 0; i--) {
            elements.addFirst(new Element(currentClass, typeElements[i].getArguments()));
            currentClass = currentClass.getDeclaringClass();
            if (currentClass == null) break;
        }
        java.lang.reflect.Type result = elements.getFirst().arguments.isEmpty() ? null : currentClass;
        for (Element element : elements) {
            result = (result == null && element.arguments.isEmpty()) ? element.rawType
                    : new ParameterizedTypeImpl(createArguments(element.arguments), element.rawType, result);
        }
        return result;
    }

    private java.lang.reflect.Type[] createArguments(List<TypeArgument> arguments) {
        java.lang.reflect.Type[] result = new java.lang.reflect.Type[arguments.size()];
        Iterator<TypeArgument> iterator = arguments.iterator();
        for (int i = 0; i < result.length; i++) {
            TypeArgument argument = iterator.next();
            result[i] = argument.wildcard == INSTANCEOF ? createType(argument.descriptor)
                    : new WildcardTypeImpl(argument.wildcard == EXTENDS, getLazyType(argument.descriptor));
        }
        return result;
    }

    private Annotation_ createAnnotation(AnnotationValue annotationValue) {
        Class annotationType = getClassByDesc(annotationValue.getDesc());
        StringBuffer buffer = new StringBuffer("@").append(annotationType.getName()).append('(');
        Map<String, Object> values = new HashMap<String, Object>();
        boolean isFirst = true;
        for (MethodDescriptor descriptor : ClassDescriptor.getInstance(annotationType).getMethodDescriptors()) {
            if (isFirst) {
                isFirst = false;
            } else {
                buffer.append(", ");
            }
            String elementName = descriptor.getName();
            Object elementValue = annotationValue.getElement(elementName);
            Object resolvedValue = elementValue == null ? descriptor.getDefaultValue()
                    : resolveValue(elementValue, descriptor.getReturnType(), descriptor);
            if (resolvedValue == null) throw new IncompleteAnnotationException_(annotationType, elementName);
            values.put(elementName, resolvedValue);
            buffer.append(elementName).append('=');
            append(buffer, resolvedValue);
        }
        buffer.append(")");
        ClassLoader classLoader;
        Class[] interfaces;
        if (Annotation_.class.isAssignableFrom(annotationType)) {
            classLoader = getClassLoader();
            interfaces = new Class[]{annotationType};
        } else {
            classLoader = getProxyClassLoader(annotationType);
            interfaces = new Class[]{annotationType, Annotation_.class};
        }
        return (Annotation_) Proxy.newProxyInstance(classLoader,
                interfaces, new AnnotationHandler(annotationType, buffer.toString(), values));
    }

    private ClassLoader getProxyClassLoader(Class annotationType) {
        try {
            if (Annotation_.class == Class.forName(Annotation_.class.getName(), false, getClassLoader())) {
                return getClassLoader();
            }
        } catch (ClassNotFoundException e) {
            // ignore
        }
        try {
            if (annotationType == Class.forName(annotationType.getName(), false, Annotation_.class.getClassLoader())) {
                return Annotation_.class.getClassLoader();
            }
        } catch (ClassNotFoundException e) {
            // ignore
        }
        return getClassLoader();
    }
    
    protected Object resolveValue(Object value, Class type, MethodDescriptor descriptor) {
        if (value == null) return null;
        if (value instanceof net.sf.retrotranslator.runtime.asm.Type) {
            value = getClassByType((net.sf.retrotranslator.runtime.asm.Type) value);
        } else if (value instanceof EnumValue) {
            EnumValue enumValue = (EnumValue) value;
            value = getEnumValue(getClassByDesc(enumValue.getDescriptor()), enumValue.getValue());
        } else if (value instanceof AnnotationValue) {
            value = createAnnotation((AnnotationValue) value);
        } else if (value instanceof AnnotationArray) {
            AnnotationArray array = (AnnotationArray) value;
            Class componentType = type.getComponentType();
            List<Object> values = array.getValues();
            value = Array.newInstance(componentType, values.size());
            for (int i = Array.getLength(value) - 1; i >= 0; i--) {
                Array.set(value, i, resolveValue(values.get(i), componentType, descriptor));
            }
        }
        if (!type.isPrimitive() && !type.isInstance(value)) {
            throw new AnnotationTypeMismatchException_(descriptor.getMethod(), type.getName());
        }
        return value;
    }

    private Object getEnumValue(Class enumType, String name) {
        try {
            return Enum_.valueOf(enumType, name);
        } catch (IllegalArgumentException e) {
            try {
                return enumType.getMethod("valueOf", String.class).invoke(null, name);
            } catch (Exception ex) {
                throw e;
            }
        }
    }

    private static void append(StringBuffer buffer, Object value) {
        if (value.getClass().isArray()) {
            buffer.append('[');
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                if (i > 0) buffer.append(", ");
                append(buffer, Array.get(value, i));
            }
            buffer.append(']');
        } else {
            buffer.append(value);
        }
    }

    protected LazyValue<TypeDescriptor, java.lang.reflect.Type> getLazyType(TypeDescriptor descriptor) {
        if (descriptor == null) return null;
        return new LazyValue<TypeDescriptor, java.lang.reflect.Type>(descriptor) {
            protected java.lang.reflect.Type resolve(TypeDescriptor input) {
                return createType(input);
            }
        };
    }

    protected LazyList<TypeDescriptor, java.lang.reflect.Type> getLazyList() {
        return new LazyList<TypeDescriptor, java.lang.reflect.Type>() {

            protected java.lang.reflect.Type resolve(TypeDescriptor descriptor) {
                return createType(descriptor);
            }

            protected java.lang.reflect.Type[] newArray(int size) {
                return new java.lang.reflect.Type[size];
            }
        };
    }

}
