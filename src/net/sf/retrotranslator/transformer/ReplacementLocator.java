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
import net.sf.retrotranslator.runtime.asm.*;
import net.sf.retrotranslator.runtime.impl.*;
import net.sf.retrotranslator.runtime.java.lang.annotation.Annotation_;

/**
 * @author Taras Puchko
 */
class ReplacementLocator {

    private static final ClassReplacement NULL = new ClassReplacement();

    private final OperationMode mode;
    private final List<Backport> backports;
    private final ClassReaderFactory classReaderFactory;
    private final Map<String, ClassReplacement> replacements = new Hashtable<String, ClassReplacement>();

    interface KeyProvider {
        MemberKey getKey(String name, String desc);
    }

    public ReplacementLocator(OperationMode mode, List<Backport> backports, ClassReaderFactory classReaderFactory) {
        this.mode = mode;
        this.backports = backports;
        this.classReaderFactory = classReaderFactory;
    }

    public ClassReplacement getReplacement(String className) {
        if (className == null || className.length() == 0 || className.charAt(0) == '[') {
            return null;
        }
        ClassReplacement replacement = replacements.get(className);
        if (replacement != null) {
            return replacement == NULL ? null : replacement;
        }
        replacement = buildReplacement(className);
        replacements.put(className, replacement);
        if (mode.isSmart()) {
            addInheritedMembers(replacement);
        }
        if (replacement.isEmpty(className)) {
            replacements.put(className, NULL);
            return null;
        }
        return replacement;
    }

    private void addInheritedMembers(ClassReplacement replacement) {
        ClassReader classReader = classReaderFactory.findClassReader(replacement.getUniqueTypeName());
        if (classReader != null) {
            SmartReplacementVisitor visitor = new SmartReplacementVisitor(this);
            classReader.accept(visitor, true);
            visitor.addInheritedMembers(replacement);
        }
    }

    private ClassReplacement buildReplacement(String originalName) {
        ClassReplacement replacement = new ClassReplacement();
        for (Backport backport : backports) {
            if (backport instanceof ClassBackport) {
                buildForClass(originalName, replacement, (ClassBackport) backport);
            } else if (backport instanceof MemberBackport) {
                buildForMember(originalName, replacement, (MemberBackport) backport);
            } else if (backport instanceof PackageBackport) {
                buildForPackage(originalName, replacement, (PackageBackport) backport);
            } else {
                throw new IllegalStateException();
            }
        }
        completeReplacement(replacement, originalName);
        return replacement;
    }

    private void buildForClass(String originalName, ClassReplacement replacement, ClassBackport backport) {
        if (replacement.getUniqueTypeName() == null && originalName.equals(backport.getOriginalName())) {
            replacement.setUniqueTypeName(backport.getReplacementName());
        }
    }

    private void buildForMember(String originalName, ClassReplacement replacement, final MemberBackport backport) {
        if (!originalName.equals(backport.getOriginalClassName())) {
            return;
        }
        ClassDescriptor classDescriptor = getClassDescriptor(backport.getReplacementClassName());
        if (classDescriptor == null) {
            return;
        }
        loadAllMembers(replacement, classDescriptor, new KeyProvider() {
            public MemberKey getKey(String name, String desc) {
                if (name.equals(backport.getReplacementMemberName())) {
                    return new MemberKey(true, backport.getOriginalMemberName(), desc);
                }
                return null;
            }
        });
    }

