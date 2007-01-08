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
import java.util.zip.*;
import net.sf.retrotranslator.runtime.asm.ClassReader;
import net.sf.retrotranslator.runtime.impl.RuntimeTools;

/**
 * @author Taras Puchko
 */
class ClassReaderFactory {

    private ClassLoader classLoader;
    private List<Entry> entries = new ArrayList<Entry>();
    private Map<String, SoftReference<ClassReader>> cache = new HashMap<String, SoftReference<ClassReader>>();

    public ClassReaderFactory(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void appendPath(File element) {
        if (!element.exists()) throw new RuntimeException(element.getPath() + " not found.");
        entries.add(element.isDirectory() ? new DirectoryEntry(element) : new ZipFileEntry(element));
    }

    public ClassReader getClassReader(String name) throws ClassNotFoundException {
        if (cache.containsKey(name)) {
            SoftReference<ClassReader> reference = cache.get(name);
            if (reference == null) throw new ClassNotFoundException(name);
            ClassReader classReader = reference.get();
            if (classReader != null) return classReader;
        }
        InputStream stream = getStream(name + RuntimeTools.CLASS_EXTENSION);
        if (stream == null) {
            cache.put(name, null);
            throw new ClassNotFoundException(name);
        }
        try {
            try {
                ClassReader classReader = new ClassReader(stream);
                cache.put(name, new SoftReference<ClassReader>(classReader));
                return classReader;
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream getStream(String name) {
        for (Entry entry : entries) {
            InputStream stream = entry.getResourceAsStream(name);
            if (stream != null) return stream;
        }
        return classLoader == null ? null : classLoader.getResourceAsStream(name);
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

        private ZipFile zipFile;

        public ZipFileEntry(File file) {
            try {
                this.zipFile = new ZipFile(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public InputStream getResourceAsStream(String name) {
            ZipEntry entry = zipFile.getEntry(name);
            try {
                return entry == null ? null : zipFile.getInputStream(entry);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void close() {
            try {
                zipFile.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
