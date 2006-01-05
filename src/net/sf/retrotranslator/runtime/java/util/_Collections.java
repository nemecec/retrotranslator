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

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.*;

/**
 * @author Taras Puchko
 */
public class _Collections {

    public static <T> boolean addAll(Collection<? super T> c, T... a) {
        boolean result = false;
        for (T t : a) {
            result |= c.add(t);
        }
        return result;
    }

    public static <E> Collection<E> checkedCollection(Collection<E> c, Class<E> type) {
        return new CheckedCollection<E>(c, type);
    }

    public static <E> List<E> checkedList(List<E> list, Class<E> type) {
        return new CheckedList<E>(list, type);
    }

    public static <K,V> Map<K, V> checkedMap(Map<K, V> m, Class<K> keyType, Class<V> valueType) {
        return new CheckedMap<K, V>(m, keyType, valueType);
    }

    public static <E> Set<E> checkedSet(Set<E> s, Class<E> type) {
        return new CheckedSet<E>(s, type);
    }

    public static <K,V> SortedMap<K, V> checkedSortedMap(SortedMap<K, V> m, Class<K> keyType, Class<V> valueType) {
        return new CheckedSortedMap<K, V>(m, keyType, valueType);
    }

    public static <E> SortedSet<E> checkedSortedSet(SortedSet<E> s, Class<E> type) {
        return new CheckedSortedSet<E>(s, type);
    }

    public static boolean disjoint(Collection<?> c1, Collection<?> c2) {
        for (Object o : c1) {
            if (c2.contains(o)) return false;
        }
        return true;
    }

    public static final <T> List<T> emptyList() {
        return Collections.EMPTY_LIST;
    }

    public static final <K,V> Map<K, V> emptyMap() {
        return Collections.EMPTY_MAP;
    }

    public static final <T> Set<T> emptySet() {
        return Collections.EMPTY_SET;
    }

    public static int frequency(Collection<?> c, Object o) {
        int result = 0;
        if (o == null) {
            for (Object current : c) {
                result += (current == null) ? 1 : 0;
            }
        } else {
            for (Object current : c) {
                result += (o.equals(current)) ? 1 : 0;
            }
        }
        return result;
    }

    public static <T> Comparator<T> reverseOrder(final Comparator<T> cmp) {
        return (cmp == null) ? Collections.<T>reverseOrder() : new ReverseComparator<T>(cmp);
    }

    private static class CheckedCollection<E> implements Collection<E>, Serializable {

        static final long serialVersionUID = 6295674601624604684L;

        final Collection<E> collection;
        final Class<E> type;

        public CheckedCollection(Collection<E> collection, Class<E> type) {
            this.collection = collection;
            this.type = type;
        }

        public int size() {
            return collection.size();
        }

        public boolean isEmpty() {
            return collection.isEmpty();
        }

        public boolean contains(Object o) {
            return collection.contains(o);
        }

        public Iterator<E> iterator() {
            return collection.iterator();
        }

        public Object[] toArray() {
            return collection.toArray();
        }

        public <T>T[] toArray(T[] a) {
            return collection.toArray(a);
        }

        public boolean add(E o) {
            return collection.add(type.cast(o));
        }

        public boolean remove(Object o) {
            return collection.remove(o);
        }

        public boolean containsAll(Collection<?> c) {
            return collection.containsAll(c);
        }

        public boolean addAll(Collection<? extends E> c) {
            return collection.addAll(check(c));
        }

        public boolean removeAll(Collection<?> c) {
            return collection.removeAll(c);
        }

        public boolean retainAll(Collection<?> c) {
            return collection.removeAll(c);
        }

        public void clear() {
            collection.clear();
        }

        List<? extends E> check(Collection<? extends E> c) {
            try {
                return Arrays.asList(c.toArray((E[]) Array.newInstance(type, c.size())));
            } catch (ArrayStoreException e) {
                throw new ClassCastException();
            }
        }

    }

    private static class CheckedList<E> extends CheckedCollection<E> implements List<E> {

        static final long serialVersionUID = 7544634693847682845L;

        private final List<E> list;

        public CheckedList(List<E> list, Class<E> type) {
            super(list, type);
            this.list = list;
        }

        public boolean addAll(int index, Collection<? extends E> c) {
            return list.addAll(index, check(c));
        }

        public E get(int index) {
            return list.get(index);
        }

        public E set(int index, E element) {
            return list.set(index, type.cast(element));
        }

        public void add(int index, E element) {
            list.add(index, type.cast(element));
        }

