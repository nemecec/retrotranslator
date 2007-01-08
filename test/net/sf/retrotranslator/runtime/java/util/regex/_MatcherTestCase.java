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
package net.sf.retrotranslator.runtime.java.util.regex;

import java.util.regex.*;
import junit.framework.TestCase;

/**
 * @author Taras Puchko
 */
public class _MatcherTestCase extends TestCase {

    public void testQuoteReplacement() throws Exception {
        assertEquals("a\\$b", Matcher.quoteReplacement("a$b"));
        assertEquals("a\\\\b", Matcher.quoteReplacement("a\\b"));
        assertSame("ab", Matcher.quoteReplacement("ab"));
    }

    public void testToMatchResult() throws Exception {
        Matcher matcher = Pattern.compile("a(b.?)c").matcher("XabcYab1cZ");
        assertTrue(matcher.find());
        checkFirst(matcher);
        MatchResult first = matcher.toMatchResult();
        assertTrue(matcher.find());
        checkSecond(matcher);
        MatchResult second = matcher.toMatchResult();
        assertFalse(matcher.find());
        MatchResult illegal = matcher.toMatchResult();
        checkIllegal(illegal);
        checkFirst(first);
        checkSecond(second);
    }

    private void checkFirst(MatchResult first) {
        assertEquals(1, first.groupCount());

        assertEquals(1, first.start());
        assertEquals(1, first.start(0));
        assertEquals(2, first.start(1));
        try {
            first.start(2);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        assertEquals(4, first.end());
        assertEquals(4, first.end(0));
        assertEquals(3, first.end(1));
        try {
            first.end(2);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        assertEquals("abc", first.group());
        assertEquals("abc", first.group(0));
        assertEquals("b", first.group(1));
        try {
            first.group(2);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
    }

    private void checkSecond(MatchResult first) {
        assertEquals(1, first.groupCount());

        assertEquals(5, first.start());
        assertEquals(5, first.start(0));
        assertEquals(6, first.start(1));
        try {
            first.start(2);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        assertEquals(9, first.end());
        assertEquals(9, first.end(0));
        assertEquals(8, first.end(1));
        try {
            first.end(2);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
        assertEquals("ab1c", first.group());
        assertEquals("ab1c", first.group(0));
        assertEquals("b1", first.group(1));
        try {
            first.group(2);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //ok
        }
    }

    private void checkIllegal(MatchResult illegal) {
        assertEquals(1, illegal.groupCount());
        for (int i = 0; i < 10; i++) {
            try {
                illegal.start(i);
                fail();
            } catch (IllegalStateException e) {
                //ok
            }
            try {
                illegal.end(i);
                fail();
            } catch (IllegalStateException e) {
                //ok
            }
            try {
                illegal.group(i);
                fail();
            } catch (IllegalStateException e) {
                //ok
            }
        }
    }

}