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

import net.sf.retrotranslator.runtime.asm.ClassAdapter;
import net.sf.retrotranslator.runtime.asm.ClassVisitor;
import net.sf.retrotranslator.runtime.asm.MethodVisitor;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.HashSet;

/**
 * @author Taras Puchko
 */
public class DuplicateCleaningVisitor extends ClassAdapter {

    private MessageLogger logger;
    private Set<String> methods = new HashSet<String>();

    public DuplicateCleaningVisitor(ClassVisitor cv, MessageLogger logger) {
        super(cv);
        this.logger = logger;
    }

    public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
        super.visit(version, access, name, signature, superName, clean(interfaces, "interface name"));
    }

    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        if (methods.add(name + desc)) {
            return super.visitMethod(access, name, desc, signature, clean(exceptions, "exception name"));
        } else {
            log("method/signature");
            return null;
        }
    }

    private String[] clean(String[] values, String name) {
        if (values == null) return null;
        Set<String> set = new LinkedHashSet<String>();
        for (String s : values) set.add(s);
        if (set.size() == values.length) return values;
        log(name);
        return set.toArray(new String[set.size()]);
    }

    private void log(String name) {
        if (logger != null) logger.log(new Message(Level.INFO, "Repetitive " + name + " removed."));
    }
}
