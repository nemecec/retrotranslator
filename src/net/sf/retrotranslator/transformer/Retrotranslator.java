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
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author Taras Puchko
 */
public class Retrotranslator implements MessageLogger {

    private List<FileContainer> src = new ArrayList<FileContainer>();
    private FileContainer dest;
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
        src.add(new FolderFileContainer(srcdir));
    }

    public void addSrcjar(File srcjar) {
        if (!srcjar.isFile()) throw new IllegalArgumentException("Invalid srcjar: " + srcjar);
        src.add(new JarFileContainer(srcjar));
    }

    public void addSourceFiles(File srcdir, List<String> fileNames) {
        if (!srcdir.isDirectory()) throw new IllegalArgumentException("Invalid srcdir: " + srcdir);
        src.add(new FolderFileContainer(srcdir, fileNames));
    }

    public void setDestdir(File destdir) {
        if (!destdir.isDirectory()) throw new IllegalArgumentException("Invalid destdir: " + destdir);
        dest = new FolderFileContainer(destdir);
    }

    public void setDestjar(File destjar) {
        if (destjar.isDirectory()) throw new IllegalArgumentException("Invalid destjar: " + destjar);
        dest = new JarFileContainer(destjar);
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
        if (src.isEmpty()) throw new IllegalArgumentException("Source not set.");
        ClassTransformer transformer = new ClassTransformer(lazy, stripsign);
        for (FileContainer container : src) {
            transform(transformer, container, dest != null ? dest : container);
        }
        if (dest != null) dest.flush();
        if (!verify) return true;
        ClassReaderFactory factory = new ClassReaderFactory(classpath.isEmpty());
        try {
            return verify(factory);
        } finally {
            factory.close();
        }
    }

    private void transform(ClassTransformer transformer, FileContainer source, FileContainer destination) {
        int classFileCount = source.getClassFileCount();
        logger.log(new Message(Level.INFO, "Transforming " + classFileCount + " file(s)" +
                (source == destination ? " in " + source : " from " + source + " to " + destination) + "."));
        for (FileEntry entry : source.getEntries()) {
            if (entry.isClassFile()) {
                String name = entry.getName();
                if (verbose) logger.log(new Message(Level.VERBOSE, "Transformation", source.getLocation(), name));
                byte[] sourceData = entry.getContent();
                byte[] resultData = transformer.transform(sourceData, 0, sourceData.length);
                if (source != destination || sourceData != resultData) {
                    String fixedName = TransformerTools.fixIdentifier(name);
                    if (!fixedName.equals(name)) destination.removeEntry(name);
                    destination.putEntry(fixedName, resultData);
                }
            } else if (source != destination) {
                destination.putEntry(entry.getName(), entry.getContent());
            }
        }
        source.flush();
        logger.log(new Message(Level.INFO, "Transformation of " + classFileCount + " file(s) completed successfully."));
    }

    private boolean verify(ClassReaderFactory factory) {
        for (File file : classpath) {
            factory.appendPath(file);
        }
        if (dest != null) {
            factory.appendPath(dest.getLocation());
        } else {
            for (FileContainer container : src) {
                factory.appendPath(container.getLocation());
            }
        }
        if (dest != null) {
            return verify(factory, dest);
        }
        boolean verified = true;
        for (FileContainer container : src) {
            verified &= verify(factory, container);
        }
        return verified;
    }

    private boolean verify(ClassReaderFactory factory, final FileContainer container) {
        int classFileCount = container.getClassFileCount();
        logger.log(new Message(Level.INFO, "Verifying " + classFileCount + " file(s) in " + container + "."));
        int warningCount = 0;
        for (final FileEntry entry : container.getEntries()) {
            if (entry.isClassFile()) {
                if (verbose) logger.log(new Message(Level.VERBOSE,
                        "Verification", container.getLocation(), entry.getName()));
                warningCount += new ReferenceVerifyingVisitor(factory, logger,
                        container.getLocation(), entry.getName()).verify(entry.getContent());
            }
        }
        String result = warningCount != 0 ? " with " + warningCount + " warning(s)." : " successfully.";
        logger.log(new Message(Level.INFO, "Verification of " + classFileCount + " file(s) completed" + result));
        return warningCount == 0;
    }

    private boolean execute(String[] args) {
        int i = 0;
        while (i < args.length) {
            String string = args[i++];
            if (string.equals("-srcdir") && i < args.length) {
                addSrcdir(new File(args[i++]));
            } else if (string.equals("-srcjar") && i < args.length) {
                addSrcjar(new File(args[i++]));
            } else if (string.equals("-destdir") && i < args.length) {
                setDestdir(new File(args[i++]));
            } else if (string.equals("-destjar") && i < args.length) {
                setDestjar(new File(args[i++]));
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
                " [-srcdir <path> | -srcjar <file>] [-destdir <path> | -destjar <file>]" +
                " [-stripsign] [-verbose] [-lazy] [-verify] [-classpath <classpath>]");
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
