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

import java.util.*;
import java.util.regex.*;
import java.text.DecimalFormatSymbols;

/**
 * @author Taras Puchko
 */
public abstract class FormatContext {

    private static final DecimalFormatSymbols US_SYMBOLS = new DecimalFormatSymbols(Locale.US);
    private static final Pattern PATTERN = Pattern.compile("%(?:(\\d+)\\$)?([-#+ 0,(\\<]*)?(\\d+)?(?:\\.(\\d+))?" +
            "([bBhHsScCdoxXeEfgGaA%n]|(?:[tT][HIklMSLNpzZsQBbhAaCYyjmdeRTrDFc]))");

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

    protected FormatContext(Locale locale) {
        this.locale = locale;
    }

    public abstract void append(char c);

    public abstract void append(String s);

    public abstract void append(String s, int start, int end);

    public abstract boolean writeFormattable();

    public void printf(String format, Object... args) {
        arguments = args;
        effectiveIndex = 0;
        ordinaryIndex = 0;
        int position = 0;
        for (Matcher matcher = PATTERN.matcher(format); matcher.find(position); position = matcher.end()) {
            writeText(format, position, matcher.start());
            effectiveIndexComputed = false;
            specifier = matcher.group();
            explicitIndex = parse(matcher.group(1));
            String f = matcher.group(2);
            flags = f != null ? f : "";
            width = parse(matcher.group(3));
            precision = parse(matcher.group(4));
            conversion = matcher.group(5);
            Conversion.getInstance(conversion).format(this);
        }
        writeText(format, position, format.length());
    }

    private static int parse(String s) {
        return s == null ? -1 : Integer.valueOf(s);
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

    private void writeText(String s, int start, int end) {
        if (start == end) return;
        int i = s.indexOf('%', start);
        if (i >= 0 && i < end) {
            String unknownFormat = s.substring(i + 1, end);
            throw new UnknownFormatConversionException(unknownFormat.length() > 0 ? unknownFormat : "%");
        }
        append(s, start, end);
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
