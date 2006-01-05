package net.sf.retrotranslator.runtime.java.util;

import junit.framework.TestCase;

import java.util.*;

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
