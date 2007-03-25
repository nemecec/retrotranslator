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
class Backport {

    private String originalPrefix;
    private String replacementPrefix;
    private String originalName;
    private String replacementName;

    public Backport(String originalPrefix, String replacementPrefix, String originalName, String replacementName) {
        this.originalPrefix = originalPrefix;
        this.replacementPrefix = replacementPrefix;
        this.originalName = originalName;
        this.replacementName = replacementName;
    }

    public String getOriginalPrefix() {
        return originalPrefix;
    }

    public String getReplacementPrefix() {
        return replacementPrefix;
    }

    public String getOriginalName() {
        return originalName;
    }

    public String getReplacementName() {
        return replacementName;
    }

    public static List<Backport> asList(String s) {
        ArrayList<Backport> result = new ArrayList<Backport>();
        if (s != null) {
            for (String token : s.split(";")) {
                result.add(Backport.valueOf(token));
            }
        }
        return result;
    }

    private static Backport valueOf(String s) {
        String original;
        String replacement;
        int index = s.indexOf(':');
        if (index < 0) {
            original = "";
            replacement = toInternalName(s);
        } else {
            original = toInternalName(s.substring(0, index));
            replacement = toInternalName(s.substring(index + 1));
        }
        return new Backport(toPrefix(original), toPrefix(replacement), original, replacement);
    }

    private static String toInternalName(String name) {
        String result = name.replace('.', '/').trim();
        if (result.startsWith("/") || result.endsWith("/")) {
            throw new IllegalArgumentException("Illegal name: " + name);
        }
        return result;
    }

    private static String toPrefix(String name) {
        return name.length() == 0 ? "" : name + '/';
    }

}