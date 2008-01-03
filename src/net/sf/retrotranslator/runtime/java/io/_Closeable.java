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
package net.sf.retrotranslator.runtime.java.io;

import java.io.*;
import java.nio.channels.Channel;

/**
 * @author Taras Puchko
 */
public class _Closeable {

    public static boolean executeInstanceOfInstruction(Object object) {
        return object instanceof InputStream ||
                object instanceof OutputStream ||
                object instanceof Reader ||
                object instanceof Writer ||
                object instanceof RandomAccessFile ||
                object instanceof Channel ||
                object instanceof Closeable_;
    }

    public static Object executeCheckCastInstruction(Object object) {
        if (object instanceof InputStream) {
            return (InputStream) object;
        }
        if (object instanceof OutputStream) {
            return (OutputStream) object;
        }
        if (object instanceof Reader) {
            return (Reader) object;
        }
        if (object instanceof Writer) {
            return (Writer) object;
        }
        if (object instanceof RandomAccessFile) {
            return (RandomAccessFile) object;
        }
        if (object instanceof Channel) {
            return (Channel) object;
        }
        return (Closeable_) object;
    }

    public static void close(Object object) throws IOException {
        if (object instanceof InputStream) {
            ((InputStream) object).close();
        } else if (object instanceof OutputStream) {
            ((OutputStream) object).close();
        } else if (object instanceof Reader) {
            ((Reader) object).close();
        } else if (object instanceof Writer) {
            ((Writer) object).close();
        } else if (object instanceof RandomAccessFile) {
            ((RandomAccessFile) object).close();
        } else if (object instanceof Channel) {
            ((Channel) object).close();
        } else {
            ((Closeable_) object).close();
        }
    }

}
