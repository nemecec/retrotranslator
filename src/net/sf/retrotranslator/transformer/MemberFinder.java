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

import net.sf.retrotranslator.runtime.asm.*;
import net.sf.retrotranslator.runtime.impl.*;

/**
 * @author Taras Puchko
 */
class MemberFinder extends EmptyVisitor {

    private final TargetEnvironment environment;
    private final boolean method;
    private final boolean stat;
    private final String name;
    private final String desc;

    private String superName;
    private String[] interfaces;
    private int found;

    public MemberFinder(TargetEnvironment environment, boolean method, boolean stat, String name, String desc) {
        this.environment = environment;
        this.method = method;
        this.stat = stat;
        this.name = name;
        this.desc = desc;
    }

    public int findIn(String className, String location) throws ClassNotFoundException {
        if (className == null) return 0;
        try {
            environment.getClassReader(className).accept(this, true);
        } catch (ClassNotFoundException e) {
            if (location == null) throw e;
            throw new ClassNotFoundException(e.getMessage() + ", location: " + location, e);
        }
        if (found > 0 || name.equals(RuntimeTools.CONSTRUCTOR_NAME)) {
            return found;
        }
        String superClassName = superName;
        for (String interfaceName : interfaces) {
            if (findIn(interfaceName, className) > 0) return found;
        }
        return findIn(superClassName, className);
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.superName = superName;
        this.interfaces = interfaces;
    }

    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        if (!method) check(access, name, desc);
        return null;
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (method) check(access, name, desc);
        return null;
    }

    private void check(int access, String name, String desc) {
        if (name.equals(this.name) && desc.equals(this.desc) && stat == ((access & Opcodes.ACC_STATIC) != 0)) found++;
    }
}
