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
package net.sf.retrotranslator.runtime.java.util.regex;

import java.util.regex.*;

/**
 * @author Taras Puchko
 */
public class _Matcher {

    public static String quoteReplacement(String s) {
        if (s.indexOf('\\') < 0 && s.indexOf('$') < 0) return s;
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' || c == '$') buffer.append('\\');
            buffer.append(c);
        }
        return buffer.toString();
    }

    public static MatchResult toMatchResult(Matcher matcher) {
        boolean available = true;
        int groupCount = matcher.groupCount();
        int[] starts = new int[groupCount + 1];
        int[] ends = new int[groupCount + 1];
        String[] groups = new String[groupCount + 1];
        try {
            for (int i = 0; i <= groupCount; i++) {
                starts[i] = matcher.start(i);
                ends[i] = matcher.end(i);
                groups[i] = matcher.group(i);
            }
        } catch (IllegalStateException e) {
            available = false;
        }
        return new MatchResultImpl(available, groupCount, starts, ends, groups);
    }

    private static class MatchResultImpl implements MatchResult {

        private final boolean available;
        private final int groupCount;
        private final int[] starts;
        private final int[] ends;
        private final String[] groups;

        public MatchResultImpl(boolean available, int groupCount,
                               int[] starts, int[] ends, String[] groups) {
            this.available = available;
            this.groupCount = groupCount;
            this.starts = starts;
            this.ends = ends;
            this.groups = groups;
        }

        public int start() {
            return start(0);
        }

        public int start(int group) {
            if (!available) throw new IllegalStateException();
            return starts[group];
        }

        public int end() {
            return end(0);
        }

        public int end(int group) {
            if (!available) throw new IllegalStateException();
            return ends[group];
        }

        public String group() {
            return group(0);
        }

        public String group(int group) {
            if (!available) throw new IllegalStateException();
            return groups[group];
        }

        public int groupCount() {
            return groupCount;
        }

    }

}
