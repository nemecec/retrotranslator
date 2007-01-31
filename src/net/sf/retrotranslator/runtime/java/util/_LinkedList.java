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

import java.util.*;

/**
 * @author Taras Puchko
 */
public class _LinkedList {

    public static Iterator descendingIterator(final LinkedList list) {
        return new Iterator() {
            private final ListIterator iterator = list.listIterator(list.size());

            public boolean hasNext() {
                return iterator.hasPrevious();
            }

            public Object next() {
                return iterator.previous();
            }

            public void remove() {
                iterator.remove();
            }
        };
    }

    public static Object element(LinkedList list) {
        return list.getFirst();
    }

    public static boolean offer(LinkedList list, Object o) {
        return list.add(o);
    }

    public static boolean offerFirst(LinkedList list, Object o) {
        list.addFirst(o);
        return true;
    }

    public static boolean offerLast(LinkedList list, Object o) {
        list.addLast(o);
        return true;
    }

    public static Object peek(LinkedList list) {
        return list.isEmpty() ? null : list.getFirst();
    }

    public static Object peekFirst(LinkedList list) {
        return list.isEmpty() ? null : list.getFirst();
    }

    public static Object peekLast(LinkedList list) {
        return list.isEmpty() ? null : list.getLast();
    }

    public static Object poll(LinkedList list) {
        return list.isEmpty() ? null : list.removeFirst();
    }

    public static Object pollFirst(LinkedList list) {
        return list.isEmpty() ? null : list.removeFirst();
    }

    public static Object pollLast(LinkedList list) {
        return list.isEmpty() ? null : list.removeLast();
    }

    public static Object pop(LinkedList list) {
        return list.removeFirst();
    }

    public static void push(LinkedList list, Object o) {
        list.addFirst(o);
    }

    public static Object remove(LinkedList list) {
        return list.removeFirst();
    }

    public static boolean removeFirstOccurrence(LinkedList list, Object o) {
        return list.remove(o);
    }

    public static boolean removeLastOccurrence(LinkedList list, Object o) {
        ListIterator iterator = list.listIterator(list.size());
        if (o != null) {
            while (iterator.hasPrevious()) {
                if (o.equals(iterator.previous())) {
                    iterator.remove();
                    return true;
                }
            }
        } else {
            while (iterator.hasPrevious()) {
                if (iterator.previous() == null) {
                    iterator.remove();
                    return true;
                }
            }
        }
        return false;
    }

}
