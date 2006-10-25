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

import net.sf.retrotranslator.runtime.asm.ClassReader;
import net.sf.retrotranslator.runtime.asm.ClassVisitor;
import net.sf.retrotranslator.runtime.asm.ClassWriter;
import net.sf.retrotranslator.runtime.impl.BytecodeTransformer;

/**
 * @author Taras Puchko
 */
class ClassTransformer implements BytecodeTransformer {

    private boolean lazy;
    private boolean advanced;
    private boolean stripsign;
    private boolean retainapi;
    private EmbeddingConverter converter;
    private MessageLogger logger;
    private BackportLocatorFactory factory;

    public ClassTransformer(boolean lazy, boolean advanced, boolean stripsign, boolean retainapi,
                            EmbeddingConverter converter, MessageLogger logger, BackportLocatorFactory factory) {
        this.lazy = lazy;
        this.advanced = advanced;
        this.stripsign = stripsign;
        this.retainapi = retainapi;
        this.converter = converter;
        this.logger = logger;
        this.factory = factory;
    }

    public byte[] transform(byte[] bytes, int offset, int length) {
        if (lazy && (bytes[offset + 7] <= 48 ||
                bytes[offset + 6] != 0 || bytes[offset + 5] != 0 || bytes[offset + 4] != 0)) {
            if (offset == 0 && length == bytes.length) return bytes;
            byte[] result = new byte[length];
            System.arraycopy(bytes, offset, result, 0, length);
            return result;
        }
        ClassWriter classWriter = new ClassWriter(true);
        ClassVisitor visitor = new DuplicateCleaningVisitor(classWriter, logger);
        visitor = new VersionVisitor(new ArrayCloningVisitor(new ClassLiteralVisitor(visitor)));
        if (converter != null) visitor = new PrefixingVisitor(visitor, converter);
        if (!retainapi) {
            BackportLocator locator = factory.getLocator();
            visitor = new InheritanceVisitor(visitor, locator);
            visitor = new ConstructorSubstitutionVisitor(visitor, locator, advanced);
            visitor = new UtilBackportVisitor(visitor);
            visitor = new MemberSubstitutionVisitor(visitor, locator, advanced);
            visitor = new EnumVisitor(visitor);
            visitor = new ClassSubstitutionVisitor(visitor, locator);
        }
        if (stripsign) visitor = new SignatureStrippingVisitor(visitor);
        new ClassReader(bytes, offset, length).accept(visitor, false);
        return classWriter.toByteArray();
    }
}
