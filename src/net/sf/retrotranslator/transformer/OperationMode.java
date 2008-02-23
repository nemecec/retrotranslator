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
package net.sf.retrotranslator.transformer;

import java.util.*;

/**
 * @author Taras Puchko
 */
class OperationMode {

    private final boolean advanced;
    private final boolean smart;
    private final Set<String> features = Collections.synchronizedSet(new HashSet<String>());
    private final ClassVersion target;
    private final boolean fixHyphen;

    public OperationMode(boolean advanced, String support, boolean smart, ClassVersion target) {
        this.advanced = advanced;
        this.target = target;
        this.smart = smart;
        if (support != null) {
            if (advanced) {
                throw new IllegalArgumentException("The -support option is unnecessary when -advanced is specified.");
            }
            StringTokenizer tokenizer = new StringTokenizer(support, ";");
            while (tokenizer.hasMoreTokens()) {
                features.add(tokenizer.nextToken());
            }
        }
        fixHyphen = isSupportedFeature("Retrotranslator.fixHyphen");
    }

    public boolean isSupportedFeature(String feature) {
        return advanced || features.contains(feature);
    }

    public boolean isSmart() {
        return smart;
    }

    public ClassVersion getTarget() {
        return target;
    }

    public String fixName(String className) {
        return fixHyphen ? className.replace('-', '$') : className;
    }

}
