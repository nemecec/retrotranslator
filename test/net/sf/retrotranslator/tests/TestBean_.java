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
package net.sf.retrotranslator.tests;

/**
 * @author Taras Puchko
 */
public class TestBean_ {

    private boolean visible;
    private char sign;
    private byte code;
    private short width;
    private int height;
    private float opacity;
    private long area;
    private double weight;
    private String name;
    private String direction;
    private String state;

    public TestBean_(
            boolean visible, char sign, byte code, short width,
            int height, float opacity, long area, double weight, String name) {
        this.direction = "right";
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

    public TestBean_(
            String name, double weight, long area, float opacity,
            int height, short width, byte code, char sign, boolean visible) {
        this.direction = "reverse";
        this.name = name;
        this.weight = weight;
        this.area = area;
        this.opacity = opacity;
        this.height = height;
        this.width = width;
        this.code = code;
        this.sign = sign;
        this.visible = visible;
    }

    public TestBean_(String state) {
        this.direction = "A";
        this.state = state;
    }

    public TestBean_() {
        this.direction = "B";
    }

    public boolean isVisible() {
        return visible;
    }

    public char getSign() {
        return sign;
    }

    public byte getCode() {
        return code;
    }

    public short getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public float getOpacity() {
        return opacity;
    }

    public long getArea() {
        return area;
    }

    public double getWeight() {
        return weight;
    }

    public String getName() {
        return name;
    }

    public String getDirection() {
        return direction;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
