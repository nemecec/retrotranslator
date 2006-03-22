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

import java.io.*;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Taras Puchko
 */
class JarFileContainer extends FileContainer {

    private Map<String, JarFileEntry> entries;
    private boolean modified;

    public JarFileContainer(File location) {
        super(location);
    }

    public Collection<? extends FileEntry> getEntries() {
        if (entries == null) init();
        return entries.values();
    }

    public void removeEntry(String name) {
        if (entries != null) {
            entries.remove(name);
        }
    }

    private void init() {
        entries = new LinkedHashMap<String, JarFileEntry>();
        try {
            FileInputStream fileInputStream = new FileInputStream(location);
            try {
                ZipInputStream stream = new ZipInputStream(fileInputStream);
                ZipEntry entry;
                while ((entry = stream.getNextEntry()) != null) {
                    byte[] content = entry.isDirectory() ? null : readFully(stream, (int) entry.getSize());
                    entries.put(entry.getName(), new JarFileEntry(entry.getName(), content));
                }
            } finally {
                fileInputStream.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void putEntry(String name, byte[] contents) {
        if (entries == null) entries = new LinkedHashMap<String, JarFileEntry>();
        entries.put(name, new JarFileEntry(name, contents));
        modified = true;
    }

    public void flush() {
        if (!modified) return;
        try {
            FileOutputStream stream = new FileOutputStream(location);
            try {
                flush(stream);
                modified = false;
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void flush(FileOutputStream fileOutputStream) throws IOException {
        JarFileEntry manifestEntry = entries.get(JarFile.MANIFEST_NAME);
        Manifest manifest = manifestEntry == null ? new Manifest()
                : new Manifest(new ByteArrayInputStream(manifestEntry.getContent()));
        Attributes attributes = manifest.getMainAttributes();
        attributes.putValue("Manifest-Version", "1.0");
        attributes.putValue("Created-By",
                System.getProperty("java.vm.version") + " (" + System.getProperty("java.vm.vendor") + ")");
        String title = getClass().getPackage().getImplementationTitle();
        String version = getClass().getPackage().getImplementationVersion();
        if (title != null && version != null) {
            attributes.putValue("Retrotranslator-Version", title + " " + version);
        }
        JarOutputStream stream = new JarOutputStream(fileOutputStream, manifest);
        stream.setLevel(Deflater.BEST_COMPRESSION);
        for (JarFileEntry entry : entries.values()) {
            if (entry == manifestEntry) continue;
            stream.putNextEntry(new ZipEntry(entry.getName()));
            byte[] content = entry.getContent();
            if (content != null) stream.write(content);
        }
        stream.close();
    }

    private static class JarFileEntry extends FileEntry {

        private byte[] content;

        public JarFileEntry(String name, byte[] content) {
            super(name);
            this.content = content;
        }

        public byte[] getContent() {
            return content;
        }
    }
}
