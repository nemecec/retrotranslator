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

import java.io.*;
import net.sf.retrotranslator.runtime.asm.*;

/**
 * @author Taras Puchko
 */
public class Runtime13Creator {

    private static final String RUNTIME_FOLDER = "net/sf/retrotranslator/runtime/";
    private static final String RUNTIME13_FOLDER = "net/sf/retrotranslator/runtime13/";

    private final File root;

    private class Runtime13ClassVisitor extends GenericClassVisitor {
        public String className;

        public Runtime13ClassVisitor(ClassVisitor visitor) {
            super(visitor);
        }

        protected String typeName(String s) {
            if (s == null || !s.startsWith(RUNTIME_FOLDER)) {
                return s;
            }
            String name = s.substring(RUNTIME_FOLDER.length());
            StringBuilder builder = new StringBuilder(RUNTIME13_FOLDER);
            if (name.startsWith("java")) {
                builder.append("v15/");
            }
            return builder.append(name).toString();
        }

        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
            className = typeName(name);
        }


        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (isClass("v15/java/lang/_Character") && (name.equals("getDirectionality") || name.equals("isMirrored"))) {
                return null;
            }
            if (isClass("v15/java/io/_PrintStream") && name.equals("createInstanceBuilder")) {
                return null;
            }
            if (isClass("v15/java/lang/_Thread$AdvancedThreadBuilder") && name.equals("argument4")) {
                return null;
            }
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

        private boolean isClass(String name) {
            return className.equals(RUNTIME13_FOLDER + name);
        }

        public void visitInnerClass(String name, String outerName, String innerName, int access) {
            if (isClass("v15/java/io/_PrintStream") && innerName.equals("PrintStreamBuilder")) {
                return;
            }
            super.visitInnerClass(name, outerName, innerName, access);
        }
    }

    public Runtime13Creator(File root) {
        this.root = root;
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException();
        }
        File root = new File(args[0]);
        new Runtime13Creator(root).traverse(new File(root, RUNTIME13_FOLDER));
        new Runtime13Creator(root).traverse(new File(root, RUNTIME_FOLDER));
    }

    private void traverse(File sourceFolder) throws IOException {
        for (File file : sourceFolder.listFiles()) {
            if (file.isDirectory()) {
                traverse(file);
            } else if (file.getPath().endsWith(".class")) {
                copy(file);
            }
        }
    }

    private void copy(File source) throws IOException {
        ClassWriter classWriter = new ClassWriter(true);
        Runtime13ClassVisitor visitor = new Runtime13ClassVisitor(classWriter);
        FileInputStream inputStream = new FileInputStream(source);
        try {
            new ClassReader(inputStream).accept(visitor, false);
        } finally {
            inputStream.close();
        }
        File target = new File(root, visitor.className + ".class");
        target.getParentFile().mkdirs();
        FileOutputStream outputStream = new FileOutputStream(target);
        try {
            outputStream.write(classWriter.toByteArray(false));
        } finally {
            outputStream.close();
        }
    }

}
