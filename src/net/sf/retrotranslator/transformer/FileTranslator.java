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

import edu.emory.mathcs.backport.java.util.Queue;
import java.io.*;
import java.net.URL;
import java.util.*;
import net.sf.retrotranslator.runtime.impl.*;

/**
 * @author Taras Puchko
 */
class FileTranslator {

    private static final String CLASS_DESCRIPTOR_PATH = getFileName(ClassDescriptor.class);
    private static final String SIGNATURES_PATH = getFileName(ClassDescriptor.class, ClassDescriptor.SIGNATURES_NAME);

    private final ClassTransformer classTransformer;
    private final TextFileTransformer fileTransformer;
    private final EmbeddingConverter converter;
    private final SystemLogger logger;
    private final SourceMask mask;
    private final boolean uptodatecheck;
    private int countTransformed;

    public FileTranslator(ClassTransformer classTransformer, TextFileTransformer fileTransformer,
                          EmbeddingConverter converter, SystemLogger logger, SourceMask mask, boolean uptodatecheck) {
        this.classTransformer = classTransformer;
        this.fileTransformer = fileTransformer;
        this.converter = converter;
        this.logger = logger;
        this.mask = mask;
        this.uptodatecheck = uptodatecheck;
    }

    public void transform(FileContainer source, FileContainer destination) {
        countTransformed = 0;
        logger.log(new Message(Level.INFO, "Processing " + source.getFileCount() + " file(s)" +
                (source == destination ? " in " + source : " from " + source + " to " + destination) + "."));
        for (FileEntry entry : source.getEntries()) {
            transform(entry, source, destination);
        }
        source.flush(logger);
        logger.log(new Message(Level.INFO, "Transformed " + countTransformed + " file(s)."));
    }

    private void transform(FileEntry entry, FileContainer source, FileContainer destination) {
        String name = entry.getName();
        String fixedName = converter == null ? name : converter.convertFileName(name);
        logger.setFile(source.getLocation(), name);
        if (uptodatecheck && destination.containsUpToDate(fixedName, entry.lastModified())) {
            logger.logForFile(Level.VERBOSE, "Up to date");
            return;
        }
        if (converter != null && name.equals(SIGNATURES_PATH)) {
            logger.logForFile(Level.VERBOSE, "Transformation");
            destination.putEntry(fixedName, transformSignatures(entry.getContent()));
            countTransformed++;
        } else if (mask.matches(name) || !name.equals(fixedName)) {
            logger.logForFile(Level.VERBOSE, "Transformation");
            byte[] sourceData = entry.getContent();
            byte[] resultData = TransformerTools.isClassFile(sourceData)
                    ? classTransformer.transform(sourceData, 0, sourceData.length)
                    : fileTransformer.transform(sourceData, converter);
            boolean transformed = sourceData != resultData || !fixedName.equals(name);
            if (transformed || source != destination) {
                if (!fixedName.equals(name)) {
                    destination.removeEntry(name);
                }
                destination.putEntry(fixedName, resultData);
            }
            if (transformed) {
                countTransformed++;
            }
        } else if (source != destination) {
            destination.putEntry(name, entry.getContent());
        }
    }

    public void embed(FileContainer destination) {
        Map<String, Boolean> runtimeNames = converter.getRuntimeFileNames();
        Map<String, Boolean> concurrentNames = converter.getConcurrentFileNames();
        if (runtimeNames.isEmpty() && concurrentNames.isEmpty()) {
            return;
        }
        logger.log(new Message(Level.INFO, "Embedding backported classes"));
        FileContainer runtimeContainer = findContainer(ClassDescriptor.class);
        FileContainer concurrentContainer = findContainer(Queue.class);
        while (true) {
            if (embed(runtimeContainer, destination, runtimeNames) &&
                    embed(concurrentContainer, destination, concurrentNames)) {
                break;
            }
        }
        logger.log(new Message(Level.INFO, "Embedded required files."));
    }

    private boolean embed(FileContainer source, FileContainer destination, Map<String, Boolean> fileNames) {
        boolean finished = true;
        for (FileEntry entry : source.getEntries()) {
            Boolean processed = fileNames.get(entry.getName());
            if (Boolean.FALSE.equals(processed)) {
                transform(entry, source, destination);
                String name = entry.getName();
                fileNames.put(name, Boolean.TRUE);
                if (name.equals(CLASS_DESCRIPTOR_PATH)) {
                    fileNames.put(SIGNATURES_PATH, Boolean.FALSE);
                }
                finished = false;
            }
        }
        return finished;
    }

    private byte[] transformSignatures(byte[] content) {
        try {
            NameTranslator transformer = new NameTranslator() {
                protected String typeName(String s) {
                    return converter.convertClassName(s);
                }
            };
            Properties source = new Properties();
            source.load(new ByteArrayInputStream(content));
            Properties target = new Properties();
            for (Map.Entry entry : source.entrySet()) {
                String key = converter.convertClassName((String) entry.getKey());
                String value = transformer.declarationSignature((String) entry.getValue());
                target.put(key, value);
            }
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            target.store(stream, null);
            return stream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static FileContainer findContainer(Class aClass) {
        String path = "/" + getFileName(aClass);
        URL resource = aClass.getResource(path);
        if (resource == null) {
            throw new IllegalArgumentException("Location not found: " + aClass);
        }
        String url = resource.toExternalForm();
        String prefix = "jar:file:";
        String suffix = "!" + path;
        if (!url.startsWith(prefix) || !url.endsWith(suffix)) {
            throw new IllegalArgumentException("Not in a jar file: " + aClass);
        }
        File file = new File(url.substring(prefix.length(), url.length() - suffix.length()));
        if (!file.isFile()) {
            throw new IllegalArgumentException("File not found: " + file);
        }
        return new JarFileContainer(file);
    }

    private static String getFileName(Class aClass) {
        return aClass.getName().replace('.', '/') + RuntimeTools.CLASS_EXTENSION;
    }

    private static String getFileName(Class aClass, String fileName) {
        return aClass.getPackage().getName().replace('.', '/') + "/" + fileName;
    }

}
