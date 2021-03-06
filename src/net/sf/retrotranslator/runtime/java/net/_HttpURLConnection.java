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

import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import net.sf.retrotranslator.registry.Advanced;
import net.sf.retrotranslator.runtime.impl.RuntimeTools;

/**
 * @author Taras Puchko
 */
public class _HttpURLConnection {

    @Advanced("URLConnection.getConnectTimeout")
    public static int getConnectTimeout(HttpURLConnection httpURLConnection) {
        return _URLConnection.getConnectTimeout(httpURLConnection);
    }

    @Advanced("URLConnection.getReadTimeout")
    public static int getReadTimeout(HttpURLConnection httpURLConnection) {
        return _URLConnection.getReadTimeout(httpURLConnection);
    }

    @Advanced("HttpURLConnection.setChunkedStreamingMode")
    public static void setChunkedStreamingMode(HttpURLConnection httpURLConnection, int chunklen) {
        try {
            RuntimeTools.invokeMethod(httpURLConnection,
                    "setChunkedStreamingMode", new Class[]{int.class}, new Object[]{chunklen});
        } catch (NoSuchMethodException e) {
            // ignore
        } catch (InvocationTargetException e) {
            throw RuntimeTools.unwrap(e);
        }
    }

    @Advanced("URLConnection.setConnectTimeout")
    public static void setConnectTimeout(HttpURLConnection httpURLConnection, int timeout) {
        _URLConnection.setConnectTimeout(httpURLConnection, timeout);
    }

    @Advanced("HttpURLConnection.setFixedLengthStreamingMode")
    public static void setFixedLengthStreamingMode(HttpURLConnection httpURLConnection, int contentLength) {
        try {
            RuntimeTools.invokeMethod(httpURLConnection,
                    "setFixedLengthStreamingMode", new Class[]{int.class}, new Object[]{contentLength});
        } catch (NoSuchMethodException e) {
            httpURLConnection.setRequestProperty("Content-Length", Integer.toString(contentLength));
        } catch (InvocationTargetException e) {
            throw RuntimeTools.unwrap(e);
        }
    }

    @Advanced("URLConnection.setReadTimeout")
    public static void setReadTimeout(HttpURLConnection httpURLConnection, int timeout) {
        _URLConnection.setReadTimeout(httpURLConnection, timeout);
    }

}
