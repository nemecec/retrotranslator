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

import net.sf.retrotranslator.runtime.asm.*;
import java.io.*;

/**
 * @author Taras Puchko
 */
public abstract class RuntimeCreator {

    private static final String SOURCE_PACKAGE = "net/sf/retrotranslator/runtime/";

    private final File rootFolder;
    private final String targetPackage;
    private final String infix;
    private String className;

    protected RuntimeCreator(File rootFolder, String targetPackage, String infix) {
        this.targetPackage = targetPackage;
        this.rootFolder = rootFolder;
        this.infix = infix;
    }

    protected abstract boolean isRightField(String name);

    protected abstract boolean isRightMethod(String name);

    protected abstract boolean isRightInnerClass(String innerName);

    protected boolean isClass(String name) {
        return className.equals(targetPackage + infix + name);
    }

    protected void execute() throws IOException {
        traverse(new File(rootFolder, targetPackage));
        traverse(new File(rootFolder, SOURCE_PACKAGE));
    }

    private void traverse(File folder) throws IOException {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                traverse(file);
            } else if (file.getPath().endsWith(".class")) {
                copy(file);
            }
        }
    }

    private void copy(File source) throws IOException {
        ClassWriter classWriter = new ClassWriter(true);
        FileInputStream inputStream = new FileInputStream(source);
        try {
            new ClassReader(inputStream).accept(new RuntimeCreatorVisitor(classWriter), false);
        } finally {
            inputStream.close();
        }
        File target = new File(rootFolder, className + ".class");
        target.getParentFile().mkdirs();
        FileOutputStream outputStream = new FileOutputStream(target);
        try {
            outputStream.write(classWriter.toByteArray(false));
        } finally {
            outputStream.close();
        }
    }

    private class RuntimeCreatorVisitor extends GenericClassVisitor {

        public RuntimeCreatorVisitor(ClassVisitor visitor) {
            super(visitor);
        }

        protected String typeName(String s) {
            if (s == null || !s.startsWith(SOURCE_PACKAGE)) {
                return s;
            }
            String name = s.substring(SOURCE_PACKAGE.length());
            StringBuilder builder = new StringBuilder(targetPackage);
            if (name.startsWith("java")) {
                builder.append(infix);
            }
            return builder.append(name).toString();
        }

        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
            className = typeName(name);
        }

        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            return isRightField(name) ? super.visitField(access, name, desc, signature, value) : null;
        }

        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            return isRightMethod(name) ? super.visitMethod(access, name, desc, signature, exceptions) : null;
        }

        public void visitInnerClass(String name, String outerName, String innerName, int access) {
            if (isRightInnerClass(innerName)) {
                super.visitInnerClass(name, outerName, innerName, access);
            }
        }
    }

}
