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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Taras Puchko
 */
public class RetrotranslatorTask extends MatchingTask implements MessageLogger {

    private Path src;
    private File destdir;
    private boolean verbose;
    private boolean stripsign;
    private boolean verify;
    private boolean failonwarning = true;
    private Path classpath;

    public RetrotranslatorTask() {
        setIncludes("**/*.class");
    }

    public void setSrcdir(Path srcdir) {
        if (src == null) {
            src = srcdir;
        } else {
            src.append(srcdir);
        }
    }

    public Path createSrc() {
        if (src == null) src = new Path(getProject());
        return src.createPath();
    }

    public void setDestdir(File destdir) {
        this.destdir = destdir;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void setStripsign(boolean stripsign) {
        this.stripsign = stripsign;
    }

    public void setVerify(boolean verify) {
        this.verify = verify;
    }

    public void setFailonwarning(boolean failonwarning) {
        this.failonwarning = failonwarning;
    }

    public void setClasspathref(Reference classpathref) {
        createClasspath().setRefid(classpathref);
    }

    public void setClasspath(Path classpath) {
        if (this.classpath == null) {
            this.classpath = classpath;
        } else {
            this.classpath.append(classpath);
        }
    }

    public Path createClasspath() {
        if (classpath == null) classpath = new Path(getProject());
        return classpath.createPath();
    }

    public void execute() throws BuildException {
        if (destdir != null) {
            if (!destdir.exists()) throw new BuildException(destdir + " not found.");
            if (!destdir.isDirectory()) throw new BuildException(destdir + " is not a directory.");
        }
        List<FileCollection> fileCollections = getFileCollection();
        ClassTransformer transformer = new ClassTransformer(stripsign);
        for (FileCollection fileCollection : fileCollections) {
            transformer.transform(fileCollection.dir, getDestDir(fileCollection), fileCollection.fileNames, this);
        }
        if (verify) {
            verifyAll(fileCollections);
        }
    }

    private void verifyAll(List<FileCollection> fileCollections) {
        ClassReaderFactory factory = new ClassReaderFactory(classpath == null);
        try {
            if (classpath != null) {
                for (String fileName : classpath.list()) {
                    factory.appendPath(getProject().resolveFile(fileName));
                }
            }
            new Path(getProject()).concatSystemClasspath();

            if (destdir != null) {
                factory.appendPath(destdir);
            } else {
                for (FileCollection fileCollection : fileCollections) {
                    factory.appendPath(fileCollection.dir);
                }
            }
            ClassVerifier verifier = new ClassVerifier(factory);
            boolean verified = true;
            for (FileCollection fileCollection : fileCollections) {
                verified &= verifier.verify(getDestDir(fileCollection), fileCollection.fileNames, this);
            }
            if (!verified && failonwarning) throw new BuildException("Verification failed.", getLocation());
        } finally {
            factory.close();
        }
    }

    private File getDestDir(FileCollection fileCollection) {
        return destdir != null ? destdir : fileCollection.dir;
    }

    private List<FileCollection> getFileCollection() {
        List<FileCollection> result = new ArrayList<FileCollection>();
        if (src != null) {
            for (String srcDir : src.list()) {
                File dir = getProject().resolveFile(srcDir);
                String[] files = getDirectoryScanner(dir).getIncludedFiles();
                result.add(new FileCollection(dir, Arrays.asList(files)));
            }
        }
        if (result.isEmpty()) {
            throw new BuildException("Source directory is not set.");
        }
        return result;
    }

    public void verbose(String message) {
        if (verbose) log(message, Project.MSG_INFO);
    }

    public void info(String message) {
        log(message, Project.MSG_INFO);
    }

    public void warning(String message) {
        log(message, Project.MSG_WARN);
    }

    private static class FileCollection {
        public final File dir;
        public final List<String> fileNames;

        public FileCollection(File dir, List<String> fileNames) {
            this.dir = dir;
            this.fileNames = fileNames;
        }
    }
}
