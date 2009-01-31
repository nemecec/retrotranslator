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

import java.util.regex.Pattern;
import java.lang.reflect.*;
import net.sf.retrotranslator.tests.TestCaseBase;

/**
 * @author Taras Puchko
 */
public class _PatternTestCase extends TestCaseBase {

    public void testQuote() throws Exception {
        assertEquals("\\Qabc\\E", Pattern.quote("abc"));
        assertEquals("\\Qa\\Qbc\\E", Pattern.quote("a\\Qbc"));
        assertEquals("\\Qa\\E\\\\E\\Qbc\\E", Pattern.quote("a\\Ebc"));
        assertEquals("\\Q\\Qa\\E\\\\E\\Qb\\Qc\\E\\\\E\\Q\\E", Pattern.quote("\\Qa\\Eb\\Qc\\E"));
        Method method = Pattern.class.getMethod("matches", String.class, CharSequence.class);
        assertTrue((Boolean) method.invoke(null, Pattern.quote("\\"), "\\"));
    }

    public void testCompile() throws Exception {
        Pattern patternA = Pattern.compile("a\\p{javaJavaIdentifierStart}c");
        assertTrue(patternA.matcher("abc").matches());
        assertFalse(patternA.matcher("a?c").matches());
        Pattern patternB = Pattern.compile("a\\wc", Pattern.LITERAL);
        assertTrue(patternB.matcher("a\\wc").matches());
        assertFalse(patternB.matcher("abc").matches());
    }

    public void testMatches() throws Exception {
        assertTrue(Pattern.matches("a\\p{javaJavaIdentifierPart}c", "a2c"));
        assertFalse(Pattern.matches("a\\p{javaJavaIdentifierPart}c", "a;c"));
        Field field = _Pattern.class.getDeclaredField("REPLACEMENTS");
        field.setAccessible(true);
        for (String[] replacement : (String[][]) field.get(null)) {
            assertEquals(replacement[0], count(replacement[0]), count(replacement[1]));
        }
    }

    public void testMatchesQuoted() throws Exception {
        assertTrue(Pattern.matches("a", "a"));
        assertTrue(Pattern.matches("\\Q\\E", ""));
        assertTrue(Pattern.matches("\\\\Q\\\\E", "\\Q\\E"));
        assertTrue(Pattern.matches("\\\\\\Q\\\\E", "\\\\"));
        assertTrue(Pattern.matches("\\\\\\\\Q\\\\E", "\\\\Q\\E"));
        assertTrue(Pattern.matches("\\\\E", "\\E"));
        assertTrue(Pattern.matches("\\Qa\\E", "a"));
        assertTrue(Pattern.matches("\\Qa\\\\E", "a\\"));
        assertTrue(Pattern.matches("a\\Qb\\\\Ec\\\\Ed\\Q\\\\\\Ee", "ab\\c\\Ed\\\\e"));
    }

    private static int count(String regex) {
        int result = 0;
        for (int i = 0; i <= Character.MAX_VALUE; i++) {
            if (Pattern.matches(regex, String.valueOf((char) i))) {
                result++;
            }
        }
        return result;
    }

}