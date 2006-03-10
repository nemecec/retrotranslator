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

import edu.emory.mathcs.backport.java.util.AbstractQueue;
import edu.emory.mathcs.backport.java.util.PriorityQueue;
import edu.emory.mathcs.backport.java.util.Queue;
import net.sf.retrotranslator.runtime.asm.ClassVisitor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Taras Puchko
 */
class ClassSubstitutionVisitor extends GenericClassVisitor {

    private static final String RUNTIME = "net/sf/retrotranslator/runtime/";
    private static final String BACKPORT = "edu/emory/mathcs/backport/";
    private static final ClassLoader LOADER = ClassSubstitutionVisitor.class.getClassLoader();

    private static Map<String, String> map = new HashMap<String, String>();

    static {
        map.put("java/lang/StringBuilder", "java/lang/StringBuffer");
        map.put("net/sf/retrotranslator/transformer/ClassFileTransformer", "sun/misc/ClassFileTransformer");
        map.put("net/sf/retrotranslator/transformer/ClassPreProcessor", "com/bea/jvm/ClassPreProcessor");
        for (Class type : new Class[]{AbstractQueue.class, PriorityQueue.class, Queue.class}) {
            String name = type.getName().replace('.', '/');
            if (!name.startsWith(BACKPORT)) throw new IllegalArgumentException(name);
            map.put(name.substring(BACKPORT.length()), name);
        }
    }

    public ClassSubstitutionVisitor(final ClassVisitor cv) {
        super(cv);
    }

    protected String visitIdentifier(String identifier) {
        return TransformerTools.fixIdentifier(identifier);
    }

    protected String visitInternalName(String name) {
        name = TransformerTools.fixIdentifier(name);
        if (name.startsWith("java/util/concurrent/")) {
            return BACKPORT + name;
        }
        String result = map.get(name);
        if (result == null) {
            String backportName = RUNTIME + name + "_";
            result = LOADER.getResource(backportName + ".class") != null ? backportName : name;
            map.put(name, result);
        }
        return result;
    }
}
