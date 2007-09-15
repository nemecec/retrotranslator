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

import java.util.*;

/**
 * @author Taras Puchko
 */
class ClassReplacement {

    private String uniqueTypeName;
    private String referenceTypeName;
    private MemberReplacement checkCastReplacement;
    private MemberReplacement instanceOfReplacement;
    private final Map<MemberKey, MemberReplacement> fieldReplacements = createMap();
    private final Map<String, ConstructorReplacement> constructorReplacements = createMap();
    private final Map<String, MemberReplacement> converterReplacements = createMap();
    private final Map<MemberKey, MemberReplacement> methodReplacements = createMap();
    private final Map<String, MemberReplacement> instantiationReplacements = createMap();

    public ClassReplacement() {
    }

    public String getUniqueTypeName() {
        return uniqueTypeName;
    }

    public void setUniqueTypeName(String uniqueTypeName) {
        this.uniqueTypeName = uniqueTypeName;
    }

    public String getReferenceTypeName() {
        return referenceTypeName;
    }

    public void setReferenceTypeName(String referenceTypeName) {
        this.referenceTypeName = referenceTypeName;
    }

    public MemberReplacement getCheckCastReplacement() {
        return checkCastReplacement;
    }

    public void setCheckCastReplacement(MemberReplacement checkCastReplacement) {
        this.checkCastReplacement = checkCastReplacement;
    }

    public MemberReplacement getInstanceOfReplacement() {
        return instanceOfReplacement;
    }

    public void setInstanceOfReplacement(MemberReplacement instanceOfReplacement) {
        this.instanceOfReplacement = instanceOfReplacement;
    }

    public Map<MemberKey, MemberReplacement> getFieldReplacements() {
        return fieldReplacements;
    }

    public Map<String, ConstructorReplacement> getConstructorReplacements() {
        return constructorReplacements;
    }

    public Map<String, MemberReplacement> getConverterReplacements() {
        return converterReplacements;
    }

    public Map<MemberKey, MemberReplacement> getMethodReplacements() {
        return methodReplacements;
    }

    public Map<String, MemberReplacement> getInstantiationReplacements() {
        return instantiationReplacements;
    }

    public boolean isEmpty(String className) {
        return uniqueTypeName.equals(className) &&
                referenceTypeName.equals(className) &&
                checkCastReplacement == null &&
                instanceOfReplacement == null &&
                fieldReplacements.isEmpty() &&
                constructorReplacements.isEmpty() &&
                converterReplacements.isEmpty() &&
                methodReplacements.isEmpty() &&
                instantiationReplacements.isEmpty();
    }

    private static <K, V> Map<K, V> createMap() {
        return Collections.synchronizedMap(new LinkedHashMap<K, V>());
    }

}
