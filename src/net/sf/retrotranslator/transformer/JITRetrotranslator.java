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

import net.sf.retrotranslator.runtime.asm.ClassWriter;
import net.sf.retrotranslator.runtime.asm.*;
import net.sf.retrotranslator.runtime.asm.Type;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author Taras Puchko
 */
public class JITRetrotranslator {

    private static boolean installed;

    public static class JITTransformer {
        private ClassTransformer transformer = new ClassTransformer(true);

        public byte[] transform(byte[] filecontent, int offset, int length) {
            return transformer.transform(filecontent, offset, length);
        }
    }

    private static class JITCreator extends GenericClassVisitor {
        public JITCreator(final ClassVisitor cv) {
            super(cv);
        }

        protected String visitInternalName(String name) {
            if (name.equals(Type.getInternalName(Object.class))) {
                return "sun/misc/ClassFileTransformer";
            }
            if (name.equals(Type.getInternalName(JITTransformer.class))) {
                return Type.getInternalName(JITTransformer.class) + "$";
            }
            return name;
        }
    }

    private JITRetrotranslator() {
    }

    public static synchronized void install() {
        if (installed) return;
        try {
            Class transformerClass = Class.forName("sun.misc.ClassFileTransformer");
            Class unsafeClass = Class.forName("sun.misc.Unsafe");
            Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            Method method = unsafeClass.getMethod("defineClass", String.class, byte[].class, int.class, int.class);
            ClassReader reader = new ClassReader(JITTransformer.class.getName());
            ClassWriter writer = new ClassWriter(false);
            reader.accept(new JITCreator(writer), true);
            byte[] bytes = writer.toByteArray();
            Class jitClass = (Class) method.invoke(theUnsafe.get(null),
                    JITTransformer.class.getName() + "$", bytes, 0, bytes.length);
            transformerClass.getMethod("add", transformerClass).invoke(null, jitClass.newInstance());
            installed = true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        boolean jar = args.length > 0 && args[0].equals("-jar");
        if (args.length == 0 || jar && args.length == 1) {
            printUsageAndExit();
        }
        install();
        if (jar) {
            File file = new File(args[1]);
            if (!file.isFile()) printErrorAndExit("Unable to access jarfile " + file);
            JarClassLoader classLoader = new JarClassLoader(file, getClassLoader());
            String mainClass = classLoader.getMainClass();
            if (mainClass == null) printErrorAndExit("Failed to load Main-Class manifest attribute from " + file);
            execute(classLoader, mainClass, remove(args, 2));
        } else {
            execute(getClassLoader(), args[0], remove(args, 1));
        }
    }

    private static ClassLoader getClassLoader() {
        ClassLoader classLoader = JITRetrotranslator.class.getClassLoader();
        return classLoader != null ? classLoader : ClassLoader.getSystemClassLoader();
    }

    private static String[] remove(String[] original, int count) {
        String[] result = new String[original.length - count];
        System.arraycopy(original, original.length - result.length, result, 0, result.length);
        return result;
    }

    private static void execute(ClassLoader classLoader, String mainClass, String[] args) throws Exception {
        try {
            classLoader.loadClass(mainClass).getMethod("main", String[].class).invoke(null, new Object[]{args});
        } catch (NoSuchMethodException e) {
            printErrorAndExit("Could not find the method \"main\" in: " + mainClass);
        } catch (ClassNotFoundException e) {
            printErrorAndExit("Could not find the main class: " + mainClass);
        }
    }

    private static void printUsageAndExit() {
        String version = JITRetrotranslator.class.getPackage().getImplementationVersion();
        String suffix = (version == null) ? "" : "-" + version;
        StringBuilder builder = new StringBuilder("Usage: java -cp retrotranslator-transformer").append(suffix);
        builder.append(".jar").append(File.pathSeparator);
        builder.append("<classpath> net.sf.retrotranslator.transformer.JITRetrotranslator <class> [<args...>]\n");
        builder.append("   or  java -cp retrotranslator-transformer").append(suffix);
        builder.append(".jar net.sf.retrotranslator.transformer.JITRetrotranslator -jar <jarfile> [<args...>]");
        System.out.println(builder);
        System.exit(1);
    }

    private static void printErrorAndExit(String msg) {
        System.out.println(msg);
        System.exit(1);
    }
}
