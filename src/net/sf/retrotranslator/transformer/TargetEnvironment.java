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
import java.lang.ref.SoftReference;
import java.util.*;
import java.util.zip.*;
import java.net.URL;
import net.sf.retrotranslator.runtime.asm.ClassReader;
import net.sf.retrotranslator.runtime.impl.*;

/**
 * @author Taras Puchko
 */
class TargetEnvironment {

    private static byte[] NO_CONTENT = new byte[0];

    private final ClassLoader classLoader;
    private final SystemLogger logger;
    private final boolean contextual;
    private final List<Entry> entries = new Vector<Entry>();
    private SoftReference<Map<String, byte[]>> cacheReference;

    public TargetEnvironment(ClassLoader classLoader, SystemLogger logger, boolean contextual) {
        this.classLoader = classLoader;
        this.logger = logger;
        this.contextual = contextual;
    }

    public void appendPath(File element) {
        if (!element.exists()) {
            throw new RuntimeException(element.getPath() + " not found.");
        }
        entries.add(element.isDirectory() ? new DirectoryEntry(element) : new ZipFileEntry(element));
    }

    public ClassReader getClassReader(String name) throws ClassNotFoundException {
        byte[] content = getClassContent(name);
        if (content == null) {
            throw new ClassNotFoundException(name);
        }
        return new ClassReader(content);
    }

    public ClassReader findClassReader(String className) {
        try {
            return getClassReader(className);
        } catch (ClassNotFoundException e) {
            logger.logForFile(Level.VERBOSE, "Cannot find " + RuntimeTools.getDisplayClassName(className));
            return null;
        }
    }

    public byte[] getClassContent(String name) {
        Map<String, byte[]> cache = getCache();
        byte[] content = cache.get(name);
        if (content != null) {
            return content != NO_CONTENT ? content : null;
        }
        List<byte[]> resources = getResources(name + RuntimeTools.CLASS_EXTENSION, 1);
        content = resources.isEmpty() ? null : resources.get(0);
        cache.put(name, content != null ? content : NO_CONTENT);
        return content;
    }

    private synchronized Map<String, byte[]> getCache() {
        Map<String, byte[]> cache = cacheReference == null ? null : cacheReference.get();
        if (cache == null) {
            cache = new Hashtable<String, byte[]>();
            cacheReference = new SoftReference<Map<String, byte[]>>(cache);
        }
        return cache;
    }

    public void close() {
        for (Entry entry : entries) {
            try {
                entry.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Collection<String> readRegistry(String name, ClassVersion target) {
        try {
            LinkedHashSet<String> result = new LinkedHashSet<String>();
            String resourceName = "net/sf/retrotranslator/registry/" +
                    name + target.getName().replace(".", "") + ".properties";
            for (byte[] resource : getResources(resourceName, Integer.MAX_VALUE)) {
                String content = new String(resource, "UTF-8");
                BufferedReader reader = new BufferedReader(new StringReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.length() > 0) {
                        result.add(line);
                    }
                }
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<byte[]> getResources(final String name, final int maxCount) {
        final List<byte[]> result = new ArrayList<byte[]>();

        class ResourceLoader {
            boolean addResource(InputStream stream) {
                if (stream == null) {
                    return false;
                }
                result.add(RuntimeTools.readAndClose(stream));
                return result.size() >= maxCount;
            }

            boolean addResources(ClassLoader loader) {
                if (loader == null) {
                    return false;
                }
                try {
                    Enumeration<URL> resources = loader.getResources(name);
                    while (resources.hasMoreElements()) {
                        URL url = resources.nextElement();
                        if (addResource(url.openStream())) {
                            return true;
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return false;
            }
        }

        ResourceLoader loader = new ResourceLoader();
        for (Entry entry : entries) {
            InputStream stream = entry.getResourceAsStream(name);
            if (loader.addResource(stream)) {
                return result;
            }
        }
        if (loader.addResources(classLoader)) {
            return result;
        }
        if (contextual) {
            loader.addResources(Thread.currentThread().getContextClassLoader());
        }
        return result;
    }

    private static interface Entry {
        InputStream getResourceAsStream(String name);

        void close();
    }

    private static class DirectoryEntry implements Entry {
        private File directory;

        public DirectoryEntry(File directory) {
            this.directory = directory;
        }

        public InputStream getResourceAsStream(String name) {
            try {
                return new FileInputStream(new File(directory, name));
            } catch (FileNotFoundException e) {
                return null;
            }
        }

        public void close() {
        }
    }

    private static class ZipFileEntry implements Entry {
        private final File file;
        private ZipFile zipFile;

        public ZipFileEntry(File file) {
            this.file = file;
        }

        public InputStream getResourceAsStream(String name) {
            if (zipFile == null) {
                openZipFile();
            }
            try {
                ZipEntry entry = zipFile.getEntry(name);
                return entry == null ? null : zipFile.getInputStream(entry);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void openZipFile() {
            try {
                zipFile = new ZipFile(file);
            } catch (IOException e) {
                throw new RuntimeException("Cannot open zip file: " + file.getAbsolutePath(), e);
            }
        }

        public void close() {
            try {
                if (zipFile != null) {
                    zipFile.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
