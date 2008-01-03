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
package net.sf.retrotranslator.runtime.impl;

import java.util.*;
import net.sf.retrotranslator.runtime.asm.AnnotationVisitor;

/**
 * @author Taras Puchko
 */
public class AnnotationValue implements AnnotationVisitor {

    private final String desc;
    private final Map<String, Object> elements = new HashMap<String, Object>();

    public AnnotationValue(String descriptor) {
        this.desc = descriptor;
    }

    public String getDesc() {
        return desc;
    }

    public Object getElement(String name) {
        return elements.get(name);
    }

    public void visit(String name, Object value) {
        elements.put(name, value);
    }

    public void visitEnum(String name, String desc, String value) {
        elements.put(name, new EnumValue(desc, value));
    }

    public AnnotationVisitor visitAnnotation(String name, String desc) {
        AnnotationValue value = new AnnotationValue(desc);
        elements.put(name, value);
        return value;
    }

    public AnnotationVisitor visitArray(String name) {
        AnnotationArray array = new AnnotationArray();
        elements.put(name, array);
        return array;
    }

    public void visitEnd() {
    }

}