        public E remove(int index) {
            return list.remove(index);
        }

        public int indexOf(Object o) {
            return list.indexOf(o);
        }

        public int lastIndexOf(Object o) {
            return list.lastIndexOf(o);
        }

        public ListIterator<E> listIterator() {
            return listIterator(0);
        }

        public ListIterator<E> listIterator(final int index) {
            return new ListIterator<E>() {

                private ListIterator<E> iterator = list.listIterator(index);

                public boolean hasNext() {
                    return iterator.hasNext();
                }

                public E next() {
                    return iterator.next();
                }

                public boolean hasPrevious() {
                    return iterator.hasPrevious();
                }

                public E previous() {
                    return iterator.previous();
                }

                public int nextIndex() {
                    return iterator.nextIndex();
                }

                public int previousIndex() {
                    return iterator.previousIndex();
                }

                public void remove() {
                    iterator.remove();
                }

                public void set(E o) {
                    iterator.set(type.cast(o));
                }

                public void add(E o) {
                    iterator.add(type.cast(o));
                }
            };
        }

        public List<E> subList(int fromIndex, int toIndex) {
            return new CheckedList<E>(list.subList(fromIndex, toIndex), type);
        }

        public boolean equals(Object obj) {
            return list.equals(obj);
        }

        public int hashCode() {
            return list.hashCode();
        }
    }

    private static class CheckedSet<E> extends CheckedCollection<E> implements Set<E> {

        static final long serialVersionUID = 2655826269013753724L;

        public CheckedSet(Set<E> set, Class<E> type) {
            super(set, type);
        }

        public int hashCode() {
            return collection.hashCode();
        }

        public boolean equals(Object obj) {
            return collection.equals(obj);
        }
    }

    private static class CheckedSortedSet<E> extends CheckedSet<E> implements SortedSet<E> {

        static final long serialVersionUID = 5682305462714452989L;

        private final SortedSet<E> sortedSet;

        public CheckedSortedSet(SortedSet<E> sortedSet, Class<E> type) {
            super(sortedSet, type);
            this.sortedSet = sortedSet;
        }

        public Comparator<? super E> comparator() {
            return sortedSet.comparator();
        }

        public SortedSet<E> subSet(E fromElement, E toElement) {
            return new CheckedSortedSet<E>(sortedSet.subSet(fromElement, toElement), type);
        }

        public SortedSet<E> headSet(E toElement) {
            return new CheckedSortedSet<E>(sortedSet.headSet(toElement), type);
        }

        public SortedSet<E> tailSet(E fromElement) {
            return new CheckedSortedSet<E>(sortedSet.tailSet(fromElement), type);
        }

        public E first() {
            return sortedSet.first();
        }

        public E last() {
            return sortedSet.last();
        }
    }

    private static class CheckedMap<K, V> implements Map<K, V>, Serializable {

        static final long serialVersionUID = 5466234636130576534L;

        private final Map<K, V> map;
        final Class<K> keyType;
        final Class<V> valueType;

        public CheckedMap(Map<K, V> map, Class<K> keyType, Class<V> valueType) {
            this.map = map;
            this.keyType = keyType;
            this.valueType = valueType;
        }

        public int size() {
            return map.size();
        }

        public boolean isEmpty() {
            return map.isEmpty();
        }

        public boolean containsKey(Object key) {
            return map.containsKey(key);
        }

        public boolean containsValue(Object value) {
            return map.containsValue(value);
        }

        public V get(Object key) {
            return map.get(key);
        }

        public V put(K key, V value) {
            return map.put(keyType.cast(key), valueType.cast(value));
        }

        public V remove(Object key) {
            return map.remove(key);
        }

        public void putAll(Map<? extends K, ? extends V> t) {
            K[] keys = (K[]) Array.newInstance(keyType, t.size());
            V[] values = (V[]) Array.newInstance(valueType, t.size());
            int index = 0;
            for (Entry<? extends K, ? extends V> entry : t.entrySet()) {
                try {
                    keys[index] = entry.getKey();
                    values[index] = entry.getValue();
                } catch (ArrayStoreException e) {
                    throw new ClassCastException();
                }
                index++;
            }
            if (index != keys.length || index != values.length) throw new ConcurrentModificationException();
            for (int i = 0; i < values.length; i++) {
                map.put(keys[i], values[i]);
            }
        }

        public void clear() {
            map.clear();
        }

        public Set<K> keySet() {
            return map.keySet();
        }

        public Collection<V> values() {
            return map.values();
        }

