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
import java.net.URL;
import java.util.*;
import net.sf.retrotranslator.runtime.impl.*;

/**
 * @author Taras Puchko
 */
class EmbeddingConverter {

    private static final String JAR_FILE = "jar:file:";

    private final String embeddingPrefix;
    private final SystemLogger logger;
    private final List<String> prefixes = new ArrayList<String>();
    private final Map<String, Boolean> fileNames = new HashMap<String, Boolean>();

    public EmbeddingConverter(ClassVersion target, String embed, SystemLogger systemLogger) {
        embeddingPrefix = makePrefix(embed);
        logger = systemLogger;
        for (String packageName : TransformerTools.readFile("embed", target)) {
            prefixes.add(makePrefix(packageName));
        }
    }

    private static String makePrefix(String packageName) {
        return packageName.replace('.', '/') + '/';
    }

    public Map<String, Boolean> getFileNames() {
        return fileNames;
    }

    public String convertFileName(String fileName) {
        return isEmbedded(fileName) ? embeddingPrefix + fileName : fileName;
    }

    public String convertClassName(String className) {
        if (isEmbedded(className)) {
            String key = className + RuntimeTools.CLASS_EXTENSION;
            if (!fileNames.containsKey(key)) {
                fileNames.put(key, Boolean.FALSE);
            }
            return embeddingPrefix + className;
        } else {
            return className;
        }
    }

    private boolean isEmbedded(String name) {
        for (String prefix : prefixes) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    public Collection<FileContainer> getContainers() {
        List<FileContainer> result = new ArrayList<FileContainer>();
        for (String prefix : prefixes) {
            try {
                Enumeration<URL> resources = TransformerTools.getDefaultClassLoader().getResources(prefix);
                while (resources.hasMoreElements()) {
                    String url = resources.nextElement().toExternalForm();
                    FileContainer container = getContainer(url, "!/" + prefix);
                    if (container != null) {
                        result.add(container);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    private FileContainer getContainer(String url, String tail) {
        if (!url.startsWith(JAR_FILE) || !url.endsWith(tail)) {
            logger.log(new Message(Level.INFO, "Not in a jar file: " + url));
            return null;
        }
        File file = new File(url.substring(JAR_FILE.length(), url.length() - tail.length()));
        if (!file.isFile()) {
            logger.log(new Message(Level.INFO, "File not found: " + file));
            return null;
        }
        return new JarFileContainer(file);
    }

}
