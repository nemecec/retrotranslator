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
package net.sf.retrotranslator.runtime.format;

import java.text.*;
import java.util.*;
import net.sf.retrotranslator.tests.BaseTestCase;

/**
 * @author Taras Puchko
 */
public class DateTimeConversionTestCase extends BaseTestCase {

    public void testFormat_Flags() throws Exception {
        assertFormat("   null null", "%7tH %1tT", null, null);
        assertDate("  05", "%4tC", "0567-01-04");
        assertTime("00:07:08  ", "%-10tT", "00:07:08.093");
        assertTime("  00:07:08", "%10tT", "00:07:08.093");
        assertFormatException(MissingFormatWidthException.class, "%-tT", "x");
        assertFormatException(MissingFormatWidthException.class, "%-tT", new Date());
        assertFormatException(FormatFlagsConversionMismatchException.class, "%#tT", new Date());
        assertFormatException(IllegalFormatPrecisionException.class, "%10.2tT", new Date());
        assertFormatException(IllegalFormatPrecisionException.class, "%#-10.1tT", "x");
    }

    public void testFormat_Time() throws Exception {
        assertTime("00", "%tH", "00:07:08.093");
        assertTime("14", "%tH", "14:15:45.178");

        assertTime("12", "%tI", "00:07:08.093");
        assertTime("02", "%tI", "14:15:45.178");

        assertTime("0", "%tk", "00:07:08.093");
        assertTime("14", "%tk", "14:15:45.178");

        assertTime("12", "%tl", "00:07:08.093");
        assertTime("2", "%tl", "14:15:45.178");

        assertTime("07", "%tM", "00:07:08.093");
        assertTime("15", "%tM", "14:15:45.178");

        assertTime("08", "%tS", "00:07:08.093");
        assertTime("45", "%tS", "14:15:45.178");

        assertTime("093", "%tL", "00:07:08.093");
        assertTime("178", "%tL", "14:15:45.178");

        assertTime("093000000", "%tN", "00:07:08.093");
        assertTime("178000000", "%tN", "14:15:45.178");

        assertTime("am", "%tp", "00:07:08.093");
        assertTime("pm", "%tp", "14:15:45.178");

        assertTime("AM", "%Tp", "00:07:08.093");
        assertTime("PM", "%Tp", "14:15:45.178");

        assertDateTime("1234567", "%ts", new Date(1234567890));
        assertDateTime("1234567890", "%tQ", new Date(1234567890));

        assertTime("00:07", "%tR", "00:07:08.093");
        assertTime("14:15", "%tR", "14:15:45.178");

        assertTime("00:07:08", "%tT", "00:07:08.093");
        assertTime("14:15:45", "%tT", "14:15:45.178");

        assertTime("12:07:08 AM", "%tr", "00:07:08.093");
        assertTime("02:15:45 PM", "%tr", "14:15:45.178");
    }

