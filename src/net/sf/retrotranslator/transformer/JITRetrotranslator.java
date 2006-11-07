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

import net.sf.retrotranslator.runtime.impl.ClassDescriptor;
import net.sf.retrotranslator.runtime.impl.RuntimeTools;

import java.io.File;

/**
 * @author Taras Puchko
 */
public class JITRetrotranslator {

    private boolean advanced;
    private String backport;

    public JITRetrotranslator() {
    }

    public void setAdvanced(boolean advanced) {
        this.advanced = advanced;
    }

    public void setBackport(String backport) {
        this.backport = backport;
    }

    /**
     * @deprecated Use {@link #run} instead.
     */
    public static synchronized boolean install() {
        JITRetrotranslator jit = new JITRetrotranslator();
        jit.setAdvanced(true);
        return jit.run();
    }

    public boolean run() {
        if (isJava5Supported()) return true;
        BackportLocatorFactory factory = new BackportLocatorFactory(backport);
        ClassTransformer transformer = new ClassTransformer(true, advanced, false, false, false, null, null, factory);
        ClassDescriptor.setBytecodeTransformer(transformer);
        return (JRockitJITRetrotranslator.install(transformer) ||
                SunJITRetrotranslator.install(transformer)) && isJava5Supported();
    }

    private static boolean isJava5Supported() {
        class ClassFactory extends ClassLoader {
            public Class defineClass(String name, byte[] bytes) {
                return defineClass(name, bytes, 0, bytes.length);
            }
        }
        byte[] bytes = RuntimeTools.getBytecode(JITRetrotranslator.class);
        System.arraycopy(new byte[]{0, 0, 0, 49}, 0, bytes, 4, 4);
        try {
            new ClassFactory().defineClass(JITRetrotranslator.class.getName(), bytes);
            return true;
        } catch (ClassFormatError e) {
            return false;
        }
    }

    public static void main(String[] args) throws Exception {
        JITRetrotranslator jit = new JITRetrotranslator();
        boolean jar = false;
        int i;
        for (i = 0; i < args.length; i++) {
            String option = args[i];
            if (option.equals("-advanced")) {
                jit.setAdvanced(true);
            } else if (option.equals("-backport") && i + 1 < args.length) {
                jit.setBackport(args[++i]);
            } else if (option.equals("-jar")) {
                jar = true;
                i++;
                break;
            } else {
                break;
            }
        }
        if (i == args.length) {
            printUsageAndExit();
        }
        if (!jit.run()) {
            System.out.println("Cannot install JIT Retrotranslator.");
        }
        if (jar) {
            File file = new File(args[i]);
            if (!file.isFile()) printErrorAndExit("Unable to access jarfile " + file);
            JarClassLoader classLoader = new JarClassLoader(file, getClassLoader());
            String mainClass = classLoader.getMainClass();
            if (mainClass == null) printErrorAndExit("Failed to load Main-Class manifest attribute from " + file);
            Thread.currentThread().setContextClassLoader(classLoader);
            execute(classLoader, mainClass, remove(args, i + 1));
        } else {
            execute(getClassLoader(), args[i], remove(args, i + 1));
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
        builder.append("<classpath> net.sf.retrotranslator.transformer.JITRetrotranslator" +
                " [-advanced] [-backport <packages>] <class> [<args...>]\n");
        builder.append("   or  java -cp retrotranslator-transformer").append(suffix);
        builder.append(".jar net.sf.retrotranslator.transformer.JITRetrotranslator" +
                " [-advanced] [-backport <packages>] -jar <jarfile> [<args...>]");
        System.out.println(builder);
        System.exit(1);
    }

    private static void printErrorAndExit(String msg) {
        System.out.println(msg);
        System.exit(1);
    }
}
