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
import net.sf.retrotranslator.runtime.asm.*;
import net.sf.retrotranslator.runtime.impl.*;

/**
 * @author Taras Puchko
 */
class ReferenceVerifyingVisitor extends GenericClassVisitor {

    private final ClassVersion target;
    private final TargetEnvironment environment;
    private final SystemLogger logger;
    private Set<String> warnings;

    public ReferenceVerifyingVisitor(ClassVersion target, TargetEnvironment environment, SystemLogger logger) {
        super(new EmptyVisitor());
        this.target = target;
        this.environment = environment;
        this.logger = logger;
    }

    public int verify(byte[] bytes) {
        warnings = new LinkedHashSet<String>();
        new ClassReader(bytes).accept(this, true);
        return warnings.size();
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        checkVersion(version, name);
        super.visit(version, access, name, signature, superName, interfaces);
    }

    private void checkVersion(int version, String name) {
        if (target.isBefore(version)) {
            println("Incompatible class: " + RuntimeTools.getDisplayClassName(name));
        }
    }

    protected String typeName(String s) {
        if (s == null) return null;
        try {
            environment.getClassReader(s);
        } catch (ClassNotFoundException e) {
            printClassNotFound(e);
        }
        return s;
    }

    private void printClassNotFound(ClassNotFoundException e) {
        println("Class not found: " + RuntimeTools.getDisplayClassName(e.getMessage()));
    }

    protected void visitFieldInstruction(MethodVisitor visitor, int opcode, String owner, String name, String desc) {
        super.visitFieldInstruction(visitor, opcode, owner, name, desc);
        boolean stat = (opcode == Opcodes.GETSTATIC || opcode == Opcodes.PUTSTATIC);
        try {
            int found = findMember(false, stat, name, desc, owner);
            if (found == 0) {
                println(getFieldInfo(owner, stat, name, desc, "not found"));
            } else if (found > 1) {
                println(getFieldInfo(owner, stat, name, desc, "duplicated"));
            }
        } catch (ClassNotFoundException e) {
            cannotVerify(getFieldInfo(owner, stat, name, desc, "not verified"), e);
        }
    }

    protected void visitMethodInstruction(MethodVisitor visitor, int opcode, String owner, String name, String desc) {
        super.visitMethodInstruction(visitor, opcode, owner, name, desc);
        if (owner.startsWith("[")) return;
        boolean stat = (opcode == Opcodes.INVOKESTATIC);
        try {
            int found = findMember(true, stat, name, desc, owner);
            if (found == 0) {
                println(getMethodInfo(owner, stat, name, desc, "not found"));
            } else if (found > 1) {
                println(getMethodInfo(owner, stat, name, desc, "duplicated"));
            }
        } catch (ClassNotFoundException e) {
            cannotVerify(getMethodInfo(owner, stat, name, desc, "not verified"), e);
        }
    }

    private int findMember(boolean method, boolean stat, String name,
                           String desc, String owner) throws ClassNotFoundException {
        return new MemberFinder(environment, method, stat, name, desc) {
            public void visit(int version, int access, String name, String signature,
                              String superName, String[] interfaces) {
                checkVersion(version, name);
                super.visit(version, access, name, signature, superName, interfaces);
            }
        }.findIn(owner, null);
    }

    private void cannotVerify(String text, ClassNotFoundException e) {
        printClassNotFound(e);
        println(text);
    }

    private void println(String text) {
        if (!warnings.contains(text)) {
            warnings.add(text);
            logger.logForFile(Level.WARNING, text);
        }
    }

    private static String getFieldInfo(String owner, boolean stat, String name, String desc, String message) {
        StringBuffer buffer = new StringBuffer("Field ").append(message).append(": ");
        if (stat) buffer.append("static ");
        buffer.append(Type.getType(desc).getClassName()).append(' ');
        buffer.append(RuntimeTools.getDisplayClassName(owner)).append('.').append(name);
        return buffer.toString();
    }

    private static String getMethodInfo(String owner, boolean stat, String name, String desc, String message) {
        StringBuffer buffer = new StringBuffer();
        if (name.equals(RuntimeTools.CONSTRUCTOR_NAME)) {
            buffer.append("Constructor ").append(message).append(": ");
            buffer.append(RuntimeTools.getDisplayClassName(owner));
        } else {
            buffer.append("Method ").append(message).append(": ");
            if (stat) buffer.append("static ");
            buffer.append(Type.getReturnType(desc).getClassName());
            buffer.append(' ').append(RuntimeTools.getDisplayClassName(owner)).append('.').append(name);
        }
        buffer.append('(');
        boolean first = true;
        for (Type type : Type.getArgumentTypes(desc)) {
            buffer.append(first ? "" : ",").append(type.getClassName());
            first = false;
        }
        return buffer.append(')').toString();
    }

}
