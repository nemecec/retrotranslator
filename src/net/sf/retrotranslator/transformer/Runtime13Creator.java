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

import java.io.*;

/**
 * @author Taras Puchko
 */
public class Runtime13Creator extends RuntimeCreator {

    public Runtime13Creator(File rootFolder, String targetPackage, String infix) {
        super(rootFolder, targetPackage, infix);
    }

    public static void main(String[] args) throws IOException {
        new Runtime13Creator(new File(args[0]), "net/sf/retrotranslator/runtime13/", "v15/").execute();
    }

    protected boolean isRightMethod(String name) {
        if (isClass("java/lang/_Character") && (name.equals("getDirectionality") || name.equals("isMirrored"))) {
            return false;
        }
        if (isClass("java/io/_PrintStream") && name.equals("createInstanceBuilder")) {
            return false;
        }
        if (isClass("java/lang/_Thread$AdvancedThreadBuilder") && name.equals("argument4")) {
            return false;
        }
        return true;
    }

    protected boolean isRightInnerClass(String innerName) {
        return !isClass("java/io/_PrintStream") || !innerName.equals("PrintStreamBuilder");
    }

}
