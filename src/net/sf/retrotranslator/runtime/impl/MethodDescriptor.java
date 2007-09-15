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
import net.sf.retrotranslator.runtime.asm.AnnotationVisitor;
import net.sf.retrotranslator.runtime.asm.signature.*;
import net.sf.retrotranslator.runtime.java.lang.annotation.Annotation_;

/**
 * @author Taras Puchko
 */
public class MethodDescriptor extends GenericDeclarationDescriptor implements MemberDescriptor {

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
    private LazyList<List<AnnotationValue>, Annotation_[]> parameterAnnotations;

    public MethodDescriptor(ClassDescriptor classDescriptor, int access, final String name, final String desc, String signature) {
        this.classDescriptor = classDescriptor;
        this.access = access;
        this.name = name;
        this.desc = desc;
        if (signature != null) new SignatureReader(signature).accept(this);
        this.returnType = createReturnType();
        if (name.equals(RuntimeTools.CONSTRUCTOR_NAME)) {
            this.constructor = createConstructor();
        } else if (!name.equals(RuntimeTools.STATIC_NAME)) {
            this.method = createMethod();
        }
        parameterAnnotations = createParameterAnnotations();
    }

    private LazyValue<String, Class> createReturnType() {
        return new LazyValue<String, Class>(desc) {
            protected Class resolve(String input) {
                return getClassByType(net.sf.retrotranslator.runtime.asm.Type.getReturnType(input));
            }
        };
    }

    private LazyValue<Class, Constructor> createConstructor() {
        return new LazyValue<Class, Constructor>(classDescriptor.getTarget()) {
            protected Constructor resolve(Class input) {
                for (Constructor constructor : input.getDeclaredConstructors()) {
                    if (RuntimeTools.getConstructorDescriptor(constructor).equals(desc)) return constructor;
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
                            net.sf.retrotranslator.runtime.asm.Type.getMethodDescriptor(method).equals(desc)) return method;
                }
                return null;
            }
        };
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public Class getReturnType() {
        return returnType.get();
    }

    public Object getDefaultValue() {
        return defaultValue == null ? null : RuntimeTools.cloneNonEmptyArray(defaultValue.get());
    }

    public static MethodDescriptor getInstance(Method method) {
        ClassDescriptor classDescriptor = ClassDescriptor.getInstance(method.getDeclaringClass());
        String desc = net.sf.retrotranslator.runtime.asm.Type.getMethodDescriptor(method);
        MethodDescriptor methodDescriptor = classDescriptor.getMethodDescriptor(method.getName() + desc);
        if (methodDescriptor == null) {
            methodDescriptor = new MethodDescriptor(classDescriptor, method.getModifiers(), method.getName(), desc, null);
        }
        methodDescriptor.method.provide(method);
        return methodDescriptor;
    }

    public static MethodDescriptor getInstance(Constructor constructor) {
        ClassDescriptor classDescriptor = ClassDescriptor.getInstance(constructor.getDeclaringClass());
        String desc = RuntimeTools.getConstructorDescriptor(constructor);
        MethodDescriptor methodDescriptor = classDescriptor.getMethodDescriptor(RuntimeTools.CONSTRUCTOR_NAME + desc);
        if (methodDescriptor == null) {
            methodDescriptor = new MethodDescriptor(classDescriptor, constructor.getModifiers(), constructor.getName(), desc, null);
        }
        methodDescriptor.constructor.provide(constructor);
        return methodDescriptor;
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

    public Annotation_[][] getParameterAnnotations() {
        return parameterAnnotations.getClone();
    }

    private LazyList<List<AnnotationValue>, Annotation_[]> createParameterAnnotations() {
        return new LazyList<List<AnnotationValue>, Annotation_[]>() {
            protected Annotation_[] resolve(List<AnnotationValue> input) {
                return createAnnotations(input);
            }

            protected Annotation_[][] newArray(int size) {
                return new Annotation_[net.sf.retrotranslator.runtime.asm.Type.getArgumentTypes(desc).length][];
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

    protected Annotation_[] createAnnotations(Annotation_[] declaredAnnotations) {
        return declaredAnnotations;
    }

    public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
        if (!visible) return EMPTY_VISITOR;
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
