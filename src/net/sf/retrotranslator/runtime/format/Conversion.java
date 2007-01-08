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
package net.sf.retrotranslator.runtime.format;

import java.util.*;

/**
 * @author Taras Puchko
 */
abstract class Conversion {

    private static Map<String, Conversion> map = new HashMap<String, Conversion>();

    static {
        put("b", "B", new GeneralConversion.BooleanConversion());
        put("h", "H", new GeneralConversion.HashConversion());
        put("s", "S", new GeneralConversion.StringConversion());
        put("c", "C", new CharacterConversion());
        put("d", null, new DecimalIntegralConversion());
        put("o", null, new NondecimalIntegralConversion.OctalConversion());
        put("x", "X", new NondecimalIntegralConversion.HexadecimalConversion());
        put("e", "E", new FloatingPointConversion.ComputerizedScientificConversion());
        put("f", null, new FloatingPointConversion.DecimalConversion());
        put("g", "G", new FloatingPointConversion.GeneralScientificConversion());
        put("a", "A", new HexadecimalExponentialConversion());
        put("%", null, new PercentConversion());
        put("n", null, new LineSeparatorConversion());
        for (Map.Entry<Character, DateTimeConversion> entry : DateTimeConversion.getConversions().entrySet()) {
            Character c = entry.getKey();
            put("t" + c, "T" + c, entry.getValue());
        }
    }

    public static Conversion getInstance(String code) {
        return map.get(code);
    }

    public abstract void format(FormatContext context);

    private static void put(String lower, String upper, Conversion value) {
        map.put(lower, value);
        if (upper != null) {
            map.put(upper, value);
        }
    }

}
