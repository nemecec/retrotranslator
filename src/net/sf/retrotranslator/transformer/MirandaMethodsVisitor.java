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
import static net.sf.retrotranslator.runtime.asm.Opcodes.*;
import net.sf.retrotranslator.runtime.impl.EmptyVisitor;

/**
 * @author Taras Puchko
 */
class MirandaMethodsVisitor extends ClassAdapter {

    private final ReplacementLocator locator;
    private final NameTranslator translator;

    public MirandaMethodsVisitor(ClassVisitor visitor, ReplacementLocator locator) {
        super(visitor);
        this.locator = locator;
        this.translator = locator.getTranslator();
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        if ((access & (ACC_ABSTRACT | ACC_INTERFACE)) == ACC_ABSTRACT) {
            addMirandaMethods(name, superName, interfaces);
        }
    }

    private void addMirandaMethods(String name, String superName, String[] interfaces) {
        if (interfaces == null) {
            return;
        }
        Set<InheritedMethod> methods = new LinkedHashSet<InheritedMethod>();
        for (String anInterface : interfaces) {
            methods.addAll(getMethods(anInterface, true));
        }
        methods.removeAll(getMethods(superName, true));
        methods.removeAll(getMethods(name, false));
        for (InheritedMethod method : methods) {
            MethodVisitor visitor = cv.visitMethod(
                    ACC_PUBLIC | ACC_ABSTRACT | ACC_SYNTHETIC,
                    method.name, method.desc, null, method.exceptions);
            visitor.visitEnd();
        }
    }

    private Set<InheritedMethod> getMethods(String className, boolean recursive) {
        String uniqueTypeName = locator.getUniqueTypeName(className);
        Set<InheritedMethod> methods = new LinkedHashSet<InheritedMethod>();
        ClassReader classReader = locator.getEnvironment().findClassReader(uniqueTypeName);
        if (classReader == null) {
            return methods;
        }
        MethodCollector collector = new MethodCollector(methods);
        classReader.accept(collector, true);
        if (recursive) {
            if (collector.superName != null) {
                methods.addAll(getMethods(collector.superName, true));
            }
            if (collector.interfaces != null) {
                for (String anInterface : collector.interfaces) {
                    methods.addAll(getMethods(anInterface, true));
                }
            }
        }
        return methods;
    }

    private class MethodCollector extends EmptyVisitor {

        public String superName;
        public String[] interfaces;
        private Set<InheritedMethod> methods;

        public MethodCollector(Set<InheritedMethod> methods) {
            this.methods = methods;
        }

        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            this.superName = superName;
            this.interfaces = interfaces;
        }

        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if ((access & ACC_PRIVATE) == 0 && (access & ACC_STATIC) == 0) {
                methods.add(new InheritedMethod(
                        translator.identifier(name),
                        translator.methodDescriptor(desc),
                        translator.typeNames(exceptions)));
            }
            return null;
        }
    }

    private static class InheritedMethod {

        public final String name;
        public final String desc;
        public final String[] exceptions;

        public InheritedMethod(String name, String desc, String[] exceptions) {
            this.name = name;
            this.desc = desc;
            this.exceptions = exceptions;
        }

        public int hashCode() {
            return name.hashCode();
        }

        public boolean equals(Object obj) {
            if (obj instanceof InheritedMethod) {
                InheritedMethod method = (InheritedMethod) obj;
                return name.equals(method.name) && desc.equals(method.desc);
            }
            return false;
        }
    }

}
