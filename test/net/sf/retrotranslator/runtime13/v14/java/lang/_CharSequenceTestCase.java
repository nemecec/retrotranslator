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
package net.sf.retrotranslator.runtime13.v14.java.lang;

import java.util.*;
import junit.framework.TestCase;

/**
 * @author Taras Puchko
 */
public class _CharSequenceTestCase extends TestCase {

    private List<Object> charSequenceList = Arrays.<Object>asList(
            "abc", new StringBuffer("abc"), new MyCharSequence("abc"));
    private Object integer = 123;

    private static class MyCharSequence implements CharSequence {

        private String s;

        public MyCharSequence(String s) {
            this.s = s;
        }

        public int length() {
            return s.length();
        }

        public char charAt(int index) {
            return s.charAt(index);
        }

        public CharSequence subSequence(int start, int end) {
            return s.substring(start, end);
        }
    }

    public void testExecuteInstanceOfInstruction() throws Exception {
        for (Object o : charSequenceList) {
            assertTrue(o instanceof CharSequence);
        }
        assertFalse(integer instanceof CharSequence);
    }

    public void testExecuteCheckCastInstruction() throws Exception {
        for (Object o : charSequenceList) {
            CharSequence charSequence = (CharSequence) o;
            assertNotNull(charSequence);
        }
        try {
            CharSequence charSequence = (CharSequence) integer;
            assertNotNull(charSequence);
            fail();
        } catch (ClassCastException e) {
            //ok
        }
    }

    public void testCharAt() throws Exception {
        for (Object o : charSequenceList) {
            assertEquals('b', ((CharSequence) o).charAt(1));
        }
    }

    public void testLength() throws Exception {
        for (Object o : charSequenceList) {
            assertEquals(3, ((CharSequence) o).length());
        }
    }

    public void testSubSequence() throws Exception {
        for (Object o : charSequenceList) {
            assertEquals("bc", ((CharSequence) o).subSequence(1, 3).toString());
        }
    }

}