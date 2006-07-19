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

import net.sf.retrotranslator.runtime.asm.ClassReader;
import net.sf.retrotranslator.runtime.asm.FieldVisitor;
import net.sf.retrotranslator.runtime.asm.MethodVisitor;
import net.sf.retrotranslator.runtime.asm.Opcodes;
import net.sf.retrotranslator.runtime.asm.signature.SignatureReader;
import net.sf.retrotranslator.runtime.asm.signature.SignatureVisitor;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.ref.SoftReference;
import java.lang.reflect.MalformedParameterizedTypeException;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;

/**
 * @author Taras Puchko
 */
public class ClassDescriptor extends GenericDeclarationDescriptor {

    private static SoftReference<Map<Class, ClassDescriptor>> cache;
    private static Properties signatures = getSignatures();
    private static BytecodeTransformer bytecodeTransformer;

    private String name;
    private Class target;
    private String enclosingClass;
    private String enclosingMethod;
    private LazyList<TypeDescriptor, Type> genericInterfaces;
    private LazyValue<TypeDescriptor, Type> genericSuperclass;
    private Map<String, FieldDescriptor> fieldDescriptors = new HashMap<String, FieldDescriptor>();
    private Map<String, MethodDescriptor> methodDescriptors = new HashMap<String, MethodDescriptor>();

    private static Properties getSignatures() {
        try {
            Properties properties = new Properties();
            InputStream stream = ClassDescriptor.class.getResourceAsStream("signatures.properties");
            if (stream != null) {
                properties.load(stream);
                stream.close();
            }
            return properties;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ClassDescriptor(Class target, byte[] bytecode) {
        this.target = target;
        if (bytecode != null) {
            if (bytecodeTransformer != null) {
                bytecode = bytecodeTransformer.transform(bytecode, 0, bytecode.length);
            }
            new ClassReader(bytecode).accept(this, true);
        }
    }

    public static void setBytecodeTransformer(BytecodeTransformer transformer) {
        bytecodeTransformer = transformer;
    }

    public static ClassDescriptor getInstance(Class target) {
        Map<Class, ClassDescriptor> map = getMap();
        ClassDescriptor descriptor = map.get(target);
        if (descriptor != null) return descriptor;
        descriptor = new ClassDescriptor(target, RuntimeTools.getBytecode(target));
        map.put(target, descriptor);
        return descriptor;
    }

    private static synchronized Map<Class, ClassDescriptor> getMap() {
        Map<Class, ClassDescriptor> map = cache == null ? null : cache.get();
        if (map == null) {
            map = new Hashtable<Class, ClassDescriptor>();
            cache = new SoftReference<Map<Class, ClassDescriptor>>(map);
        }
        return map;
    }

    protected Annotation[] createAnnotations(Annotation[] declaredAnnotations) {
        Class superclass = target.getSuperclass();
        if (superclass == null) return declaredAnnotations;
        Annotation[] superAnnotations = getInstance(superclass).getAnnotations();
        if (superAnnotations.length == 0) return declaredAnnotations;
        Map<Class, Annotation> result = new HashMap<Class, Annotation>();
        for (Annotation annotation : superAnnotations) {
            Class annotationClass = annotation.getClass().getInterfaces()[0];
            if (annotationClass.isAnnotationPresent(Inherited.class)) {
                result.put(annotationClass, annotation);
            }
        }
        for (Annotation annotation : declaredAnnotations) {
            result.put(annotation.getClass().getInterfaces()[0], annotation);
        }
        return result.values().toArray(new Annotation[result.size()]);
    }

    public String getName() {
        return name;
    }

    public ClassDescriptor getClassDescriptor() {
        return this;
    }

    public MethodDescriptor getEnclosingMethodDescriptor() {
        return enclosingMethod == null ? null
                : getInstance(getClassByInternalName(enclosingClass)).getMethodDescriptor(enclosingMethod);
    }

    public boolean isLocalOrAnonymous() {
        return enclosingMethod != null;
    }

    public FieldDescriptor getFieldDescriptor(String name) {
        return fieldDescriptors.get(name);
    }

    public Type[] getGenericInterfaces() {
        return genericInterfaces == null ? null : genericInterfaces.getClone();
    }

    public Type getGenericSuperclass() {
        return genericSuperclass == null ? null : genericSuperclass.get();
    }

    public MethodDescriptor getMethodDescriptor(String key) {
        return methodDescriptors.get(key);
    }

    public Collection<MethodDescriptor> getMethodDescriptors() {
        return methodDescriptors.values();
    }

    public Collection<FieldDescriptor> getFieldDescriptors() {
        return fieldDescriptors.values();
    }

    public Class getTarget() {
        return target;
    }

    protected TypeVariable findTypeVariable(String name) {
        TypeVariable variable = getTypeVariable(name);
        if (variable != null) return variable;
        MethodDescriptor methodDescriptor = getEnclosingMethodDescriptor();
        if (methodDescriptor != null) return methodDescriptor.findTypeVariable(name);
        Class declaringClass = target.getDeclaringClass();
        if (declaringClass != null) return getInstance(declaringClass).findTypeVariable(name);
        throw new MalformedParameterizedTypeException();
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.access = access;
        this.name = name;
        if (signature == null) signature = signatures.getProperty(name);
        if (signature != null) new SignatureReader(signature).accept(this);
    }

    public void visitOuterClass(String owner, String name, String desc) {
        if (name != null) {
            enclosingClass = owner;
            enclosingMethod = name + desc;
        }
    }

    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        if (name.equals(this.name)) this.access |= access;
    }

    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        FieldDescriptor fieldDescriptor = new FieldDescriptor(this, access, name, desc, signature);
        fieldDescriptors.put(name, fieldDescriptor);
        return fieldDescriptor;
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodDescriptor methodDescriptor = new MethodDescriptor(this, access, name, desc, signature);
        methodDescriptors.put(name + desc, methodDescriptor);
        return methodDescriptor;
    }

    public SignatureVisitor visitSuperclass() {
        TypeDescriptor descriptor = new TypeDescriptor();
        if (!isAccess(Opcodes.ACC_INTERFACE)) {
            genericSuperclass = getLazyType(descriptor);
        }
        return descriptor;
    }

    public SignatureVisitor visitInterface() {
        TypeDescriptor descriptor = new TypeDescriptor();
        if (genericInterfaces == null) genericInterfaces = getLazyList();
        genericInterfaces.add(descriptor);
        return descriptor;
    }

}
