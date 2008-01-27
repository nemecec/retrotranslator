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
package net.sf.retrotranslator.registry;

import java.io.*;
import java.util.*;
import net.sf.retrotranslator.runtime.asm.*;
import net.sf.retrotranslator.runtime.impl.*;

/**
 * @author Taras Puchko
 */
public class AdvancedPostProcessor {

    private final static String ADVANCED_DESCRIPTOR = Type.getType(Advanced.class).getDescriptor();

    private final Map<String, String> map = new LinkedHashMap<String, String>();

    private class AdvancedClassVisitor extends ClassAdapter {

        private String className;

        public AdvancedClassVisitor(ClassVisitor visitor) {
            super(visitor);
        }

        public void visit(int version, int access, String name,
                          String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
            className = RuntimeTools.getDisplayClassName(name);
        }

        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            return desc.equals(ADVANCED_DESCRIPTOR) ?
                    new AdvancedAnnotationVisitor(className) :
                    super.visitAnnotation(desc, visible);
        }

        public MethodVisitor visitMethod(int access, String name,
                                         String desc, String signature, String[] exceptions) {
            return new AdvancedMethodVisitor(RuntimeTools.getMethodInfo(className, name, desc),
                    super.visitMethod(access, name, desc, signature, exceptions));
        }

        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            return new AdvancedFieldVisitor(RuntimeTools.getFieldInfo(className, name),
                    super.visitField(access, name, desc, signature, value));
        }
    }

    private class AdvancedMethodVisitor extends MethodAdapter {

        private final String key;

        public AdvancedMethodVisitor(String key, MethodVisitor visitor) {
            super(visitor);
            this.key = key;
        }

        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            return desc.equals(ADVANCED_DESCRIPTOR) ?
                    new AdvancedAnnotationVisitor(key) :
                    super.visitAnnotation(desc, visible);
        }
    }

    private class AdvancedFieldVisitor implements FieldVisitor {

        private final String key;
        private final FieldVisitor visitor;

        public AdvancedFieldVisitor(String key, FieldVisitor visitor) {
            this.key = key;
            this.visitor = visitor;
        }

        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            return desc.equals(ADVANCED_DESCRIPTOR) ?
                    new AdvancedAnnotationVisitor(key) :
                    visitor.visitAnnotation(desc, visible);
        }

        public void visitAttribute(Attribute attr) {
            visitor.visitAttribute(attr);
        }

        public void visitEnd() {
            visitor.visitEnd();
        }
    }

    private class AdvancedAnnotationVisitor extends AnnotationValue {

        public AdvancedAnnotationVisitor(String descriptor) {
            super(descriptor);
        }

        public void visitEnd() {
            AnnotationArray array = (AnnotationArray) getElement("value");
            StringBuilder builder = new StringBuilder();
            for (Object value : array.getValues()) {
                if (builder.length() > 0) {
                    builder.append(',');
                }
                builder.append(value);
            }
            map.put(getDesc(), builder.toString());
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            throw new IllegalArgumentException();
        }
        AdvancedPostProcessor processor = new AdvancedPostProcessor();
        processor.traverse(new File(args[0]));
        processor.save(new File(args[1]));
    }

    private void traverse(File sourceFolder) throws IOException {
        for (File file : sourceFolder.listFiles()) {
            if (file.isDirectory()) {
                traverse(file);
            } else if (file.getPath().endsWith(".class")) {
                translate(file);
            }
        }
    }

    private void translate(File file) throws IOException {
        ClassWriter classWriter = new ClassWriter(true);
        FileInputStream inputStream = new FileInputStream(file);
        try {
            new ClassReader(inputStream).accept(new AdvancedClassVisitor(classWriter), false);
        } finally {
            inputStream.close();
        }
        FileOutputStream outputStream = new FileOutputStream(file);
        try {
            outputStream.write(classWriter.toByteArray(true));
        } finally {
            outputStream.close();
        }
    }

    private void save(File outputFile) throws IOException {
        outputFile.getParentFile().mkdirs();
        Writer writer = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8");
        try {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                writer.write(entry.getKey());
                writer.write(':');
                writer.write(entry.getValue());
                writer.write("\r\n");
            }
        } finally {
            writer.close();
        }
    }

}
