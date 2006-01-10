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
package net.sf.retrotranslator.runtime.java.util;

import junit.framework.TestCase;

import java.util.*;

/**
 * @author Taras Puchko
 */
public class EnumSet_TestCase extends TestCase {

    private static enum Day {
        SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIEDAY, SATURDAY
    }

    public void testAllOf() {
        EnumSet<Day> days = EnumSet.allOf(Day.class);
        assertEquals(7, days.size());
        assertContent(days, Day.values());
    }

    public void testComplementOf() {
        EnumSet<Day> days = EnumSet.complementOf(
                EnumSet.of(Day.SUNDAY, Day.TUESDAY, Day.THURSDAY, Day.SATURDAY));
        assertContent(days, Day.MONDAY, Day.WEDNESDAY, Day.FRIEDAY);
    }

    public void testCopyOf() {
        EnumSet<Day> enumSetCopy = EnumSet.copyOf(
                EnumSet.of(Day.SUNDAY, Day.SATURDAY));
        assertContent(enumSetCopy, Day.SUNDAY, Day.SATURDAY);

        EnumSet<Day> collectionCopy = EnumSet.copyOf(
                (Collection<Day>) EnumSet.of(Day.SUNDAY, Day.SATURDAY));
        assertContent(collectionCopy, Day.SUNDAY, Day.SATURDAY);

        EnumSet<Day> listCopy = EnumSet.copyOf(
                Arrays.asList(Day.SATURDAY, Day.SUNDAY)
        );
        assertContent(listCopy, Day.SUNDAY, Day.SATURDAY);

        try {
            EnumSet.copyOf(Collections.<Day>emptyList());
            fail();
        } catch (IllegalArgumentException e) {
            //ok
        }
    }

    public void testNoneOf() {
        EnumSet<Day> days = EnumSet.noneOf(Day.class);
        assertTrue(days.isEmpty());
    }

    public void testOf() {
        assertContent(EnumSet.of(Day.FRIEDAY),
                Day.FRIEDAY);
        assertContent(EnumSet.of(Day.SUNDAY, Day.MONDAY),
                Day.SUNDAY, Day.MONDAY);
        assertContent(EnumSet.of(Day.SUNDAY, Day.MONDAY, Day.TUESDAY),
                Day.SUNDAY, Day.MONDAY, Day.TUESDAY);
        assertContent(EnumSet.of(Day.SUNDAY, Day.MONDAY, Day.TUESDAY, Day.WEDNESDAY),
                Day.SUNDAY, Day.MONDAY, Day.TUESDAY, Day.WEDNESDAY);
        assertContent(EnumSet.of(Day.SUNDAY, Day.MONDAY, Day.TUESDAY, Day.WEDNESDAY, Day.THURSDAY),
                Day.SUNDAY, Day.MONDAY, Day.TUESDAY, Day.WEDNESDAY, Day.THURSDAY);
        assertContent(EnumSet.of(Day.SUNDAY, Day.MONDAY, Day.TUESDAY, Day.WEDNESDAY, Day.THURSDAY, Day.FRIEDAY),
                Day.SUNDAY, Day.MONDAY, Day.TUESDAY, Day.WEDNESDAY, Day.THURSDAY, Day.FRIEDAY);
    }

    public void testRange() throws Exception {
        assertContent(EnumSet.range(Day.TUESDAY, Day.THURSDAY), Day.TUESDAY, Day.WEDNESDAY, Day.THURSDAY);
        try {
            EnumSet.range(Day.WEDNESDAY, Day.SUNDAY);
        } catch (IllegalArgumentException e) {
            //ok
        }
    }

    public void testAdd() {
        EnumSet<Day> days = EnumSet.of(Day.SUNDAY);
        try {
            days.add(null);
            fail();
        } catch (NullPointerException e) {
            //ok
        }
    }

    public void testIterator() {
        EnumSet<Day> days = EnumSet.of(
                Day.FRIEDAY, Day.WEDNESDAY, Day.SUNDAY, Day.SATURDAY, Day.TUESDAY);
        Iterator<Day> iterator = days.iterator();
        days.add(Day.THURSDAY);
        assertEquals(Day.SUNDAY, iterator.next());
        days.add(Day.MONDAY);
        assertEquals(Day.TUESDAY, iterator.next());
        assertEquals(Day.WEDNESDAY, iterator.next());
        assertEquals(Day.FRIEDAY, iterator.next());
        assertEquals(Day.SATURDAY, iterator.next());
        assertFalse(iterator.hasNext());
    }

    public void testClone() throws Exception {
        EnumSet<Day> days = EnumSet.of(Day.SUNDAY);
        EnumSet<Day> clone = days.clone();
        assertContent(clone, Day.SUNDAY);
    }

    private void assertContent(EnumSet<Day> set, Day... days) {
        assertEquals(days.length, set.size());
        List<Day> list = Arrays.asList(days);
        for (Day day : Day.values()) {
            if (list.contains(day)) {
                assertTrue(set.contains(day));
            } else {
                assertFalse(set.contains(day));
            }
        }
    }
}
