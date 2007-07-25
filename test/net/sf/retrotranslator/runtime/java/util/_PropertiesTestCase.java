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
package net.sf.retrotranslator.runtime.java.util;

import java.io.*;
import java.util.*;
import junit.framework.TestCase;

/**
 * @author Taras Puchko
 */
public class _PropertiesTestCase extends TestCase {

    public void testLoadFromXML() throws Exception {
        String xml = "<?xml version='1.0' encoding='UTF-8' standalone='no'?>\n" +
                "<!DOCTYPE properties SYSTEM 'http://java.sun.com/dtd/properties.dtd'>\n" +
                "<properties>\n" +
                "<comment>test</comment>\n" +
                "<entry key='keyB'>valueB</entry>\n" +
                "<entry key='keyA'>valueA</entry>\n" +
                "</properties>";
        Properties properties = new Properties();
        properties.loadFromXML(new ByteArrayInputStream(xml.getBytes("UTF-8")));
        assertEquals(2, properties.size());
        assertEquals("valueA", properties.getProperty("keyA"));
        assertEquals("valueB", properties.getProperty("keyB"));
    }

    public void testLoadFromXML_Ok() throws Exception {
        String xml = "<?xml version='1.0'?>\n" +
                "<!DOCTYPE properties SYSTEM 'http://java.sun.com/dtd/properties.dtd'>\n" +
                "<properties version='1.0'>\n" +
                "<entry key='keyA'>valueA</entry>\n" +
                "<entry key='keyB'>valueB</entry>\n" +
                "</properties>";
        Properties properties = new Properties();
        properties.loadFromXML(new ByteArrayInputStream(xml.getBytes("UTF-8")));
        assertEquals(2, properties.size());
        assertEquals("valueA", properties.getProperty("keyA"));
        assertEquals("valueB", properties.getProperty("keyB"));
    }

    public void testLoadFromXML_ErrorVersion() throws Exception {
        String xml = "<?xml version='1.0' encoding='UTF-8' standalone='no'?>\n" +
                "<!DOCTYPE properties SYSTEM 'http://java.sun.com/dtd/properties.dtd'>\n" +
                "<properties version='1.1'>\n" +
                "<comment>test</comment>\n" +
                "<entry key='keyB'>valueB</entry>\n" +
                "<entry key='keyA'>valueA</entry>\n" +
                "</properties>";
        try {
            new Properties().loadFromXML(new ByteArrayInputStream(xml.getBytes("UTF-8")));
            fail();
        } catch (InvalidPropertiesFormatException e) {
            //ok
        }
    }

    public void testLoadFromXML_ErrorXML() throws Exception {
        String xml = "<?xml version='1.0' encoding='UTF-8' standalone='no'?>\n" +
                "<!DOCTYPE properties SYSTEM 'http://java.sun.com/dtd/properties.dtd'>\n" +
                "<properties>\n" +
                "<comment>test</comment>\n" +
                "<item key='keyB'>valueB</item>\n" +
                "<entry key='keyA'>valueA</entry>\n" +
                "</properties>";
        try {
            new Properties().loadFromXML(new ByteArrayInputStream(xml.getBytes("UTF-8")));
            fail();
        } catch (InvalidPropertiesFormatException e) {
            //ok
        }
    }

    public void testStoreToXML() throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Properties properties = new Properties();
        properties.setProperty("keyA", "valueA");
        properties.setProperty("keyB", "valueB");
        properties.storeToXML(stream, "test");
        byte[] bytes = stream.toByteArray();
        String content = new String(bytes, "UTF-8");
        assertTrue(content.contains("UTF-8"));
        assertTrue(content.contains("comment"));
        assertTrue(content.contains("test"));
        properties.clear();
        assertTrue(properties.isEmpty());
        properties.loadFromXML(new ByteArrayInputStream(bytes));
        assertEquals(2, properties.size());
        assertEquals("valueA", properties.getProperty("keyA"));
        assertEquals("valueB", properties.getProperty("keyB"));
    }

    public void testStoreToXML_ASCII() throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Properties properties = new Properties();
        properties.setProperty("keyA", "valueA");
        properties.setProperty("keyB", "valueB");
        properties.storeToXML(stream, null, "US-ASCII");
        byte[] bytes = stream.toByteArray();
        String content = new String(bytes, "US-ASCII");
        assertTrue(content.contains("US-ASCII"));
        assertFalse(content.contains("comment"));
        properties.clear();
        assertTrue(properties.isEmpty());
        properties.loadFromXML(new ByteArrayInputStream(bytes));
        assertEquals(2, properties.size());
        assertEquals("valueA", properties.getProperty("keyA"));
        assertEquals("valueB", properties.getProperty("keyB"));
    }

}