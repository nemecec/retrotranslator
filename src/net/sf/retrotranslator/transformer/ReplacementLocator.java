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
package net.sf.retrotranslator.transformer;

import java.util.*;
import static net.sf.retrotranslator.runtime.asm.Opcodes.*;
import net.sf.retrotranslator.runtime.asm.Type;
import net.sf.retrotranslator.runtime.impl.*;

/**
 * @author Taras Puchko
 */
class ReplacementLocator {

    public static final String RUNTIME_PREFIX = "net/sf/retrotranslator/runtime/";
    public static final String CONCURRENT_PREFIX = "edu/emory/mathcs/backport/";

    private static final ClassReplacement NULL = new ClassReplacement();
    private static final Set<String> JAVA_UTIL_CLASSES = Collections.synchronizedSet(new HashSet<String>(
            Arrays.asList("java/util/Queue", "java/util/AbstractQueue", "java/util/PriorityQueue")));

    private final boolean target14;
    private final boolean advanced;
    private final List<Backport> backports;
    private final Map<String, ClassReplacement> replacements = new Hashtable<String, ClassReplacement>();

    public ReplacementLocator(boolean target14, boolean advanced, List<Backport> backports) {
        this.target14 = target14;
        this.advanced = advanced;
        this.backports = backports;
        putReplacement("sun/misc/", "ClassFileTransformer");
        putReplacement("com/bea/jvm/", "ClassPreProcessor");
    }

    private void putReplacement(String packagePrefix, String simpleName) {
        ClassReplacement replacement = new ClassReplacement();
        String name = packagePrefix + simpleName;
        replacement.setUniqueTypeName(name);
        replacement.setReferenceTypeName(name);
        replacements.put("net/sf/retrotranslator/transformer/" + simpleName, replacement);
    }

    public ClassReplacement getReplacement(String className) {
        if (className == null || className.length() == 0 || className.charAt(0) == '[') {
            return null;
        }
        ClassReplacement replacement = replacements.get(className);
        if (replacement == null) {
            replacement = buildReplacement(className);
            replacements.put(className, replacement);
        }
        return replacement == NULL ? null : replacement;
    }

    private ClassReplacement buildReplacement(String originalName) {
        ClassReplacement replacement = new ClassReplacement();
        for (Backport backport : backports) {
            String prefix = backport.getReplacementPrefix();
            String suffix = originalName;
            if (backport.getOriginalPrefix().length() > 0) {
                if (originalName.startsWith(backport.getOriginalPrefix())) {
                    suffix = originalName.substring(backport.getOriginalPrefix().length());
                } else {
                    continue;
                }
            }
            if (replacement.getUniqueTypeName() == null) {
                replacement.setUniqueTypeName(findUniqueName(prefix, suffix));
            }
            ClassDescriptor classDescriptor = getClassDescriptor(createServiceName(prefix, suffix));
            if (classDescriptor != null) {
                loadFields(replacement, classDescriptor);
                loadMethods(replacement, classDescriptor, originalName);
            }
        }
        if (replacement.getUniqueTypeName() == null) {
            replacement.setUniqueTypeName(findSpecialUniqueName(originalName));
        }
        if (replacement.isEmpty()) {
            return NULL;
        }
        if (replacement.getUniqueTypeName() == null) {
            replacement.setUniqueTypeName(originalName);
        }
        replacement.setReferenceTypeName(replacement.getCheckCastReplacement() != null ?
                Type.getReturnType(replacement.getCheckCastReplacement().getDesc()).getInternalName() :
                replacement.getUniqueTypeName()
        );
        return replacement;
    }

    private String findUniqueName(String prefix, String suffix) {
        String uniqueName = prefix + suffix + "_";
        if (isClassAvailable(uniqueName)) {
            return uniqueName;
        }
        int index = suffix.indexOf('$');
        if (index >= 0) {
            uniqueName = prefix + replaceChar(suffix, index, "_$");
            if (isClassAvailable(uniqueName)) {
                return uniqueName;
            }
            uniqueName = prefix + replaceChar(suffix, index, "_") + "_";
            if (isClassAvailable(uniqueName)) {
                return uniqueName;
            }
        }
        uniqueName = prefix + suffix;
        if (isClassAvailable(uniqueName)) {
            return uniqueName;
        }
        return null;
    }

    private static String replaceChar(String s, int index, String replacement) {
        return s.substring(0, index) + replacement + s.substring(index + 1);
    }

    private String findSpecialUniqueName(String originalName) {
        if (target14) {
            if (originalName.startsWith("java/util/concurrent/") || JAVA_UTIL_CLASSES.contains(originalName)) {
                return CONCURRENT_PREFIX + originalName;
            }
            if (originalName.equals("java/lang/StringBuilder")) {
                return Type.getInternalName(StringBuffer.class);
            }
        }
        return null;
    }

    private void loadFields(ClassReplacement classReplacement, ClassDescriptor classDescriptor) {
        for (FieldDescriptor fieldDescriptor : classDescriptor.getFieldDescriptors()) {
            if (!fieldDescriptor.isAccess(ACC_PUBLIC) || !fieldDescriptor.isAccess(ACC_STATIC)) {
                continue;
            }
            if (!advanced && fieldDescriptor.isAnnotationPresent(Advanced.class)) {
                continue;
            }
            String fieldName = fieldDescriptor.getName();
            String fieldDesc = fieldDescriptor.getDesc();
            String key = fieldName + fieldDesc;
            Map<String, MemberReplacement> replacements = classReplacement.getFieldReplacements();
            if (replacements.containsKey(key)) {
                continue;
            }
            replacements.put(key, new MemberReplacement(classDescriptor.getName(), fieldName, fieldDesc));
        }
    }

