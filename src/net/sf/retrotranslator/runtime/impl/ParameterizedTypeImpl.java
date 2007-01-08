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
package net.sf.retrotranslator.runtime.impl;

import java.lang.reflect.*;
import java.util.Arrays;

/**
 * @author Taras Puchko
 */
public class ParameterizedTypeImpl implements ParameterizedType {

    private Type[] actualTypeArguments;
    private Class rawType;
    private Type ownerType;

    public ParameterizedTypeImpl(Type[] actualTypeArguments, Class rawType, Type ownerType) {
        this.actualTypeArguments = actualTypeArguments;
        this.rawType = rawType;
        this.ownerType = ownerType;
    }

    public Type[] getActualTypeArguments() {
        return actualTypeArguments.clone();
    }

    public Type getRawType() {
        return rawType;
    }

    public Type getOwnerType() {
        return ownerType;
    }

    public int hashCode() {
        return Arrays.hashCode(actualTypeArguments) ^ rawType.hashCode() ^
                (ownerType == null ? 0 : ownerType.hashCode());
    }

    public boolean equals(Object obj) {
        if (obj instanceof ParameterizedType) {
            if (obj == this) return true;
            ParameterizedType type = ((ParameterizedType) obj);
            return Arrays.equals(actualTypeArguments, type.getActualTypeArguments()) &&
                    rawType.equals(type.getRawType()) &&
                    (ownerType == null ? type.getOwnerType() == null : ownerType.equals(type.getOwnerType()));
        }
        return false;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (ownerType != null) {
            builder.append(RuntimeTools.getString(ownerType)).append('.');
        }
        String rawName = rawType.getName();
        if (ownerType instanceof ParameterizedTypeImpl) {
            rawName = rawName.substring(((ParameterizedTypeImpl) ownerType).rawType.getName().length() + 1);
        }
        builder.append(rawName);
        if (actualTypeArguments.length > 0) {
            builder.append('<');
            for (int i = 0; i < actualTypeArguments.length; i++) {
                if (i > 0) builder.append(", ");
                builder.append(RuntimeTools.getString(actualTypeArguments[i]));
            }
            builder.append('>');
        }
        return builder.toString();
    }

}
