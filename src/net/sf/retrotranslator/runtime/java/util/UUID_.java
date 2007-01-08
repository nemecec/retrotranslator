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
package net.sf.retrotranslator.runtime.java.util;

import java.io.Serializable;
import java.security.*;

/**
 * @author Taras Puchko
 */
public class UUID_ implements Serializable, Comparable<UUID_> {

    private static final long serialVersionUID = 4759128571957838954L;
    private static final SecureRandom randomGenerator = new SecureRandom();

    private long mostSignificantBits;
    private long leastSignificantBits;

    public UUID_(long mostSigBits, long leastSigBits) {
        mostSignificantBits = mostSigBits;
        leastSignificantBits = leastSigBits;
    }

    public static UUID_ randomUUID() {
        byte[] bytes = new byte[16];
        randomGenerator.nextBytes(bytes);
        return newInstance(bytes, 4);
    }

    public static UUID_ nameUUIDFromBytes(byte[] name) {
        try {
            return newInstance(MessageDigest.getInstance("MD5").digest(name), 3);
        } catch (NoSuchAlgorithmException e) {
            throw new InternalError(e.getMessage());
        }
    }

    public static UUID_ fromString(String name) {
        String[] values = name.split("-");
        if (values.length != 5) throw new IllegalArgumentException(name);
        try {
            return new UUID_(Long.parseLong(values[0], 16) << 32 | Long.parseLong(values[1], 16) << 16 |
                    Long.parseLong(values[2], 16), Long.parseLong(values[3], 16) << 48 | Long.parseLong(values[4], 16));
        } catch (NumberFormatException e) {
            throw new IllegalStateException(name);
        }
    }

    public long getMostSignificantBits() {
        return mostSignificantBits;
    }

    public long getLeastSignificantBits() {
        return leastSignificantBits;
    }

    public int version() {
        return (int) (mostSignificantBits >>> 12) & 0x0F;
    }

    public int variant() {
        if (leastSignificantBits >>> 63 == 0) return 0;
        if (leastSignificantBits >>> 62 == 2) return 2;
        return (int) (leastSignificantBits >>> 61);
    }

    public long timestamp() {
        assertVersion1();
        return (mostSignificantBits & 0x00000FFFL) << 48 |
                (mostSignificantBits & 0xFFFF0000L) << 16 |
                mostSignificantBits >>> 32;
    }

    public int clockSequence() {
        assertVersion1();
        return (int) (leastSignificantBits >>> 48) & 0x3FFF;
    }

    public long node() {
        assertVersion1();
        return leastSignificantBits & 0x0000FFFFFFFFFFFFL;
    }

    public String toString() {
        return hex(mostSignificantBits >> 32, 8) +
                '-' + hex(mostSignificantBits >> 16, 4) +
                '-' + hex(mostSignificantBits, 4) +
                '-' + hex(leastSignificantBits >> 48, 4) +
                '-' + hex(leastSignificantBits, 12);
    }

    public int hashCode() {
        return (int) (mostSignificantBits >> 32 ^ mostSignificantBits
                ^ leastSignificantBits >> 32 ^ leastSignificantBits);
    }

    public boolean equals(Object obj) {
        if (obj instanceof UUID_) {
            UUID_ val = (UUID_) obj;
            return mostSignificantBits == val.mostSignificantBits &&
                    leastSignificantBits == val.leastSignificantBits;
        }
        return false;
    }

    public int compareTo(UUID_ val) {
        if (mostSignificantBits > val.mostSignificantBits) return 1;
        if (mostSignificantBits < val.mostSignificantBits) return -1;
        if (leastSignificantBits > val.leastSignificantBits) return 1;
        if (leastSignificantBits < val.leastSignificantBits) return -1;
        return 0;
    }

    private static UUID_ newInstance(byte[] bytes, int version) {
        return new UUID_(getLong(bytes, 0) & ~0xF000L | version << 12,
                getLong(bytes, 8) & ~0xC000000000000000L | 0x8000000000000000L);
    }

    private static long getLong(byte[] bytes, int offset) {
        long result = 0;
        for (int i = 0; i < 8; i++) {
            result = result << 8 | bytes[i + offset] & 0xFF;
        }
        return result;
    }

    private static String hex(long value, int length) {
        String s = Long.toHexString(1L << (length << 2) | value);
        return s.substring(s.length() - length);
    }

    private void assertVersion1() {
        if (version() != 1) throw new UnsupportedOperationException("Not a version 1 UUID");
    }

}
