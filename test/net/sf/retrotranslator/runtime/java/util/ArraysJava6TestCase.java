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
package net.sf.retrotranslator.runtime.java.util;

import junit.framework.TestCase;
import java.util.*;

/**
 * @author Taras Puchko
 */
public class ArraysJava6TestCase extends TestCase {

    public void testCopyOf() {
        assertEquals("[true, false]", Arrays.toString(Arrays.copyOf(new boolean[]{true, false, true}, 2)));
        assertEquals("[true, false, true, false]", Arrays.toString(Arrays.copyOf(new boolean[]{true, false, true}, 4)));

        assertEquals("[1, 2]", Arrays.toString(Arrays.copyOf(new byte[]{1, 2, 3}, 2)));
        assertEquals("[1, 2, 3, 0]", Arrays.toString(Arrays.copyOf(new byte[]{1, 2, 3}, 4)));

        assertEquals("[a, b]", Arrays.toString(Arrays.copyOf(new char[]{'a', 'b', 'c'}, 2)));
        assertEquals("[a, b, c, \u0000]", Arrays.toString(Arrays.copyOf(new char[]{'a', 'b', 'c'}, 4)));

        assertEquals("[1.1, 2.2]", Arrays.toString(Arrays.copyOf(new double[]{1.1, 2.2, 3.3}, 2)));
        assertEquals("[1.1, 2.2, 3.3, 0.0]", Arrays.toString(Arrays.copyOf(new double[]{1.1, 2.2, 3.3}, 4)));

        assertEquals("[1.1, 2.2]", Arrays.toString(Arrays.copyOf(new float[]{1.1f, 2.2f, 3.3f}, 2)));
        assertEquals("[1.1, 2.2, 3.3, 0.0]", Arrays.toString(Arrays.copyOf(new float[]{1.1f, 2.2f, 3.3f}, 4)));

        assertEquals("[1, 2]", Arrays.toString(Arrays.copyOf(new int[]{1, 2, 3}, 2)));
        assertEquals("[1, 2, 3, 0]", Arrays.toString(Arrays.copyOf(new int[]{1, 2, 3}, 4)));

        assertEquals("[1, 2]", Arrays.toString(Arrays.copyOf(new long[]{1, 2, 3}, 2)));
        assertEquals("[1, 2, 3, 0]", Arrays.toString(Arrays.copyOf(new long[]{1, 2, 3}, 4)));

        assertEquals("[1, 2]", Arrays.toString(Arrays.copyOf(new short[]{1, 2, 3}, 2)));
        assertEquals("[1, 2, 3, 0]", Arrays.toString(Arrays.copyOf(new short[]{1, 2, 3}, 4)));

        assertEquals("[a, b]", Arrays.toString(Arrays.copyOf(new Object[]{"a", "b", "c"}, 2)));
        assertEquals("[a, b, c, null]", Arrays.toString(Arrays.copyOf(new Object[]{"a", "b", "c"}, 4)));

        assertEquals("[a, b]", Arrays.toString(Arrays.copyOf(new Object[]{"a", "b", "c"}, 2, String[].class)));
        assertEquals("[a, b, c, null]", Arrays.toString(Arrays.copyOf(new Object[]{"a", "b", "c"}, 4, String[].class)));
    }

    public void testCopyOfRange() {
        assertEquals("[false, true]", Arrays.toString(Arrays.copyOfRange(new boolean[]{true, false, true, false}, 1, 3)));

        assertEquals("[2, 3]", Arrays.toString(Arrays.copyOfRange(new byte[]{1, 2, 3, 4}, 1, 3)));

        assertEquals("[b, c]", Arrays.toString(Arrays.copyOfRange(new char[]{'a', 'b', 'c', 'd'}, 1, 3)));

        assertEquals("[2.2, 3.3]", Arrays.toString(Arrays.copyOfRange(new double[]{1.1, 2.2, 3.3, 4.4}, 1, 3)));

        assertEquals("[2.2, 3.3]", Arrays.toString(Arrays.copyOfRange(new float[]{1.1f, 2.2f, 3.3f, 4.4f}, 1, 3)));

        assertEquals("[2, 3]", Arrays.toString(Arrays.copyOfRange(new int[]{1, 2, 3, 4}, 1, 3)));

        assertEquals("[2, 3]", Arrays.toString(Arrays.copyOfRange(new long[]{1, 2, 3, 4}, 1, 3)));

        assertEquals("[2, 3]", Arrays.toString(Arrays.copyOfRange(new short[]{1, 2, 3, 4}, 1, 3)));

        assertEquals("[b, c]", Arrays.toString(Arrays.copyOfRange(new Object[]{"a", "b", "c", "d"}, 1, 3)));

        assertEquals("[b, c]", Arrays.toString(Arrays.copyOfRange(new Object[]{"a", "b", "c", "d"}, 1, 3, String[].class)));
    }

}
