/***
 * Retrotranslator: a Java bytecode transformer that translates Java classes
 * compiled with JDK 5.0 into classes that can be run on JVM 1.4.
 * 
 * Copyright (c) 2005 - 2007 Taras Puchko
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
import java.util.*;
import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;

/**
 * @author Taras Puchko
 */
public class RetrotranslatorTask extends Task {

    private File srcdir;
    private File srcjar;
    private File destdir;
    private File destjar;
    private List<FileSet> fileSets = new ArrayList<FileSet>();
    private List<FileSet> jarFileSets = new ArrayList<FileSet>();
    private List<DirSet> dirSets = new ArrayList<DirSet>();
    private boolean verbose;
    private boolean stripsign;
    private boolean retainapi;
    private boolean retainflags;
    private boolean lazy;
    private boolean advanced;
    private boolean verify;
    private boolean uptodatecheck;
    private boolean failonwarning = true;
    private String srcmask;
    private String embed;
    private String support;
    private String backport;
    private String target;
    private Path classpath;

    public RetrotranslatorTask() {
    }

    public void setSrcdir(File srcdir) {
        this.srcdir = srcdir;
    }

    public void setSrcjar(File srcjar) {
        this.srcjar = srcjar;
    }

    public void setDestdir(File destdir) {
        this.destdir = destdir;
    }

    public void setDestjar(File destjar) {
        this.destjar = destjar;
    }

    public void addConfiguredFileset(FileSet fileSet) {
        fileSets.add(fileSet);
    }

    public void addConfiguredJarfileset(FileSet fileSet) {
        jarFileSets.add(fileSet);
    }

    public void addConfiguredDirset(DirSet dirSet) {
        dirSets.add(dirSet);
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void setStripsign(boolean stripsign) {
        this.stripsign = stripsign;
    }

    public void setRetainapi(boolean retainapi) {
        this.retainapi = retainapi;
    }

    public void setRetainflags(boolean retainflags) {
        this.retainflags = retainflags;
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

    public void setUptodatecheck(boolean uptodatecheck) {
        this.uptodatecheck = uptodatecheck;
    }

    public void setFailonwarning(boolean failonwarning) {
        this.failonwarning = failonwarning;
    }

    public void setSrcmask(String srcmask) {
        this.srcmask = srcmask;
    }

    public void setEmbed(String embed) {
        this.embed = embed;
    }

    public void setSupport(String support) {
        this.support = support;
    }

    public void setBackport(String backport) {
        this.backport = backport;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setClasspathref(Reference classpathref) {
        createClasspath().setRefid(classpathref);
    }

    public void setClasspath(Path classpath) {
        this.getClasspath().append(classpath);
    }

    public Path createClasspath() {
        return getClasspath().createPath();
    }

    private Path getClasspath() {
        return classpath != null ? classpath : (classpath = new Path(getProject()));
    }

    public void execute() throws BuildException {
        Retrotranslator retrotranslator = new Retrotranslator();
        if (srcdir != null) retrotranslator.addSrcdir(srcdir);
        if (srcjar != null) retrotranslator.addSrcjar(srcjar);
        if (destdir != null) retrotranslator.setDestdir(destdir);
        if (destjar != null) retrotranslator.setDestjar(destjar);
        for (FileSet fileSet : fileSets) {
            DirectoryScanner scanner = fileSet.getDirectoryScanner(getProject());
            retrotranslator.addSourceFiles(scanner.getBasedir(), Arrays.asList(scanner.getIncludedFiles()));
        }
        for (FileSet jarFileSet : jarFileSets) {
            DirectoryScanner scanner = jarFileSet.getDirectoryScanner(getProject());
            File basedir = scanner.getBasedir();
            for (String jarFile : scanner.getIncludedFiles()) {
                retrotranslator.addSrcjar(new File(basedir, jarFile));
            }
        }
        for (DirSet dirSet : dirSets) {
            DirectoryScanner scanner = dirSet.getDirectoryScanner(getProject());
            File basedir = scanner.getBasedir();
            for (String subdirectory : scanner.getIncludedDirectories()) {
                retrotranslator.addSrcdir(new File(basedir, subdirectory));
            }
        }
        retrotranslator.setVerbose(verbose);
        retrotranslator.setStripsign(stripsign);
        retrotranslator.setRetainapi(retainapi);
        retrotranslator.setRetainflags(retainflags);
        retrotranslator.setLazy(lazy);
        retrotranslator.setAdvanced(advanced);
        retrotranslator.setVerify(verify);
        retrotranslator.setUptodatecheck(uptodatecheck);
        retrotranslator.setSrcmask(srcmask);
        retrotranslator.setEmbed(embed);
        retrotranslator.setSupport(support);
        retrotranslator.setBackport(backport);
        if (target != null) retrotranslator.setTarget(target);
        for (String fileName : getClasspath().list()) {
            retrotranslator.addClasspathElement(getProject().resolveFile(fileName));
        }
        retrotranslator.setLogger(new AbstractLogger() {
            protected void log(String text, Level level) {
                RetrotranslatorTask.this.log(text,
                        level.isCritical() ? Project.MSG_WARN : Project.MSG_INFO);
            }
        });
        boolean success = retrotranslator.run();
        if (!success && failonwarning) {
            throw new BuildException("Translation failed.", getLocation());
        }
    }

}
