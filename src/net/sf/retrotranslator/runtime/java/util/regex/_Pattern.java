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
package net.sf.retrotranslator.runtime.java.util.regex;

import java.util.regex.Pattern;
import net.sf.retrotranslator.registry.Advanced;
import net.sf.retrotranslator.runtime.java.lang._String;

/**
 * @author Taras Puchko
 */
public class _Pattern {

    public static final int LITERAL = 0x10;

    private static final String[][] REPLACEMENTS = {
            {"\\p{javaDefined}", "\\P{Cn}"},
            {"\\p{javaDigit}", "\\p{Nd}"},
            {"\\p{javaIdentifierIgnorable}", "[\\u0000-\\u0008\\u000E-\\u001B\\u007F-\\u009F\\p{Cf}]"},
            {"\\p{javaISOControl}", "\\p{Cc}"},
            {"\\p{javaJavaIdentifierPart}", "[\\u0000-\\u0008\\u000E-\\u001B\\u007F-\\u009F" +
                    "\\p{Cf}\\p{L}\\p{Sc}\\p{Pc}\\p{Nd}\\p{Nl}\\p{Mc}\\p{Mn}]"},
            {"\\p{javaJavaIdentifierStart}", "[\\p{L}\\p{Sc}\\p{Pc}\\p{Nl}]"},
            {"\\p{javaLetter}", "\\p{L}"},
            {"\\p{javaLetterOrDigit}", "[\\p{L}\\p{Nd}]"},
            {"\\p{javaLowerCase}", "\\p{Ll}"},
            {"\\p{javaSpaceChar}", "\\p{Z}"},
            {"\\p{javaTitleCase}", "\\p{Lt}"},
            {"\\p{javaUnicodeIdentifierPart}",
                    "[\\u0000-\\u0008\\u000E-\\u001B\\u007F-\\u009F\\p{Cf}\\p{L}\\p{Pc}\\p{Nd}\\p{Nl}\\p{Mc}\\p{Mn}]"},
            {"\\p{javaUnicodeIdentifierStart}", "[\\p{L}\\p{Nl}]"},
            {"\\p{javaUpperCase}", "\\p{Lu}"},
            {"\\p{javaWhitespace}", "[\\u0009-\\u000D\\u001C-\\u001F\\p{Z}&&[^\\u00A0\\u2007\\u202F]]"}
    };

    @Advanced("Pattern.compile")
    public static Pattern compile(String regex) {
        return Pattern.compile(fix(regex));
    }

    @Advanced("Pattern.compile")
    public static Pattern compile(String regex, int flags) {
        if ((flags & LITERAL) == LITERAL) {
            flags ^= LITERAL;
            regex = quote(regex);
        }
        return Pattern.compile(fix(regex), flags);
    }

    @Advanced("Pattern.matches")
    public static boolean matches(String regex, CharSequence input) {
        return Pattern.matches(fix(regex), input);
    }

    private static String fix(String regex) {
        for (String[] replacement : REPLACEMENTS) {
            regex = _String.replace(regex, replacement[0], replacement[1]);
        }
        return regex;
    }

    public static String quote(String s) {
        StringBuilder builder = new StringBuilder(s.length() + 4).append("\\Q");
        int lastIndex = 0;
        int nextIndex;
        while ((nextIndex = s.indexOf("\\E", lastIndex)) >= 0) {
            builder.append(s.substring(lastIndex, nextIndex)).append("\\E\\\\E\\Q");
            lastIndex = nextIndex + 2;
        }
        return builder.append(s.substring(lastIndex)).append("\\E").toString();
    }

}
