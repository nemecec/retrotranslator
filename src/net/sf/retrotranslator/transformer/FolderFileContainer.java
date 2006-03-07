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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        entries = new LinkedHashMap<String, FolderFileEntry>();
        for (String name : fileNames) {
            entries.put(name, new FolderFileEntry(name, new File(location, name)));
        }
    }

    public Collection<? extends FileEntry> getEntries() {
        if (entries == null) {
            entries = new LinkedHashMap<String, FolderFileEntry>();
            scanFolder(location, location.getPath().length() + 1);
        }
        return entries.values();
    }

    public void removeEntry(String name) {
        FolderFileEntry entry = entries.remove(name);
        if (entry != null) entry.file.delete();
    }

    private void scanFolder(File folder, int prefixLength) {
        for (File file : folder.listFiles()) {
            String name = file.getPath().substring(prefixLength).replace(File.separatorChar, '/');
            boolean isFolder = file.isDirectory();
            if (isFolder) name += "/";
            entries.put(name, new FolderFileEntry(name, file));
            if (isFolder) scanFolder(file, prefixLength);
        }
    }

    public void putEntry(String name, byte[] contents) {
        File file = new File(location, name);
        if (entries == null) entries = new LinkedHashMap<String, FolderFileEntry>();
        entries.put(name, new FolderFileEntry(name, file));
        if (name.endsWith("/")) {
            file.mkdirs();
            return;
        }
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

    public void flush() {
        //no-op
    }

    private static class FolderFileEntry extends FileEntry {

        private File file;

        public FolderFileEntry(String name, File file) {
            super(name);
            this.file = file;
        }

        public byte[] getContent() {
            if (!file.isFile()) return null;
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
    }
}
