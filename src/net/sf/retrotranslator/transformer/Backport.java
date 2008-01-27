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

/**
 * @author Taras Puchko
 */
abstract class Backport {

    public static List<Backport> getBackports(String s) {
        int colonIndex = s.indexOf(':');
        if (colonIndex < 0) {
            return Collections.<Backport>singletonList(new PackageBackport("", toPrefixName(s.trim())));
        }
        String leftToken = s.substring(0, colonIndex).trim();
        String rightToken = s.substring(colonIndex + 1).trim();
        int leftIndex = leftToken.lastIndexOf('.');
        int rightIndex = rightToken.lastIndexOf('.');
        List<Backport> result = new ArrayList<Backport>();
        if (leftIndex >= 0 && rightIndex >= 0) {
            result.add(new MemberBackport(
                    toInternalName(leftToken.substring(0, leftIndex)), leftToken.substring(leftIndex + 1),
                    toInternalName(rightToken.substring(0, rightIndex)), rightToken.substring(rightIndex + 1)));
        }
        result.add(new ClassBackport(toInternalName(leftToken), toInternalName(rightToken)));
        result.add(new PackageBackport(toPrefixName(leftToken), toPrefixName(rightToken)));
        return result;
    }

    private static String toInternalName(String name) {
        return name.replace('.', '/');
    }

    private static String toPrefixName(String name) {
        return toInternalName(name) + '/';
    }

}
