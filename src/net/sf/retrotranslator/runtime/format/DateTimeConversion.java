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

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Taras Puchko
 */
abstract class DateTimeConversion extends Conversion {

    public static Map<Character, DateTimeConversion> getConversions() {
        HashMap<Character, DateTimeConversion> map = new HashMap<Character, DateTimeConversion>();
        map.put('H', new PatternConversion("HH"));
        map.put('I', new PatternConversion("hh"));
        map.put('k', new PatternConversion("H"));
        map.put('l', new PatternConversion("h"));
        map.put('M', new PatternConversion("mm"));
        map.put('S', new PatternConversion("ss"));
        map.put('L', new PatternConversion("SSS"));
        map.put('N', new PatternConversion("SSS000000"));
        map.put('p', new PatternConversion("a", true));
        map.put('z', new TimeZoneOffsetConversion());
        map.put('Z', new PatternConversion("z"));
        map.put('s', new MillisConversion(1000));
        map.put('Q', new MillisConversion(1));
        map.put('B', new PatternConversion("MMMM"));
        map.put('b', new PatternConversion("MMM"));
        map.put('h', new PatternConversion("MMM"));
        map.put('A', new PatternConversion("EEEE"));
        map.put('a', new PatternConversion("EEE"));
        map.put('C', new CenturyConversion());
        map.put('Y', new PatternConversion("yyyy"));
        map.put('y', new PatternConversion("yy"));
        map.put('j', new PatternConversion("DDD"));
        map.put('m', new PatternConversion("MM"));
        map.put('d', new PatternConversion("dd"));
        map.put('e', new PatternConversion("d"));
        map.put('R', new PatternConversion("HH:mm"));
        map.put('T', new PatternConversion("HH:mm:ss"));
        map.put('r', new PatternConversion("hh:mm:ss a"));
        map.put('D', new PatternConversion("MM/dd/yy"));
        map.put('F', new PatternConversion("yyyy-MM-dd"));
        map.put('c', new PatternConversion("EEE MMM dd HH:mm:ss z yyyy"));
        return map;
    }

    public void format(FormatContext context) {
        context.assertNoPrecision();
        context.assertNoFlag('#');
        context.checkWidth();
        Object argument = context.getArgument();
        if (argument == null) {
            context.writePadded(String.valueOf(argument));
        } else {
            printf(context);
        }
    }

    protected abstract void printf(FormatContext context);

    protected static Calendar getCalendar(FormatContext context) {
        Object argument = context.getArgument();
        Calendar calendar;
        if (argument instanceof Date) {
            calendar = new GregorianCalendar();
            calendar.setTime((Date) argument);
        } else if (argument instanceof Calendar) {
            calendar = (Calendar) argument;
        } else if (argument instanceof Long) {
            calendar = new GregorianCalendar();
            calendar.setTimeInMillis((Long) argument);
        } else {
            throw context.getConversionException();
        }
        return calendar;
    }

    private static class PatternConversion extends DateTimeConversion {

        private String pattern;
        private boolean toLowerCase;

        public PatternConversion(String pattern) {
            this.pattern = pattern;
        }

        public PatternConversion(String pattern, boolean toLowerCase) {
            this.pattern = pattern;
            this.toLowerCase = toLowerCase;
        }

        protected void printf(FormatContext context) {
            Locale locale = context.getLocale();
            if (locale == null) {
                locale = Locale.US;
            }
            SimpleDateFormat format = new SimpleDateFormat(pattern, locale);
            Object argument = context.getArgument();
            Date date;
            if (argument instanceof Date) {
                date = (Date) argument;
            } else if (argument instanceof Calendar) {
                Calendar calendar = (Calendar) argument;
                format.setTimeZone(calendar.getTimeZone());
                date = calendar.getTime();
            } else if (argument instanceof Long) {
                date = new Date((Long) argument);
            } else {
                throw context.getConversionException();
            }
            String result = format.format(date);
            if (toLowerCase) {
                result = result.toLowerCase(locale);
            }
            context.writePadded(result);
        }
    }

    private static class MillisConversion extends DateTimeConversion {

        private long divisor;

        public MillisConversion(long divisor) {
            this.divisor = divisor;
        }

        protected void printf(FormatContext context) {
            Object argument = context.getArgument();
            long millis;
            if (argument instanceof Date) {
                millis = ((Date) argument).getTime();
            } else if (argument instanceof Calendar) {
                millis = ((Calendar) argument).getTimeInMillis();
            } else if (argument instanceof Long) {
                millis = (Long) argument;
            } else {
                throw context.getConversionException();
            }
            context.writePadded(String.valueOf(millis / divisor));
        }
    }

    private static class TimeZoneOffsetConversion extends DateTimeConversion {

        protected void printf(FormatContext context) {
            int offset = getCalendar(context).get(Calendar.ZONE_OFFSET) / 60000;
            StringBuilder builder = new StringBuilder(5).append(offset < 0 ? '-' : '+');
            int value = offset < 0 ? -offset : offset;
            String s = Integer.toString(value / 60 * 100 + value % 60);
            for (int i = 4 - s.length(); i > 0; i--) {
                builder.append('0');
            }
            context.writePadded(builder.append(s).toString());
        }
    }

    private static class CenturyConversion extends DateTimeConversion {

        protected void printf(FormatContext context) {
            int century = getCalendar(context).get(Calendar.YEAR) / 100;
            if (century < 10) {
                char[] chars = Integer.toString(century + 10).toCharArray();
                chars[0] = '0';
                context.writePadded(new String(chars));
            } else {
                context.writePadded(Integer.toString(century));
            }
        }
    }

}
