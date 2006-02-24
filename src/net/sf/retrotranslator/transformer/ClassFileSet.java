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
import java.util.ArrayList;
import java.util.List;

/**
 * @author Taras Puchko
 */
public class ClassFileSet {

    private File baseDir;
    private List<String> fileNames;

    public ClassFileSet(File baseDir) {
        this.baseDir = baseDir;
    }

    public ClassFileSet(File baseDir, List<String> fileNames) {
        this.baseDir = baseDir;
        this.fileNames = fileNames;
    }

    public File getBaseDir() {
        return baseDir;
    }

    public List<String> getFileNames() {
        if (fileNames == null) {
            fileNames = new ArrayList<String>();
            addFileNames(baseDir);
        }
        return fileNames;
    }

    private void addFileNames(File dir) {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                addFileNames(file);
            } else if (file.getName().endsWith(".class")) {
                if (!file.getPath().startsWith(baseDir.getPath())) throw new IllegalStateException();
                fileNames.add(file.getPath().substring(baseDir.getPath().length() + 1));
            }
        }
    }
}