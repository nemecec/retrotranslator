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

/**
 * @author Taras Puchko
 */
class FileTranslator {

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

    public boolean transform(FileContainer source, FileContainer destination) {
        countTransformed = 0;
        logger.log(new Message(Level.INFO, "Processing " + source.getFileCount() + " file(s)" +
                (source == destination ? " in " + source : " from " + source + " to " + destination) + "."));
        if (uptodatecheck && source.lastModified() < destination.lastModified()) {
            logger.log(new Message(Level.INFO, "Skipped up-to-date file(s)."));
            return false;
        }
        for (FileEntry entry : source.getEntries()) {
            transform(entry, source, destination);
        }
        source.flush(logger);
        logger.log(new Message(Level.INFO, "Transformed " + countTransformed + " file(s)."));
        return true;
    }

    private void transform(FileEntry entry, FileContainer source, FileContainer destination) {
        String name = entry.getName();
        String fixedName = converter == null ? name : converter.convertFileName(name);
        if (uptodatecheck && destination.containsUpToDate(fixedName, entry.lastModified())) {
            logger.logForFile(Level.VERBOSE, "Up to date");
            return;
        }
        if (mask.matches(name) || !name.equals(fixedName)) {
            logger.setFile(source.getLocation(), name);
            logger.logForFile(Level.VERBOSE, "Transformation");
            byte[] sourceData = entry.getContent();
            byte[] resultData = TransformerTools.isClassFile(sourceData)
                    ? classTransformer.transform(sourceData, 0, sourceData.length)
                    : fileTransformer.transform(sourceData);
            boolean transformed = sourceData != resultData || !fixedName.equals(name);
            if (transformed || source != destination) {
                if (!fixedName.equals(name)) {
                    destination.removeEntry(name);
                }
                destination.putEntry(fixedName, resultData, transformed);
            }
            if (transformed) {
                countTransformed++;
            }
            logger.setFile(null, null);
        } else if (source != destination) {
            destination.putEntry(name, entry.getContent(), false);
        }
    }

}
