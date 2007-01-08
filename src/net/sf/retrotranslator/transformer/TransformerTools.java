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
package net.sf.retrotranslator.transformer;

import net.sf.retrotranslator.runtime.asm.Type;

/**
 * @author Taras Puchko
 */
class TransformerTools {

    public static String descriptor(Class returnType, Class... parameterTypes) {
        Type[] argumentTypes = new Type[parameterTypes.length];
        for (int i = 0; i < argumentTypes.length; i++) {
            argumentTypes[i] = Type.getType(parameterTypes[i]);
        }
        return Type.getMethodDescriptor(Type.getType(returnType), argumentTypes);
    }

    public static Type getTypeByInternalName(String name) {
        return Type.getType('L' + name + ';');
    }

    public static Type getArrayTypeByInternalName(String name, int dimensions) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < dimensions; i++) {
            builder.append('[');
        }
        return Type.getType(builder.append('L').append(name).append(';').toString());
    }

    public static boolean isClassFile(byte[] bytes) {
        return bytes.length >= 4 &&
                bytes[0] == ((byte) 0xCA) &&
                bytes[1] == ((byte) 0xFE) &&
                bytes[2] == ((byte) 0xBA) &&
                bytes[3] == ((byte) 0xBE);
    }

    public static int getClassVersion(byte[] bytes, int offset) {
        return get(bytes, offset, 4, 24) | get(bytes, offset, 5, 16) |
                get(bytes, offset, 6, 8) | get(bytes, offset, 7, 0);
    }

    private static int get(byte[] bytes, int offset, int index, int shift) {
        return (bytes[offset + index] & 0xFF) << shift;
    }

}
