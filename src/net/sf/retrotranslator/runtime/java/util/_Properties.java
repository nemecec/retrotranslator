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
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;
import org.xml.sax.*;
import org.w3c.dom.*;
import net.sf.retrotranslator.runtime.impl.StrictErrorHandler;

/**
 * @author Taras Puchko
 */
public class _Properties {

    private static final String SYSTEM_ID = "http://java.sun.com/dtd/properties.dtd";

    private static final String DTD_CONTENT = "<?xml version='1.0' encoding='UTF-8'?>" +
            "<!ELEMENT properties ( comment?, entry* ) >" +
            "<!ATTLIST properties version CDATA #FIXED '1.0'>" +
            "<!ELEMENT comment (#PCDATA) >" +
            "<!ELEMENT entry (#PCDATA) >" +
            "<!ATTLIST entry key CDATA #REQUIRED>";


    public static void loadFromXML(Properties properties, InputStream stream) throws IOException {
        if (stream == null) {
            throw new NullPointerException();
        }
        NodeList entryList = parseStream(stream).getDocumentElement().getElementsByTagName("entry");
        int length = entryList.getLength();
        for (int i = 0; i < length; i++) {
            Element entry = (Element) entryList.item(i);
            String key = entry.getAttribute("key");
            Node textNode = entry.getFirstChild();
            String value = textNode == null ? "" : textNode.getNodeValue();
            properties.setProperty(key, value);
        }
        stream.close();
    }

    public static void storeToXML(Properties properties, OutputStream stream, String comment) throws IOException {
        storeToXML(properties, stream, comment, "UTF-8");
    }

    public static void storeToXML(Properties properties, OutputStream stream, String comment, String encoding)
            throws IOException {
        if (stream == null) {
            throw new NullPointerException();
        }
        Document document = createDocument();
        Element propertiesElement = document.createElement("properties");
        document.appendChild(propertiesElement);
        if (comment != null) {
            Element commentElement = document.createElement("comment");
            propertiesElement.appendChild(commentElement);
            commentElement.appendChild(document.createTextNode(comment));
        }
        for (Object keyObject : properties.keySet()) {
            String key = (String) keyObject;
            String value = properties.getProperty(key);
            Element entryElement = document.createElement("entry");
            propertiesElement.appendChild(entryElement);
            entryElement.setAttribute("key", key);
            entryElement.appendChild(document.createTextNode(value));
        }
        Transformer transformer = createTransformer();
        transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, SYSTEM_ID);
        transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.STANDALONE, "no");
        try {
            transformer.transform(new DOMSource(document), new StreamResult(stream));
        } catch (TransformerException e) {
            IOException exception = new IOException(e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    private static Document parseStream(InputStream stream) throws IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setCoalescing(true);
        factory.setValidating(true);
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new Error(e);
        }
        builder.setEntityResolver(new EntityResolver() {
            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                if (systemId.equals(SYSTEM_ID)) {
                    InputSource source = new InputSource();
                    source.setSystemId(SYSTEM_ID);
                    source.setCharacterStream(new StringReader(DTD_CONTENT));
                    return source;
                }
                return null;
            }
        });
        builder.setErrorHandler(new StrictErrorHandler());
        try {
            return builder.parse(stream);
        } catch (SAXException e) {
            throw new InvalidPropertiesFormatException(e);
        }
    }

    private static Document createDocument() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new Error(e);
        }
    }

    private static Transformer createTransformer() {
        try {
            return TransformerFactory.newInstance().newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new Error(e);
        }
    }

}
