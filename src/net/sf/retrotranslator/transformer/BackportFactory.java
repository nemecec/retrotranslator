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
package net.sf.retrotranslator.transformer;

import edu.emory.mathcs.backport.java.util.concurrent.Callable;
import static net.sf.retrotranslator.runtime.asm.Opcodes.ACC_PUBLIC;
import static net.sf.retrotranslator.runtime.asm.Opcodes.ACC_STATIC;
import net.sf.retrotranslator.runtime.asm.Type;
import net.sf.retrotranslator.runtime.impl.*;
import net.sf.retrotranslator.runtime.java.util._Queue;

import java.lang.annotation.Annotation;
import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.MissingResourceException;

/**
 * @author Taras Puchko
 */
class BackportFactory {

    private static final String CONCURRENT = "java/util/concurrent/";
    private static final String BACKPORT = getPrefix(Callable.class, CONCURRENT + "Callable");
    private static final String RUNTIME = getPrefix(Derived.class, "impl/Derived");

    private static SoftReference<BackportFactory> softReference = new SoftReference<BackportFactory>(null);

    private final Map<String, String> replacements = new Hashtable<String, String>();
    private final Map<String, Boolean> extensions = new Hashtable<String, Boolean>();
    private final Map<String, String[]> implementations = new Hashtable<String, String[]>();
    private final Map<ClassMember, ClassMember> fields = new Hashtable<ClassMember, ClassMember>();
    private final Map<ClassMember, ClassMember> methods = new Hashtable<ClassMember, ClassMember>();

    private BackportFactory() {
        String transformer = getPrefix(BackportFactory.class, "BackportFactory");
        replacements.put(transformer + "ClassFileTransformer", "sun/misc/ClassFileTransformer");
        replacements.put(transformer + "ClassPreProcessor", "com/bea/jvm/ClassPreProcessor");
        replacements.put("java/lang/StringBuilder", "java/lang/StringBuffer");
        String queue = "java/util/Queue";
        for (String name : new String[]{queue, "java/util/AbstractQueue", "java/util/PriorityQueue"}) {
            replacements.put(name, BACKPORT + name);
        }
        String backportedQueue = BACKPORT + queue;
        for (Class aClass : new Class[]{Collection.class, _Queue.class}) {
            loadExtension(new StringBuilder(Type.getInternalName(aClass)), backportedQueue);
        }
        implementations.clear();
        extensions.put(backportedQueue, true);
    }

    public static BackportFactory getInstance() {
        BackportFactory factory = readFromCache();
        if (factory == null) {
            factory = new BackportFactory();
            writeToCache(factory);
        }
        return factory;
    }

    private static synchronized BackportFactory readFromCache() {
        return softReference.get();
    }

    private static synchronized void writeToCache(BackportFactory factory) {
        softReference = new SoftReference<BackportFactory>(factory);
    }

    public static String prefixBackportName(String className, String backportPrefix) {
        return backportPrefix != null && (className.startsWith(RUNTIME) || className.startsWith(BACKPORT))
                ? backportPrefix + className : className;
    }

    public String getClassName(String name) {
        String result = replacements.get(name);
        if (result == null) {
            if (name.startsWith(CONCURRENT)) {
                result = BACKPORT + name;
            } else {
                String backportName = RUNTIME + name + "_";
                result = getClass().getResource("/" + backportName + ".class") != null ? backportName : name;
            }
            replacements.put(name, result);
        }
        return result;
    }

    public String[] getImplementations(String desc) {
        return isExtended(desc) ? implementations.get(desc) : null;
    }

    public ClassMember getMethod(boolean isStatic, String owner, String name, String desc) {
        return isExtended(owner) ? methods.get(new ClassMember(isStatic, owner, name, desc, false)) : null;
    }

    public ClassMember getField(String owner, String name, String desc) {
        return isExtended(owner) ? fields.get(new ClassMember(true, owner, name, desc, false)) : null;
    }