        public Set<Entry<K, V>> entrySet() {
            return new CheckedEntrySet<K, V>(map.entrySet(), valueType);
        }
    }

    private static class CheckedEntrySet<K, V> implements Set<Map.Entry<K, V>> {

        private Set<Map.Entry<K, V>> set;
        private Class<V> valueType;

        public CheckedEntrySet(Set<Map.Entry<K, V>> set, Class<V> valueType) {
            this.set = set;
            this.valueType = valueType;
        }

        public int size() {
            return set.size();
        }

        public boolean isEmpty() {
            return set.isEmpty();
        }

        public boolean contains(Object o) {
            return set.contains(o);
        }

        public Iterator<Map.Entry<K, V>> iterator() {
            final Iterator<Map.Entry<K, V>> iterator = set.iterator();
            return new Iterator<Map.Entry<K, V>>() {

                public boolean hasNext() {
                    return iterator.hasNext();
                }

                public Map.Entry<K, V> next() {
                    return new CheckedEntry<K, V>(iterator.next(), valueType);
                }

                public void remove() {
                    iterator.remove();
                }
            };
        }

        public Object[] toArray() {
            return toArray(new Object[set.size()]);
        }

        public <T>T[] toArray(T[] a) {
            T[] result = set.toArray(a);
            for (int i = 0; i < result.length; i++) {
                result[i] = (T) new CheckedEntry<K, V>((Map.Entry<K,V>) result[i], valueType);
            }
            return result;
        }

        public boolean add(Map.Entry<K, V> o) {
            throw new UnsupportedOperationException();
        }

        public boolean remove(Object o) {
            return set.remove(o);
        }

        public boolean containsAll(Collection<?> c) {
            return set.containsAll(c);
        }

        public boolean addAll(Collection<? extends Map.Entry<K, V>> c) {
            throw new UnsupportedOperationException();
        }

        public boolean retainAll(Collection<?> c) {
            return set.retainAll(c);
        }

        public boolean removeAll(Collection<?> c) {
            return set.removeAll(c);
        }

        public void clear() {
            set.clear();
        }
    }

    private static class CheckedEntry<K,V> implements Map.Entry<K, V> {

        private Map.Entry<K, V> entry;
        private Class<V> valueType;

        public CheckedEntry(Map.Entry<K, V> entry, Class<V> valueType) {
            this.entry = entry;
            this.valueType = valueType;
        }

        public K getKey() {
            return entry.getKey();
        }

        public V getValue() {
            return entry.getValue();
        }

        public V setValue(V value) {
            return entry.setValue(valueType.cast(value));
        }

        public boolean equals(Object obj) {
            if (obj instanceof Map.Entry) {
                Map.Entry entry = ((Map.Entry) obj);
                return equal(entry.getKey(), this.entry.getKey())
                        && equal(entry.getValue(), this.entry.getValue());
            }
            return false;
        }

        private static boolean equal(Object o1, Object o2) {
            return o1 == null ? o2 == null : o1.equals(o2);
        }
    }

    private static class CheckedSortedMap<K, V> extends CheckedMap<K, V> implements SortedMap<K, V> {

        static final long serialVersionUID = 4436469034535189432L;

        private final SortedMap<K, V> map;

        public CheckedSortedMap(SortedMap<K, V> map, Class<K> keyType, Class<V> valueType) {
            super(map, keyType, valueType);
            this.map = map;
        }

        public Comparator<? super K> comparator() {
            return map.comparator();
        }

        public SortedMap<K, V> subMap(K fromKey, K toKey) {
            return new CheckedSortedMap<K, V>(map.subMap(fromKey, toKey), keyType, valueType);
        }

        public SortedMap<K, V> headMap(K toKey) {
            return new CheckedSortedMap<K, V>(map.headMap(toKey), keyType, valueType);
        }

        public SortedMap<K, V> tailMap(K fromKey) {
            return new CheckedSortedMap<K, V>(map.tailMap(fromKey), keyType, valueType);
        }

        public K firstKey() {
            return map.firstKey();
        }

        public K lastKey() {
            return map.lastKey();
        }
    }

    private static class ReverseComparator<T> implements Comparator<T>, Serializable {

        static final long serialVersionUID = 1955474637275920642L;

        private final Comparator<T> comparator;

        public ReverseComparator(Comparator<T> cmp) {
            this.comparator = cmp;
        }

        public int compare(T o1, T o2) {
            return comparator.compare(o2, o1);
        }
    }
}
