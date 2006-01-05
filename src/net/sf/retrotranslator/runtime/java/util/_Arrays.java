package net.sf.retrotranslator.runtime.java.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class _Arrays {

    private static final String NULL = "null";
    private static final String EMPTY_ARRAY = "[]";
    private static final char LEFT_BRACKET = '[';
    private static final char RIGHT_BRACKET = ']';
    private static final String SEPARATOR = ", ";

    public static boolean deepEquals(Object[] a1, Object[] a2) {
        return isEqual(a1, a2);
    }

    public static int deepHashCode(Object[] a) {
        if (a == null) return 0;
        int hashCode = 1;
        for (Object element : a) {
            hashCode = 31 * hashCode + getHashCode(element);
        }
        return hashCode;
    }

    public static String deepToString(Object[] a) {
        if (a == null) return NULL;
        StringBuilder builder = new StringBuilder();
        appendArray(builder, a, new HashSet<Object[]>());
        return builder.toString();
    }

    public static int hashCode(boolean[] a) {
        if (a == null) return 0;
        int hashCode = 1;
        for (boolean element : a) {
            hashCode = 31 * hashCode + (element ? 1231 : 1237);
        }
        return hashCode;
    }

    public static int hashCode(byte[] a) {
        if (a == null) return 0;
        int hashCode = 1;
        for (byte element : a) {
            hashCode = 31 * hashCode + element;
        }
        return hashCode;
    }

    public static int hashCode(char[] a) {
        if (a == null) return 0;
        int hashCode = 1;
        for (char element : a) {
            hashCode = 31 * hashCode + element;
        }
        return hashCode;
    }

    public static int hashCode(double[] a) {
        if (a == null) return 0;
        int hashCode = 1;
        for (double element : a) {
            long longBits = Double.doubleToLongBits(element);
            hashCode = 31 * hashCode + (int) (longBits ^ (longBits >>> 32));
        }
        return hashCode;
    }

    public static int hashCode(float[] a) {
        if (a == null) return 0;
        int hashCode = 1;
        for (float element : a) {
            hashCode = 31 * hashCode + Float.floatToIntBits(element);
        }
        return hashCode;
    }

    public static int hashCode(int[] a) {
        if (a == null) return 0;
        int hashCode = 1;
        for (int element : a) {
            hashCode = 31 * hashCode + element;
        }
        return hashCode;
    }

    public static int hashCode(long[] a) {
        if (a == null) return 0;
        int hashCode = 1;
        for (long element : a) {
            hashCode = 31 * hashCode + (int) (element ^ (element >>> 32));
        }
        return hashCode;
    }

    public static int hashCode(Object[] a) {
        if (a == null) return 0;
        int hashCode = 1;
        for (Object element : a) {
            hashCode = 31 * hashCode + (element == null ? 0 : element.hashCode());
        }
        return hashCode;
    }

    public static int hashCode(short[] a) {
        if (a == null) return 0;
        int hashCode = 1;
        for (short element : a) {
            hashCode = 31 * hashCode + element;
        }
        return hashCode;
    }

    public static String toString(boolean[] a) {
        if (a == null) return NULL;
        if (a.length == 0) return EMPTY_ARRAY;
        StringBuilder builder = new StringBuilder();
        builder.append(LEFT_BRACKET).append(a[0]);
        for (int i = 1; i < a.length; i++) {
            builder.append(SEPARATOR).append(a[i]);
        }
        return builder.append(RIGHT_BRACKET).toString();
    }

    public static String toString(byte[] a) {
        if (a == null) return NULL;
        if (a.length == 0) return EMPTY_ARRAY;
        StringBuilder builder = new StringBuilder();
        builder.append(LEFT_BRACKET).append(a[0]);
        for (int i = 1; i < a.length; i++) {
            builder.append(SEPARATOR).append(a[i]);
        }
        return builder.append(RIGHT_BRACKET).toString();
    }

    public static String toString(char[] a) {
        if (a == null) return NULL;
        if (a.length == 0) return EMPTY_ARRAY;
        StringBuilder builder = new StringBuilder();
        builder.append(LEFT_BRACKET).append(a[0]);
        for (int i = 1; i < a.length; i++) {
            builder.append(SEPARATOR).append(a[i]);
        }
        return builder.append(RIGHT_BRACKET).toString();
    }

    public static String toString(double[] a) {
        if (a == null) return NULL;
        if (a.length == 0) return EMPTY_ARRAY;
        StringBuilder builder = new StringBuilder();
        builder.append(LEFT_BRACKET).append(a[0]);
        for (int i = 1; i < a.length; i++) {
            builder.append(SEPARATOR).append(a[i]);
        }
        return builder.append(RIGHT_BRACKET).toString();
    }

    public static String toString(float[] a) {
        if (a == null) return NULL;
        if (a.length == 0) return EMPTY_ARRAY;
        StringBuilder builder = new StringBuilder();
        builder.append(LEFT_BRACKET).append(a[0]);
        for (int i = 1; i < a.length; i++) {
            builder.append(SEPARATOR).append(a[i]);
        }
        return builder.append(RIGHT_BRACKET).toString();
    }

    public static String toString(int[] a) {
        if (a == null) return NULL;
        if (a.length == 0) return EMPTY_ARRAY;
        StringBuilder builder = new StringBuilder();
        builder.append(LEFT_BRACKET).append(a[0]);
        for (int i = 1; i < a.length; i++) {
            builder.append(SEPARATOR).append(a[i]);
        }
        return builder.append(RIGHT_BRACKET).toString();
    }

    public static String toString(long[] a) {
        if (a == null) return NULL;
        if (a.length == 0) return EMPTY_ARRAY;
        StringBuilder builder = new StringBuilder();
        builder.append(LEFT_BRACKET).append(a[0]);
        for (int i = 1; i < a.length; i++) {
            builder.append(SEPARATOR).append(a[i]);
        }
        return builder.append(RIGHT_BRACKET).toString();
    }

    public static String toString(Object[] a) {
        if (a == null) return NULL;
        if (a.length == 0) return EMPTY_ARRAY;
        StringBuilder builder = new StringBuilder();
        builder.append(LEFT_BRACKET).append(a[0]);
        for (int i = 1; i < a.length; i++) {
            builder.append(SEPARATOR).append(a[i]);
        }
        return builder.append(RIGHT_BRACKET).toString();
    }

    public static String toString(short[] a) {
        if (a == null) return NULL;
        if (a.length == 0) return EMPTY_ARRAY;
        StringBuilder builder = new StringBuilder();
        builder.append(LEFT_BRACKET).append(a[0]);
        for (int i = 1; i < a.length; i++) {
            builder.append(SEPARATOR).append(a[i]);
        }
        return builder.append(RIGHT_BRACKET).toString();
    }

    private static void appendArray(StringBuilder builder, Object[] a, Set<Object[]> history) {
        int length = a.length;
        if (length == 0) {
            builder.append(EMPTY_ARRAY);
            return;
        }
        if (!history.add(a)) {
            builder.append("[...]");
            return;
        }
        appendObject(builder.append(LEFT_BRACKET), a[0], history);
        for (int i = 1; i < length; i++) {
            appendObject(builder.append(SEPARATOR), a[i], history);
        }
        builder.append(RIGHT_BRACKET);
    }

    private static void appendObject(StringBuilder builder, Object o, Set<Object[]> history) {
        if (o instanceof Object[]) {
            appendArray(builder, (Object[]) o, history);
        } else {
            builder.append(getString(o));
        }
    }

    private static int getHashCode(Object o) {
        if (o == null) return 0;
        if (o instanceof Object[]) return deepHashCode((Object[]) o);
        if (o instanceof boolean[]) return hashCode((boolean[]) o);
        if (o instanceof byte[]) return hashCode((byte[]) o);
        if (o instanceof char[]) return hashCode((char[]) o);
        if (o instanceof double[]) return hashCode((double[]) o);
        if (o instanceof float[]) return hashCode((float[]) o);
        if (o instanceof int[]) return hashCode((int[]) o);
        if (o instanceof long[]) return hashCode((long[]) o);
        if (o instanceof short[]) return hashCode((short[]) o);
        return o.hashCode();
    }

    private static String getString(Object o) {
        if (o == null) return null;
        if (o instanceof boolean[]) return toString((boolean[]) o);
        if (o instanceof byte[]) return toString((byte[]) o);
        if (o instanceof char[]) return toString((char[]) o);
        if (o instanceof double[]) return toString((double[]) o);
        if (o instanceof float[]) return toString((float[]) o);
        if (o instanceof int[]) return toString((int[]) o);
        if (o instanceof long[]) return toString((long[]) o);
        if (o instanceof short[]) return toString((short[]) o);
        return o.toString();
    }

    private static boolean isEqual(Object o1, Object o2) {
        if (o1 == o2) return true;
        if (o1 == null || o2 == null) return false;
        if (o1 instanceof Object[] && o2 instanceof Object[]) {
            Object[] a1 = (Object[]) o1;
            Object[] a2 = (Object[]) o2;
            int length = a1.length;
            if (length != a2.length) return false;
            for (int i = 0; i < length; i++) {
                if (!isEqual(a1[i], a2[i])) return false;
            }
            return true;
        }
        if (o1 instanceof boolean[] && o2 instanceof boolean[]) {
            return Arrays.equals(((boolean[]) o1), ((boolean[]) o2));
        }
        if (o1 instanceof byte[] && o2 instanceof byte[]) {
            return Arrays.equals(((byte[]) o1), ((byte[]) o2));
        }
        if (o1 instanceof char[] && o2 instanceof char[]) {
            return Arrays.equals(((char[]) o1), ((char[]) o2));
        }
        if (o1 instanceof double[] && o2 instanceof double[]) {
            return Arrays.equals(((double[]) o1), ((double[]) o2));
        }
        if (o1 instanceof float[] && o2 instanceof float[]) {
            return Arrays.equals(((float[]) o1), ((float[]) o2));
        }
        if (o1 instanceof int[] && o2 instanceof int[]) {
            return Arrays.equals(((int[]) o1), ((int[]) o2));
        }
        if (o1 instanceof long[] && o2 instanceof long[]) {
            return Arrays.equals(((long[]) o1), ((long[]) o2));
        }
        if (o1 instanceof short[] && o2 instanceof short[]) {
            return Arrays.equals(((short[]) o1), ((short[]) o2));
        }
        return o1.equals(o2);
    }

}
