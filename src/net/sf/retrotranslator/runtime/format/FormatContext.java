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

import java.text.DecimalFormatSymbols;
import java.util.*;

/**
 * @author Taras Puchko
 */
public abstract class FormatContext {

    private static final DecimalFormatSymbols US_SYMBOLS = new DecimalFormatSymbols(Locale.US);

    private Locale locale;
    private DecimalFormatSymbols symbols;
    private Object[] arguments;
    private int effectiveIndex;
    private int ordinaryIndex;
    private boolean effectiveIndexComputed;
    private String specifier;
    private int explicitIndex;
    private String flags;
    private int width;
    private int precision;
    private String conversion;
    private String format;
    private int position;

    protected FormatContext(Locale locale) {
        this.locale = locale;
    }

    public abstract void append(char c);

    public abstract void append(String s);

    public abstract void append(String s, int start, int end);

    public abstract boolean writeFormattable();

    public void printf(String format, Object... args) {
        this.format = format;
        arguments = args;
        effectiveIndex = 0;
        ordinaryIndex = 0;
        position = 0;
        int index;
        while ((index = format.indexOf('%', position)) >= 0) {
            append(format, position, index);
            scanOptions(index);
            effectiveIndexComputed = false;
            specifier = format.substring(index, position);
            Conversion instance = Conversion.getInstance(conversion);
            if (instance == null) {
                throw new UnknownFormatConversionException(conversion);
            }
            instance.format(this);
        }
        append(format, position, format.length());
    }

    private void scanOptions(int index) {
        position = index + 1;
        try {
            scanExplicitIndex();
            scanFlags();
            scanWidth();
            scanPrecision();
            scanConversion();
        } catch (IndexOutOfBoundsException e) {
            throw new UnknownFormatConversionException(format.substring(index));
        }
    }

    private void scanExplicitIndex() {
        int index = skipDigits(format, position);
        if (index > position && format.charAt(index) == '$') {
            explicitIndex = parse(format, position, index);
            position = index + 1;
        } else {
            explicitIndex = -1;
        }
    }

    private void scanFlags() {
        int index = skipFlags(format, position);
        if (index > position) {
            flags = format.substring(position, index);
            position = index;
        } else {
            flags = "";
        }
    }

    private void scanWidth() {
        int index = skipDigits(format, position);
        if (index > position) {
            width = parse(format, position, index);
            position = index;
        } else {
            width = -1;
        }
    }

    private void scanPrecision() {
        if (format.charAt(position) == '.') {
            position++;
            int index = skipDigits(format, position);
            if (index > position) {
                precision = parse(format, position, index);
                position = index;
            } else {
                throw new IndexOutOfBoundsException();
            }
        } else {
            precision = -1;
        }
    }

    private void scanConversion() {
        char c = format.charAt(position);
        int endIndex = (c == 't' || c == 'T') ? position + 2 : position + 1;
        conversion = format.substring(position, endIndex);
        position = endIndex;
    }

    private static int skipDigits(String s, int index) {
        char c = s.charAt(index);
        while (c >= '0' && c <= '9') {
            c = s.charAt(++index);
        }
        return index;
    }

    private static int skipFlags(String s, int index) {
        char c = s.charAt(index);
        while (c == '-' || c == '#' || c == '+' || c == ' ' ||
                c == '0' || c == ',' || c == '(' || c == '<' ) {
            c = s.charAt(++index);
        }
        return index;
    }

    private static Integer parse(String format, int beginIndex, int endIndex) {
        return Integer.valueOf(format.substring(beginIndex, endIndex));
    }

    private char getConversionType() {
        return conversion.charAt(0);
    }

    public int getWidth() {
        return width;
    }

    public int getPrecision() {
        return precision;
    }

    public int getNumberPrecision() {
        return precision >= 0 ? precision : 6;
    }

    public Locale getLocale() {
        return locale;
    }

    public DecimalFormatSymbols getSymbols(boolean localized) {
        if (localized && locale != null) {
            if (symbols == null) {
                symbols = new DecimalFormatSymbols(locale);
            }
            return symbols;
        } else {
            return US_SYMBOLS;
        }
    }

    public IllegalFormatConversionException getConversionException() {
        return new IllegalFormatConversionException(getConversionType(), getArgument().getClass());
    }

    public boolean isUpperCase() {
        return Character.isUpperCase(getConversionType());
    }

    public boolean isFlag(char c) {
        return flags.indexOf(c) >= 0;
    }

    public Object getArgument() {
        if (!effectiveIndexComputed) {
            computeEffectiveIndex();
            effectiveIndexComputed = true;
        }
        return arguments[effectiveIndex - 1];
    }

    private void computeEffectiveIndex() {
        if (flags.indexOf('<') < 0) {
            effectiveIndex = explicitIndex != -1 ? explicitIndex : ++ordinaryIndex;
        }
        if (arguments == null || effectiveIndex == 0 || effectiveIndex > arguments.length) {
            throw new MissingFormatArgumentException(specifier);
        }
    }

    public void writeRestricted(String s) {
        writePadded(precision != -1 && precision < s.length() ? s.substring(0, precision) : s);
    }

    public void writePadded(String s) {
        if (isFlag('-')) {
            writeCaseSensitive(s);
            writePadding(s);
        } else {
            writePadding(s);
            writeCaseSensitive(s);
        }
    }

    private void writePadding(String s) {
        for (int i = width - s.length(); i > 0; i--) {
            append(' ');
        }
    }

    private void writeCaseSensitive(String s) {
        if (isUpperCase()) {
            append(s.toUpperCase());
        } else {
            append(s);
        }
    }

    public void assertNoFlag(char flag) {
        if (isFlag(flag)) throw new FormatFlagsConversionMismatchException(flags, getConversionType());
    }

    public void assertNoPrecision() {
        if (precision != -1) throw new IllegalFormatPrecisionException(precision);
    }

    public void assertNoWidth() {
        if (width != -1) throw new IllegalFormatWidthException(width);
    }

    public void checkWidth() {
        if ((isFlag('-') || isFlag('0')) && width == -1) {
            throw new MissingFormatWidthException(specifier);
        }
    }

    public void checkFlags() {
        if (isFlag('+') && isFlag(' ') || isFlag('-') && isFlag('0')) {
            throw new IllegalFormatFlagsException(flags);
        }
    }

}
