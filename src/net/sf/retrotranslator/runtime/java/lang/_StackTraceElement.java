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

import java.io.*;
import static java.io.ObjectStreamConstants.*;

/**
 * @author Taras Puchko
 */
public class _StackTraceElement {

    public static StackTraceElement createNewInstance(
            String declaringClass, String methodName, String fileName, int lineNumber) {
        if (declaringClass == null) {
            throw new NullPointerException("Declaring class is null");
        }
        if (methodName == null) {
            throw new NullPointerException("Method name is null");
        }
        try {
            ObjectStreamClass streamClass = ObjectStreamClass.lookup(StackTraceElement.class);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            DataOutputStream stream = new DataOutputStream(outputStream);
            stream.writeShort(STREAM_MAGIC);
            stream.writeShort(STREAM_VERSION);
            stream.writeByte(TC_OBJECT);
            stream.writeByte(TC_CLASSDESC);
            stream.writeUTF(streamClass.forClass().getName());
            stream.writeLong(streamClass.getSerialVersionUID());
            stream.writeByte(SC_SERIALIZABLE);
            stream.writeShort(4);
            stream.writeByte('I');
            stream.writeUTF("lineNumber");
            stream.writeByte('L');
            stream.writeUTF("declaringClass");
            stream.writeByte(TC_STRING);
            stream.writeUTF("Ljava/lang/String;");
            stream.writeByte('L');
            stream.writeUTF("fileName");
            stream.writeByte(TC_REFERENCE);
            stream.writeInt(baseWireHandle + 1);
            stream.writeByte('L');
            stream.writeUTF("methodName");
            stream.writeByte(TC_REFERENCE);
            stream.writeInt(baseWireHandle + 1);
            stream.writeByte(TC_ENDBLOCKDATA);
            stream.writeByte(TC_NULL);
            stream.writeInt(lineNumber);
            stream.writeByte(TC_STRING);
            stream.writeUTF(declaringClass);
            if (fileName == null) {
                stream.writeByte(TC_NULL);
            } else {
                stream.writeByte(TC_STRING);
                stream.writeUTF(fileName);
            }
            stream.writeByte(TC_STRING);
            stream.writeUTF(methodName);
            stream.close();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            return (StackTraceElement) new ObjectInputStream(inputStream).readObject();
        } catch (IOException e) {
            throw new Error(e);
        } catch (ClassNotFoundException e) {
            throw new Error(e);
        }
    }

}