    public void testFormat_Date() throws Exception {
        assertPatternDate("MMMM", "%tB", "2006-01-03", false);
        assertPatternDate("MMMM", "%tB", "2006-07-07", false);

        assertDate("JANVIER", "%TB", "2006-01-03");
        assertDate("JUILLET", "%TB", "2006-07-07");

        assertPatternDate("MMM", "%tb", "2006-01-05", false);
        assertPatternDate("MMM", "%tb", "2006-07-09", false);

        assertPatternDate("MMM", "%Tb", "2006-01-05", true);
        assertPatternDate("MMM", "%Tb", "2006-07-09", true);

        assertPatternDate("MMM", "%th", "2006-04-05", false);
        assertPatternDate("MMM", "%th", "2006-09-09", false);

        assertPatternDate("MMM", "%Th", "2006-04-05", true);
        assertPatternDate("MMM", "%Th", "2006-09-09", true);

        assertPatternDate("EEEE", "%tA", "2006-01-03", false);
        assertPatternDate("EEEE", "%tA", "2006-07-07", false);

        assertDate("MARDI", "%TA", "2006-01-03");
        assertDate("VENDREDI", "%TA", "2006-07-07");

        assertPatternDate("EEE", "%ta", "2006-01-04", false);
        assertPatternDate("EEE", "%ta", "2006-07-08", false);

        assertPatternDate("EEE", "%Ta", "2006-01-04", true);
        assertPatternDate("EEE", "%Ta", "2006-07-08", true);

        assertDate("20", "%tC", "2006-01-04");
        assertDate("01", "%tC", "0123-01-04");
        assertDate("123", "%tC", "12345-07-08");

        assertDate("2006", "%tY", "2006-01-04");
        assertDate("0123", "%tY", "0123-01-04");
        assertDate("12345", "%tY", "12345-07-08");

        assertDate("06", "%ty", "2006-01-04");
        assertDate("03", "%ty", "0003-01-04");
        assertDate("45", "%ty", "12345-07-08");

        assertDate("004", "%tj", "2006-01-04");
        assertDate("076", "%tj", "0003-03-17");
        assertDate("366", "%tj", "2000-12-31");

        assertDate("01", "%tm", "2006-01-04");
        assertDate("03", "%tm", "0003-03-17");
        assertDate("12", "%tm", "2000-12-31");

        assertDate("04", "%td", "2006-01-04");
        assertDate("17", "%td", "0003-03-17");
        assertDate("31", "%td", "2000-12-31");

        assertDate("4", "%te", "2006-01-04");
        assertDate("17", "%te", "0003-03-17");
        assertDate("31", "%te", "2000-12-31");

        assertDate("11/12/04", "%tD", "0004-11-12");
        assertDate("01/03/06", "%tD", "2006-01-03");
        assertDate("07/08/05", "%tD", "12005-07-08");

        assertDate("0004-11-12", "%tF", "0004-11-12");
        assertDate("2006-01-03", "%tF", "2006-01-03");
        assertDate("12005-07-08", "%tF", "12005-07-08");
    }

    public void testFormat_TimeZone() throws Exception {
        TimeZone defaultTimeZone = TimeZone.getDefault();
        try {
            assertTimeZone("Asia/Katmandu", "+0545", "NPT", "NPT");
            assertTimeZone("GMT+02:00", "+0200", "GMT+02:00", "GMT+02:00");
            assertTimeZone("Europe/Helsinki", "+0200", "EET", "EEST");
            assertTimeZone("Africa/Casablanca", "+0000", "WET", "WET");
            assertTimeZone("America/Los_Angeles", "-0800", "PST", "PDT");
            assertTimeZone("America/New_York", "-0500", "EST", "EDT");
            TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            format.setTimeZone(TimeZone.getTimeZone("America/New_York"));
            Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("America/Los_Angeles"));
            calendar.setTime(format.parse("2006-05-08 02:25:45"));
            assertFormat("2006-05-07 23:25:45", "%tF %tT", calendar, calendar);
        } finally {
            TimeZone.setDefault(defaultTimeZone);
        }
    }

    private void assertTime(String expected, String format, String time) throws Exception {
        assertDateTime(expected, format, parse("2006-01-03 " + time));
    }

    private void assertDate(String expected, String format, String date) throws Exception {
        assertDateTime(expected, format, parseDate(date));
    }

    private void assertPatternDate(String pattern, String format, String date, boolean upperCase) throws Exception {
        Date parsedDate = parseDate(date);
        String expected = format(parsedDate, pattern);
        if (upperCase) {
            expected = expected.toUpperCase();
        }
        assertDateTime(expected, format, parsedDate);
    }

    private void assertTimeZone(String id, String numeric, String standard, String daylight) throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone(id));
        Date winter = parse("2006-01-03 00:07:08.093");
        Date summer = parse("2006-04-07 14:15:45.178");
        assertDateTime(numeric, "%tz", winter);
        assertDateTime(numeric, "%tz", summer);
        assertDateTime(standard, "%tZ", winter);
        assertDateTime(daylight, "%tZ", summer);
        assertDateTime(format(winter, "EEE MMM") + " 03 00:07:08 " + standard + " 2006", "%tc", winter);
        assertDateTime(format(summer, "EEE MMM") + " 07 14:15:45 " + daylight + " 2006", "%tc", summer);
    }

    private void assertDateTime(String expected, String format, Date date) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        for (Object argument : new Object[]{date.getTime(), date, calendar}) {
            assertFormat(expected, format, argument);
        }
    }

    private static Date parseDate(String date) throws ParseException {
        return parse(date + " 00:07:08.093");
    }

    private static Date parse(String dateTime) throws ParseException {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(dateTime);
    }

    private static String format(Date date, String pattern) {
        return new SimpleDateFormat(pattern, Locale.FRANCE).format(date);
    }

}