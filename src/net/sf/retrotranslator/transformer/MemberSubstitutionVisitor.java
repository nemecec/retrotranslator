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
import edu.emory.mathcs.backport.java.util.concurrent.DelayQueue;
import edu.emory.mathcs.backport.java.util.concurrent.Delayed;
import net.sf.retrotranslator.runtime.impl.EmptyVisitor;
import net.sf.retrotranslator.runtime.java.util._Queue;
import net.sf.retrotranslator.runtime.asm.*;
import static net.sf.retrotranslator.runtime.asm.Opcodes.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Taras Puchko
 */
public class MemberSubstitutionVisitor extends ClassAdapter {

    private static final String RUNTIME = "net/sf/retrotranslator/runtime/";
    private static final String DELAY_QUEUE_NAME = Type.getInternalName(DelayQueue.class);
    private static final String DELAYED_NAME = Type.getInternalName(Delayed.class);

    private static final ClassLoader LOADER = MemberSubstitutionVisitor.class.getClassLoader();

    private static Map<String, Boolean> classes = new HashMap<String, Boolean>();
    private static Map<ClassMember, ClassMember> methods = new HashMap<ClassMember, ClassMember>();

    private static DescriptorTransformer DELAYED_TRANSFORMER = new DescriptorTransformer() {
        protected String transformInternalName(String internalName) {
            return DELAYED_NAME.equals(internalName) ? Type.getInternalName(Object.class) : internalName;
        }
    };

    private String currentClass;

    static {
        String queueName = Type.getInternalName(Queue.class);
        for (Class aClass : new Class[] {Collection.class, _Queue.class}) {
            loadBackport(queueName, new StringBuilder(Type.getInternalName(aClass)));
        }
        classes.put(queueName, true);
    }

    public MemberSubstitutionVisitor(final ClassVisitor cv) {
        super(cv);
    }

    public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        currentClass = name;
    }

    private ClassMember getMethod(boolean isStatic, String owner, String name, String desc) {
        if (owner.equals(currentClass) || owner.charAt(0) == '[') return null;
        Boolean isBackported = classes.get(owner);
        if (isBackported == null) {
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
            isBackported = loadBackport(owner, builder);
            classes.put(owner, isBackported);
        }
        return isBackported ? methods.get(new ClassMember(isStatic, owner, name, desc)) : null;
    }

    private static Boolean loadBackport(String originalName, StringBuilder backportName) {
        InputStream stream = LOADER.getResourceAsStream(backportName.append(".class").toString());
        if (stream == null) return false;
        try {
            try {
                MemberCollector memberCollector = new MemberCollector(TransformerTools.getTypeByInternalName(originalName));
                new ClassReader(stream).accept(memberCollector, true);
                return true;
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        return new MethodAdapter(super.visitMethod(access, name, desc, signature, exceptions)) {
            public void visitMethodInsn(int opcode, String owner, String name, String desc) {
                ClassMember method = getMethod(opcode == INVOKESTATIC, owner, name, desc);
                if (method != null) {
                    opcode = method.isStatic ? INVOKESTATIC : INVOKEINTERFACE;
                    owner = method.owner;
                    name = method.name;
                    desc = method.desc;
                }
                if (owner.equals(DELAY_QUEUE_NAME)) {
                    desc = DELAYED_TRANSFORMER.transformDescriptor(desc);
                }
                super.visitMethodInsn(opcode, owner, name, desc);
            }
        };
    }

    private static Type[] removeFirst(Type[] types) {
        Type[] result = new Type[types.length - 1];
        System.arraycopy(types, 1, result, 0, result.length);
        return result;
    }

    private static class MemberCollector extends EmptyVisitor {

        private final Type originalType;
        private String substitutionName;

        public MemberCollector(Type originalType) {
            this.originalType = originalType;
        }

        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            substitutionName = name;
        }

        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if ((access & ACC_PUBLIC) == 0 || name.charAt(0) == '<') return null;
            boolean isStatic = (access & ACC_STATIC) != 0;
            ClassMember substitutionMember = new ClassMember(isStatic, substitutionName, name, desc);
            Type[] types = Type.getArgumentTypes(desc);
            if (isStatic && types.length > 0 && types[0].equals(originalType)) {
                isStatic = false;
                desc = Type.getMethodDescriptor(Type.getReturnType(desc), removeFirst(types));
            }
            ClassMember originalMember = new ClassMember(isStatic, originalType.getInternalName(), name, desc);
            methods.put(originalMember, substitutionMember);
            return null;
        }
    }

    private static class ClassMember {

        public final boolean isStatic;
        public final String owner;
        public final String name;
        public final String desc;

        public ClassMember(boolean isStatic, String owner, String name, String desc) {
            this.isStatic = isStatic;
            this.owner = owner;
            this.name = name;
            this.desc = desc;
        }

        public boolean equals(Object o) {
            if (o == this) return true;
            if (o == null || o.getClass() != this.getClass()) return false;
            final ClassMember that = (ClassMember) o;
            return isStatic == that.isStatic && owner.equals(that.owner) && name.equals(that.name) && desc.equals(that.desc);
        }

        public int hashCode() {
            return owner.hashCode() + name.hashCode() + desc.hashCode();
        }
    }
}
