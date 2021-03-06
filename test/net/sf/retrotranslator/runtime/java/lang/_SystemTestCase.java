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

import net.sf.retrotranslator.tests.TestCaseBase;

/**
 * @author Taras Puchko
 */
public class _SystemTestCase extends TestCaseBase {

    public void testClearProperty() throws Exception {
        String key = _SystemTestCase.class.getName();
        System.setProperty(key, "x");
        assertEquals("x", System.getProperty(key));
        System.clearProperty(key);
        assertNull(System.getProperty(key));
        try {
            System.clearProperty(null);
            fail();
        } catch (NullPointerException e) {
            //ok
        }
        try {
            System.clearProperty("");
            fail();
        } catch (IllegalArgumentException e) {
            //ok
        }
    }

    public void testGetProperty() throws Exception {
        String javaVersion = findProperty("java.version");
        String specVersion = findProperty("java.specification.version");
        String classVersion = findProperty("java.class.version");
        if (isJava5AtLeast()) {
            assertTrue(javaVersion.compareTo("1.5.0") >= 0);
            assertTrue(specVersion.compareTo("1.5") >= 0);
            assertTrue(classVersion.compareTo("49.0") >= 0);
        } else {
            assertEquals("1.5.0", javaVersion);
            assertEquals("1.5", specVersion);
            assertEquals("49.0", classVersion);
        }
        assertNull(System.getProperty("does not exist"));
        assertEquals("default value", System.getProperty("does not exist", "default value"));
    }

    private String findProperty(String key) {
        String s1 = System.getProperty(key);
        String s2 = System.getProperty(key, null);
        assertEquals(s1, s2);
        return s1;
    }

}