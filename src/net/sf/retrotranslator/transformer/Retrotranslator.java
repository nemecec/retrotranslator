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
import net.sf.retrotranslator.runtime.impl.BytecodeTransformer;

import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Taras Puchko
 */
public class Retrotranslator implements MessageLogger {

    private LinkedList<FileContainer> src = new LinkedList<FileContainer>();
    private FileContainer dest;
    private boolean stripsign;
    private boolean verbose;
    private boolean lazy;
    private boolean advanced;
    private boolean verify;
    private List<File> classpath = new ArrayList<File>();
    private MessageLogger logger = this;
    private Pattern srcmaskPattern;
    private String embed;
    private ClassLoader classLoader;

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

    public void setAdvanced(boolean advanced) {
        this.advanced = advanced;
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

    public void setSrcmask(String srcmask) {
        if (srcmask == null) {
            srcmaskPattern = null;
            return;
        }
        StringBuilder builder = new StringBuilder();
        for (String s : srcmask.split(";")) {
            if (builder.length() > 0) builder.append('|');
            builder.append("((./)?");
            StringTokenizer tokenizer = new StringTokenizer(s, "*?", true);
            while (tokenizer.hasMoreTokens()) {
                builder.append(wildcardToRegex(tokenizer.nextToken()));
            }
            builder.append(")");
        }
        srcmaskPattern = Pattern.compile(builder.toString());
    }

    private static String wildcardToRegex(String s) {
        if (s.equals("*")) return ".*";
        if (s.equals("?")) return ".";
        return Pattern.quote(s);
    }

    public void setEmbed(String embed) {
        this.embed = embed;
    }

    public void setLogger(MessageLogger logger) {
        this.logger = logger;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void log(Message message) {
        System.out.println(message);
    }

    public boolean run() {
        if (src.isEmpty()) throw new IllegalArgumentException("Source not set.");
        String backportPrefix = null;
        if (embed != null) {
            if (dest == null) throw new IllegalArgumentException("Destination not set.");
            backportPrefix = embed.replace('.', '/') + '/';
            prependLocationToSource(Queue.class);
            prependLocationToSource(BytecodeTransformer.class);
        }
        ClassTransformer classTransformer = new ClassTransformer(lazy, stripsign, advanced, backportPrefix);
        for (FileContainer container : src) {
            transform(classTransformer, backportPrefix, container, dest != null ? dest : container);
        }
        if (dest != null) dest.flush();
        if (!verify) return true;
        ClassLoader loader = classLoader;
        if (loader == null && classpath.isEmpty()) {
            loader = this.getClass().getClassLoader();
        }
        ClassReaderFactory factory = new ClassReaderFactory(loader);
        try {
            return verify(factory);
        } finally {
            factory.close();
        }
    }

    private void transform(ClassTransformer transformer, String backportPrefix, FileContainer source, FileContainer destination) {
        logger.log(new Message(Level.INFO, "Transforming " + source.getFileCount() + " file(s)" +
                (source == destination ? " in " + source : " from " + source + " to " + destination) + "."));
        for (FileEntry entry : source.getEntries()) {
            String name = entry.getName();
            if (backportPrefix != null && name.equals("net/sf/retrotranslator/runtime/impl/signatures.properties")) {
                destination.putEntry(backportPrefix + name, transformSignatures(entry.getContent(), backportPrefix));
            } else if (isTransformable(name)) {
                if (verbose) logger.log(new Message(Level.VERBOSE, "Transformation", source.getLocation(), name));
                byte[] sourceData = entry.getContent();
                byte[] resultData = isClassFile(sourceData)
                        ? transformer.transform(sourceData, 0, sourceData.length)
                        : TextFileTransformer.transform(sourceData, backportPrefix);
                String fixedName = BackportFactory.prefixBackportName(name, backportPrefix);
                if (source != destination || sourceData != resultData || !fixedName.equals(name)) {
                    if (!fixedName.equals(name)) destination.removeEntry(name);
                    destination.putEntry(fixedName, resultData);
                }
            } else if (source != destination) {
                destination.putEntry(entry.getName(), entry.getContent());
            }
        }
        source.flush();
        logger.log(new Message(Level.INFO, "Transformation of " + source.getFileCount() + " file(s) completed successfully."));
    }

    private boolean isTransformable(String name) {
        return srcmaskPattern != null ? srcmaskPattern.matcher(name).matches() : name.endsWith(".class");
    }

    private byte[] transformSignatures(byte[] content, final String backportPrefix) {
        try {
            DescriptorTransformer transformer = new DescriptorTransformer() {
                protected String transformInternalName(String internalName) {
                    return BackportFactory.prefixBackportName(internalName, backportPrefix);
                }
            };
            Properties source = new Properties();
            source.load(new ByteArrayInputStream(content));
            Properties target = new Properties();
            for (Map.Entry entry : source.entrySet()) {
                String key = BackportFactory.prefixBackportName((String) entry.getKey(), backportPrefix);
                String value = transformer.transformDescriptor((String) entry.getValue());
                target.put(key, value);
            }
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            target.store(stream, null);
            return stream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isClassFile(byte[] bytes) {
        return bytes.length >= 4 &&
                bytes[0] == ((byte) 0xCA) &&
                bytes[1] == ((byte) 0xFE) &&
                bytes[2] == ((byte) 0xBA) &&
                bytes[3] == ((byte) 0xBE);
    }

    private boolean verify(ClassReaderFactory factory) {
        if (dest != null) {
            factory.appendPath(dest.getLocation());
        } else {
            for (FileContainer container : src) {
                factory.appendPath(container.getLocation());
            }
        }
        for (File file : classpath) {
            factory.appendPath(file);
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
        logger.log(new Message(Level.INFO, "Verifying " + container.getFileCount() + " file(s) in " + container + "."));
        int warningCount = 0;
        for (final FileEntry entry : container.getEntries()) {
            if (isTransformable(entry.getName())) {
                byte[] content = entry.getContent();
                if (isClassFile(content)) {
                    if (verbose) logger.log(new Message(Level.VERBOSE,
                            "Verification", container.getLocation(), entry.getName()));
                    warningCount += new ReferenceVerifyingVisitor(factory, logger,
                            container.getLocation(), entry.getName()).verify(content);
                }
            }
        }
        String result = warningCount != 0 ? " with " + warningCount + " warning(s)." : " successfully.";
        logger.log(new Message(Level.INFO, "Verification of " + container.getFileCount() + " file(s) completed" + result));
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
            } else if (string.equals("-advanced")) {
                setAdvanced(true);
            } else if (string.equals("-verify")) {
                setVerify(true);
            } else if (string.equals("-classpath") && i < args.length) {
                addClasspath(args[i++]);
            } else if (string.equals("-srcmask") && i < args.length) {
                setSrcmask(args[i++]);
            } else if (string.equals("-embed") && i < args.length) {
                setEmbed(args[i++]);
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
                " [-srcdir <path> | -srcjar <file>] [-destdir <path> | -destjar <file>] [-stripsign] [-verbose]" +
                " [-lazy] [-advanced] [-verify] [-classpath <classpath>] [-srcmask <mask>] [-embed <package>]");
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

    private void prependLocationToSource(Class aClass) {
        String path = "/" + aClass.getName().replace('.', '/') + ".class";
        URL resource = aClass.getResource(path);
        if (resource == null) {
            throw new IllegalArgumentException("Location not found: " + aClass);
        }
        String url = resource.toExternalForm();
        String prefix = "jar:file:/";
        String suffix = "!" + path;
        if (!url.startsWith(prefix) || !url.endsWith(suffix)) {
            throw new IllegalArgumentException("Not in a jar file: " + aClass);
        }
        File file = new File(url.substring(prefix.length(), url.length() - suffix.length()));
        if (!file.isFile()) {
            throw new IllegalArgumentException("File not found: " + file);
        }
        src.addFirst(new JarFileContainer(file));
    }
}
