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

import java.util.*;
import net.sf.retrotranslator.runtime.impl.RuntimeTools;

/**
 * @author Taras Puchko
 */
class EmbeddingConverter {

    private static final String RUNTIME_PREFIX = "net/sf/retrotranslator/runtime";
    private static final String CONCURRENT_PREFIX = "edu/emory/mathcs/backport";

    private String embeddingPrefix;
    private Map<String, Boolean> runtimeFileNames = new HashMap<String, Boolean>();
    private Map<String, Boolean> concurrentFileNames = new HashMap<String, Boolean>();

    public EmbeddingConverter(String embed) {
        this.embeddingPrefix = embed.replace('.', '/') + '/';
    }

    public Map<String, Boolean> getRuntimeFileNames() {
        return runtimeFileNames;
    }

    public Map<String, Boolean> getConcurrentFileNames() {
        return concurrentFileNames;
    }

    public String convertFileName(String fileName) {
        return getMap(fileName) == null ? fileName : embeddingPrefix + fileName;
    }

    public String convertClassName(String className) {
        Map<String, Boolean> map = getMap(className);
        if (map == null) {
            return className;
        }
        String key = className + RuntimeTools.CLASS_EXTENSION;
        if (!map.containsKey(key)) {
            map.put(key, Boolean.FALSE);
        }
        return embeddingPrefix + className;
    }

    private Map<String, Boolean> getMap(String name) {
        if (name.startsWith(RUNTIME_PREFIX)) {
            return runtimeFileNames;
        }
        if (name.startsWith(CONCURRENT_PREFIX)) {
            return concurrentFileNames;
        }
        return null;
    }

}
