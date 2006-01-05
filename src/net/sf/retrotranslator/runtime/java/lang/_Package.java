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
package net.sf.retrotranslator.runtime.java.lang;

import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Taras Puchko
 */
public class _Package {

    public static Annotation getAnnotation(Package aPackage, Class annotationType) {
        return _Class.getAnnotation(getPackageInfo(aPackage), annotationType);
    }

    public static Annotation[] getAnnotations(Package aPackage) {
        return _Class.getAnnotations(getPackageInfo(aPackage));
    }

    public static Annotation[] getDeclaredAnnotations(Package aPackage) {
        return _Class.getDeclaredAnnotations(getPackageInfo(aPackage));
    }

    public static boolean isAnnotationPresent(Package aPackage, Class annotationType) {
        return _Class.isAnnotationPresent(getPackageInfo(aPackage), annotationType);
    }

    private static Class<?> getPackageInfo(Package aPackage) {
        try {
            return loadClass(aPackage.getName() + ".package$info");
        } catch (ClassNotFoundException e) {
            return ExecutionContext.class;
        }
    }

    private static Class loadClass(String name) throws ClassNotFoundException {
        for (ClassLoader classLoader : getClassLoaders()) {
            try {
                return Class.forName(name, false, classLoader);
            } catch (ClassNotFoundException e) {
                //ignore
            }
        }
        return Class.forName(name);
    }

    private static Set<ClassLoader> getClassLoaders() {
        Set<ClassLoader> result = new LinkedHashSet<ClassLoader>();
        try {
            for (Class aClass : new ExecutionContext().getClassContext()) {
                result.add(aClass.getClassLoader());
            }
        } catch (SecurityException e) {
            //ignore
        }
        return result;
    }

    public static class ExecutionContext extends SecurityManager {
        public Class[] getClassContext() {
            return super.getClassContext();
        }
    }
}
