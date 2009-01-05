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
package net.sf.retrotranslator.transformer;

import java.util.*;
import net.sf.retrotranslator.runtime.asm.*;
import net.sf.retrotranslator.runtime.impl.*;

/**
 * @author Taras Puchko
 */
class SmartReplacementVisitor extends EmptyVisitor {

    private final ReplacementLocator locator;
    private final NameTranslator translator;
    private boolean enabled;
    private Set<String> constructorDescriptors = new HashSet<String>();
    private Map<MemberKey, MemberReplacement> fieldReplacements = new HashMap<MemberKey, MemberReplacement>();
    private Map<MemberKey, MemberReplacement> methodReplacements = new HashMap<MemberKey, MemberReplacement>();
    private Map<String, MemberReplacement> converterReplacements = new HashMap<String, MemberReplacement>();
    private Map<String, ConstructorReplacement> constructorReplacements = new HashMap<String, ConstructorReplacement>();

    public SmartReplacementVisitor(ReplacementLocator locator) {
        this.locator = locator;
        this.translator = locator.getTranslator();
    }

    public void addInheritedMembers(ClassReplacement replacement) {
        if (enabled) {
            cleanConstructorReplacements();
            cleanConverterReplacements();
            copyIfAbsent(fieldReplacements, replacement.getFieldReplacements());
            copyIfAbsent(methodReplacements, replacement.getMethodReplacements());
            copyIfAbsent(converterReplacements, replacement.getConverterReplacements());
            copyIfAbsent(constructorReplacements, replacement.getConstructorReplacements());
        }
    }

    private void cleanConstructorReplacements() {
        Iterator<ConstructorReplacement> iterator = constructorReplacements.values().iterator();
        while (iterator.hasNext()) {
            ConstructorReplacement replacement = iterator.next();
            if (!constructorDescriptors.contains(replacement.getConstructorDesc())) {
                iterator.remove();
            }
        }
    }

    private void cleanConverterReplacements() {
        Iterator<MemberReplacement> iterator = converterReplacements.values().iterator();
        while (iterator.hasNext()) {
            MemberReplacement replacement = iterator.next();
            if (!constructorDescriptors.contains(ClassReplacement.getConstructorDesc(replacement))) {
                iterator.remove();
            }
        }
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        if (superName != null) {
            saveInheritedMembers(superName);
        }
        if (interfaces != null) {
            for (String interfaceName : interfaces) {
                saveInheritedMembers(interfaceName);
            }
        }
    }

    private void saveInheritedMembers(String className) {
        ClassReplacement replacement = locator.getReplacement(locator.getUniqueTypeName(className));
        if (replacement == null) {
            return;
        }
        enabled = true;
        copyIfAbsent(replacement.getFieldReplacements(), fieldReplacements);
        copyIfAbsent(replacement.getMethodReplacements(), methodReplacements);
        copyIfAbsent(replacement.getConverterReplacements(), converterReplacements);
        copyIfAbsent(replacement.getConstructorReplacements(), constructorReplacements);
    }

    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        if (enabled && (access & Opcodes.ACC_STATIC) != 0) {
            MemberKey key = new MemberKey(true, translator.identifier(name), translator.typeDescriptor(desc));
            fieldReplacements.remove(key);
        }
        return null;
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (enabled) {
            String descriptor = translator.methodDescriptor(desc);
            if (name.equals(RuntimeTools.CONSTRUCTOR_NAME)) {
                constructorDescriptors.add(descriptor);
                converterReplacements.remove(descriptor);
                constructorReplacements.remove(descriptor);
            } else {
                boolean statical = (access & Opcodes.ACC_STATIC) != 0;
                methodReplacements.remove(new MemberKey(statical, translator.identifier(name), descriptor));
            }
        }
        return null;
    }

    private <K, V> void copyIfAbsent(Map<K, V> source, Map<K, V> destination) {
        for (Map.Entry<K, V> entry : source.entrySet()) {
            if (!destination.containsKey(entry.getKey())) {
                destination.put(entry.getKey(), entry.getValue());
            }
        }
    }

}
