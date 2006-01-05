package net.sf.retrotranslator.runtime.java.util;

import java.util.*;

public class EnumSet_<E extends Enum<E>> extends HashSet<E> {

    private static final long serialVersionUID = 7684628957901243852L;

    private Class<E> elementType;

    private EnumSet_(Class<E> elementType) {
        if (!elementType.isEnum()) {
            throw new ClassCastException();
        }
        this.elementType = elementType;
    }

    public static <E extends Enum<E>> EnumSet_<E> allOf(Class<E> elementType) {
        EnumSet_<E> result = new EnumSet_<E>(elementType);
        for (E e : elementType.getEnumConstants()) {
            result.add(e);
        }
        return result;
    }

    public static <E extends Enum<E>> EnumSet_<E> complementOf(EnumSet_<E> enumSet) {
        Class<E> elementType = enumSet.elementType;
        EnumSet_<E> result = new EnumSet_<E>(elementType);
        for (E e : elementType.getEnumConstants()) {
            if (!enumSet.contains(e)) {
                result.add(e);
            }
        }
        return result;
    }

    public static <E extends Enum<E>> EnumSet_<E> copyOf(Collection<E> collection) {
        if (collection instanceof EnumSet_) {
            return copyOf((EnumSet_<E>) collection);
        }
        Iterator<E> iterator = collection.iterator();
        if (!iterator.hasNext()) {
            throw new IllegalArgumentException();
        }
        EnumSet_<E> result = EnumSet_.of(iterator.next());
        while (iterator.hasNext()) {
            result.add(iterator.next());
        }
        return result;
    }

    public static <E extends Enum<E>> EnumSet_<E> copyOf(EnumSet_<E> enumSet) {
        return enumSet.clone();
    }

    public static <E extends Enum<E>> EnumSet_<E> noneOf(Class<E> elementType) {
        return new EnumSet_<E>(elementType);
    }

    public static <E extends Enum<E>> EnumSet_<E> of(E e) {
        EnumSet_<E> result = new EnumSet_<E>(e.getDeclaringClass());
        result.add(e);
        return result;
    }

    public static <E extends Enum<E>> EnumSet_<E> of(E e1, E e2) {
        EnumSet_<E> result = of(e1);
        result.add(e2);
        return result;
    }

    public static <E extends Enum<E>> EnumSet_<E> of(E e1, E e2, E e3) {
        EnumSet_<E> result = of(e1);
        result.add(e2);
        result.add(e3);
        return result;
    }

    public static <E extends Enum<E>> EnumSet_<E> of(E e1, E e2, E e3, E e4) {
        EnumSet_<E> result = of(e1);
        result.add(e2);
        result.add(e3);
        result.add(e4);
        return result;
    }

    public static <E extends Enum<E>> EnumSet_<E> of(E e1, E e2, E e3, E e4, E e5) {
        EnumSet_<E> result = of(e1);
        result.add(e2);
        result.add(e3);
        result.add(e4);
        result.add(e5);
        return result;
    }

    public static <E extends Enum<E>> EnumSet_<E> of(E first, E... rest) {
        EnumSet_<E> result = of(first);
        for (E e : rest) {
            result.add(e);
        }
        return result;
    }

    public boolean add(E o) {
        if (o == null) {
            throw new NullPointerException();
        }
        return super.add(elementType.cast(o));
    }

    public Iterator<E> iterator() {
        TreeSet<E> treeSet = new TreeSet<E>(ENUM_COMPARATOR);
        Iterator<E> iterator = super.iterator();
        while (iterator.hasNext()) {
            treeSet.add(iterator.next());
        }
        return treeSet.iterator();
    }

    private static final Comparator<Enum> ENUM_COMPARATOR = new Comparator<Enum>() {
        public int compare(Enum o1, Enum o2) {
            return o1.ordinal() - o2.ordinal();
        }
    };

    public EnumSet_<E> clone() {
        return (EnumSet_<E>) super.clone();
    }
}
