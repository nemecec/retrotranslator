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

import junit.framework.TestCase;

/**
 * @author Taras Puchko
 */
public class StackTraceElement_TestCase extends TestCase {

    private StackTraceElement element1 = new StackTraceElement("theClass", "theMethod", "theFile", 123);
    private StackTraceElement element2 = new StackTraceElement("theClass", "theMethod", "theFile", 123);
    private StackTraceElement element3 = new StackTraceElement("otherClass", "otherMethod", "otherFile", -2);

    public void testGetClassName() throws Exception {
        assertEquals("theClass", element1.getClassName());
    }

    public void testGetFileName() throws Exception {
        assertEquals("theFile", element1.getFileName());
    }

    public void testGetLineNumber() throws Exception {
        assertEquals(123, element1.getLineNumber());
    }

    public void testGetMethodName() throws Exception {
        assertEquals("theMethod", element1.getMethodName());
    }

    public void testIsNativeMethod() throws Exception {
        assertFalse(element1.isNativeMethod());
        assertTrue(element3.isNativeMethod());
    }

    public void testToString() throws Exception {
        assertFalse(element1.toString().equals(element3.toString()));
    }

    public void testEquals() throws Exception {
        assertEquals(element1, element2);
        assertFalse(element1.equals(element3));
    }

    public void testHashCode() throws Exception {
        assertEquals(element1.hashCode(), element2.hashCode());
    }

}