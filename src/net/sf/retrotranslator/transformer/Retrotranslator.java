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

    private List<File> src = new ArrayList<File>();
    private File destdir;
    private boolean stripsign;
    private boolean verbose;
    private boolean lazy;
    private boolean verify;
    private List<File> classpath = new ArrayList<File>();
    private MessageLogger logger = this;

    public Retrotranslator() {
    }

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
        this.src.add(srcdir);
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

    public void verbose(String message) {
        if (verbose) System.out.println(message);
    }

    public void info(String message) {
        System.out.println(message);
    }

    public void warning(String message) {
        System.out.println(message);
    }

    public boolean run() {
        if (src.isEmpty()) throw new IllegalArgumentException("Source directory is not set.");
        ClassTransformer transformer = new ClassTransformer(lazy, stripsign);
        List<FolderScanner> scanners = new ArrayList<FolderScanner>();
        for (File srcdir : src) {
            FolderScanner scanner = new FolderScanner(srcdir);
            scanners.add(scanner);
            transform(transformer, scanner);
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
                for (FolderScanner scanner : scanners) {
                    factory.appendPath(scanner.getBaseDir());
                }
            }
            boolean verified = true;
            for (FolderScanner scanner : scanners) {
                verified &= verify(factory, scanner);
            }
            return verified;
        } finally {
            factory.close();
        }
    }

    private void transform(ClassTransformer transformer, FolderScanner scanner) {
        File src = scanner.getBaseDir();
        File dest = destdir != null ? destdir : src;
        List<String> fileNames = scanner.getFileNames();
        logger.info("Transforming " + fileNames.size() + " file(s)" +
                (dest.equals(src) ? " in " + src + "." : " from " + src + " to " + dest + "."));
        for (int i = 0; i < fileNames.size(); i++) {
            String fileName = fileNames.get(i);
            logger.verbose(fileName);
            byte[] sourceData = TransformerTools.readFileToByteArray(new File(src, fileName));
            byte[] resultData = transformer.transform(sourceData, 0, sourceData.length);
            if (src != dest || sourceData != resultData) {
                String fixedName = ClassSubstitutionVisitor.fixIdentifier(fileName);
                fileNames.set(i, fixedName);
                if (src == dest && !fixedName.equals(fileName)) new File(dest, fileName).delete();
                TransformerTools.writeByteArrayToFile(new File(dest, fixedName), resultData);
            }
        }
        logger.info("Transformation of " + fileNames.size() + " file(s) completed successfully.");
    }

    private static boolean isByteCode15(byte[] sourceData) {
        return sourceData[4] == 0 && sourceData[5] == 0 && sourceData[6] == 0 && sourceData[7] == 49;
    }

    private boolean verify(ClassReaderFactory factory, FolderScanner scanner) {
        File dir = destdir != null ? destdir : scanner.getBaseDir();
        List<String> fileNames = scanner.getFileNames();
        logger.info("Verifying " + fileNames.size() + " file(s) in " + dir + ".");
        ReferenceVerifyingVisitor visitor = new ReferenceVerifyingVisitor(factory, logger);
        for (String fileName : fileNames) {
            logger.verbose(fileName);
            byte[] data = TransformerTools.readFileToByteArray(new File(dir, fileName));
            new ClassReader(data).accept(visitor, true);
        }
        int warningCount = visitor.getWarningCount();
        logger.info("Verification of " + fileNames.size() + " file(s) completed" +
                (warningCount != 0 ? " with " + warningCount + " warning(s)." : " successfully."));
        return warningCount == 0;
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
