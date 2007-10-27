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
package net.sf.retrotranslator.transformer;

import junit.framework.TestCase;
import java.io.IOException;

/**
 * @author Taras Puchko
 */
public class MirandaMethodsVisitorTestCase extends TestCase {

    public static interface GenericInterface<T> {
        T getT();
        void setT(T t);
    }

    public abstract static class BaseClass {
        public String getT() {
            return null;
        }
    }

    public static abstract class FirstAbstractClass implements MirandaTestInterface {
        public void justMethod() {
        }
    }

    public static abstract class SecondAbstractClass extends FirstAbstractClass implements MirandaTestInterface {
        public void justMethod() {
        }
    }

    public static abstract class ThirdAbstractClass extends MirandaTestClass implements MirandaTestInterface {
        public void justMethod() {
        }
    }

    public static abstract class FourthAbstractClass implements GenericInterface<String> {
        public void justMethod() {
        }
        
        public void setT(String s) {
        }
    }

    public static abstract class FifthAbstractClass extends BaseClass implements GenericInterface<String> {
        public void justMethod() {
        }
    }

    public static abstract class SixthAbstractClass implements GenericInterface {
        public Object getT() {
            return null;
        }

        public void setT(Object o) {
        }

        public void justMethod() {
        }
    }

    public static class FirstConcreteClass extends FirstAbstractClass {
        public void method(StringBuilder builder) throws IOException {
        }
    }

    public static class SecondConcreteClass extends SecondAbstractClass {
        public void method(StringBuilder builder) throws IOException {
        }
    }

    public static class ThirdConcreteClass extends ThirdAbstractClass {
        public void method(StringBuilder builder) throws IOException {
        }
    }

    public static class FourthConcreteClass extends FourthAbstractClass {
        public String getT() {
            return null;
        }
    }

    public static class FifthConcreteClass extends FifthAbstractClass {
        public void setT(String s) {
        }
    }

    public static class SixthConcreteClass extends SixthAbstractClass {
    }

    public void testMirandaMethods() throws Exception {
        MirandaTestInterface first = new FirstConcreteClass();
        first.method(null);
        MirandaTestInterface second = new SecondConcreteClass();
        second.method(null);
        String vmName = System.getProperty("java.vm.name");
        if (vmName != null && !vmName.startsWith("IBM J9")) {
            MirandaTestInterface third = new ThirdConcreteClass();
            third.method(null);
        }
        GenericInterface<String> fourth = new FourthConcreteClass();
        fourth.getT();
        fourth.setT(null);
        GenericInterface fifth = new FifthConcreteClass();
        fifth.getT();
        fifth.setT(null);
        GenericInterface sixth = new SixthConcreteClass();
        sixth.getT();
        sixth.setT(null);
    }

    public void testInheritedMethods() throws Exception {
        FirstConcreteClass first = new FirstConcreteClass();
        first.justMethod();
        SecondConcreteClass second = new SecondConcreteClass();
        second.justMethod();
        ThirdConcreteClass third = new ThirdConcreteClass();
        third.justMethod();
        FourthConcreteClass fourth = new FourthConcreteClass();
        fourth.justMethod();
        FifthConcreteClass fifth = new FifthConcreteClass();
        fifth.justMethod();
        SixthConcreteClass sixth = new SixthConcreteClass();
        sixth.justMethod();
    }

}