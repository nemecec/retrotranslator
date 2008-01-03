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
package net.sf.retrotranslator.runtime.java.util;

import java.util.*;
import edu.emory.mathcs.backport.java.util.Deque;

/**
 * @author Taras Puchko
 */
public class _Deque {

    public static boolean executeInstanceOfInstruction(Object object) {
        return object instanceof LinkedList ||
                object instanceof Deque;
    }

    public static Collection executeCheckCastInstruction(Object object) {
        if (object instanceof LinkedList) {
            return (LinkedList) object;
        }
        return (Deque) object;
    }

    public static void addFirst(Collection collection, Object o) {
        if (collection instanceof LinkedList) {
            ((LinkedList) collection).addFirst(o);
        } else {
            ((Deque) collection).addFirst(o);
        }
    }

    public static void addLast(Collection collection, Object o) {
        if (collection instanceof LinkedList) {
            ((LinkedList) collection).addLast(o);
        } else {
            ((Deque) collection).addLast(o);
        }
    }

    public static Iterator descendingIterator(Collection collection) {
        if (collection instanceof LinkedList) {
            return _LinkedList.descendingIterator((LinkedList) collection);
        }
        return ((Deque) collection).descendingIterator();
    }

    public static Object element(Collection collection) {
        if (collection instanceof LinkedList) {
            return _LinkedList.element((LinkedList) collection);
        }
        return ((Deque) collection).element();
    }

    public static Object getFirst(Collection collection) {
        if (collection instanceof LinkedList) {
            return ((LinkedList) collection).getFirst();
        }
        return ((Deque) collection).getFirst();
    }

    public static Object getLast(Collection collection) {
        if (collection instanceof LinkedList) {
            return ((LinkedList) collection).getLast();
        }
        return ((Deque) collection).getLast();
    }

    public static boolean offer(Collection collection, Object element) {
        if (collection instanceof LinkedList) {
            return _LinkedList.offer((LinkedList) collection, element);
        }
        return ((Deque) collection).offer(element);
    }

    public static boolean offerFirst(Collection collection, Object element) {
        if (collection instanceof LinkedList) {
            return _LinkedList.offerFirst((LinkedList) collection, element);
        }
        return ((Deque) collection).offerFirst(element);
    }

    public static boolean offerLast(Collection collection, Object element) {
        if (collection instanceof LinkedList) {
            return _LinkedList.offerLast((LinkedList) collection, element);
        }
        return ((Deque) collection).offerLast(element);
    }

    public static Object peek(Collection collection) {
        if (collection instanceof LinkedList) {
            return _LinkedList.peek((LinkedList) collection);
        }
        return ((Deque) collection).peek();
    }

    public static Object peekFirst(Collection collection) {
        if (collection instanceof LinkedList) {
            return _LinkedList.peekFirst((LinkedList) collection);
        }
        return ((Deque) collection).peekFirst();
    }

    public static Object peekLast(Collection collection) {
        if (collection instanceof LinkedList) {
            return _LinkedList.peekLast((LinkedList) collection);
        }
        return ((Deque) collection).peekLast();
    }

    public static Object poll(Collection collection) {
        if (collection instanceof LinkedList) {
            return _LinkedList.poll((LinkedList) collection);
        }
        return ((Deque) collection).poll();
    }

    public static Object pollFirst(Collection collection) {
        if (collection instanceof LinkedList) {
            return _LinkedList.pollFirst((LinkedList) collection);
        }
        return ((Deque) collection).pollFirst();
    }

    public static Object pollLast(Collection collection) {
        if (collection instanceof LinkedList) {
            return _LinkedList.pollLast((LinkedList) collection);
        }
        return ((Deque) collection).pollLast();
    }

    public static Object pop(Collection collection) {
        if (collection instanceof LinkedList) {
            return _LinkedList.pop((LinkedList) collection);
        }
        return ((Deque) collection).pop();
    }

    public static void push(Collection collection, Object o) {
        if (collection instanceof LinkedList) {
            _LinkedList.push((LinkedList) collection, o);
        } else {
            ((Deque) collection).push(o);
        }
    }

    public static Object remove(Collection collection) {
        if (collection instanceof LinkedList) {
            return _LinkedList.remove((LinkedList) collection);
        }
        return ((Deque) collection).remove();
    }

    public static Object removeFirst(Collection collection) {
        if (collection instanceof LinkedList) {
            return ((LinkedList) collection).removeFirst();
        }
        return ((Deque) collection).removeFirst();
    }

    public static Object removeLast(Collection collection) {
        if (collection instanceof LinkedList) {
            return ((LinkedList) collection).removeLast();
        }
        return ((Deque) collection).removeLast();
    }

    public static boolean removeFirstOccurrence(Collection collection, Object o) {
        if (collection instanceof LinkedList) {
            return _LinkedList.removeFirstOccurrence((LinkedList) collection, o);
        }
        return ((Deque) collection).removeFirstOccurrence(o);
    }

    public static boolean removeLastOccurrence(Collection collection, Object o) {
        if (collection instanceof LinkedList) {
            return _LinkedList.removeLastOccurrence((LinkedList) collection, o);
        }
        return ((Deque) collection).removeLastOccurrence(o);
    }

}
