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
package net.sf.retrotranslator.runtime.java.lang;

import junit.framework.TestCase;

/**
 * @author Taras Puchko
 */
public class _StackTraceElementTestCase extends TestCase {

    class Holder {
        public StackTraceElement element;

        public Holder(StackTraceElement element) {
            this.element = element;
        }
    }

    class Creator {
        public StackTraceElement element;

        public Creator(String declaringClass, String methodName, String fileName, int lineNumber) {
            this.element = new StackTraceElement(declaringClass, methodName, fileName, lineNumber);
        }
    }

    class CreatingHolder extends Holder {
        public CreatingHolder(String declaringClass, String methodName, String fileName, int lineNumber) {
            super(new StackTraceElement(declaringClass, methodName, fileName, lineNumber));
        }
    }

    public void testCreateNewInstance() throws Exception {
        assertTrace(new StackTraceElement("mypackage.MyClass", "myMethod", "MyFile.java", 123));
        assertTrace(new Holder(new StackTraceElement("mypackage.MyClass", "myMethod", "MyFile.java", 123)).element);
        assertTrace(new Creator("mypackage.MyClass", "myMethod", "MyFile.java", 123).element);
        assertTrace(new CreatingHolder("mypackage.MyClass", "myMethod", "MyFile.java", 123).element);
    }

    private void assertTrace(StackTraceElement element) {
        assertEquals("mypackage.MyClass", element.getClassName());
        assertEquals("myMethod", element.getMethodName());
        assertEquals("MyFile.java", element.getFileName());
        assertEquals(123, element.getLineNumber());
    }

    public void testCreateNewInstance_SourceUnknown() throws Exception {
        StackTraceElement element = new StackTraceElement("SomeClass", "someMethod", null, 345);
        assertEquals("SomeClass", element.getClassName());
        assertEquals("someMethod", element.getMethodName());
        assertNull(element.getFileName());
        assertEquals(345, element.getLineNumber());
    }

    public void testCreateNewInstance_NPE() throws Exception {
        try {
            StackTraceElement element = new StackTraceElement(null, "someMethod", "somefile", 345);
            fail(element.toString());
        } catch (NullPointerException e) {
            //ok
        }
        try {
            StackTraceElement element = new StackTraceElement("SomeClass", null, "somefile", 345);
            fail(element.toString());
        } catch (NullPointerException e) {
            //ok
        }
    }
}