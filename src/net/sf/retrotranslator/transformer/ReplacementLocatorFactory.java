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

import java.io.*;
import java.lang.ref.SoftReference;
import java.util.*;
import java.net.URL;

/**
 * @author Taras Puchko
 */
class ReplacementLocatorFactory {

    private final ClassVersion target;
    private final OperationMode mode;
    private final boolean retainapi;
    private List<Backport> backports;
    private final ClassReaderFactory classReaderFactory;

    private SoftReference<ReplacementLocator> softReference = new SoftReference<ReplacementLocator>(null);

    public ReplacementLocatorFactory(ClassVersion target, OperationMode mode, boolean retainapi,
                                     List<Backport> backports, ClassReaderFactory classReaderFactory) {
        this.target = target;
        this.mode = mode;
        this.retainapi = retainapi;
        this.backports = backports;
        this.classReaderFactory = classReaderFactory;
        if (retainapi) {
            return;
        }
        String fileName = getClass().getPackage().getName().replace('.', '/') +
                "/backport" + target.getName().replace(".", "") + ".properties";
        try {
            Enumeration<URL> resources = TransformerTools.getDefaultClassLoader().getResources(fileName);
            while (resources.hasMoreElements()) {
                backports.addAll(Backport.loadFromURL(resources.nextElement()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ClassVersion getTarget() {
        return target;
    }

    public OperationMode getMode() {
        return mode;
    }

    public boolean isRetainapi() {
        return retainapi;
    }

    public ClassReaderFactory getClassReaderFactory() {
        return classReaderFactory;
    }

    public synchronized ReplacementLocator getLocator() {
        ReplacementLocator locator = softReference.get();
        if (locator == null) {
            locator = new ReplacementLocator(mode, backports, classReaderFactory);
            softReference = new SoftReference<ReplacementLocator>(locator);
        }
        return locator;
    }
}
