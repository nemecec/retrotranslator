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
package net.sf.retrotranslator.runtime.impl;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Taras Puchko
 */
public class MethodDescriptor extends GenericDeclarationDescriptor {

    private String name;
    private String desc;
    private ClassDescriptor classDescriptor;
    private LazyValue<Class, Method> method;
    private LazyValue<String, Class> returnType;
    private LazyValue<Object, Object> defaultValue;
    private LazyValue<Class, Constructor> constructor;
    private LazyValue<TypeDescriptor, Type> genericReturnType;
    private LazyList<TypeDescriptor,Type> genericParameterTypes;
    private LazyList<TypeDescriptor,Type> genericExceptionTypes;
    private LazyList<List<AnnotationValue>, Annotation[]> parameterAnnotations;

    public MethodDescriptor(ClassDescriptor classDescriptor, int access, final String name, final String desc, String signature) {
        this.classDescriptor = classDescriptor;
        this.access = access;
        this.name = name;
        this.desc = desc;
        if (signature != null) new SignatureReader(signature).accept(this);
        this.returnType = createReturnType();
        if (name.equals(TypeTools.CONSTRUCTOR_NAME)) {
            this.constructor = createConstructor();
        } else if (!name.equals(TypeTools.STATIC_NAME)) {
            this.method = createMethod();
        }
        parameterAnnotations = createParameterAnnotations();
    }

    private LazyValue<String, Class> createReturnType() {
        return new LazyValue<String, Class>(desc) {
            protected Class resolve(String input) {
                return getClassByType(org.objectweb.asm.Type.getReturnType(input));
            }
        };
    }

    private LazyValue<Class, Constructor> createConstructor() {
        return new LazyValue<Class, Constructor>(classDescriptor.getTarget()) {
            protected Constructor resolve(Class input) {
                for (Constructor constructor : input.getDeclaredConstructors()) {
                    if (TypeTools.getConstructorDescriptor(constructor).equals(desc)) return constructor;
                }
                return null;
            }
        };
    }

    private LazyValue<Class, Method> createMethod() {
        return new LazyValue<Class, Method>(classDescriptor.getTarget()) {
            protected Method resolve(Class input) {
                for (Method method : input.getDeclaredMethods()) {
                    if (method.getName().equals(name) &&
                            org.objectweb.asm.Type.getMethodDescriptor(method).equals(desc)) return method;
                }
                return null;
            }
        };
    }

    public String getName() {
        return name;
    }

    public Class getReturnType() {
        return returnType.get();
    }

    public Object getDefaultValue() {
        return defaultValue == null ? null : TypeTools.cloneNonEmptyArray(defaultValue.get());
    }

    public static MethodDescriptor getInstance(Method method) {
        String key = method.getName() + org.objectweb.asm.Type.getMethodDescriptor(method);
        MethodDescriptor descriptor = ClassDescriptor.getInstance(method.getDeclaringClass()).getMethodDescriptor(key);
        descriptor.method.provide(method);
        return descriptor;
    }

    public static MethodDescriptor getInstance(Constructor constructor) {
        String key = TypeTools.CONSTRUCTOR_NAME + TypeTools.getConstructorDescriptor(constructor);
        MethodDescriptor descriptor = ClassDescriptor.getInstance(constructor.getDeclaringClass()).getMethodDescriptor(key);
        descriptor.constructor.provide(constructor);
        return descriptor;
    }

    public GenericDeclaration getTarget() {
        return method != null ? method.get() : constructor != null ? constructor.get() : null;
    }

    public Method getMethod() {
        return method == null ? null : method.get();
    }

    public Constructor getConstructor() {
        return constructor == null ? null : constructor.get();
    }

    public ClassDescriptor getClassDescriptor() {
        return classDescriptor;
    }

    protected TypeVariable findTypeVariable(String name) {
        TypeVariable variable = getTypeVariable(name);
        return variable != null ? variable : classDescriptor.findTypeVariable(name);
    }

    public Annotation[][] getParameterAnnotations() {
        return parameterAnnotations.getClone();
    }

    private LazyList<List<AnnotationValue>, Annotation[]> createParameterAnnotations() {
        return new LazyList<List<AnnotationValue>, Annotation[]>() {
            protected Annotation[] resolve(List<AnnotationValue> input) {
                return createAnnotations(input);
            }

            protected Annotation[][] newArray(int size) {
                return new Annotation[org.objectweb.asm.Type.getArgumentTypes(desc).length][];
            }
        };
    }

    public Type getGenericReturnType() {
        return genericReturnType == null ? null : genericReturnType.get();
    }

    public Type[] getGenericParameterTypes() {
        return genericParameterTypes == null ? null : genericParameterTypes.getClone();
    }

    public Type[] getGenericExceptionTypes() {
        return genericExceptionTypes == null ? null : genericExceptionTypes.getClone();
    }

    protected Annotation[] createAnnotations(Annotation[] declaredAnnotations) {
        return declaredAnnotations;
    }

    public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
        if (!visible) return null;
        List<AnnotationValue> values = parameterAnnotations.get(parameter);
        if (values == null) {
            values = new ArrayList<AnnotationValue>();
            parameterAnnotations.set(parameter, values);
        }
        AnnotationValue value = new AnnotationValue(desc);
        values.add(value);
        return value;
    }

    public void visit(String name, Object value) {
        setDefaultValue(value);
    }

    public void visitEnum(String name, String desc, String value) {
        setDefaultValue(new EnumValue(desc, value));
    }

    public AnnotationVisitor visitAnnotation(String name, String desc) {
        AnnotationValue value = new AnnotationValue(desc);
        setDefaultValue(value);
        return value;
    }

    public AnnotationVisitor visitArray(String name) {
        AnnotationArray array = new AnnotationArray();
        setDefaultValue(array);
        return array;
    }

    public SignatureVisitor visitParameterType() {
        TypeDescriptor descriptor = new TypeDescriptor();
        if (genericParameterTypes == null) genericParameterTypes = getLazyList();
        genericParameterTypes.add(descriptor);
        return descriptor;
    }

    public SignatureVisitor visitReturnType() {
        TypeDescriptor descriptor = new TypeDescriptor();
        genericReturnType = getLazyType(descriptor);
        return descriptor;
    }

    public SignatureVisitor visitExceptionType() {
        TypeDescriptor descriptor = new TypeDescriptor();
        if (genericExceptionTypes == null) genericExceptionTypes = getLazyList();
        genericExceptionTypes.add(descriptor);
        return descriptor;
    }

    private void setDefaultValue(Object o) {
        defaultValue = new LazyValue<Object, Object>(o) {
            protected Object resolve(Object input) {
                return resolveValue(input, getReturnType(), MethodDescriptor.this);
            }
        };
    }
}
