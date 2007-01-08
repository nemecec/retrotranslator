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
package net.sf.retrotranslator.tests.custom;

import net.sf.retrotranslator.tests.TestBean;

/**
 * @author Taras Puchko
 */
public class _TestBean {

    public static class FirstTestBeanBuilder {
        private boolean visible;
        private char sign;
        private byte code;
        private short width;
        private int height;
        private float opacity;
        private long area;
        private double weight;
        private String name;

        protected FirstTestBeanBuilder(
                boolean visible, char sign, byte code, short width,
                int height, float opacity, long area, double weight, String name) {
            this.visible = visible;
            this.sign = sign;
            this.code = code;
            this.width = width;
            this.height = height;
            this.opacity = opacity;
            this.area = area;
            this.weight = weight;
            this.name = name;
        }

        public String argument1() {
            return name;
        }

        public double argument2() {
            return weight;
        }

        public long argument3() {
            return area;
        }

        public float argument4() {
            return opacity;
        }

        public int argument5() {
            return height;
        }

        public short argument6() {
            return width;
        }

        public byte argument7() {
            return code;
        }

        public boolean argument9() {
            return visible;
        }

        public char argument8() {
            return sign;
        }

        public void initialize(TestBean testBean) {
            testBean.setState("initialized");
        }

    }

    public static class SecondTestBeanBuilder {

        private String state;

        public SecondTestBeanBuilder(String state) {
            this.state = state;
        }

        public void initialize(TestBean testBean) {
            testBean.setState(state);
        }

    }

    public static FirstTestBeanBuilder createInstanceBuilder(
            boolean visible, char sign, byte code, short width,
            int height, float opacity, long area, double weight, String name) {
        return new FirstTestBeanBuilder(visible, sign, code, width, height, opacity, area, weight, name);
    }

    public static SecondTestBeanBuilder createInstanceBuilder(String state) {
        return new SecondTestBeanBuilder(state);
    }

}
