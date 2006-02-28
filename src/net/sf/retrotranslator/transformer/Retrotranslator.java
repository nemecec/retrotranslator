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

import net.sf.retrotranslator.runtime.asm.ClassReader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author Taras Puchko
 */
public class Retrotranslator implements MessageLogger {

    private List<ClassFileSet> src = new ArrayList<ClassFileSet>();
    private File destdir;
    private boolean stripsign;
    private boolean verbose;
    private boolean lazy;
    private boolean verify;
    private List<File> classpath = new ArrayList<File>();
    private MessageLogger logger = this;

    public Retrotranslator() {
    }

    /**
     * @deprecated Provided for backwards compatibility. Use {@link #Retrotranslator()} instead
     */
    public Retrotranslator(File srcdir, File destdir, String classpath, boolean verify, boolean stripsign, boolean verbose) {
        addSrcdir(srcdir);
        setDestdir(destdir);
        addClasspath(classpath);
        setVerify(verify);
        setStripsign(stripsign);
        setVerbose(verbose);
    }

    public void addSrcdir(File srcdir) {
        if (!srcdir.isDirectory()) throw new IllegalArgumentException("Invalid srcdir: " + srcdir);
        this.src.add(new ClassFileSet(srcdir));
    }

    public void addSourceFiles(File srcdir, List<String> fileNames) {
        if (!srcdir.isDirectory()) throw new IllegalArgumentException("Invalid srcdir: " + srcdir);
        this.src.add(new ClassFileSet(srcdir, fileNames));
    }

    public void setDestdir(File destdir) {
        if (!destdir.isDirectory()) throw new IllegalArgumentException("Invalid destdir: " + destdir);
        this.destdir = destdir;
    }

    public void setStripsign(boolean stripsign) {
        this.stripsign = stripsign;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void setLazy(boolean lazy) {
        this.lazy = lazy;
    }

    public void setVerify(boolean verify) {
        this.verify = verify;
    }

    public void addClasspathElement(File classpathElement) {
        this.classpath.add(classpathElement);
    }

    public void addClasspath(String classpath) {
        StringTokenizer tokenizer = new StringTokenizer(classpath, File.pathSeparator);
        while (tokenizer.hasMoreTokens()) {
            addClasspathElement(new File(tokenizer.nextToken()));
        }
    }

    public void setLogger(MessageLogger logger) {
        this.logger = logger;
    }

    public void log(Message message) {
        System.out.println(message);
    }

    public boolean run() {
        if (src.isEmpty()) throw new IllegalArgumentException("Source directory is not set.");
        ClassTransformer transformer = new ClassTransformer(lazy, stripsign);
        for (ClassFileSet fileSet : src) {
            transform(transformer, fileSet);
        }

        if (!verify) return true;
        ClassReaderFactory factory = new ClassReaderFactory(classpath.isEmpty());
        try {
            for (File file : classpath) {
                factory.appendPath(file);
            }
            if (destdir != null) {
                factory.appendPath(destdir);
            } else {
                for (ClassFileSet fileSet : src) {
                    factory.appendPath(fileSet.getBaseDir());
                }
            }
            boolean verified = true;
            for (ClassFileSet fileSet : src) {
                verified &= verify(factory, fileSet);
            }
            return verified;
        } finally {
            factory.close();
        }
    }

    private void transform(ClassTransformer transformer, ClassFileSet fileSet) {
        File src = fileSet.getBaseDir();
        File dest = destdir != null ? destdir : src;
        List<String> fileNames = fileSet.getFileNames();
        String location = dest.equals(src) ? " in " + src + "." : " from " + src + " to " + dest + ".";
        logger.log(new Message(Level.INFO, "Transforming " + fileNames.size() + " file(s)" + location));
        for (int i = 0; i < fileNames.size(); i++) {
            String fileName = fileNames.get(i);
            if (verbose) logger.log(new Message(Level.VERBOSE, "Transformation...", src, fileName));
            byte[] sourceData = TransformerTools.readFileToByteArray(new File(src, fileName));
            byte[] resultData = transformer.transform(sourceData, 0, sourceData.length);
            if (src != dest || sourceData != resultData) {
                String fixedName = ClassSubstitutionVisitor.fixIdentifier(fileName);
                fileNames.set(i, fixedName);
                if (src == dest && !fixedName.equals(fileName)) new File(dest, fileName).delete();
                TransformerTools.writeByteArrayToFile(new File(dest, fixedName), resultData);
            }
        }
        logger.log(new Message(Level.INFO, "Transformation of " + fileNames.size() + " file(s) completed successfully."));
    }

    private boolean verify(ClassReaderFactory factory, ClassFileSet fileSet) {
        final File dir = destdir != null ? destdir : fileSet.getBaseDir();
        List<String> fileNames = fileSet.getFileNames();
        logger.log(new Message(Level.INFO, "Verifying " + fileNames.size() + " file(s) in " + dir + "."));
        final int[] warningCount = new int[1];
        for (final String fileName : fileNames) {
            if (verbose) logger.log(new Message(Level.VERBOSE, "Verification...", dir, fileName));
            byte[] data = TransformerTools.readFileToByteArray(new File(dir, fileName));
            ReferenceVerifyingVisitor visitor = new ReferenceVerifyingVisitor(factory) {
                protected void warning(String text) {
                    warningCount[0]++;
                    logger.log(new Message(Level.WARNING, text, dir, fileName));
                }
            };
            new ClassReader(data).accept(visitor, true);
        }
        String result = warningCount[0] != 0 ? " with " + warningCount[0] + " warning(s)." : " successfully.";
        logger.log(new Message(Level.INFO, "Verification of " + fileNames.size() + " file(s) completed" + result));
        return warningCount[0] == 0;
    }

    private boolean execute(String[] args) {
        int i = 0;
        while (i < args.length) {
            String string = args[i++];
            if (string.equals("-srcdir") && i < args.length) {
                addSrcdir(new File(args[i++]));
            } else if (string.equals("-destdir") && i < args.length) {
                setDestdir(new File(args[i++]));
            } else if (string.equals("-stripsign")) {
                setStripsign(true);
            } else if (string.equals("-verbose")) {
                setVerbose(true);
            } else if (string.equals("-lazy")) {
                setLazy(true);
            } else if (string.equals("-verify")) {
                setVerify(true);
            } else if (string.equals("-classpath") && i < args.length) {
                addClasspath(args[i++]);
            } else {
                throw new IllegalArgumentException("Unknown option: " + string);
            }
        }
        return run();
    }

    private static void printUsage() {
        String version = Retrotranslator.class.getPackage().getImplementationVersion();
        String suffix = (version == null) ? "" : "-" + version;
        System.out.println("Usage: java -jar retrotranslator-transformer" + suffix + ".jar" +
                " -srcdir <path> [-destdir <path>] [-stripsign] [-verbose] [-lazy] [-verify] [-classpath <classpath>]");
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }
        try {
            if (!new Retrotranslator().execute(args)) System.exit(2);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            printUsage();
            System.exit(1);
        }
    }
}
