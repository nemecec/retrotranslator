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

import java.io.File;

/**
 * @author Taras Puchko
 */
public class JITRetrotranslator extends sun.misc.ClassFileTransformer {

    private static boolean installed;
    private ClassTransformer transformer = new ClassTransformer(true);

    private JITRetrotranslator() {
    }

    public byte[] transform(byte[] filecontent, int offset, int length) {
        return transformer.transform(filecontent, offset, length);
    }

    public static void install() {
        if (installed) return;
        add(new JITRetrotranslator());
        installed = true;
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
        System.out.println("Usage: java -cp retrotranslator-transformer.jar" + File.pathSeparator + "<classpath>" +
                " net.sf.retrotranslator.transformer.JITRetrotranslator <class> [<args...>]\n" +
                "   or  java -cp retrotranslator-transformer.jar" +
                " net.sf.retrotranslator.transformer.JITRetrotranslator -jar <jarfile> [<args...>]");
        System.exit(1);
    }

    private static void printErrorAndExit(String msg) {
        System.out.println(msg);
        System.exit(1);
    }
}
