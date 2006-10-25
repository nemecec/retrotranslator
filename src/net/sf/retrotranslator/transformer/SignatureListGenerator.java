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
package net.sf.retrotranslator.transformer;

import net.sf.retrotranslator.runtime.asm.*;
import net.sf.retrotranslator.runtime.impl.EmptyVisitor;
import net.sf.retrotranslator.runtime.impl.RuntimeTools;

import java.util.Properties;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * @author Taras Puchko
 */
public class SignatureListGenerator extends EmptyVisitor {

    private static Object[][] CLASSES_14 = {
            {java.util.Collection.class, "<E:Ljava/lang/Object;>Ljava/lang/Object;"},
            {java.util.Set.class},
            {java.util.List.class},
            {java.util.Queue.class},
            {java.util.Map.class},
            {java.util.SortedSet.class},
            {java.util.SortedMap.class},
            {java.util.concurrent.BlockingQueue.class},
            {java.util.concurrent.ConcurrentMap.class},
            {java.util.HashSet.class},
            {java.util.TreeSet.class,
                    "<E:Ljava/lang/Object;>Ljava/util/AbstractSet<TE;>;Ljava/util/SortedSet<TE;>;" +
                            "Ljava/lang/Cloneable;Ljava/io/Serializable;"},
            {java.util.LinkedHashSet.class},
            {java.util.ArrayList.class},
            {java.util.LinkedList.class,
                    "<E:Ljava/lang/Object;>Ljava/util/AbstractSequentialList<TE;>;" +
                            "Ljava/util/List<TE;>;Ljava/lang/Cloneable;Ljava/io/Serializable;"},
            {java.util.PriorityQueue.class,
                    "<E:Ljava/lang/Object;>Ledu/emory/mathcs/backport/java/util/AbstractQueue<TE;>;" +
                            "Ljava/io/Serializable;Ledu/emory/mathcs/backport/java/util/Queue<TE;>;"},
            {java.util.HashMap.class},
            {java.util.TreeMap.class, "<K:Ljava/lang/Object;V:Ljava/lang/Object;>Ljava/util/AbstractMap<TK;TV;>;" +
                    "Ljava/util/SortedMap<TK;TV;>;Ljava/lang/Cloneable;Ljava/io/Serializable;"},
            {java.util.LinkedHashMap.class, "<K:Ljava/lang/Object;V:Ljava/lang/Object;>Ljava/util/HashMap<TK;TV;>;"},
            {java.util.Vector.class},
            {java.util.Hashtable.class},
            {java.util.WeakHashMap.class},
            {java.util.IdentityHashMap.class},
            {java.util.concurrent.CopyOnWriteArrayList.class},
            {java.util.concurrent.CopyOnWriteArraySet.class},
            {java.util.EnumSet.class,
                    "<E:Lnet/sf/retrotranslator/runtime/java/lang/Enum_<TE;>;>Ljava/util/HashSet<TE;>;"},
            {java.util.EnumMap.class,
                    "<K:Lnet/sf/retrotranslator/runtime/java/lang/Enum_<TK;>;V:Ljava/lang/Object;>" +
                            "Ljava/util/TreeMap<TK;TV;>;"},
            {java.util.concurrent.ConcurrentLinkedQueue.class},
            {java.util.concurrent.LinkedBlockingQueue.class},
            {java.util.concurrent.ArrayBlockingQueue.class},
            {java.util.concurrent.PriorityBlockingQueue.class},
            {java.util.concurrent.DelayQueue.class},
            {java.util.concurrent.SynchronousQueue.class},
            {java.util.concurrent.ConcurrentHashMap.class,
                    "<K:Ljava/lang/Object;V:Ljava/lang/Object;>Ledu/emory/mathcs/backport/java/util/AbstractMap<TK;TV;>;" +
                            "Ledu/emory/mathcs/backport/java/util/concurrent/ConcurrentMap<TK;TV;>;Ljava/io/Serializable;"},
            {java.util.AbstractCollection.class},
            {java.util.AbstractSet.class},
            {java.util.AbstractList.class},
            {java.util.AbstractSequentialList.class},
            {java.util.AbstractQueue.class,
                    "<E:Ljava/lang/Object;>Ledu/emory/mathcs/backport/java/util/AbstractCollection<TE;>;" +
                            "Ledu/emory/mathcs/backport/java/util/Queue<TE;>;"},
            {java.util.AbstractMap.class},
            {java.util.Enumeration.class},
            {java.lang.Iterable.class},
            {java.util.Iterator.class},
            {java.util.ListIterator.class},
            {java.lang.Comparable.class},
            {java.util.Comparator.class}
    };

    private Properties properties = new Properties();
    private String specialSignature;

    public static void main(String[] args) throws Exception {
        new SignatureListGenerator().execute(args[0]);
    }

    private void execute(String fileName) throws Exception {
        ClassTransformer classTransformer = new ClassTransformer(false, false, false, false,
                null, null, new BackportLocatorFactory(null));
        for (Object[] objects : CLASSES_14) {
            Class aClass = (Class) objects[0];
            specialSignature = objects.length > 1 ? (String) objects[1] : null;
            byte[] bytes = RuntimeTools.getBytecode(aClass);
            bytes = classTransformer.transform(bytes, 0, bytes.length);
            new ClassReader(bytes).accept(this, true);
        }
        OutputStream outputStream = new FileOutputStream(fileName);
        try {
            properties.store(outputStream, null);
        } finally {
            outputStream.close();
        }
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        if (specialSignature != null) {
            signature = specialSignature;
        }
        if (signature != null) {
            properties.setProperty(name, signature);
        }
    }
}
