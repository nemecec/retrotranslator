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
import java.util.Collection;

/**
 * @author Taras Puchko
 */
abstract class FileContainer {

    protected File location;

    protected FileContainer(File location) {
        this.location = location;
    }

    public File getLocation() {
        return location;
    }

    public abstract Collection<? extends FileEntry> getEntries();

    public abstract void removeEntry(String name);

    public abstract void putEntry(String name, byte[] contents, boolean modified);

    public abstract void flush(SystemLogger logger);

    public abstract boolean containsUpToDate(String name, long sourceTime);

    public abstract long lastModified();

    protected static byte[] readFully(InputStream stream, int length) throws IOException {
        if (length <= 0) length = 0x8000;
        byte[] buffer = new byte[length];
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(length);
        int count;
        while((count = stream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, count);
        }
        return outputStream.toByteArray();
    }

    public String toString() {
        return location.toString();
    }

    public int getFileCount() {
        return getEntries().size();
    }

}
