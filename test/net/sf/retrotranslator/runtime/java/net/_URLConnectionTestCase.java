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
package net.sf.retrotranslator.runtime.java.net;

import junit.framework.*;
import java.net.*;

/**
 * @author Taras Puchko
 */
public class _URLConnectionTestCase extends TestCase {

    public void testGetConnectTimeout() throws Exception {
        MockHttpURLConnection mockHttpURLConnection = new MockHttpURLConnection();
        mockHttpURLConnection.setConnectTimeout(12345);
        URLConnection urlConnection = mockHttpURLConnection.toURLConnection();
        assertEquals(12345, urlConnection.getConnectTimeout());
    }

    public void testGetConnectTimeout_2() throws Exception {
        URLConnection urlConnection = new DummyHttpURLConnection().toURLConnection();
        assertEquals(0, urlConnection.getConnectTimeout());
    }

    public void testGetConnectTimeout_3() throws Exception {
        URLConnection urlConnection = new MockHttpURLConnection(new IllegalArgumentException()).toURLConnection();
        try {
            urlConnection.getConnectTimeout();
            fail();
        } catch (IllegalArgumentException e) {
            //ok
        }
    }

    public void testGetReadTimeout() throws Exception {
        MockHttpURLConnection mockHttpURLConnection = new MockHttpURLConnection();
        mockHttpURLConnection.setReadTimeout(23456);
        URLConnection urlConnection = mockHttpURLConnection.toURLConnection();
        assertEquals(23456, urlConnection.getReadTimeout());
    }

    public void testGetReadTimeout_2() throws Exception {
        URLConnection urlConnection = new DummyHttpURLConnection().toURLConnection();
        assertEquals(0, urlConnection.getReadTimeout());
    }

    public void testGetReadTimeout_3() throws Exception {
        URLConnection urlConnection = new MockHttpURLConnection(new IllegalArgumentException()).toURLConnection();
        try {
            urlConnection.getReadTimeout();
            fail();
        } catch (IllegalArgumentException e) {
            //ok
        }
    }

    public void testSetConnectTimeout() throws Exception {
        MockHttpURLConnection mockHttpURLConnection = new MockHttpURLConnection();
        URLConnection urlConnection = mockHttpURLConnection.toURLConnection();
        urlConnection.setConnectTimeout(34567);
        assertEquals(34567, mockHttpURLConnection.getConnectTimeout());
    }

    public void testSetConnectTimeout_2() throws Exception {
        URLConnection urlConnection = new DummyHttpURLConnection().toURLConnection();
        urlConnection.setConnectTimeout(12345);
    }

    public void testSetConnectTimeout_3() throws Exception {
        URLConnection urlConnection = new MockHttpURLConnection(new IllegalArgumentException()).toURLConnection();
        try {
            urlConnection.setConnectTimeout(34567);
            fail();
        } catch (IllegalArgumentException e) {
            //ok
        }
    }

    public void testSetReadTimeout() throws Exception {
        MockHttpURLConnection mockHttpURLConnection = new MockHttpURLConnection();
        URLConnection urlConnection = mockHttpURLConnection.toURLConnection();
        urlConnection.setReadTimeout(45678);
        assertEquals(45678, mockHttpURLConnection.getReadTimeout());
    }

    public void testSetReadTimeout_2() throws Exception {
        URLConnection urlConnection = new DummyHttpURLConnection().toURLConnection();
        urlConnection.setReadTimeout(67890);
    }

    public void testSetReadTimeout_3() throws Exception {
        URLConnection urlConnection = new MockHttpURLConnection(new IllegalArgumentException()).toURLConnection();
        try {
            urlConnection.setReadTimeout(56789);
            fail();
        } catch (IllegalArgumentException e) {
            //ok
        }
    }

}