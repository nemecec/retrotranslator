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

import edu.emory.mathcs.backport.java.util.AbstractQueue;
import edu.emory.mathcs.backport.java.util.PriorityQueue;
import edu.emory.mathcs.backport.java.util.Queue;
import net.sf.retrotranslator.runtime.asm.Opcodes;
import net.sf.retrotranslator.runtime.asm.Type;
import net.sf.retrotranslator.runtime.impl.*;
import net.sf.retrotranslator.runtime.java.util._Queue;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * @author Taras Puchko
 */
class BackportLocator {

    public static final String RUNTIME_PREFIX = getPrefix(Derived.class, "impl/Derived");
    public static final String CONCURRENT_PREFIX = getPrefix(Queue.class, "java/util/Queue");

    private static final String CONCURRENT_PACKAGE = "java/util/concurrent/";
    private static final String BACKPORTED_QUEUE = Type.getInternalName(Queue.class);

    private static final Set<String> JAVA_UTIL_CLASSES =
            getOriginalClasses(CONCURRENT_PREFIX, Queue.class, AbstractQueue.class, PriorityQueue.class);

    private final List<String> prefixes = new Vector<String>();
    private final Map<String, String> replacements = new Hashtable<String, String>();
    private final Map<String, Boolean> extensions = new Hashtable<String, Boolean>();
    private final Map<String, Set<String>> implementations = new Hashtable<String, Set<String>>();
    private final Map<ClassMember, ClassMember> fields = new Hashtable<ClassMember, ClassMember>();
    private final Map<ClassMember, ClassMember> methods = new Hashtable<ClassMember, ClassMember>();
    private final Map<ClassMember, ClassMember> converters = new Hashtable<ClassMember, ClassMember>();
    private final Map<ClassMember, InstanceBuilder> builders = new Hashtable<ClassMember, InstanceBuilder>();

    public BackportLocator(List<String> customPrefixes) {
        prefixes.addAll(customPrefixes);
        prefixes.add(RUNTIME_PREFIX);
        String transformerPrefix = getPrefix(BackportLocator.class, "BackportLocator");
        replacements.put(transformerPrefix + "ClassFileTransformer", "sun/misc/ClassFileTransformer");
        replacements.put(transformerPrefix + "ClassPreProcessor", "com/bea/jvm/ClassPreProcessor");
    }

    public String getClassName(String name) {
        String replacement = replacements.get(name);
        if (replacement == null) {
            replacement = loadReplacement(name);
            replacements.put(name, replacement);
        }
        return replacement;
    }

    private String loadReplacement(String name) {
        for (String prefix : prefixes) {
            String replacement = prefix + name + "_";
            if (isClassAvailable(replacement)) {
                return replacement;
            }
            if (name.indexOf('$') >= 0) {
                replacement = prefix + name.replace('$', '_') + "_";
                if (isClassAvailable(replacement)) {
                    return replacement;
                }
            }
        }
        if (name.startsWith(CONCURRENT_PACKAGE) || JAVA_UTIL_CLASSES.contains(name)) {
            return CONCURRENT_PREFIX + name;
        }
        if (name.equals("java/lang/StringBuilder")) {
            return Type.getInternalName(StringBuffer.class);
        }
        return name;
    }

    private boolean isClassAvailable(String internalName) {
        return getClass().getResource("/" + internalName + RuntimeTools.CLASS_EXTENSION) != null;
    }

    public Set<String> getImplementations(String desc) {
        return isExtended(desc) ? implementations.get(desc) : null;
    }

    public ClassMember getMethod(boolean isStatic, String owner, String name, String desc) {
        return isExtended(owner) ? methods.get(new ClassMember(isStatic, owner, name, desc, false)) : null;
    }

    public ClassMember getField(String owner, String name, String desc) {
        return isExtended(owner) ? fields.get(new ClassMember(true, owner, name, desc, false)) : null;
    }

    public ClassMember getConverter(String owner, String desc) {
        return isExtended(owner) ? converters.get(
                new ClassMember(false, owner, RuntimeTools.CONSTRUCTOR_NAME, desc, false)) : null;
    }

