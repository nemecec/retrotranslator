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

import edu.emory.mathcs.backport.java.util.Queue;
import static net.sf.retrotranslator.runtime.asm.Opcodes.ACC_PUBLIC;
import static net.sf.retrotranslator.runtime.asm.Opcodes.ACC_STATIC;
import net.sf.retrotranslator.runtime.asm.Type;
import net.sf.retrotranslator.runtime.impl.ClassDescriptor;
import net.sf.retrotranslator.runtime.impl.Derived;
import net.sf.retrotranslator.runtime.impl.MethodDescriptor;
import net.sf.retrotranslator.runtime.java.util._Queue;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Taras Puchko
 */
public class BackportFactory {

    private static final String RUNTIME = "net/sf/retrotranslator/runtime/";
    private static Map<String, Boolean> classes = new HashMap<String, Boolean>();
    private static Map<String, String[]> implementations = new HashMap<String, String[]>();
    private static Map<ClassMember, ClassMember> methods = new HashMap<ClassMember, ClassMember>();

    static {
        String queueName = Type.getInternalName(Queue.class);
        for (Class aClass : new Class[]{Collection.class, _Queue.class}) {
            loadBackport(Type.getInternalName(aClass), queueName);
        }
        classes.put(queueName, true);
    }

    public static String[] getImplementations(String desc) {
        return isBackported(desc) ? implementations.get(desc) : null;
    }

    public static ClassMember getMethod(boolean isStatic, String owner, String name, String desc) {
        return isBackported(owner) ? methods.get(new ClassMember(isStatic, owner, name, desc)) : null;
    }

    private static boolean isBackported(String owner) {
        if (owner.charAt(0) == '[') return false;
        Boolean isBackported = classes.get(owner);
        if (isBackported != null) {
            return isBackported;
        }
        String original = owner;
        if (original.startsWith(RUNTIME) && original.endsWith("_")) {
            original = original.substring(RUNTIME.length(), original.length() - 1);
        }
        StringBuilder builder = new StringBuilder(RUNTIME);
        int index = original.lastIndexOf('/');
        if (index >= 0) {
            builder.append(original.substring(0, index + 1));
        }
        builder.append('_').append(original.substring(index + 1));
        isBackported = loadBackport(builder.toString(), owner);
        classes.put(owner, isBackported);
        return isBackported;
    }

    private static boolean loadBackport(String backportName, String originalName) {
        try {
            Class backportClass = Class.forName(backportName.replace('/', '.'));
            ClassDescriptor descriptor = ClassDescriptor.getInstance(backportClass);
            loadImplementations(descriptor, originalName);
            loadMethods(descriptor, originalName);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static void loadImplementations(ClassDescriptor descriptor, String originalName) {
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

    private static void loadMethods(ClassDescriptor descriptor, String originalName) {
        Type originalType = TransformerTools.getTypeByInternalName(originalName);
        for (MethodDescriptor methodDescriptor : descriptor.getMethodDescriptors()) {
            String name = methodDescriptor.getName();
            if (!methodDescriptor.isAccess(ACC_PUBLIC) || name.charAt(0) == '<') continue;
            boolean isStatic = methodDescriptor.isAccess(ACC_STATIC);
            String desc = methodDescriptor.getDesc();
            ClassMember substitutionMember = new ClassMember(isStatic, descriptor.getName(), name, desc);
            Type[] types = Type.getArgumentTypes(desc);
            if (isStatic && types.length > 0 && types[0].equals(originalType)) {
                isStatic = false;
                desc = Type.getMethodDescriptor(Type.getReturnType(desc), removeFirst(types));
            }
            ClassMember originalMember = new ClassMember(isStatic, originalType.getInternalName(), name, desc);
            methods.put(originalMember, substitutionMember);
        }
    }

    private static Type[] removeFirst(Type[] types) {
        Type[] result = new Type[types.length - 1];
        System.arraycopy(types, 1, result, 0, result.length);
        return result;
    }

}
