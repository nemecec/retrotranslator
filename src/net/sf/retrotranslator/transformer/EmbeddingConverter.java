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

import java.util.*;
import net.sf.retrotranslator.runtime.impl.RuntimeTools;

/**
 * @author Taras Puchko
 */
class EmbeddingConverter {

    private final String embeddingPrefix;
    private final SystemLogger logger;
    private final List<String> prefixes = new ArrayList<String>();
    private final Map<String, Boolean> classNames = new HashMap<String, Boolean>();
    private final TargetEnvironment environment;
    private int countEmbedded;

    public EmbeddingConverter(ClassVersion target, String embed, TargetEnvironment environment, SystemLogger logger) {
        this.environment = environment;
        embeddingPrefix = makePrefix(embed);
        this.logger = logger;
        for (String packageName : environment.readRegistry("embed", target)) {
            prefixes.add(makePrefix(packageName));
        }
    }

    private static String makePrefix(String packageName) {
        return packageName.replace('.', '/') + '/';
    }

    public String convertFileName(String fileName) {
        return isEmbedded(fileName) ? embeddingPrefix + fileName : fileName;
    }

    public String convertClassName(String className) {
        if (isEmbedded(className)) {
            if (!classNames.containsKey(className)) {
                classNames.put(className, Boolean.FALSE);
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

    public void embed(FileContainer destination, ClassTransformer transformer) {
        if (classNames.isEmpty()) {
            logger.log(new Message(Level.INFO, "Embedding skipped."));
            return;
        }
        logger.log(new Message(Level.INFO, "Embedding backported classes."));
        countEmbedded = 0;
        boolean modified;
        do {
            modified = false;
            for (Map.Entry<String, Boolean> entry : new HashMap<String, Boolean>(classNames).entrySet()) {
                modified |= embed(entry, destination, transformer);
            }
        } while (modified);
        logger.log(new Message(Level.INFO, "Embedded " + countEmbedded + " class(es)."));
    }

    private boolean embed(Map.Entry<String, Boolean> entry, FileContainer destination, ClassTransformer transformer) {
        if (entry.getValue()) {
            return false;
        }
        String name = entry.getKey();
        String fileName = name + RuntimeTools.CLASS_EXTENSION;
        logger.log(new Message(Level.VERBOSE, "Embedding", null, fileName));
        classNames.put(name, Boolean.TRUE);
        byte[] content = environment.getClassContent(name);
        if (content != null) {
            destination.putEntry(convertFileName(fileName), transformer.transform(content, 0, content.length), true);
            countEmbedded++;
            return true;
        } else {
            logger.log(new Message(Level.WARNING, "Cannot find to embed: " + name));
            return false;
        }
    }

}
