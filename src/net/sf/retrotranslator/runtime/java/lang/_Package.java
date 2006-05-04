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

import net.sf.retrotranslator.runtime.impl.ClassDescriptor;
import net.sf.retrotranslator.runtime.impl.RuntimeTools;

import java.lang.annotation.Annotation;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * @author Taras Puchko
 */
public class _Package {

    public static Annotation getAnnotation(Package aPackage, Class annotationType) {
        return getPackageInfo(aPackage).getAnnotation(annotationType);
    }

    public static Annotation[] getAnnotations(Package aPackage) {
        return getPackageInfo(aPackage).getAnnotations();
    }

    public static Annotation[] getDeclaredAnnotations(Package aPackage) {
        return getPackageInfo(aPackage).getDeclaredAnnotations();
    }

    public static boolean isAnnotationPresent(Package aPackage, Class annotationType) {
        return getPackageInfo(aPackage).isAnnotationPresent(annotationType);
    }

    private static ClassDescriptor getPackageInfo(Package aPackage) {
        String resourceName = "/" + aPackage.getName().replace('.', '/') + "/package-info.class";
        try {
            return createPackageInfo(_Package.class, resourceName);
        } catch (Throwable t) {
            return getPrivilegedInfo(resourceName);
        }
    }

    private static ClassDescriptor createPackageInfo(Class loader, String resourceName) {
        return new ClassDescriptor(loader, RuntimeTools.readResourceToByteArray(loader, resourceName));
    }

    private static ClassDescriptor getPrivilegedInfo(final String resourceName) {
        return AccessController.doPrivileged(new PrivilegedAction<ClassDescriptor>() {
            public ClassDescriptor run() {
                try {
                    return getContextInfo(resourceName);
                } catch (Throwable e) {
                    return ClassDescriptor.getInstance(_Package.class);
                }
            }
        });
    }

    private static ClassDescriptor getContextInfo(String resourceName) {
        for (Class contextClass : new ExecutionContext().getClassContext()) {
            try {
                return createPackageInfo(contextClass, resourceName);
            } catch (Throwable e) {
                //continue;
            }
        }
        return ClassDescriptor.getInstance(_Package.class);
    }

    private static class ExecutionContext extends SecurityManager {
        public Class[] getClassContext() {
            return super.getClassContext();
        }
    }

}
