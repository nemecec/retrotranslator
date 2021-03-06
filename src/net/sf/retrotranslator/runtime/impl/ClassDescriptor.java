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
package net.sf.retrotranslator.runtime.impl;

import java.lang.annotation.*;
import java.lang.ref.SoftReference;
import java.lang.reflect.*;
import java.lang.reflect.Type;
import java.util.*;
import net.sf.retrotranslator.runtime.asm.*;
import net.sf.retrotranslator.runtime.asm.signature.*;

/**
 * @author Taras Puchko
 */
public class ClassDescriptor extends GenericDeclarationDescriptor {

    private static final WeakIdentityTable<Class, String> metadataTable = new WeakIdentityTable<Class, String>();
    private static SoftReference<Map<Class, ClassDescriptor>> cache;
    private static BytecodeTransformer bytecodeTransformer;

    private String name;
    private Class target;
    private String declaringClass;
    private String enclosingClass;
    private String enclosingMethod;
    private LazyList<TypeDescriptor, Type> genericInterfaces;
    private LazyValue<TypeDescriptor, Type> genericSuperclass;
    private Map<String, FieldDescriptor> fieldDescriptors = new HashMap<String, FieldDescriptor>();
    private Map<String, MethodDescriptor> methodDescriptors = new HashMap<String, MethodDescriptor>();

    public ClassDescriptor(Class target, byte[] bytecode) {
        this.target = target;
        if (bytecode != null) {
            if (bytecodeTransformer != null) {
                bytecode = bytecodeTransformer.transform(bytecode, 0, bytecode.length);
            }
            new ClassReader(bytecode).accept(this, true);
        } else {
            visit(0, target.getModifiers(),
                    net.sf.retrotranslator.runtime.asm.Type.getInternalName(target), null, null, null);
        }
    }

    public static void setBytecodeTransformer(BytecodeTransformer transformer) {
        bytecodeTransformer = transformer;
    }

    public static ClassDescriptor getInstance(Class target) {
        Map<Class, ClassDescriptor> map = getMap();
        ClassDescriptor descriptor = map.get(target);
        if (descriptor != null) {
            return descriptor;
        }
        byte[] bytecode = RuntimeTools.getBytecode(target);
        if (bytecode != null) {
            descriptor = new ClassDescriptor(target, bytecode);
        }
        if (descriptor == null || descriptor.isMetadataPresent()) {
            String s = getEncodedMetadata(target);
            if (s != null) {
                descriptor = new ClassDescriptor(target, decode(s));
            }
        }
        if (descriptor == null) {
            descriptor = new ClassDescriptor(target, null);
        }
        map.put(target, descriptor);
        return descriptor;
    }

    public static void setEncodedMetadata(Class target, String metadata) {
        if (metadataTable != null) {
            metadataTable.putIfAbsent(target, metadata);
        }
    }

    private static String getEncodedMetadata(Class target) {
        try {
            Class.forName(target.getName(), true, target.getClassLoader());
        } catch (ClassNotFoundException e) {
            // ignore
        }
        return metadataTable.lookup(target);
    }

    private static byte[] decode(String s) {
        byte[] bytecode = new byte[s.length()];
        for (int i = 0; i < bytecode.length; i++) {
            bytecode[i] = (byte) (127 - s.charAt(i));
        }
        return bytecode;
    }

    private boolean isMetadataPresent() {
        for (MethodDescriptor descriptor : methodDescriptors.values()) {
            if (descriptor.isMetadataPresent()) {
                return true;
            }
        }
        return false;
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

    public String getDesc() {
        return null;
    }

    public ClassDescriptor getClassDescriptor() {
        return this;
    }

    public String getInfo() {
        return RuntimeTools.getDisplayClassName(name);
    }

    public MethodDescriptor getEnclosingMethodDescriptor() {
        return enclosingMethod == null ? null
                : getInstance(getClassByInternalName(enclosingClass)).getMethodDescriptor(enclosingMethod);
    }

    public Class getDeclaringClass() {
        return declaringClass == null ? null : getClassByInternalName(declaringClass);
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
        if (variable != null) {
            return variable;
        }
        MethodDescriptor methodDescriptor = getEnclosingMethodDescriptor();
        if (methodDescriptor != null) {
            return methodDescriptor.findTypeVariable(name);
        }
        Class declaringClass = target.getDeclaringClass();
        if (declaringClass != null) {
            return getInstance(declaringClass).findTypeVariable(name);
        }
        throw new MalformedParameterizedTypeException();
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.access = access;
        this.name = name;
        if (signature == null) {
            signature = SignatureList.getSignature(name);
        }
        if (signature != null) {
            new SignatureReader(signature).accept(this);
        }
    }

    public void visitOuterClass(String owner, String name, String desc) {
        if (name != null) {
            enclosingClass = owner;
            enclosingMethod = name + desc;
        }
    }

    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        if (name.equals(this.name)) {
            this.access |= access;
            declaringClass = outerName;
        }
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
