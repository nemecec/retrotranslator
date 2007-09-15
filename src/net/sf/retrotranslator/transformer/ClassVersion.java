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

/**
 * @author Taras Puchko
 */
class ClassVersion {

    private static Map<String, ClassVersion> VALUES = new Hashtable<String, ClassVersion>();

    public static final ClassVersion VERSION_12 = new ClassVersion("1.2", 46);
    public static final ClassVersion VERSION_13 = new ClassVersion("1.3", 47);
    public static final ClassVersion VERSION_14 = new ClassVersion("1.4", 48);
    public static final ClassVersion VERSION_15 = new ClassVersion("1.5", 49);
    
    private String name;
    private int version;

    private ClassVersion(String name, int version) {
        this.name = name;
        this.version = version;
        VALUES.put(name, this);
    }

    public String getName() {
        return name;
    }

    public int getVersion() {
        return version;
    }

    public static ClassVersion valueOf(String name) {
        ClassVersion result = VALUES.get(name);
        if (result == null) {
            throw new IllegalArgumentException("Unsupported target: " + name);
        }
        return result;
    }

    public boolean isBefore(ClassVersion classVersion) {
        return isBefore(classVersion.getVersion());
    }

    public boolean isBefore(int other) {
        return version < (other & 0xFFFF);
    }

}
