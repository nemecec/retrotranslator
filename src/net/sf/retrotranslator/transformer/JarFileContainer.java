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
import java.util.*;
import java.util.jar.*;
import java.util.regex.Pattern;
import java.util.zip.*;

/**
 * @author Taras Puchko
 */
class JarFileContainer extends FileContainer {

    private static Pattern SIGNATURE_ENTRY = Pattern.compile("META-INF/SIG-.+|META-INF/.+\\.(SF|DSA|RSA)");
    private static Pattern SIGNATURE_ATTRIBUTE = Pattern.compile("Magic|.+-Digest(-.+)?");

    private Map<String, JarFileEntry> entries;
    private boolean modified;

    public JarFileContainer(File location) {
        super(location);
    }

    public Collection<? extends FileEntry> getEntries() {
        if (entries == null) loadEntries();
        return new ArrayList<JarFileEntry>(entries.values());
    }

    public void removeEntry(String name) {
        if (entries != null) {
            entries.remove(name);
        }
    }

    private void loadEntries() {
        initEntries();
        try {
            long lastModified = location.lastModified();
            FileInputStream fileInputStream = new FileInputStream(location);
            try {
                ZipInputStream stream = new ZipInputStream(fileInputStream);
                ZipEntry entry;
                while ((entry = stream.getNextEntry()) != null) {
                    if (!entry.isDirectory()) {
                        byte[] content = readFully(stream, (int) entry.getSize());
                        entries.put(entry.getName(), new JarFileEntry(entry.getName(), content, lastModified, false));
                    }
                }
            } finally {
                fileInputStream.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void putEntry(String name, byte[] contents, boolean modified) {
        initEntries();
        entries.put(name, new JarFileEntry(name, contents, 0, modified));
        this.modified = true;
    }

    private void initEntries() {
        if (entries == null) {
            entries = new LinkedHashMap<String, JarFileEntry>();
        }
    }

    public void flush(SystemLogger logger) {
        if (!modified) return;
        try {
            FileOutputStream stream = new FileOutputStream(location);
            try {
                flush(stream, logger);
                modified = false;
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean containsUpToDate(String name, long sourceTime) {
        return false;
    }

    public long lastModified() {
        return location.lastModified();
    }

    private void flush(FileOutputStream fileOutputStream, SystemLogger logger) throws IOException {
        Manifest manifest = getManifest();
        boolean signatureRemoved = removeSignature(manifest);
        List<JarFileEntry> fileEntries = getFileEntries(logger, signatureRemoved);
        Set<String> folderNames = getFolderNames(fileEntries);
        JarOutputStream stream = new JarOutputStream(fileOutputStream);
        stream.setLevel(Deflater.BEST_COMPRESSION);
        stream.putNextEntry(new ZipEntry("META-INF/"));
        stream.putNextEntry(new ZipEntry(JarFile.MANIFEST_NAME));
        manifest.write(stream);
        for (String name : folderNames) {
            stream.putNextEntry(new ZipEntry(name));
        }
        for (JarFileEntry entry : fileEntries) {
            stream.putNextEntry(new ZipEntry(entry.getName()));
            stream.write(entry.getContent());
        }
        stream.close();
    }

    private Manifest getManifest() throws IOException {
        JarFileEntry entry = entries.get(JarFile.MANIFEST_NAME);
        Manifest manifest = entry == null ? new Manifest()
                : new Manifest(new ByteArrayInputStream(entry.getContent()));
        Attributes attributes = manifest.getMainAttributes();
        attributes.putValue("Manifest-Version", "1.0");
        attributes.putValue("Created-By",
                System.getProperty("java.vm.version") + " (" + System.getProperty("java.vm.vendor") + ")");
        String title = getClass().getPackage().getImplementationTitle();
        String version = getClass().getPackage().getImplementationVersion();
        if (title != null && version != null) {
            attributes.putValue("Retrotranslator-Version", title + " " + version);
        }
        return manifest;
    }

    private boolean removeSignature(Manifest manifest) {
        boolean result = false;
        for (Attributes attributes : manifest.getEntries().values()) {
            Iterator<Map.Entry<Object, Object>> iterator = attributes.entrySet().iterator();
            while (iterator.hasNext()) {
                if (SIGNATURE_ATTRIBUTE.matcher(iterator.next().getKey().toString()).matches()) {
                    iterator.remove();
                    result = true;
                }
            }
        }
        return result;
    }

    private List<JarFileEntry> getFileEntries(SystemLogger logger, boolean signatureRemoved) {
        List<JarFileEntry> result = new ArrayList<JarFileEntry>(entries.values());
        for (Iterator<JarFileEntry> iterator = result.iterator(); iterator.hasNext();) {
            JarFileEntry entry = iterator.next();
            if (entry.getName().equals(JarFile.MANIFEST_NAME)) {
                iterator.remove();
            }
            if (SIGNATURE_ENTRY.matcher(entry.getName()).matches()) {
                iterator.remove();
                signatureRemoved = true;
            }
        }
        if (signatureRemoved) {
            logger.log(new Message(Level.INFO, "Removing digital signature from " + location, location, null));
        }
        return result;
    }

    private Set<String> getFolderNames(List<JarFileEntry> entries) {
        Set<String> result = new LinkedHashSet<String>();
        for (JarFileEntry entry : entries) {
            String name = entry.getName();
            int index = -1;
            while ((index = name.indexOf('/', index + 1)) >= 0) {
                result.add(name.substring(0, index + 1));
            }
        }
        return result;
    }

    private static class JarFileEntry extends FileEntry {

        private byte[] content;
        private long lastModified;

        public JarFileEntry(String name, byte[] content, long lastModified, boolean modified) {
            super(name, modified);
            this.lastModified = lastModified;
            this.content = content;
        }

        public byte[] getContent() {
            return content;
        }

        public long lastModified() {
            return lastModified;
        }
    }
}
