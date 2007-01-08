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
import java.net.*;
import java.util.*;
import java.util.jar.*;

/**
 * @author Taras Puchko
 */
class JarClassLoader extends URLClassLoader {

    private File file;

    public JarClassLoader(File file, ClassLoader parent) throws IOException {
        super(getClassPath(file), parent);
        this.file = file;
    }

    public String getMainClass() throws IOException {
        return getAttribute(file, Attributes.Name.MAIN_CLASS);
    }

    private static URL[] getClassPath(File file) throws IOException {
        Set<URL> urls = new LinkedHashSet<URL>();
        addToPath(urls, file);
        return urls.toArray(new URL[urls.size()]);
    }

    private static void addToPath(Set<URL> classPath, File file) throws IOException {
        file = file.getCanonicalFile();
        if (!file.exists()) return;
        URL url = file.toURI().toURL();
        if (classPath.contains(url)) return;
        classPath.add(url);
        String attribute = getAttribute(file, Attributes.Name.CLASS_PATH);
        if (attribute != null) {
            StringTokenizer tokenizer = new StringTokenizer(attribute);
            while (tokenizer.hasMoreTokens()) {
                addToPath(classPath, new File(file.getParent(), tokenizer.nextToken()));
            }
        }
    }

    private static String getAttribute(File file, Attributes.Name name) {
        Attributes attributes = getAttributes(file);
        return attributes == null ? null : attributes.getValue(name);
    }

    private static Attributes getAttributes(File file) {
        if (!file.isFile()) return null;
        try {
            JarFile jarFile = new JarFile(file);
            try {
                return jarFile.getManifest().getMainAttributes();
            } finally {
                jarFile.close();
            }
        } catch (IOException e) {
            return null;
        }
    }
}
