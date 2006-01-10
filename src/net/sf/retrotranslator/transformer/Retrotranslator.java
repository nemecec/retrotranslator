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
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author Taras Puchko
 */
public class Retrotranslator implements MessageLogger {

    private File srcdir;
    private File destdir;
    private String classpath;
    private boolean verify;
    private boolean stripsign;
    private boolean verbose;

    public Retrotranslator(File srcdir, File destdir, String classpath, boolean verify, boolean stripsign, boolean verbose) {
        this.srcdir = srcdir;
        this.destdir = destdir;
        this.classpath = classpath;
        this.verify = verify;
        this.stripsign = stripsign;
        this.verbose = verbose;
    }

    public Retrotranslator(String[] args) throws IllegalArgumentException {
        int i = 0;
        while (i < args.length) {
            String string = args[i++];
            if (string.equals("-srcdir") && i < args.length) {
                srcdir = checkDir(args[i++]);
            } else if (string.equals("-destdir") && i < args.length) {
                destdir = checkDir(args[i++]);
            } else if (string.equals("-stripsign")) {
                stripsign = true;
            } else if (string.equals("-verbose")) {
                verbose = true;
            } else if (string.equals("-verify")) {
                verify = true;
            } else if (string.equals("-classpath") && i < args.length) {
                classpath = args[i++];
            } else {
                throw new IllegalArgumentException("Unknown option: " + string);
            }
        }
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }
        try {
            Retrotranslator retrotranslator = new Retrotranslator(args);
            if (!retrotranslator.run()) System.exit(2);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            printUsage();
            System.exit(1);
        }
    }

    private static void printUsage() {
        String version = Retrotranslator.class.getPackage().getImplementationVersion();
        String suffix = (version == null) ? "" : "-" + version;
        System.out.println("Usage: java -jar retrotranslator-transformer" + suffix + ".jar" +
                " -srcdir <path> [-destdir <path>] [-verbose] [-verify] [-classpath <classpath>]");
    }

    public boolean run() {
        if (srcdir == null) throw new IllegalArgumentException("Source directory is not set.");
        if (destdir == null) destdir = srcdir;
        List<String> fileNames = new FolderScanner(srcdir).getFileNames();
        new ClassTransformer(stripsign).transform(srcdir, destdir, fileNames, this);
        if (!verify) return true;
        ClassReaderFactory factory = new ClassReaderFactory(classpath == null);
        try {
            if (classpath != null) {
                StringTokenizer tokenizer = new StringTokenizer(classpath, File.pathSeparator);
                while (tokenizer.hasMoreTokens()) {
                    factory.appendPath(new File(tokenizer.nextToken()));
                }
            }
            factory.appendPath(destdir);
            return new ClassVerifier(factory).verify(destdir, fileNames, this);
        } finally {
            factory.close();
        }
    }

    public void verbose(String message) {
        if (verbose) System.out.println(message);
    }

    public void info(String message) {
        System.out.println(message);
    }

    public void warning(String message) {
        System.out.println(message);
    }

    private static File checkDir(String dir) throws IllegalArgumentException {
        File file = new File(dir);
        if (!file.exists()) throw new IllegalArgumentException(dir + " not found.");
        if (!file.isDirectory()) throw new IllegalArgumentException(dir + " is not a directory.");
        return file;
    }
}
