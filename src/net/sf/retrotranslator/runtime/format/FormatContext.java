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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.DecimalFormatSymbols;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * @author Taras Puchko
 */
public abstract class FormatContext {

    private static Pattern pattern = Pattern.compile("%(?:(\\d+)\\$)?([-#+ 0,(\\<]*)?(\\d+)?(?:\\.(\\d+))?" +
            "([bBhHsScCdoxXeEfgGaA%n]|(?:[tT]|[HIklMSLNpzZsQBbhAaCYyjmdeRTrDFc]))");

    protected Locale locale;
    protected Object[] arguments;

    protected String specifier;
    protected int index;
    protected String flags;
    protected int width;
    protected int precision;
    protected String conversion;

    private int lastIndex;
    private int freeIndex;
    private int currentIndex;
    private DecimalFormatSymbols symbols;
    private String exponentialSymbol;

    public abstract void append(char c);

    public abstract void append(String s);

    public abstract void append(String s, int start, int end);

    public abstract boolean writeFormattable();

    public void printf(Locale l, String format, Object... args) {
        locale = l;
        arguments = args;
        lastIndex = 0;
        freeIndex = 1;
        int position = 0;
        for (Matcher matcher = pattern.matcher(format); matcher.find(position); position = matcher.end()) {
            writeText(format, position, matcher.start());
            specifier = matcher.group();
            index = parse(matcher.group(1));
            String f = matcher.group(2);
            flags = f != null ? f : "";
            width = parse(matcher.group(3));
            precision = parse(matcher.group(4));
            conversion = matcher.group(5);
            currentIndex = 0;
            Conversion.getInstance(conversion).format(this);
        }
        writeText(format, position, format.length());
    }

    private static int parse(String s) {
        return s == null ? -1 : Integer.valueOf(s);
    }

    public char getConversionType() {
        return conversion.charAt(0);
    }

    public int getNumberPrecision() {
        return precision >= 0 ? precision : 6;
    }

    protected boolean isUpperCase() {
        return Character.isUpperCase(getConversionType());
    }

    public boolean isFlag(char c) {
        return flags.indexOf(c) >= 0;
    }

    public Object getArgument() {
        if (currentIndex == 0) {
            if (flags.indexOf('<') >= 0) {
                currentIndex = lastIndex;
            } else {
                currentIndex = (lastIndex = index != -1 ? index : freeIndex++);
            }
            if (arguments == null || currentIndex == 0 || currentIndex > arguments.length) {
                throw new MissingFormatArgumentException(specifier);
            }
        }
        return arguments[currentIndex - 1];
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
        if (checkLeftJustified()) {
            writeCaseSensitive(s);
            writePadding(s);
        } else {
            writePadding(s);
            writeCaseSensitive(s);
        }
    }

    public boolean checkLeftJustified() {
        if (flags.indexOf('-') < 0) return false;
        if (width == -1) throw new MissingFormatWidthException(specifier);
        return true;
    }

    private void writePadding(String s) {
        for (int i = width - s.length(); i > 0; i--) append(' ');
    }

    private void writeCaseSensitive(String s) {
        if (isUpperCase()) {
            append(s.toUpperCase());
        } else {
            append(s);
        }
    }

    public void denyAlternate() {
        if (flags.indexOf('#') >= 0) {
            throw new FormatFlagsConversionMismatchException("#", getConversionType());
        }
    }

    public void denyPrecision() {
        if (precision != -1) throw new IllegalFormatPrecisionException(precision);
    }

    public void denyWidth() {
        if (width != -1) throw new IllegalFormatWidthException(width);
    }

    public DecimalFormat getDecimalFormat() {
        return (DecimalFormat) NumberFormat.getInstance(locale != null ? locale : Locale.US);
    }

    public DecimalFormatSymbols getSymbols() {
        if (symbols == null) {
            symbols = getDecimalFormat().getDecimalFormatSymbols();
        }
        return symbols;
    }

    public String getExponentialSymbol() {
        if (exponentialSymbol == null) {
            DecimalFormat format = getDecimalFormat();
            format.applyPattern("E0");
            exponentialSymbol = format.format(0).substring(0, 1);
        }
        return exponentialSymbol;
    }
}
