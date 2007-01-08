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
import net.sf.retrotranslator.runtime.asm.*;
import net.sf.retrotranslator.runtime.asm.signature.*;

/**
 * @author Taras Puchko
 */
class DuplicateInterfacesVisitor extends ClassAdapter {

    private final SystemLogger logger;
    private final MethodCounter counter;

    public DuplicateInterfacesVisitor(ClassVisitor cv, SystemLogger logger, MethodCounter counter) {
        super(cv);
        this.logger = logger;
        this.counter = counter;
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        String[] cleanInterfaces = cleanInterfaces(interfaces);
        String cleanSignature = cleanInterfaces == interfaces ? signature : cleanSignature(signature);
        super.visit(version, access, name, cleanSignature, superName, cleanInterfaces);
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        counter.increment(name, desc);
        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    private String[] cleanInterfaces(String[] values) {
        if (values == null) return null;
        Set<String> set = new LinkedHashSet<String>();
        for (String s : values) {
            if (!set.add(s) && logger != null) {
                logger.logForFile(Level.VERBOSE, "Repetitive interface name removed: " + s.replace('/', '.'));
            }
        }
        if (set.size() == values.length) return values;
        return set.toArray(new String[set.size()]);
    }

    private String cleanSignature(String signature) {
        if (signature == null) return null;
        SignatureWriter writer = new SignatureWriter();
        new SignatureReader(signature).accept(new SignatureCleaningVisitor(writer));
        return writer.toString();
    }

    private static class SignatureCleaningVisitor extends SignatureAdapter {

        private Set<String> interfaces = new HashSet<String>();

        public SignatureCleaningVisitor(final SignatureVisitor visitor) {
            super(visitor);
        }

        private void addInterface(String signature) {
            new SignatureReader(signature).acceptType(super.visitInterface());
        }

        public SignatureVisitor visitInterface() {
            final SignatureWriter writer = new SignatureWriter();
            return new SignatureAdapter(writer) {

                private String interfaceName;

                public void visitClassType(String name) {
                    super.visitClassType(name);
                    interfaceName = name;
                }

                public void visitEnd() {
                    super.visitEnd();
                    if (interfaces.add(interfaceName)) {
                        addInterface(writer.toString());
                    }
                }

            };
        }

    }

}
