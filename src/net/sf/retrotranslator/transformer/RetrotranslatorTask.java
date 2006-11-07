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
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

import java.io.File;

/**
 * @author Taras Puchko
 */
public class RetrotranslatorTask extends Task implements MessageLogger {

    private Path src;
    private File destdir;
    private File destjar;
    private boolean verbose;
    private boolean stripsign;
    private boolean retainapi;
    private boolean retainflags;
    private boolean lazy;
    private boolean advanced;
    private boolean verify;
    private boolean failonwarning = true;
    private Path classpath;
    private String srcmask;
    private String embed;
    private String backport;

    public RetrotranslatorTask() {
    }

    public void setSrcdir(Path srcdir) {
        getSrc().append(srcdir);
    }

    public void setSrcjar(Path srcjar) {
        getSrc().append(srcjar);
    }

    public Path createSrc() {
        return getSrc().createPath();
    }

    private Path getSrc() {
        return src != null ? src : (src = new Path(getProject()));
    }

    public void setDestdir(File destdir) {
        this.destdir = destdir;
    }

    public void setDestjar(File destjar) {
        this.destjar = destjar;
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

    public void setVerify(boolean verify) {
        this.verify = verify;
    }

    public void setLazy(boolean lazy) {
        this.lazy = lazy;
    }

    public void setAdvanced(boolean advanced) {
        this.advanced = advanced;
    }

    public void setFailonwarning(boolean failonwarning) {
        this.failonwarning = failonwarning;
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

    public void setSrcmask(String srcmask) {
        this.srcmask = srcmask;
    }

    public void setEmbed(String embed) {
        this.embed = embed;
    }

    public void setBackport(String backport) {
        this.backport = backport;
    }

    public void log(Message message) {
        boolean info = message.getLevel().compareTo(Level.INFO) >= 0;
        log(message.toString(), info ? Project.MSG_INFO : Project.MSG_WARN);
    }

    public void execute() throws BuildException {
        Retrotranslator retrotranslator = new Retrotranslator();
        for (String name : getSrc().list()) {
            File file = getProject().resolveFile(name);
            if (file.isFile()) {
                retrotranslator.addSrcjar(file);
            } else if (file.exists()) {
                retrotranslator.addSrcdir(file);
            } else {
                throw new BuildException("Path not found: " + file);
            }
        }
        if (destdir != null && destjar != null) {
            throw new BuildException("Cannot set both destdir and destjar!");
        }
        if (destdir != null) retrotranslator.setDestdir(destdir);
        if (destjar != null) retrotranslator.setDestjar(destjar);
        retrotranslator.setVerbose(verbose);
        retrotranslator.setStripsign(stripsign);
        retrotranslator.setRetainapi(retainapi);
        retrotranslator.setRetainflags(retainflags);
        retrotranslator.setLazy(lazy);
        retrotranslator.setAdvanced(advanced);
        retrotranslator.setVerify(verify);
        retrotranslator.setSrcmask(srcmask);
        retrotranslator.setEmbed(embed);
        retrotranslator.setBackport(backport);
        for (String fileName : getClasspath().list()) {
            retrotranslator.addClasspathElement(getProject().resolveFile(fileName));
        }
        retrotranslator.setLogger(this);
        boolean verified = retrotranslator.run();
        if (!verified && failonwarning) throw new BuildException("Verification failed.", getLocation());
    }
}
