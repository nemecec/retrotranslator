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
import net.sf.retrotranslator.runtime.java.lang.Iterable_;
import net.sf.retrotranslator.runtime.java.lang.Appendable_;
import net.sf.retrotranslator.runtime.java.lang.reflect.AnnotatedElement_;
import net.sf.retrotranslator.runtime.java.lang.reflect.GenericDeclaration_;
import net.sf.retrotranslator.runtime.java.lang.reflect.Type_;
import net.sf.retrotranslator.runtime.java.io.Closeable_;
import net.sf.retrotranslator.runtime.java.io.Flushable_;
import net.sf.retrotranslator.runtime.asm.*;
import static net.sf.retrotranslator.runtime.asm.Opcodes.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.io.*;
import java.nio.channels.Channel;
import java.nio.CharBuffer;

/**
 * @author Taras Puchko
 */
public class InheritanceVisitor extends ClassAdapter {

    private static Map<String, String[]> implementations = new HashMap<String, String[]>();

    static {
        add(AnnotatedElement_.class, Package.class, Class.class, Constructor.class, Field.class, Method.class);
        add(Appendable_.class, StringBuffer.class, PrintStream.class, Writer.class, CharBuffer.class);
        add(Closeable_.class, InputStream.class, OutputStream.class,
                Reader.class, Writer.class, RandomAccessFile.class, Channel.class);
        add(Flushable_.class, OutputStream.class, Writer.class);
        add(GenericDeclaration_.class, Class.class, Constructor.class, Method.class);
        add(Iterable_.class, Collection.class);
        add(Queue.class, LinkedList.class);
        add(Type_.class, Class.class);
    }

    public InheritanceVisitor(final ClassVisitor cv) {
        super(cv);
    }

    private static void add(Class superType, Class... subTypes) {
        String[] names = new String[subTypes.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = Type.getInternalName(subTypes[i]);
        }
        implementations.put(Type.getInternalName(superType), names);
    }

    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        return new MethodAdapter(super.visitMethod(access, name, desc, signature, exceptions)) {

            public void visitTypeInsn(final int opcode, final String desc) {
                if (opcode == CHECKCAST) {
                    visitCheckCast(desc);
                } else if (opcode == INSTANCEOF) {
                    visitInstanceOf(desc);
                } else if (opcode == ANEWARRAY) {
                    super.visitTypeInsn(ANEWARRAY, fixArrayType(desc));
                } else {
                    super.visitTypeInsn(opcode, desc);
                }
            }

            public void visitMultiANewArrayInsn(final String desc, final int dims) {
                super.visitMultiANewArrayInsn(fixArrayType(desc), dims);
            }

            private String fixArrayType(String desc) {
                if (desc.charAt(0) != '[') {
                    return implementations.containsKey(desc) ? Type.getInternalName(Object.class) : desc;
                }
                int first = 1;
                while (desc.charAt(first) == '[') first++;
                if (desc.charAt(first) != 'L') return desc;
                int last = desc.length() - 1;
                if (desc.charAt(last) != ';') return desc;
                if (!implementations.containsKey(desc.substring(first + 1, last))) return desc;
                return desc.substring(0, first) + Type.getDescriptor(Object.class);
            }

            private void visitCheckCast(String desc) {
                if (desc.charAt(0) == '[') {
                    mv.visitTypeInsn(CHECKCAST, fixArrayType(desc));
                    return;
                }
                String[] list = implementations.get(desc);
                if (list == null) {
                    mv.visitTypeInsn(CHECKCAST, desc);
                    return;
                }
                Label exit = new Label();
                for (String name : list) {
                    visitInsn(DUP);
                    mv.visitTypeInsn(INSTANCEOF, name);
                    mv.visitJumpInsn(IFNE, exit);
                }
                mv.visitTypeInsn(CHECKCAST, desc);
                mv.visitLabel(exit);
            }

            private void visitInstanceOf(String desc) {
                String[] list = implementations.get(desc);
                if (list == null) {
                    mv.visitTypeInsn(INSTANCEOF, desc);
                    return;
                }
                Label okLabel = new Label();
                Label exitLabel = new Label();
                mv.visitInsn(DUP);
                mv.visitTypeInsn(INSTANCEOF, desc);
                mv.visitJumpInsn(IFNE, okLabel);
                for (String name : list) {
                    mv.visitInsn(DUP);
                    mv.visitTypeInsn(INSTANCEOF, name);
                    mv.visitJumpInsn(IFNE, okLabel);
                }
                mv.visitInsn(POP);
                mv.visitInsn(ICONST_0);
                mv.visitJumpInsn(GOTO, exitLabel);
                mv.visitLabel(okLabel);
                mv.visitInsn(POP);
                mv.visitInsn(ICONST_1);
                mv.visitLabel(exitLabel);
            }

        };
    }
}