    public InstanceBuilder getBuilder(String owner, String desc) {
        return isExtended(owner) ? builders.get(
                new ClassMember(false, owner, RuntimeTools.CONSTRUCTOR_NAME, desc, false)) : null;
    }

    private boolean isExtended(String owner) {
        if (owner.charAt(0) == '[') return false;
        Boolean extended = extensions.get(owner);
        if (extended == null) {
            extended = loadExtension(owner);
            extensions.put(owner, extended);
        }
        return extended;
    }

    private boolean loadExtension(String originalName) {
        String original = getOriginal(originalName);
        int index = original.lastIndexOf('/');
        String extension = original.substring(0, index + 1) + '_' + original.substring(index + 1);
        boolean extended = false;
        for (String prefix : prefixes) {
            extended |= loadExtension(prefix + extension, originalName);
        }
        if (originalName.equals(BACKPORTED_QUEUE)) {
            for (Class aClass : new Class[]{Collection.class, _Queue.class}) {
                extended |= loadExtension(Type.getInternalName(aClass), originalName);
            }
        }
        return extended;
    }

    private String getOriginal(String originalName) {
        if (originalName.endsWith("_")) {
            for (String prefix : prefixes) {
                if (originalName.startsWith(prefix)) {
                    return originalName.substring(prefix.length(), originalName.length() - 1);
                }
            }
        }
        return originalName;
    }

    private boolean loadExtension(String extensionName, String originalName) {
        ClassDescriptor descriptor = getDescriptor(extensionName);
        if (descriptor == null) {
            return false;
        }
        loadImplementations(descriptor, originalName);
        loadFields(descriptor, originalName);
        loadMethods(descriptor, originalName);
        return true;
    }

    private static ClassDescriptor getDescriptor(String internalName) {
        byte[] bytecode = RuntimeTools.readResourceToByteArray(BackportLocator.class, '/' +
                internalName + RuntimeTools.CLASS_EXTENSION);
        return bytecode == null ? null : new ClassDescriptor(BackportLocator.class, bytecode);
    }

    private void loadImplementations(ClassDescriptor descriptor, String originalName) {
        Annotation annotation = descriptor.getAnnotation(Derived.class);
        if (annotation == null) return;
        Set<String> internalNames = new LinkedHashSet<String>();
        Set<String> set = implementations.get(originalName);
        if (set != null) {
            internalNames.addAll(set);
        }
        for (Class aClass : ((Derived) annotation).value()) {
            internalNames.add(Type.getInternalName(aClass));
        }
        implementations.put(originalName, internalNames);
    }

    private void loadFields(ClassDescriptor descriptor, String originalName) {
        for (FieldDescriptor fieldDescriptor : descriptor.getFieldDescriptors()) {
            if (fieldDescriptor.isAccess(Opcodes.ACC_PUBLIC) && fieldDescriptor.isAccess(Opcodes.ACC_STATIC)) {
                String name = fieldDescriptor.getName();
                String desc = fieldDescriptor.getDesc();
                ClassMember originalMember = new ClassMember(true, originalName, name, desc, false);
                if (!fields.containsKey(originalMember)) {
                    ClassMember substitutionMember = new ClassMember(true, descriptor.getName(),
                            name, desc, fieldDescriptor.isAnnotationPresent(Advanced.class));
                    fields.put(originalMember, substitutionMember);
                }
            }
        }
    }

