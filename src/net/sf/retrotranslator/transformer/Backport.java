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
import java.util.regex.*;

/**
 * @author Taras Puchko
 */
abstract class Backport {

    private static final Pattern RUNTIME_PATTERN = Pattern.compile(
            "\\s*((?:\\w+\\.)*\\w+)\\s*");
    private static final Pattern PACKAGE_PATTERN = Pattern.compile(
            "\\s*((?:\\w+\\.)*\\w+)\\s*:\\s*((?:\\w+\\.)*\\w+)\\s*");
    private static final Pattern CLASS_PATTERN = Pattern.compile(
            "\\s*((?:\\w+\\.)*\\p{Upper}\\w*)\\s*:\\s*((?:\\w+\\.)*\\p{Upper}\\w*)\\s*");
    private static final Pattern MEMBER_PATTERN = Pattern.compile(
            "\\s*((?:\\w+\\.)*\\p{Upper}\\w*)\\.(\\w+)\\s*:\\s*((?:\\w+\\.)*\\p{Upper}\\w*)\\.(\\w+)\\s*");

    public static Backport valueOf(String s) {
        Matcher memberMatcher = MEMBER_PATTERN.matcher(s);
        if (memberMatcher.matches()) {
            return new MemberBackport(
                    toInternalName(memberMatcher.group(1)),
                    memberMatcher.group(2),
                    toInternalName(memberMatcher.group(3)),
                    memberMatcher.group(4));
        }
        Matcher classMatcher = CLASS_PATTERN.matcher(s);
        if (classMatcher.matches()) {
            return new ClassBackport(
                    toInternalName(classMatcher.group(1)),
                    toInternalName(classMatcher.group(2)));
        }
        Matcher packageMatcher = PACKAGE_PATTERN.matcher(s);
        if (packageMatcher.matches()) {
            return new PackageBackport(
                    toPrefixName(packageMatcher.group(1)),
                    toPrefixName(packageMatcher.group(2)));
        }
        Matcher runtimeMatcher = RUNTIME_PATTERN.matcher(s);
        if (runtimeMatcher.matches()) {
            return new PackageBackport(
                    "",
                    toPrefixName(runtimeMatcher.group(1)));
        }
        throw new IllegalArgumentException("Illegal backport name: " + s);
    }

    private static String toInternalName(String name) {
        return name.replace('.', '/');
    }

    private static String toPrefixName(String name) {
        return toInternalName(name) + '/';
    }

}
