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
package net.sf.retrotranslator.runtime.format;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Taras Puchko
 */
public abstract class Conversion {

    private static Map<String, Conversion> map = new HashMap<String, Conversion>();

    static {
        put("b", new GeneralConversion.BooleanConversion());
        put("h", new GeneralConversion.HashConversion());
        put("s", new GeneralConversion.StringConversion());
        put("c", new CharacterConversion());
        put("d", new IntegralConversion.DecimalConversion());
        put("o", new IntegralConversion.NonDecimalConversion(8, "0"));
        put("x", new IntegralConversion.NonDecimalConversion(16, "0x"));
        put("e", new FloatingPointConversion.ComputerizedScientificConversion());
        put("f", new FloatingPointConversion.DecimalConversion());
        put("g", new FloatingPointConversion.GeneralScientificConversion());
        put("a", new FloatingPointConversion.HexadecimalExponentialConversion());
        put("%", new PercentConversion());
        put("n", new LineSeparatorConversion());

        put("tH", new DateTimeConversion.ConversionU_H());
        put("tI", new DateTimeConversion.ConversionU_I());
        put("tk", new DateTimeConversion.ConversionL_k());
        put("tl", new DateTimeConversion.ConversionL_l());
        put("tM", new DateTimeConversion.ConversionU_M());
        put("tS", new DateTimeConversion.ConversionU_S());
        put("tL", new DateTimeConversion.ConversionU_L());
        put("tN", new DateTimeConversion.ConversionU_N());
        put("tp", new DateTimeConversion.ConversionL_p());
        put("tz", new DateTimeConversion.ConversionL_z());
        put("tZ", new DateTimeConversion.ConversionU_Z());
        put("ts", new DateTimeConversion.ConversionL_s());
        put("tQ", new DateTimeConversion.ConversionU_Q());

        put("tB", new DateTimeConversion.ConversionU_B());
        put("tb", new DateTimeConversion.ConversionL_b());
        put("th", new DateTimeConversion.ConversionL_b());
        put("tA", new DateTimeConversion.ConversionU_A());
        put("ta", new DateTimeConversion.ConversionL_a());
        put("tC", new DateTimeConversion.ConversionU_C());
        put("tY", new DateTimeConversion.ConversionU_Y());
        put("ty", new DateTimeConversion.ConversionL_y());
        put("tj", new DateTimeConversion.ConversionL_j());
        put("tm", new DateTimeConversion.ConversionL_m());
        put("td", new DateTimeConversion.ConversionL_d());
        put("te", new DateTimeConversion.ConversionL_e());
        put("tR", new DateTimeConversion.ConversionU_R());
        put("tT", new DateTimeConversion.ConversionU_T());
        put("tr", new DateTimeConversion.ConversionL_r());
        put("tD", new DateTimeConversion.ConversionU_D());
        put("tF", new DateTimeConversion.ConversionU_F());
        put("tc", new DateTimeConversion.ConversionL_c());
    }

    public static Conversion getInstance(String code) {
        return map.get(code);
    }

    public abstract void format(FormatContext context);

    private static void put(String key, Conversion value) {
        map.put(key, value);
        map.put(Character.toUpperCase(key.charAt(0)) + key.substring(1), value);
    }

}
