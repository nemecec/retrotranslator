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
package net.sf.retrotranslator.runtime.java.lang;

import junit.framework.TestCase;

/**
 * @author Taras Puchko
 */
public class _BooleanTestCase extends TestCase {

    public void testValueOf() throws Exception {
        assertSame(Boolean.TRUE, Boolean.valueOf(true));
        assertSame(Boolean.FALSE, Boolean.valueOf(false));
    }

    public void testParseBoolean() throws Exception {
        assertTrue(Boolean.parseBoolean("True"));
        assertTrue(Boolean.parseBoolean("true"));
        assertFalse(Boolean.parseBoolean("False"));
        assertFalse(Boolean.parseBoolean("false"));
        assertFalse(Boolean.parseBoolean("Yes"));
        assertFalse(Boolean.parseBoolean("No"));
    }

    public void testCompareTo() throws Exception {
        assertEquals(1, Boolean.TRUE.compareTo(Boolean.FALSE));
        assertEquals(0, Boolean.TRUE.compareTo(Boolean.TRUE));
        assertEquals(-1, Boolean.FALSE.compareTo(Boolean.TRUE));
        assertEquals(0, Boolean.FALSE.compareTo(Boolean.FALSE));
    }

}