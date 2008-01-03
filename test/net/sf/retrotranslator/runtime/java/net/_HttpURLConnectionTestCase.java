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

import java.net.*;
import junit.framework.TestCase;

/**
 * @author Taras Puchko
 */
public class _HttpURLConnectionTestCase extends TestCase {

    public void testGetConnectTimeout() throws Exception {
        MockHttpURLConnection mockHttpURLConnection = new MockHttpURLConnection();
        mockHttpURLConnection.setConnectTimeout(1234);
        HttpURLConnection httpURLConnection = mockHttpURLConnection.toHttpURLConnection();
        assertEquals(1234, httpURLConnection.getConnectTimeout());
    }

    public void testGetConnectTimeout_2() throws Exception {
        HttpURLConnection httpURLConnection = new DummyHttpURLConnection().toHttpURLConnection();
        assertEquals(0, httpURLConnection.getConnectTimeout());
    }

    public void testGetConnectTimeout_3() throws Exception {
        HttpURLConnection httpURLConnection =
                new MockHttpURLConnection(new IllegalStateException()).toHttpURLConnection();
        try {
            httpURLConnection.getConnectTimeout();
            fail();
        } catch (IllegalStateException e) {
            //ok
        }
    }

    public void testGetReadTimeout() throws Exception {
        MockHttpURLConnection mockHttpURLConnection = new MockHttpURLConnection();
        mockHttpURLConnection.setReadTimeout(2345);
        HttpURLConnection httpURLConnection = mockHttpURLConnection.toHttpURLConnection();
        assertEquals(2345, httpURLConnection.getReadTimeout());
    }

    public void testGetReadTimeout_2() throws Exception {
        HttpURLConnection httpURLConnection = new DummyHttpURLConnection().toHttpURLConnection();
        assertEquals(0, httpURLConnection.getReadTimeout());
    }

    public void testGetReadTimeout_3() throws Exception {
        HttpURLConnection httpURLConnection =
                new MockHttpURLConnection(new IllegalStateException()).toHttpURLConnection();
        try {
            httpURLConnection.getReadTimeout();
            fail();
        } catch (IllegalStateException e) {
            //ok
        }
    }

    public void testSetChunkedStreamingMode() throws Exception {
        MockHttpURLConnection mockHttpURLConnection = new MockHttpURLConnection();
        HttpURLConnection httpURLConnection = mockHttpURLConnection.toHttpURLConnection();
        httpURLConnection.setChunkedStreamingMode(3456);
        assertEquals(3456, mockHttpURLConnection.getChunkedStreamingModeLength());
    }

    public void testSetChunkedStreamingMode_2() throws Exception {
        HttpURLConnection httpURLConnection = new DummyHttpURLConnection().toHttpURLConnection();
        httpURLConnection.setChunkedStreamingMode(12345);
    }

    public void testSetChunkedStreamingMode_3() throws Exception {
        HttpURLConnection httpURLConnection =
                new MockHttpURLConnection(new IllegalStateException()).toHttpURLConnection();
        try {
            httpURLConnection.setChunkedStreamingMode(23456);
            fail();
        } catch (IllegalStateException e) {
            //ok
        }
    }

    public void testSetConnectTimeout() throws Exception {
        MockHttpURLConnection mockHttpURLConnection = new MockHttpURLConnection();
        HttpURLConnection httpURLConnection = mockHttpURLConnection.toHttpURLConnection();
        httpURLConnection.setConnectTimeout(4567);
        assertEquals(4567, mockHttpURLConnection.getConnectTimeout());
    }

    public void testSetConnectTimeout_2() throws Exception {
        HttpURLConnection httpURLConnection = new DummyHttpURLConnection().toHttpURLConnection();
        httpURLConnection.setConnectTimeout(12345);
    }

    public void testSetConnectTimeout_3() throws Exception {
        HttpURLConnection httpURLConnection =
                new MockHttpURLConnection(new IllegalStateException()).toHttpURLConnection();
        try {
            httpURLConnection.setConnectTimeout(34567);
            fail();
        } catch (IllegalStateException e) {
            //ok
        }
    }

    public void testSetFixedLengthStreamingMode() throws Exception {
        MockHttpURLConnection mockHttpURLConnection = new MockHttpURLConnection();
        HttpURLConnection httpURLConnection = mockHttpURLConnection.toHttpURLConnection();
        httpURLConnection.setFixedLengthStreamingMode(5678);
        assertEquals(5678, mockHttpURLConnection.getFixedLengthStreamingModeLength());
    }

    public void testSetFixedLengthStreamingMode_2() throws Exception {
        DummyHttpURLConnection dummyHttpURLConnection = new DummyHttpURLConnection();
        HttpURLConnection httpURLConnection = dummyHttpURLConnection.toHttpURLConnection();
        httpURLConnection.setFixedLengthStreamingMode(67890);
        try {
            httpURLConnection.setChunkedStreamingMode(12345);
            // Java 1.4
            assertEquals("67890", dummyHttpURLConnection.getPropertyMap().get("Content-Length"));
        } catch (IllegalStateException e) {
            // Java 5
        }
    }

    public void testSetFixedLengthStreamingMode_3() throws Exception {
        HttpURLConnection httpURLConnection =
                new MockHttpURLConnection(new IllegalStateException()).toHttpURLConnection();
        try {
            httpURLConnection.setFixedLengthStreamingMode(45678);
            fail();
        } catch (IllegalStateException e) {
            //ok
        }
    }

    public void testSetReadTimeout() throws Exception {
        MockHttpURLConnection mockHttpURLConnection = new MockHttpURLConnection();
        HttpURLConnection httpURLConnection = mockHttpURLConnection.toHttpURLConnection();
        httpURLConnection.setReadTimeout(6789);
        assertEquals(6789, mockHttpURLConnection.getReadTimeout());
    }

    public void testSetReadTimeout_2() throws Exception {
        HttpURLConnection httpURLConnection = new DummyHttpURLConnection().toHttpURLConnection();
        httpURLConnection.setReadTimeout(67890);
    }

    public void testSetReadTimeout_3() throws Exception {
        HttpURLConnection httpURLConnection =
                new MockHttpURLConnection(new IllegalStateException()).toHttpURLConnection();
        try {
            httpURLConnection.setReadTimeout(56789);
            fail();
        } catch (IllegalStateException e) {
            //ok
        }
    }

}