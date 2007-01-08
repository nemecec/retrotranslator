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
package net.sf.retrotranslator.runtime.java.net;

import java.net.SocketAddress;

/**
 * @author Taras Puchko
 */
public class Proxy_ {

    private final Type type;
    private final SocketAddress address;

    public enum Type {
        DIRECT, HTTP, SOCKS
    }

    public static final Proxy_ NO_PROXY = new Proxy_();

    private Proxy_() {
        this.type = Type.DIRECT;
        this.address = null;
    }

    public Proxy_(Type type, SocketAddress address) {
        this.type = type;
        this.address = address;
    }

    public Type type() {
        return type;
    }

    public SocketAddress address() {
        return address;
    }

    public String toString() {
        return type == Type.DIRECT ? Type.DIRECT.toString() : type() + " @ " + address();
    }

    public final boolean equals(Object obj) {
        if (obj instanceof Proxy_) {
            Proxy_ proxy = (Proxy_) obj;
            if (type() == proxy.type()) {
                return address() == null ? proxy.address() == null : address().equals(proxy.address());
            }
        }
        return false;
    }

    public final int hashCode() {
        return address() == null ? type().hashCode() : type().hashCode() + address().hashCode();
    }
}