    private void buildForPackage(String originalName, ClassReplacement replacement, PackageBackport backport) {
        String originalPrefix = backport.getOriginalPrefix();
        String suffix = originalName;
        if (originalPrefix.length() > 0) {
            if (originalName.startsWith(originalPrefix)) {
                suffix = originalName.substring(originalPrefix.length());
            } else {
                return;
            }
        }
        String prefix = backport.getReplacementPrefix();
        if (replacement.getUniqueTypeName() == null) {
            replacement.setUniqueTypeName(findUniqueName(prefix, suffix));
        }
        int index = suffix.lastIndexOf('/');
        String memberBackportName = prefix + suffix.substring(0, index + 1) +
                '_' + suffix.substring(index + 1).replace('$', '_');
        ClassDescriptor classDescriptor = getClassDescriptor(memberBackportName);
        if (classDescriptor != null) {
            loadAllMembers(replacement, classDescriptor, new KeyProvider() {
                public MemberKey getKey(String name, String desc) {
                    return new MemberKey(true, name, desc);
                }
            });
        }
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

    private void loadAllMembers(ClassReplacement replacement, ClassDescriptor descriptor, KeyProvider keyProvider) {
        String owner = descriptor.getName();
        loadMembers(owner, descriptor.getFieldDescriptors(), keyProvider, replacement.getFieldReplacements());
        loadMembers(owner, descriptor.getMethodDescriptors(), keyProvider, replacement.getMethodReplacements());
    }

    private <T extends AnnotatedElementDescriptor & MemberDescriptor>
    void loadMembers(String owner, Collection<T> members, KeyProvider keyProvider,
                     Map<MemberKey, MemberReplacement> replacements) {
        for (T member : members) {
            if (!member.isAccess(ACC_PUBLIC) || !member.isAccess(ACC_STATIC)) {
                continue;
            }
            if (!isSupportedFeature(member)) {
                continue;
            }
            MemberKey key = keyProvider.getKey(member.getName(), member.getDesc());
            if (key == null || replacements.containsKey(key)) {
                continue;
            }
            replacements.put(key, new MemberReplacement(owner, member.getName(), member.getDesc()));
        }
    }

    private void completeReplacement(ClassReplacement replacement, String originalName) {
        if (replacement.getUniqueTypeName() == null) {
            replacement.setUniqueTypeName(originalName);
        }
        Map<MemberKey, MemberReplacement> replacements = replacement.getMethodReplacements();
        replacement.setCheckCastReplacement(findCheckCastReplacement(replacements.values()));
        replacement.setInstanceOfReplacement(findInstanceOfReplacement(replacements.values()));
        replacement.setReferenceTypeName(replacement.getCheckCastReplacement() != null ?
                Type.getReturnType(replacement.getCheckCastReplacement().getDesc()).getInternalName() :
                replacement.getUniqueTypeName());
        for (Map.Entry<MemberKey, MemberReplacement> entry :
                new HashMap<MemberKey, MemberReplacement>(replacements).entrySet()) {
            processMethod(replacement, entry.getKey().getName(), entry.getValue());
        }
    }

    private MemberReplacement findCheckCastReplacement(Collection<MemberReplacement> replacements) {
        for (MemberReplacement replacement : replacements) {
            if (replacement.getName().equals("executeCheckCastInstruction")) {
                Type[] types = Type.getArgumentTypes(replacement.getDesc());
                if (types.length == 1 && types[0].equals(Type.getType(Object.class))) {
                    return replacement;
                }
            }
        }
        return null;
    }

    private MemberReplacement findInstanceOfReplacement(Collection<MemberReplacement> replacements) {
        for (MemberReplacement replacement : replacements) {
            if (replacement.getName().equals("executeInstanceOfInstruction") &&
                    replacement.getDesc().equals(TransformerTools.descriptor(boolean.class, Object.class))) {
                return replacement;
            }
        }
        return null;
    }

    private void processMethod(ClassReplacement classReplacement, String methodName, MemberReplacement replacement) {
        Type[] types = Type.getArgumentTypes(replacement.getDesc());
        if (types.length > 0 &&
                types[0].equals(TransformerTools.getTypeByInternalName(classReplacement.getReferenceTypeName()))) {
            String descriptor = Type.getMethodDescriptor(Type.getReturnType(replacement.getDesc()), deleteFirst(types));
            MemberKey key = new MemberKey(false, methodName, descriptor);
            putIfAbsent(classReplacement.getMethodReplacements(), key, replacement);
        }
        if (methodName.equals("convertConstructorArguments")) {
            putForConstructor(classReplacement.getConverterReplacements(), replacement);
        } else if (methodName.equals("createNewInstance")) {
            putForConstructor(classReplacement.getInstantiationReplacements(), replacement);
        } else if (methodName.equals("createInstanceBuilder")) {
            loadConstructor(classReplacement, replacement);
        }
    }

    private static Type[] deleteFirst(Type[] types) {
        Type[] result = new Type[types.length - 1];
        System.arraycopy(types, 1, result, 0, result.length);
        return result;
    }

    private static void putForConstructor(Map<String, MemberReplacement> map, MemberReplacement replacement) {
        putIfAbsent(map, getConstructorDesc(replacement.getDesc()), replacement);
    }

    private static String getConstructorDesc(String desc) {
        return Type.getMethodDescriptor(Type.VOID_TYPE, Type.getArgumentTypes(desc));
    }

    private void loadConstructor(ClassReplacement classReplacement, MemberReplacement replacement) {
        Map<String, ConstructorReplacement> replacements = classReplacement.getConstructorReplacements();
        String constructorDesc = getConstructorDesc(replacement.getDesc());
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
            } else if (isInitializer(descriptor, classReplacement.getReferenceTypeName())) {
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
        String newConstructorDesc = Type.getMethodDescriptor(Type.VOID_TYPE, types.toArray(new Type[0]));
        replacements.put(constructorDesc, new ConstructorReplacement(replacement,
                arguments.toArray(new MemberReplacement[0]), newConstructorDesc, initializer));
    }

    private static boolean isArgument(MethodDescriptor descriptor) {
        if (!descriptor.getName().startsWith("argument")) return false;
        if (Type.getReturnType(descriptor.getDesc()) == Type.VOID_TYPE) return false;
        if (Type.getArgumentTypes(descriptor.getDesc()).length > 0) return false;
        return descriptor.isAccess(ACC_PUBLIC) && !descriptor.isAccess(ACC_STATIC);
    }

    private static boolean isInitializer(MethodDescriptor descriptor, String referenceTypeName) {
        if (!descriptor.getName().equals("initialize")) return false;
        if (Type.getReturnType(descriptor.getDesc()) != Type.VOID_TYPE) return false;
        Type[] types = Type.getArgumentTypes(descriptor.getDesc());
        if (types.length != 1) return false;
        if (!types[0].getInternalName().equals(referenceTypeName)) return false;
        return descriptor.isAccess(ACC_PUBLIC) && !descriptor.isAccess(ACC_STATIC);
    }

    private boolean isClassAvailable(String internalName) {
        return getClassDescriptor(internalName) != null;
    }

    private ClassDescriptor getClassDescriptor(String internalName) {
        byte[] bytecode = RuntimeTools.readResourceToByteArray(
                ReplacementLocator.class, '/' + internalName + RuntimeTools.CLASS_EXTENSION);
        if (bytecode == null) return null;
        ClassDescriptor descriptor = new ClassDescriptor(ReplacementLocator.class, bytecode);
        return isSupportedFeature(descriptor) ? descriptor : null;
    }

    private boolean isSupportedFeature(AnnotatedElementDescriptor descriptor) {
        Annotation_ annotation = descriptor.getAnnotation(Advanced.class);
        if (annotation == null) {
            return true;
        }
        for (String feature : ((Advanced) annotation).value()) {
            if (mode.isSupportedFeature(feature)) {
                return true;
            }
        }
        return false;
    }

    public String getUniqueTypeName(String className) {
        ClassReplacement replacement = getReplacement(className);
        return replacement != null ? replacement.getUniqueTypeName() : className;
    }

    public String getReferenceTypeName(String className) {
        ClassReplacement replacement = getReplacement(className);
        return replacement != null ? replacement.getReferenceTypeName() : className;
    }

    private static <K, V> void putIfAbsent(Map<K, V> map, K key, V value) {
        if (!map.containsKey(key)) {
            map.put(key, value);
        }
    }

}
