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

import java.io.Serializable;

/**
 * @author Taras Puchko
 */
public final class StackTraceElement_ implements Serializable {

    private static final String NATIVE_METHOD = "Native Method";
    private static final String UNKNOWN_SOURCE = "Unknown Source";

    private String declaringClass;
    private String methodName;
    private String fileName;
    private int lineNumber;

    public StackTraceElement_(String declaringClass, String methodName, String fileName, int lineNumber) {
        if (declaringClass == null || methodName == null) {
            throw new NullPointerException();
        }
        this.declaringClass = declaringClass;
        this.methodName = methodName;
        this.fileName = fileName;
        this.lineNumber = lineNumber;
    }

    public String getClassName() {
        return declaringClass;
    }

    public String getFileName() {
        return fileName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getMethodName() {
        return methodName;
    }

    public boolean isNativeMethod() {
        return lineNumber == -2;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder(declaringClass).append('.').append(methodName).append('(');
        if (fileName != null) {
            builder.append(fileName);
            if (lineNumber >= 0) {
                builder.append(':').append(lineNumber);
            }
        } else {
            builder.append(isNativeMethod() ? NATIVE_METHOD : UNKNOWN_SOURCE);
        }
        return builder.append(')').toString();
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof StackTraceElement_)) {
            return false;
        }
        StackTraceElement_ element = (StackTraceElement_) obj;
        return equal(declaringClass, element.declaringClass) &&
                equal(methodName, element.methodName) &&
                equal(fileName, element.fileName) &&
                lineNumber == element.lineNumber;
    }


    public int hashCode() {
        return ((hash(declaringClass) * 31 + hash(methodName)) * 31 + hash(fileName)) * 31 + lineNumber;
    }

    private static boolean equal(Object a, Object b) {
        return a == b || a != null && a.equals(b);
    }

    private static int hash(Object o) {
        return o != null ? o.hashCode() : 0;
    }

    static StackTraceElement_ valueOf(String s) {
        int openIndex = s.lastIndexOf('(');
        if (openIndex < 0 || !s.endsWith(")")) {
            return null;
        }
        int dotIndex = s.lastIndexOf('.', openIndex);
        if (dotIndex < 0) {
            return null;
        }
        String declaringClass = s.substring(0, dotIndex);
        String methodName = s.substring(dotIndex + 1, openIndex);
        String position = s.substring(openIndex + 1, s.length() - 1);
        String fileName = null;
        int lineNumber = -1;
        if (position.equals(NATIVE_METHOD)) {
            lineNumber = -2;
        } else if (!position.equals(UNKNOWN_SOURCE)) {
            int colonIndex = position.lastIndexOf(':');
            if (colonIndex < 0) {
                fileName = position;
            } else {
                try {
                    lineNumber = Integer.parseInt(position.substring(colonIndex + 1));
                    fileName = position.substring(0, colonIndex);
                } catch (NumberFormatException e) {
                    fileName = position;
                }
            }
        }
        return new StackTraceElement_(declaringClass, methodName, fileName, lineNumber);
    }

}
