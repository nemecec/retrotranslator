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

import java.io.File;
import java.util.List;

/**
 * @author Taras Puchko
 */
public class ClassTransformer {

    private boolean stripsign;

    public ClassTransformer(boolean stripsign) {
        this.stripsign = stripsign;
    }

    public byte[] transform(byte[] fileContent, int offset, int length) {
        ClassReader classReader = new ClassReader(fileContent);
        ClassWriter classWriter = new ClassWriter(true);
        ClassVisitor visitor = new VersionVisitor(new ClassSubstitutionVisitor(
                new MemberSubstitutionVisitor(new ConstructorSubstitutionVisitor(new InheritanceVisitor(
                                new EnumVisitor(new ClassLiteralVisitor(new ArrayCloningVisitor(classWriter))))))));
        if (stripsign) {
            visitor = new SignatureStrippingVisitor(visitor);
        }
        classReader.accept(visitor, true);
        return classWriter.toByteArray();
    }
}
