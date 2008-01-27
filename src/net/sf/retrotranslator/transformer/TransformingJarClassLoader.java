/***
 * Retrotranslator: a Java bytecode transformer that translates Java classes
 * compiled with JDK 5.0 into classes that can be run on JVM 1.4.
 *
 * Copyright (c) 2005 - 2008 Taras Puchko
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
import java.util.jar.Manifest;
import net.sf.retrotranslator.runtime.impl.RuntimeTools;

/**
 * @author Taras Puchko
 */
public class TransformingJarClassLoader extends JarClassLoader {

    private final ClassTransformer transformer;

    public TransformingJarClassLoader(File file, ClassLoader parent, ClassTransformer transformer) throws IOException {
        super(file, parent);
        this.transformer = transformer;
    }

    protected Class findClass(final String name) throws ClassNotFoundException {
        URL resource = findResource(name.replace('.', '/').concat(RuntimeTools.CLASS_EXTENSION));
        if (resource == null) {
            throw new ClassNotFoundException(name);
        }
        try {
            URLConnection connection = resource.openConnection();
            initPackage(name, connection, resource);
            byte[] content = RuntimeTools.readAndClose(connection.getInputStream());
            content = transformer.transform(content, 0, content.length);
            return defineClass(name, content, 0, content.length);
        } catch (IOException e) {
            throw new ClassNotFoundException(name, e);
        }
    }

    private void initPackage(String className, URLConnection connection, URL resource) throws IOException {
        String packageName = getPackageName(className);
        if (packageName == null || getPackage(packageName) != null) {
            return;
        }
        Manifest manifest = getManifest(connection);
        try {
            if (manifest != null) {
                definePackage(packageName, manifest, resource);
            } else {
                definePackage(packageName, null, null, null, null, null, null, null);
            }
        } catch (IllegalArgumentException e) {
            // ignore
        }
    }

    private static String getPackageName(String className) {
        int dotIndex = className.lastIndexOf('.');
        return dotIndex < 0 ? null : className.substring(0, dotIndex);
    }

    private static Manifest getManifest(URLConnection connection) throws IOException {
        if (connection instanceof JarURLConnection) {
            return ((JarURLConnection) connection).getManifest();
        }
        return null;
    }

}
