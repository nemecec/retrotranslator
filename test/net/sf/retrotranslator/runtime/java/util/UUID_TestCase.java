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

import junit.framework.*;

import java.util.UUID;

/**
 * @author Taras Puchko
 */
public class UUID_TestCase extends TestCase {

    public void testRandomUUID() throws Exception {
        UUID uuid = UUID.randomUUID();
        assertEquals(2, uuid.variant());
        assertEquals(4, uuid.version());
    }

    public void testNameUUIDFromBytes() throws Exception {
        UUID uuid = UUID.nameUUIDFromBytes("abc".getBytes());
        assertEquals(2, uuid.variant());
        assertEquals(3, uuid.version());
    }

    public void testFromString() throws Exception {
        UUID uuid = UUID.fromString("f81d4fae-7dec-11d0-a765-00a0c91e6bf6");
        assertEquals(2, uuid.variant());
        assertEquals(1, uuid.version());
    }

    public void testGetMostSignificantBits() throws Exception {
        assertEquals(123, new UUID(123, 456).getMostSignificantBits());
    }

    public void testGetLeastSignificantBits() throws Exception {
        assertEquals(456, new UUID(123, 456).getLeastSignificantBits());
    }

    public void testVersion() throws Exception {
        assertEquals(2, UUID.fromString("e48a5302-524d-2a9f-8203-1fbe19572c42").version());
    }

    public void testVariant() throws Exception {
        assertEquals(0, UUID.fromString("e48a5302-524d-2a9f-7203-1fbe19572c42").variant());
        assertEquals(2, UUID.fromString("e48a5302-524d-2a9f-b203-1fbe19572c42").variant());
        assertEquals(6, UUID.fromString("e48a5302-524d-2a9f-d203-1fbe19572c42").variant());
        assertEquals(7, UUID.fromString("e48a5302-524d-2a9f-f203-1fbe19572c42").variant());
    }

    public void testTimestamp() throws Exception {
        assertEquals(130742845922168750L, UUID.fromString("f81d4fae-7dec-11d0-a765-00a0c91e6bf6").timestamp());
        try {
            UUID.randomUUID().timestamp();
            fail();
        } catch (UnsupportedOperationException e) {
            //ok
        }
    }

    public void testClockSequence() throws Exception {
        assertEquals(10085, UUID.fromString("f81d4fae-7dec-11d0-a765-00a0c91e6bf6").clockSequence());
        try {
            UUID.randomUUID().clockSequence();
            fail();
        } catch (UnsupportedOperationException e) {
            //ok
        }
    }

    public void testNode() throws Exception {
        assertEquals(690568981494L, UUID.fromString("f81d4fae-7dec-11d0-a765-00a0c91e6bf6").node());
        try {
            UUID.randomUUID().node();
            fail();
        } catch (UnsupportedOperationException e) {
            //ok
        }
    }

    public void testToString() throws Exception {
        assertEquals("112210f4-7de9-8115-e53d-0978c0cc8c54",
                new UUID(1234567890123456789L, -1928374651209348012L).toString());
    }

    public void testHashCode() throws Exception {
        assertEquals(1228543181, new UUID(1234567890123456789L, -1928374651209348012L).hashCode());
    }

    public void testEquals() throws Exception {
        assertTrue(new UUID(1234567890123456789L, -1928374651209348012L).equals(
                UUID.fromString("112210f4-7de9-8115-e53d-0978c0cc8c54")));
        assertFalse(new UUID(1234567890123456789L, -1928374651209348012L).equals(
                UUID.fromString("012210f4-7de9-8115-e53d-0978c0cc8c54")));
    }

    public void testCompareTo() throws Exception {
        assertEquals(0, new UUID(1234567890123456789L, -1928374651209348012L).compareTo(
                UUID.fromString("112210f4-7de9-8115-e53d-0978c0cc8c54")));

        assertEquals(1, new UUID(1234567890123456789L, -1928374651209348012L).compareTo(
                new UUID(1234567890123456788L, -1928374651209348012L)));

        assertEquals(1, new UUID(1234567890123456789L, -1928374651209348012L).compareTo(
                new UUID(1234567890123456789L, -1928374651209348013L)));

        assertEquals(-1, new UUID(1234567890123456789L, -1928374651209348012L).compareTo(
                new UUID(1234567890123456790L, -1928374651209348012L)));

        assertEquals(-1, new UUID(1234567890123456789L, -1928374651209348012L).compareTo(
                new UUID(1234567890123456789L, -1928374651209348007L)));
    }
}