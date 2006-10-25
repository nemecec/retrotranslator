/***
 * Retrotranslator: a Java bytecode transformer that translates Java classes
 * compiled with JDK 5.0 into classes that can be run on JVM 1.4.
 * 
 * Copyright (c) 2005, 2006 Taras Puchko
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

import net.sf.retrotranslator.runtime.asm.signature.SignatureVisitor;

/**
 * @author Taras Puchko
 */
abstract class SignatureAdapter implements SignatureVisitor {

    final private SignatureVisitor visitor;

    public SignatureAdapter(final SignatureVisitor visitor) {
        this.visitor = visitor;
    }

    protected SignatureVisitor visitStart(SignatureVisitor visitor) {
        return visitor;
    }

    public void visitFormalTypeParameter(String name) {
        visitor.visitFormalTypeParameter(name);
    }

    public SignatureVisitor visitClassBound() {
        return visitStart(this.visitor.visitClassBound());
    }

    public SignatureVisitor visitInterfaceBound() {
        return visitStart(visitor.visitInterfaceBound());
    }

    public SignatureVisitor visitSuperclass() {
        return visitStart(visitor.visitSuperclass());
    }

    public SignatureVisitor visitInterface() {
        return visitStart(visitor.visitInterface());
    }

    public SignatureVisitor visitParameterType() {
        return visitStart(visitor.visitParameterType());
    }

    public SignatureVisitor visitReturnType() {
        return visitStart(visitor.visitReturnType());
    }

    public SignatureVisitor visitExceptionType() {
        return visitStart(visitor.visitExceptionType());
    }

    public void visitBaseType(char descriptor) {
        visitor.visitBaseType(descriptor);
    }

    public void visitTypeVariable(String name) {
        visitor.visitTypeVariable(name);
    }

    public SignatureVisitor visitArrayType() {
        return visitStart(visitor.visitArrayType());
    }

    public void visitClassType(String name) {
        visitor.visitClassType(name);
    }

    public void visitInnerClassType(String name) {
        visitor.visitInnerClassType(name);
    }

    public void visitTypeArgument() {
        visitor.visitTypeArgument();
    }

    public SignatureVisitor visitTypeArgument(char wildcard) {
        return visitStart(visitor.visitTypeArgument(wildcard));
    }

    public void visitEnd() {
        visitor.visitEnd();
    }
}