    private void loadMethods(ClassReplacement classReplacement, ClassDescriptor classDescriptor, String originalName) {
        for (MethodDescriptor methodDescriptor : classDescriptor.getMethodDescriptors()) {
            if (!methodDescriptor.isAccess(ACC_PUBLIC) || !methodDescriptor.isAccess(ACC_STATIC)) {
                continue;
            }
            if (!advanced && methodDescriptor.isAnnotationPresent(Advanced.class)) {
                continue;
            }
            String methodName = methodDescriptor.getName();
            String methodDesc = methodDescriptor.getDesc();
            String methodKey = methodName + methodDesc;
            if (classReplacement.getMethodReplacements().containsKey(methodKey)) {
                continue;
            }
            MemberReplacement replacement = new MemberReplacement(classDescriptor.getName(), methodName, methodDesc);
            classReplacement.getMethodReplacements().put(methodKey, replacement);
            if (methodName.equals("convertConstructorArguments")) {
                String converterKey = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getArgumentTypes(methodDesc));
                if (!classReplacement.getConverterReplacements().containsKey(converterKey)) {
                    classReplacement.getConverterReplacements().put(converterKey, replacement);
                }
            }
            if (classReplacement.getInstanceOfReplacement() == null &&
                    methodName.equals("executeInstanceOfInstruction") &&
                    methodDesc.equals(TransformerTools.descriptor(boolean.class, Object.class))) {
                classReplacement.setInstanceOfReplacement(replacement);
            }
            if (classReplacement.getCheckCastReplacement() == null &&
                    methodName.equals("executeCheckCastInstruction") &&
                    Arrays.equals(new Type[]{Type.getType(Object.class)}, Type.getArgumentTypes(methodDesc))) {
                classReplacement.setCheckCastReplacement(replacement);
            }
            if (methodName.equals("createInstanceBuilder")) {
                loadConstructor(classReplacement.getConstructorReplacements(), replacement, originalName);
            }
        }
    }

    private void loadConstructor(Map<String, ConstructorReplacement> replacements,
                                 MemberReplacement replacement, String originalName) {
        String constructorDesc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getArgumentTypes(replacement.getDesc()));
        if (replacements.containsKey(constructorDesc)) {
            return;
        }
        String owner = Type.getReturnType(replacement.getDesc()).getInternalName();
        ClassDescriptor classDescriptor = getClassDescriptor(owner);
        if (classDescriptor == null) {
            throw new TypeNotPresentException(owner, null);
        }
        List<MemberReplacement> arguments = new ArrayList<MemberReplacement>();
        MemberReplacement initializer = null;
        for (MethodDescriptor descriptor : classDescriptor.getMethodDescriptors()) {
            if (isArgument(descriptor)) {
                arguments.add(new MemberReplacement(owner, descriptor.getName(), descriptor.getDesc()));
            } else if (isInitializer(descriptor, originalName)) {
                initializer = new MemberReplacement(owner, descriptor.getName(), descriptor.getDesc());
            }
        }
        Collections.sort(arguments, new Comparator<MemberReplacement>() {
            public int compare(MemberReplacement o1, MemberReplacement o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        List<Type> types = new ArrayList<Type>();
        for (MemberReplacement argument : arguments) {
            types.add(Type.getReturnType(argument.getDesc()));
        }
        MemberReplacement constructor = new MemberReplacement(originalName, RuntimeTools.CONSTRUCTOR_NAME,
                Type.getMethodDescriptor(Type.VOID_TYPE, types.toArray(new Type[0])));
        replacements.put(constructorDesc, new ConstructorReplacement(replacement,
                arguments.toArray(new MemberReplacement[0]), constructor, initializer));
    }

    private static boolean isArgument(MethodDescriptor descriptor) {
        if (!descriptor.getName().startsWith("argument")) return false;
        if (Type.getReturnType(descriptor.getDesc()) == Type.VOID_TYPE) return false;
        if (Type.getArgumentTypes(descriptor.getDesc()).length > 0) return false;
        return descriptor.isAccess(ACC_PUBLIC) && !descriptor.isAccess(ACC_STATIC);
    }

    private static boolean isInitializer(MethodDescriptor descriptor, String originalName) {
        if (!descriptor.getName().equals("initialize")) return false;
        if (Type.getReturnType(descriptor.getDesc()) != Type.VOID_TYPE) return false;
        Type[] types = Type.getArgumentTypes(descriptor.getDesc());
        if (types.length != 1) return false;
        if (!types[0].getInternalName().equals(originalName)) return false;
        return descriptor.isAccess(ACC_PUBLIC) && !descriptor.isAccess(ACC_STATIC);
    }

    private static String createServiceName(String prefix, String suffix) {
        int index = suffix.lastIndexOf('/');
        return prefix + suffix.substring(0, index + 1) + '_' + suffix.substring(index + 1);
    }

    private boolean isClassAvailable(String internalName) {
        return getClass().getResource("/" + internalName + RuntimeTools.CLASS_EXTENSION) != null;
    }

    private static ClassDescriptor getClassDescriptor(String internalName) {
        byte[] bytecode = RuntimeTools.readResourceToByteArray(ReplacementLocator.class,
                '/' + internalName + RuntimeTools.CLASS_EXTENSION);
        return bytecode == null ? null : new ClassDescriptor(ReplacementLocator.class, bytecode);
    }

}
