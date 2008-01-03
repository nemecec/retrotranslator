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
import java.util.*;

/**
 * @author Taras Puchko
 */
class FolderFileContainer extends FileContainer {

    private Map<String, FolderFileEntry> entries;

    public FolderFileContainer(File location) {
        super(location);
    }

    public FolderFileContainer(File location, List<String> fileNames) {
        super(location);
        initEntries();
        for (String fileName : fileNames) {
            String name = fileName.replace(File.separatorChar, '/');
            entries.put(name, new FolderFileEntry(name, new File(location, name), false));
        }
    }

    public Collection<? extends FileEntry> getEntries() {
        if (entries == null) {
            initEntries();
            scanFolder(location, location.getPath().length() + 1);
        }
        return new ArrayList<FolderFileEntry>(entries.values());
    }

    public void removeEntry(String name) {
        if (entries != null) {
            FolderFileEntry entry = entries.remove(name);
            if (entry != null) entry.file.delete();
        } else {
            new File(location, name).delete();
        }
    }

    private void scanFolder(File folder, int prefixLength) {
        for (File file : folder.listFiles()) {
            String name = file.getPath().substring(prefixLength).replace(File.separatorChar, '/');
            if (file.isDirectory()) {
                scanFolder(file, prefixLength);
            } else {
                entries.put(name, new FolderFileEntry(name, file, false));
            }
        }
    }

    public void putEntry(String name, byte[] contents, boolean modified) {
        initEntries();
        File file = new File(location, name);
        entries.put(name, new FolderFileEntry(name, file, modified));
        file.getParentFile().mkdirs();
        try {
            FileOutputStream stream = new FileOutputStream(file);
            try {
                stream.write(contents);
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void initEntries() {
        if (entries == null) {
            entries = new LinkedHashMap<String, FolderFileEntry>();
        }
    }

    public void flush(SystemLogger logger) {
        initEntries();
    }

    public boolean containsUpToDate(String name, long sourceTime) {
        if (sourceTime == 0) return false;
        long targetTime = new File(location, name).lastModified();
        return targetTime != 0 && targetTime > sourceTime;
    }

    public long lastModified() {
        long result = 0;
        for (FileEntry entry : getEntries()) {
            result = Math.max(result, entry.lastModified());
        }
        return result;
    }

    private static class FolderFileEntry extends FileEntry {

        private File file;

        public FolderFileEntry(String name, File file, boolean modified) {
            super(name, modified);
            this.file = file;
        }

        public byte[] getContent() {
            try {
                FileInputStream stream = new FileInputStream(file);
                try {
                    return readFully(stream, (int) file.length());
                } finally {
                    stream.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public long lastModified() {
            return file.lastModified();
        }
    }
}
