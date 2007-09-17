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
package net.sf.retrotranslator.runtime.impl;

import java.util.*;
import edu.emory.mathcs.backport.java.util.Queue;


/**
 * @author Taras Puchko
 */
public class SignatureList {

    private static final Map<String, String> map = getMap();

    private SignatureList() {
    }

    public static String getSignature(String className) {
        return map.get(className);
    }

    private static Map<String, String> getMap() {
        final String queueClassName = "java/util/Queue";
        Map<String, String> map = new Hashtable<String, String>();
        String name = Queue.class.getName().replace('.', '/');
        if (!name.endsWith(queueClassName)) {
            return map;
        }
        String prefix = name.substring(0, name.length() - queueClassName.length());
        map.put("java/util/Collection",
                "<E:Ljava/lang/Object;>Ljava/lang/Object;");
        map.put("java/util/Set",
                "<E:Ljava/lang/Object;>Ljava/lang/Object;Ljava/util/Collection<TE;>;");
        map.put("java/util/List",
                "<E:Ljava/lang/Object;>Ljava/lang/Object;Ljava/util/Collection<TE;>;");
        map.put(prefix + "java/util/Queue",
                "<E:Ljava/lang/Object;>Ljava/lang/Object;Ljava/util/Collection<TE;>;");
        map.put("java/util/Map",
                "<K:Ljava/lang/Object;V:Ljava/lang/Object;>Ljava/lang/Object;");
        map.put("java/util/SortedSet",
                "<E:Ljava/lang/Object;>Ljava/lang/Object;Ljava/util/Set<TE;>;");
        map.put("java/util/SortedMap",
                "<K:Ljava/lang/Object;V:Ljava/lang/Object;>Ljava/lang/Object;Ljava/util/Map<TK;TV;>;");
        map.put(prefix + "java/util/concurrent/BlockingQueue",
                "<E:Ljava/lang/Object;>Ljava/lang/Object;L" + prefix + "java/util/Queue<TE;>;");
        map.put(prefix + "java/util/concurrent/ConcurrentMap",
                "<K:Ljava/lang/Object;V:Ljava/lang/Object;>Ljava/lang/Object;Ljava/util/Map<TK;TV;>;");
        map.put("java/util/HashSet",
                "<E:Ljava/lang/Object;>Ljava/util/AbstractSet<TE;>;" +
                        "Ljava/util/Set<TE;>;Ljava/lang/Cloneable;Ljava/io/Serializable;");
        map.put("java/util/TreeSet",
                "<E:Ljava/lang/Object;>Ljava/util/AbstractSet<TE;>;Ljava/util/SortedSet<TE;>;" +
                        "Ljava/lang/Cloneable;Ljava/io/Serializable;");
        map.put("java/util/LinkedHashSet",
                "<E:Ljava/lang/Object;>Ljava/util/HashSet<TE;>;Ljava/util/Set<TE;>;" +
                        "Ljava/lang/Cloneable;Ljava/io/Serializable;");
        map.put("java/util/ArrayList",
                "<E:Ljava/lang/Object;>Ljava/util/AbstractList<TE;>;Ljava/util/List<TE;>;" +
                        "Ljava/util/RandomAccess;Ljava/lang/Cloneable;Ljava/io/Serializable;");
        map.put("java/util/LinkedList",
                "<E:Ljava/lang/Object;>Ljava/util/AbstractSequentialList<TE;>;" +
                        "Ljava/util/List<TE;>;Ljava/lang/Cloneable;Ljava/io/Serializable;");
        map.put(prefix + "java/util/PriorityQueue",
                "<E:Ljava/lang/Object;>L" + prefix + "java/util/AbstractQueue<TE;>;Ljava/io/Serializable;");
        map.put("java/util/HashMap",
                "<K:Ljava/lang/Object;V:Ljava/lang/Object;>Ljava/util/AbstractMap<TK;TV;>;" +
                        "Ljava/util/Map<TK;TV;>;Ljava/lang/Cloneable;Ljava/io/Serializable;");
        map.put("java/util/TreeMap",
                "<K:Ljava/lang/Object;V:Ljava/lang/Object;>Ljava/util/AbstractMap<TK;TV;>;" +
                        "Ljava/util/SortedMap<TK;TV;>;Ljava/lang/Cloneable;Ljava/io/Serializable;");
        map.put("java/util/LinkedHashMap",
                "<K:Ljava/lang/Object;V:Ljava/lang/Object;>Ljava/util/HashMap<TK;TV;>;");
        map.put("java/util/Vector",
                "<E:Ljava/lang/Object;>Ljava/util/AbstractList<TE;>;Ljava/util/List<TE;>;" +
                        "Ljava/util/RandomAccess;Ljava/lang/Cloneable;Ljava/io/Serializable;");
        map.put("java/util/Hashtable",
                "<K:Ljava/lang/Object;V:Ljava/lang/Object;>Ljava/util/Dictionary<TK;TV;>;" +
                        "Ljava/util/Map<TK;TV;>;Ljava/lang/Cloneable;Ljava/io/Serializable;");
        map.put("java/util/WeakHashMap",
                "<K:Ljava/lang/Object;V:Ljava/lang/Object;>Ljava/util/AbstractMap<TK;TV;>;Ljava/util/Map<TK;TV;>;");
        map.put("java/util/IdentityHashMap",
                "<K:Ljava/lang/Object;V:Ljava/lang/Object;>Ljava/util/AbstractMap<TK;TV;>;" +
                        "Ljava/util/Map<TK;TV;>;Ljava/io/Serializable;Ljava/lang/Cloneable;");
        map.put(prefix + "java/util/concurrent/CopyOnWriteArrayList",
                "<E:Ljava/lang/Object;>Ljava/lang/Object;Ljava/util/List<TE;>;" +
                        "Ljava/util/RandomAccess;Ljava/lang/Cloneable;Ljava/io/Serializable;");
        map.put(prefix + "java/util/concurrent/CopyOnWriteArraySet",
                "<E:Ljava/lang/Object;>Ljava/util/AbstractSet<TE;>;Ljava/io/Serializable;");
        map.put(prefix + "java/util/concurrent/ConcurrentLinkedQueue",
                "<E:Ljava/lang/Object;>L" + prefix + "java/util/AbstractQueue<TE;>;L" +
                        prefix + "java/util/Queue<TE;>;Ljava/io/Serializable;");
        map.put(prefix + "java/util/concurrent/LinkedBlockingQueue",
                "<E:Ljava/lang/Object;>L" + prefix + "java/util/AbstractQueue<TE;>;L" +
                        prefix + "java/util/concurrent/BlockingQueue<TE;>;Ljava/io/Serializable;");
        map.put(prefix + "java/util/concurrent/ArrayBlockingQueue",
                "<E:Ljava/lang/Object;>L" + prefix + "java/util/AbstractQueue<TE;>;L" +
                        prefix + "java/util/concurrent/BlockingQueue<TE;>;Ljava/io/Serializable;");
        map.put(prefix + "java/util/concurrent/PriorityBlockingQueue",
                "<E:Ljava/lang/Object;>L" + prefix + "java/util/AbstractQueue<TE;>;L" +
                        prefix + "java/util/concurrent/BlockingQueue<TE;>;Ljava/io/Serializable;");
        map.put(prefix + "java/util/concurrent/DelayQueue",
                "<E::L" + prefix + "java/util/concurrent/Delayed;>L" + prefix +
                        "java/util/AbstractQueue<TE;>;L" + prefix + "java/util/concurrent/BlockingQueue<TE;>;");
        map.put(prefix + "java/util/concurrent/SynchronousQueue",
                "<E:Ljava/lang/Object;>L" + prefix + "java/util/AbstractQueue<TE;>;L" +
                        prefix + "java/util/concurrent/BlockingQueue<TE;>;Ljava/io/Serializable;");
        map.put(prefix + "java/util/concurrent/ConcurrentHashMap",
                "<K:Ljava/lang/Object;V:Ljava/lang/Object;>L" + prefix + "java/util/AbstractMap<TK;TV;>;L" +
                        prefix + "java/util/concurrent/ConcurrentMap<TK;TV;>;Ljava/io/Serializable;");
        map.put("java/util/AbstractCollection",
                "<E:Ljava/lang/Object;>Ljava/lang/Object;Ljava/util/Collection<TE;>;");
        map.put("java/util/AbstractSet",
                "<E:Ljava/lang/Object;>Ljava/util/AbstractCollection<TE;>;Ljava/util/Set<TE;>;");
        map.put("java/util/AbstractList",
                "<E:Ljava/lang/Object;>Ljava/util/AbstractCollection<TE;>;Ljava/util/List<TE;>;");
        map.put("java/util/AbstractSequentialList",
                "<E:Ljava/lang/Object;>Ljava/util/AbstractList<TE;>;");
        map.put(prefix + "java/util/AbstractQueue",
                "<E:Ljava/lang/Object;>L" + prefix + "java/util/AbstractCollection<TE;>;L" +
                        prefix + "java/util/Queue<TE;>;");
        map.put("java/util/AbstractMap",
                "<K:Ljava/lang/Object;V:Ljava/lang/Object;>Ljava/lang/Object;Ljava/util/Map<TK;TV;>;");
        map.put("java/util/Enumeration",
                "<E:Ljava/lang/Object;>Ljava/lang/Object;");
        map.put("java/util/Iterator",
                "<E:Ljava/lang/Object;>Ljava/lang/Object;");
        map.put("java/util/ListIterator",
                "<E:Ljava/lang/Object;>Ljava/lang/Object;Ljava/util/Iterator<TE;>;");
        map.put("java/lang/Comparable",
                "<T:Ljava/lang/Object;>Ljava/lang/Object;");
        map.put("java/util/Comparator",
                "<T:Ljava/lang/Object;>Ljava/lang/Object;");
        return map;
    }

}