    private void loadMethods(ClassDescriptor descriptor, String originalName) {
        Type originalType = TransformerTools.getTypeByInternalName(originalName);
        for (MethodDescriptor methodDescriptor : descriptor.getMethodDescriptors()) {
            String name = methodDescriptor.getName();
            if (!methodDescriptor.isAccess(Opcodes.ACC_PUBLIC) || name.charAt(0) == '<') continue;
            boolean isStatic = methodDescriptor.isAccess(Opcodes.ACC_STATIC);
            String desc = methodDescriptor.getDesc();
            ClassMember substitutionMember = new ClassMember(isStatic, descriptor.getName(), name, desc,
                    methodDescriptor.isAnnotationPresent(Advanced.class));
            Type[] types = Type.getArgumentTypes(desc);
            if (isStatic && types.length > 0 && types[0].equals(originalType)) {
                String instanceDesc = Type.getMethodDescriptor(Type.getReturnType(desc), removeFirst(types));
                putMethod(new ClassMember(false, originalName, name, instanceDesc, false), substitutionMember);
            }
            putMethod(new ClassMember(isStatic, originalName, name, desc, false), substitutionMember);
            if (name.equals("convertConstructorArguments")) {
                ClassMember constructorMember = createConstructorMember(originalName, desc);
                if (!converters.containsKey(constructorMember)) {
                    converters.put(constructorMember, substitutionMember);
                }
            } else if (name.equals("createInstanceBuilder")) {
                ClassMember constructorMember = createConstructorMember(originalName, desc);
                if (!builders.containsKey(constructorMember)) {
                    builders.put(constructorMember, createInstanceBuilder(originalName, substitutionMember));
                }
            }
        }
    }

    private static InstanceBuilder createInstanceBuilder(String originalName, ClassMember creator) {
        String owner = Type.getReturnType(creator.desc).getInternalName();
        ClassDescriptor classDescriptor = getDescriptor(owner);
        if (classDescriptor == null) {
            throw new TypeNotPresentException(owner, null);
        }
        List<ClassMember> arguments = new ArrayList<ClassMember>();
        ClassMember initializer = null;
        for (MethodDescriptor descriptor : classDescriptor.getMethodDescriptors()) {
            if (isArgument(descriptor)) {
                arguments.add(new ClassMember(false, owner, descriptor.getName(), descriptor.getDesc(), false));
            } else if (isInitializer(descriptor, originalName)) {
                initializer = new ClassMember(false, owner, descriptor.getName(), descriptor.getDesc(), false);
            }
        }
        Collections.sort(arguments);
        List<Type> types = new ArrayList<Type>();
        for (ClassMember argument : arguments) {
            types.add(Type.getReturnType(argument.desc));
        }
        ClassMember constructor = new ClassMember(false, originalName, RuntimeTools.CONSTRUCTOR_NAME,
                Type.getMethodDescriptor(Type.VOID_TYPE, types.toArray(new Type[0])), false);
        return new InstanceBuilder(creator, arguments.toArray(new ClassMember[0]), constructor, initializer);
    }

    private static boolean isArgument(MethodDescriptor descriptor) {
        if (!descriptor.getName().startsWith("argument")) return false;
        if (Type.getReturnType(descriptor.getDesc()) == Type.VOID_TYPE) return false;
        if (Type.getArgumentTypes(descriptor.getDesc()).length > 0) return false;
        return descriptor.isAccess(Opcodes.ACC_PUBLIC) && !descriptor.isAccess(Opcodes.ACC_STATIC);
    }

    private static boolean isInitializer(MethodDescriptor descriptor, String originalName) {
        if (!descriptor.getName().equals("initialize")) return false;
        if (Type.getReturnType(descriptor.getDesc()) != Type.VOID_TYPE) return false;
        Type[] types = Type.getArgumentTypes(descriptor.getDesc());
        if (types.length != 1) return false;
        if (!types[0].getInternalName().equals(originalName)) return false;
        return descriptor.isAccess(Opcodes.ACC_PUBLIC) && !descriptor.isAccess(Opcodes.ACC_STATIC);
    }

    private ClassMember createConstructorMember(String owner, String desc) {
        return new ClassMember(false, owner, RuntimeTools.CONSTRUCTOR_NAME,
                Type.getMethodDescriptor(Type.VOID_TYPE, Type.getArgumentTypes(desc)), false);
    }

    private void putMethod(ClassMember originalMember, ClassMember substitutionMember) {
        if (!methods.containsKey(originalMember)) {
            methods.put(originalMember, substitutionMember);
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

    private static Set<String> getOriginalClasses(String prefix, Class... backportedClasses) {
        HashSet<String> result = new HashSet<String>();
        for (Class aClass : backportedClasses) {
            String name = Type.getInternalName(aClass);
            if (name.startsWith(prefix)) {
                result.add(name.substring(prefix.length()));
            } else {
                throw new IllegalArgumentException(name + " does not start with " + prefix);
            }
        }
        return result;
    }

}