    private boolean isExtended(String owner) {
        if (owner.charAt(0) == '[') return false;
        Boolean extended = extensions.get(owner);
        if (extended != null) {
            return extended;
        }
        String original = owner;
        if (original.startsWith(RUNTIME) && original.endsWith("_")) {
            original = original.substring(RUNTIME.length(), original.length() - 1);
        }
        StringBuilder extensionName = new StringBuilder(RUNTIME);
        int index = original.lastIndexOf('/');
        if (index >= 0) {
            extensionName.append(original.substring(0, index + 1));
        }
        extensionName.append('_').append(original.substring(index + 1));
        extended = loadExtension(extensionName, owner);
        extensions.put(owner, extended);
        return extended;
    }

    private boolean loadExtension(StringBuilder extensionName, String originalName) {
        try {
            String backportPath = extensionName.insert(0, '/').append(".class").toString();
            byte[] bytecode = RuntimeTools.readResourceToByteArray(BackportFactory.class, backportPath);
            ClassDescriptor descriptor = new ClassDescriptor(BackportFactory.class, bytecode);
            loadImplementations(descriptor, originalName);
            loadFields(descriptor, originalName);
            loadMethods(descriptor, originalName);
            return true;
        } catch (MissingResourceException e) {
            return false;
        }
    }

    private void loadImplementations(ClassDescriptor descriptor, String originalName) {
        if (originalName.startsWith(RUNTIME)) return;
        Annotation annotation = descriptor.getAnnotation(Derived.class);
        if (annotation == null) return;
        Class[] classes = ((Derived) annotation).value();
        String[] internalNames = new String[classes.length];
        for (int i = 0; i < internalNames.length; i++) {
            internalNames[i] = Type.getInternalName(classes[i]);
        }
        implementations.put(originalName, internalNames);
    }

    private void loadFields(ClassDescriptor descriptor, String originalName) {
        for (FieldDescriptor fieldDescriptor : descriptor.getFieldDescriptors()) {
            if (!fieldDescriptor.isAccess(ACC_PUBLIC) || !fieldDescriptor.isAccess(ACC_STATIC)) continue;
            String name = fieldDescriptor.getName();
            String desc = fieldDescriptor.getDesc();
            ClassMember substitutionMember = new ClassMember(true, descriptor.getName(), name, desc,
                    fieldDescriptor.isAnnotationPresent(Advanced.class));
            ClassMember originalMember = new ClassMember(true, originalName, name, desc, false);
            fields.put(originalMember, substitutionMember);
        }
    }

    private void loadMethods(ClassDescriptor descriptor, String originalName) {
        Type originalType = TransformerTools.getTypeByInternalName(originalName);
        for (MethodDescriptor methodDescriptor : descriptor.getMethodDescriptors()) {
            String name = methodDescriptor.getName();
            if (!methodDescriptor.isAccess(ACC_PUBLIC) || name.charAt(0) == '<') continue;
            boolean isStatic = methodDescriptor.isAccess(ACC_STATIC);
            String desc = methodDescriptor.getDesc();
            ClassMember substitutionMember = new ClassMember(isStatic, descriptor.getName(), name, desc,
                    methodDescriptor.isAnnotationPresent(Advanced.class));
            Type[] types = Type.getArgumentTypes(desc);
            if (isStatic && types.length > 0 && types[0].equals(originalType)) {
                String instanceDesc = Type.getMethodDescriptor(Type.getReturnType(desc), removeFirst(types));
                methods.put(new ClassMember(false, originalName, name, instanceDesc, false), substitutionMember);
            }
            methods.put(new ClassMember(isStatic, originalName, name, desc, false), substitutionMember);
        }
    }

    private static Type[] removeFirst(Type[] types) {
        Type[] result = new Type[types.length - 1];
        System.arraycopy(types, 1, result, 0, result.length);
        return result;
    }

    private static String getPrefix(Class aClass, String suffix) {
        String name = Type.getInternalName(aClass);
        if (name.endsWith(suffix)) return name.substring(0, name.length() - suffix.length());
        throw new IllegalArgumentException(name + " does not end with " + suffix);
    }
}
