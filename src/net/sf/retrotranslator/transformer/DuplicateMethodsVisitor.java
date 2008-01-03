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
import net.sf.retrotranslator.runtime.asm.*;

/**
 * @author Taras Puchko
 */
class DuplicateMethodsVisitor extends ClassAdapter {

    private final SystemLogger logger;
    private final MethodCounter counter;
    private final Set<String> visitedMethods = new HashSet<String>();

    public DuplicateMethodsVisitor(ClassVisitor visitor, SystemLogger logger, MethodCounter counter) {
        super(visitor);
        this.logger = logger;
        this.counter = counter;
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        String key = name + desc;
        if (visitedMethods.contains(key)) {
            log(access, name);
            return null;
        }
        if (counter.isRepetitive(name, desc) && isBridge(access)) {
            counter.decrement(name, desc);
            log(access, name);
            return null;
        }
        visitedMethods.add(key);
        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    private void log(int access, String name) {
        if (isBridge(access)) {
            logger.logForFile(Level.VERBOSE, "Bridge method removed: " + name);
        } else {
            logger.logForFile(Level.WARNING, "Repetitive method removed: " + name);
        }
    }

    private static boolean isBridge(int access) {
        return (access & Opcodes.ACC_BRIDGE) != 0;
    }

}
