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
package net.sf.retrotranslator.tests;

import java.io.*;
import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.Properties;
import junit.framework.TestCase;

/**
 * A JUnit test class for JDK1.5 to JDK1.4 bytecode & annotation transformation
 * using Retrotranslator.
 * <p/>
 * This testsuite introduces several annotations that can be useful in a JUnit
 * environment that makes use annotations like the upcoming JUnit 4 version will do.
 * Besides testing for positive and negative outcomes it provides several annotation
 * combination at the class and method level and uses different types for the
 * annotation members. Furthermore it checks if we can do something useful when
 * detecting annotations.
 *
 * @author Klaus P. Berg
 */
@AnnotationProcessingTestCase.Author(name = "Klaus P. Berg", mailTo = "Klaus-Peter.Berg@company.com")
@AnnotationProcessingTestCase.TestingExternalAPI()
public final class AnnotationProcessingTestCase extends TestCase {

    private PrintWriter out = new PrintWriter(new OutputStream() {
        public void write(int b) throws IOException {
        }
    });

    private static int nbrAnnotatedMethodsSuccessfullyExcecuted;
    private static int invocationCount = 1;

    /**
     * The type of this invocation if it is duplicated.
     */
    private InvocationTypes[] invocationTypes;
    /**
     * The method of this invocation if it is duplicated.
     */
    private Method duplicateAnnotatedMethod;
    /**
     * String parameters to pass on if it is duplicated.
     */
    private String[] stringArgs;
    /**
     * Int parameters to pass on if it is duplicated.
     */
    private int[] intArgs;
    /**
     * Char parameters to pass on if it is duplicated.
     */
    private char[] charArgs;
    /**
     * Boolean parameters to pass on if it is duplicated.
     */
    private boolean[] booleanArgs;
    /**
     * Number of parameters that will be passed to the invoked test case.
     */
    private int numParams;

    public AnnotationProcessingTestCase(final String name) {
        super(name);
    }

    public void testConstructor() {
        final AnnotationProcessingTestCase test = new AnnotationProcessingTestCase(
                "test");
        assertNotNull(test);
    }

    // --- Test @Author annotation --------------------------------------------

    public void testAuthorAnnotationPresence() {
        final Class<? extends AnnotationProcessingTestCase> annotatedClass = this
                .getClass();
        assertTrue(annotatedClass.isAnnotationPresent(Author.class));
    }

    public void testAuthorAnnotationAbsence() {
        final Class<String> annotatedClass = String.class;
        assertFalse(annotatedClass.isAnnotationPresent(Author.class));
    }

    public void testAuthorAnnotationValues() {
        final Class<? extends AnnotationProcessingTestCase> annotatedClass = this
                .getClass();
        assertTrue(annotatedClass.isAnnotationPresent(Author.class));
        final Author author = annotatedClass.getAnnotation(Author.class);
        assertEquals("Klaus P. Berg", author.name());
        assertEquals("Klaus-Peter.Berg@company.com", author.mailTo());
    }

    // --- Test @TestingExternalAPI annotation --------------------------------

    public void testTestingExternalAPIAnnotationPresence() {
        final Class<? extends AnnotationProcessingTestCase> annotatedClass = this
                .getClass();
        assertTrue(annotatedClass.isAnnotationPresent(TestingExternalAPI.class));
    }

    public void testTestingExternalAPIAnnotationAbsence() {
        final Class<String> annotatedClass = String.class;
        assertFalse(annotatedClass
                .isAnnotationPresent(TestingExternalAPI.class));
    }

    public void testTestingExternalAPIAnnotationValues() {
        final Class<? extends AnnotationProcessingTestCase> annotatedClass = this
                .getClass();
        assertTrue(annotatedClass.isAnnotationPresent(TestingExternalAPI.class));
        final TestingExternalAPI testingExternalAPI = annotatedClass
                .getAnnotation(TestingExternalAPI.class);
        final String[] value = testingExternalAPI.value();
        assertEquals(1, value.length);

        final Class<ClassWithTestingApiAnnotation> annotatedInnerClass = ClassWithTestingApiAnnotation.class;
        assertTrue(annotatedInnerClass
                .isAnnotationPresent(TestingExternalAPI.class));
    }

    public void testTestingExternalAPIAnnotationValuesForInnerClasses() {
        final Class<ClassWithTestingApiAnnotation> annotatedClass = ClassWithTestingApiAnnotation.class;
        assertTrue(annotatedClass.isAnnotationPresent(TestingExternalAPI.class));
        final TestingExternalAPI testingExternalAPI = annotatedClass
                .getAnnotation(TestingExternalAPI.class);
        final String[] value = testingExternalAPI.value();
        assertEquals(2, value.length);
        assertEquals("First API", value[0]);
        assertEquals("Second API", value[1]);
    }

    // --- Test @Ignore annotation --------------------------------------------

    public void testIgnoreAnnotationPresence() {
        final Method[] annotatedMethods = this.getClass().getDeclaredMethods();
        for (Method method : annotatedMethods) {
            if (Modifier.isPrivate(method.getModifiers())) {
                assertTrue(method.isAnnotationPresent(Ignore.class));
            }
        }
    }

    public void testIgnoreAnnotationAbsence() {
        final Method[] annotatedMethods = this.getClass().getDeclaredMethods();
        for (Method method : annotatedMethods) {
            if (Modifier.isPublic(method.getModifiers())) {
                assertFalse(method.isAnnotationPresent(Ignore.class));
            }
        }
    }

    public void testIgnoreAnnotationValues() {
        final Properties expectedFirstIndexVales = new Properties();
        expectedFirstIndexVales.put("prepareParameterArgumentArray",
                "test under construction");
        expectedFirstIndexVales.put("setParamsFromDuplicateAnnotation",
                "test under construction");
        expectedFirstIndexVales.put("invokeDuplicatedTestMethod",
                "private method");
        expectedFirstIndexVales.put("executeAnnotatedMethod",
                "another private method");
        final Method[] annotatedMethods = this.getClass().getDeclaredMethods();
        for (Method method : annotatedMethods) {
            if (Modifier.isPrivate(method.getModifiers())) {
                final Ignore ignore = method.getAnnotation(Ignore.class);
                assertEquals("KPB", ignore.initials());
                assertEquals(expectedFirstIndexVales.get(method.getName()),
                        ignore.reasons()[0]);
                if (!"executeAnnotatedMethod".equals(method.getName())) {
                    assertEquals(1, ignore.reasons().length);
                } else {
                    assertEquals(2, ignore.reasons().length);
                    assertEquals("helper method", ignore.reasons()[1]);
                }
            }
        }
    }

    // --- Test multiple class level annotations -----------------------------

    public void testMultipleClassLevelAnnotations() {
        // Note: inner classes MUST be instantiated; do NOT use
        // 'ClassWithMultipleClassLevelAnnotations.class'!
        final Class<ClassWithMultipleClassLevelAnnotations> annotatedClass = ClassWithMultipleClassLevelAnnotations.class;
        assertTrue(annotatedClass.isAnnotationPresent(Author.class));
        assertTrue(annotatedClass.isAnnotationPresent(TestingExternalAPI.class));

        final Author author = annotatedClass.getAnnotation(Author.class);
        assertEquals("Klaus P. Berg", author.name());
        assertEquals("Klaus-Peter.berg@web.de", author.mailTo());

        final TestingExternalAPI testingExternalAPI = annotatedClass
                .getAnnotation(TestingExternalAPI.class);
        final String[] value = testingExternalAPI.value();
        assertEquals(2, value.length);
        assertEquals("First API", value[0]);
        assertEquals("Second API", value[1]);
    }

    // --- Test @Duplicate annotation ----------------------------------------

    public void testDuplicateAnnotationPresence() {
        final Method[] annotatedMethods = this.getClass().getDeclaredMethods();
        for (Method method : annotatedMethods) {
            if (method.getName().startsWith("annotated")) {
                assertTrue(method.isAnnotationPresent(Duplicate.class));
            }
        }

        final Method[] annotatedMethods2 = ClassWithDefaultMethodLevelAnnotationsOnly.class
                .getDeclaredMethods();
        for (Method method : annotatedMethods2) {
            if (method.getName().startsWith("test")) {
                assertTrue(method.isAnnotationPresent(Duplicate.class));
            }
        }
    }

    public void testDuplicateAnnotationAbsence() {
        final Method[] annotatedMethods = this.getClass().getDeclaredMethods();
        for (Method method : annotatedMethods) {
            if (!method.getName().startsWith("annotated")) {
                assertFalse(method.isAnnotationPresent(Duplicate.class));
            }
        }
    }

    public void testMethodsWithDuplicateAnnotation() throws Exception {
        final Method[] annotatedMethods = this.getClass().getDeclaredMethods();
        int nbrAnnotatedMethods = 0;
        for (Method method : annotatedMethods) {
            if (method.getName().startsWith("annotated")) {
                nbrAnnotatedMethods++;
                duplicateAnnotatedMethod = method;
                setParamsFromDuplicateAnnotation();
                prepareParameterArgumentArray();
                invokeDuplicatedTestMethod();
            }
        }
        // if the invocation type is not set specifically to Async or Sync,
        // every annotated method is executed twice, once for Async and once for Sync
        // because we have two times specified an explicit invocation type in the method
        // annotation we will get the following assert:
        assertEquals((nbrAnnotatedMethods * 2) - 2,
                nbrAnnotatedMethodsSuccessfullyExcecuted);
    }

    // --- Test multiple method level annotations ----------------------------
    public void testMultipleMethodLevelAnnotations() throws SecurityException,
            NoSuchMethodException, IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {
        final ClassWithMultipleMethodLevelAnnotations classWithMultipleMethodLevelAnnotations = new ClassWithMultipleMethodLevelAnnotations();
        final Method method1 = classWithMultipleMethodLevelAnnotations
                .getClass().getDeclaredMethod("method1", (Class[]) null);
        final Ignore ignore1 = method1.getAnnotation(Ignore.class);
        assertTrue(method1.isAnnotationPresent(Ignore.class));
        assertEquals("KPB", ignore1.initials());

        final Method method2 = classWithMultipleMethodLevelAnnotations
                .getClass().getDeclaredMethod(
                "method2",
                new Class[]{InvocationTypes.class, String[].class,
                        int[].class, char[].class, boolean[].class});

        assertTrue(method2.isAnnotationPresent(Ignore.class));
        final Ignore ignore2 = method2.getAnnotation(Ignore.class);
        assertEquals("KPB", ignore2.initials());
        assertEquals("test under construction", ignore2.reasons()[0]);
        assertTrue(method2.isAnnotationPresent(Duplicate.class));
        final Duplicate duplicate = method2.getAnnotation(Duplicate.class);
//        out.println("duplicate.toString():\n" + duplicate.toString());
//        assertEquals(
//                '@'
//                        + Duplicate.class.getName()
//                        + "(stringArgs=[1, 2, 3, 1], "
//                        + "intArgs=[1, 2, 3, 1], booleanArgs=[true, false, false, true], "
//                        + "charArgs=[1, 2, 3, 1], invocationTypes=[Sync])",
//                duplicate.toString());
        method2.invoke(classWithMultipleMethodLevelAnnotations, new Object[]{
                duplicate.invocationTypes()[0], duplicate.stringArgs(),
                duplicate.intArgs(), duplicate.charArgs(),
                duplicate.booleanArgs()});
    }

    public void testTotalNumberOfDifferentAnnotationsPerClass() {
        final Class<? extends AnnotationProcessingTestCase> annotatedClass = this
                .getClass();
        final Annotation[] allAnnotations = annotatedClass
                .getDeclaredAnnotations();
        out.println("\nAll annotations for class "
                + this.getClass().getName() + ':');
        for (Annotation annotation : allAnnotations) {
            out.println(annotation);
        }
        assertEquals(2, allAnnotations.length);
    }

    public void testTotalNumberOfDifferentAnnotationsPerClass2() {
        final Class<ClassWithMultipleClassLevelAnnotations> annotatedClass = ClassWithMultipleClassLevelAnnotations.class;
        final Annotation[] allAnnotations = annotatedClass.getDeclaredAnnotations();
        out.println("\nAll annotations for class ClassWithMultipleMethodLevelAnnotations:");
        for (Annotation annotation : allAnnotations) {
            out.println(annotation);
        }
        assertEquals(2, allAnnotations.length);
    }

    // --- Helper methods ----------------------------------------------------

    @Ignore(initials = "KPB")
    private void prepareParameterArgumentArray() {
        final Class[] paramTypes = duplicateAnnotatedMethod.getParameterTypes();
        numParams = 1;
        final int actualNumParams = paramTypes.length;
        if (stringArgs.length > 0) {
            numParams++;
        }
        if (intArgs.length > 0) {
            numParams++;
        }
        if (charArgs.length > 0) {
            numParams++;
        }
        if (booleanArgs.length > 0) {
            numParams++;
        }

        if (actualNumParams != numParams) {
            throw new RuntimeException(duplicateAnnotatedMethod
                    + ": Duplicated test case must have exactly " + numParams
                    + " parameter(s)");
        }
        if (paramTypes[0] != InvocationTypes.class) {
            throw new RuntimeException(duplicateAnnotatedMethod
                    + ": Duplicated test case's parameter #1 must "
                    + "be of type 'InvocationTypes'");
        }
    }

    @Ignore(initials = "KPB")
    private void setParamsFromDuplicateAnnotation() {
        final Duplicate duplicate = duplicateAnnotatedMethod
                .getAnnotation(Duplicate.class);
        invocationTypes = duplicate.invocationTypes();
        stringArgs = duplicate.stringArgs();
        intArgs = duplicate.intArgs();
        charArgs = duplicate.charArgs();
        booleanArgs = duplicate.booleanArgs();
    }

    @Ignore(initials = "KPB", reasons = {"private method"})
    private void invokeDuplicatedTestMethod() throws Exception {
        final Object[] args = new Object[numParams];
        for (InvocationTypes invocationType : invocationTypes) {
            args[0] = invocationType;
            executeAnnotatedMethod(args);
            duplicateAnnotatedMethod.invoke(this, args);
        }
    }

    @Ignore(initials = "KPB", reasons = {"another private method",
            "helper method"})
    private void executeAnnotatedMethod(final Object[] args) {
        int i = 1;
        // Include each additional parameter that was specified in the
        // Duplicate annotation
        if (stringArgs.length > 0) {
            args[i++] = stringArgs;
        }
        if (intArgs.length > 0) {
            args[i++] = intArgs;
        }
        if (charArgs.length > 0) {
            args[i++] = charArgs;
        }
        if (booleanArgs.length > 0) {
            args[i] = booleanArgs;
        }
    }

    // /////////////////////////////////////////////////////////////////////////

    @Duplicate
    public void annotatedMethodWithDefaultDuplicate(final InvocationTypes type) {
        out.println("*** duplicated method 'annotatedMethodWithDefaultDuplicate' called with type " + type);
        nbrAnnotatedMethodsSuccessfullyExcecuted++;
    }

    @Duplicate(stringArgs = {"1", "2", "3", "1"})
    public void annotatedStringArgs(final InvocationTypes type,
                                    final String[] strArgs) {
        assertEquals(InvocationTypes.values()[invocationCount--], type);
        assertEquals("1", strArgs[0]);
        assertEquals("2", strArgs[1]);
        assertEquals("3", strArgs[2]);
        assertEquals("1", strArgs[3]);
        nbrAnnotatedMethodsSuccessfullyExcecuted++;
    }

    @Duplicate(intArgs = {1, 2, 3, 1})
    public void annotatedIntArgs(@SuppressWarnings("unused")
    final InvocationTypes type, final int[] intArgs1) {
        assertEquals(1, intArgs1[0]);
        assertEquals(2, intArgs1[1]);
        assertEquals(3, intArgs1[2]);
        assertEquals(1, intArgs1[3]);
        nbrAnnotatedMethodsSuccessfullyExcecuted++;
    }

    @Duplicate(charArgs = {'1', '2', '3', '1'})
    public void annotatedCharArgs(@SuppressWarnings("unused")
    final InvocationTypes type, final char[] charArgs1) {
        assertEquals('1', charArgs1[0]);
        assertEquals('2', charArgs1[1]);
        assertEquals('3', charArgs1[2]);
        assertEquals('1', charArgs1[3]);
        nbrAnnotatedMethodsSuccessfullyExcecuted++;
    }

    @Duplicate(booleanArgs = {true, false, false, true})
    public void annotatedBooleanArgs(@SuppressWarnings("unused")
    final InvocationTypes type, final boolean[] booleanArgs1) {
        assertTrue(booleanArgs1[0]);
        assertFalse(booleanArgs1[1]);
        assertFalse(booleanArgs1[2]);
        assertTrue(booleanArgs1[3]);
        nbrAnnotatedMethodsSuccessfullyExcecuted++;
    }

    @Duplicate(stringArgs = {"1", "2", "3", "1"}, intArgs = {1, 2, 3, 1})
    public void annotatedMixedArgs1(@SuppressWarnings("unused")
    final InvocationTypes type, final String[] strArgs, final int[] intArgs1) {
        assertEquals(1, intArgs1[0]);
        assertEquals(2, intArgs1[1]);
        assertEquals(3, intArgs1[2]);
        assertEquals(1, intArgs1[3]);
        assertEquals("1", strArgs[0]);
        assertEquals("2", strArgs[1]);
        assertEquals("3", strArgs[2]);
        assertEquals("1", strArgs[3]);
        nbrAnnotatedMethodsSuccessfullyExcecuted++;
    }

    @Duplicate(stringArgs = {"1", "2", "3", "1"}, charArgs = {'1', '2', '3',
            '1'})
    public void annotatedMixedArgs2(@SuppressWarnings("unused")
    final InvocationTypes type, final String[] strArgs, final char[] charArgs1) {
        assertEquals("1", strArgs[0]);
        assertEquals("2", strArgs[1]);
        assertEquals("3", strArgs[2]);
        assertEquals("1", strArgs[3]);
        assertEquals('1', charArgs1[0]);
        assertEquals('2', charArgs1[1]);
        assertEquals('3', charArgs1[2]);
        assertEquals('1', charArgs1[3]);
        nbrAnnotatedMethodsSuccessfullyExcecuted++;
    }

    @Duplicate(stringArgs = {"1", "2", "3", "1"}, booleanArgs = {true, false,
            false, true})
    public void annotatedMixedArgs3(@SuppressWarnings("unused")
    final InvocationTypes type, final String[] strArgs,
                                final boolean[] booleanArgs1) {
        assertTrue(booleanArgs1[0]);
        assertFalse(booleanArgs1[1]);
        assertFalse(booleanArgs1[2]);
        assertTrue(booleanArgs1[3]);
        assertEquals("1", strArgs[0]);
        assertEquals("2", strArgs[1]);
        assertEquals("3", strArgs[2]);
        assertEquals("1", strArgs[3]);
        nbrAnnotatedMethodsSuccessfullyExcecuted++;
    }

    @Duplicate(intArgs = {1, 2, 3, 1}, charArgs = {'1', '2', '3', '1'})
    public void annotatedMixedArgs4(@SuppressWarnings("unused")
    final InvocationTypes type, @SuppressWarnings("hiding")
    final int[] intArgs1, final char[] charArgs1) {
        assertEquals('1', charArgs1[0]);
        assertEquals('2', charArgs1[1]);
        assertEquals('3', charArgs1[2]);
        assertEquals('1', charArgs1[3]);
        assertEquals(1, intArgs1[0]);
        assertEquals(2, intArgs1[1]);
        assertEquals(3, intArgs1[2]);
        assertEquals(1, intArgs1[3]);
        nbrAnnotatedMethodsSuccessfullyExcecuted++;
    }

    @Duplicate(intArgs = {1, 2, 3, 1}, booleanArgs = {true, false, false, true})
    public void annotatedMixedArgs5(@SuppressWarnings("unused")
    final InvocationTypes type, final int[] intArgs1,
                                final boolean[] booleanArgs1) {
        assertTrue(booleanArgs1[0]);
        assertFalse(booleanArgs1[1]);
        assertFalse(booleanArgs1[2]);
        assertTrue(booleanArgs1[3]);
        assertEquals(1, intArgs1[0]);
        assertEquals(2, intArgs1[1]);
        assertEquals(3, intArgs1[2]);
        assertEquals(1, intArgs1[3]);
        nbrAnnotatedMethodsSuccessfullyExcecuted++;
    }

    @Duplicate(charArgs = {'1', '2', '3', '1'}, booleanArgs = {true, false,
            false, true})
    public void annotatedMixedArgs6(@SuppressWarnings("unused")
    final InvocationTypes type, final char[] charArgs1,
                                final boolean[] booleanArgs1) {
        assertTrue(booleanArgs1[0]);
        assertFalse(booleanArgs1[1]);
        assertFalse(booleanArgs1[2]);
        assertTrue(booleanArgs1[3]);
        assertEquals('1', charArgs1[0]);
        assertEquals('2', charArgs1[1]);
        assertEquals('3', charArgs1[2]);
        assertEquals('1', charArgs1[3]);
        nbrAnnotatedMethodsSuccessfullyExcecuted++;
    }

    @Duplicate(stringArgs = {"1", "2", "3", "1"}, intArgs = {1, 2, 3, 1}, charArgs = {
            '1', '2', '3', '1'})
    public void annotatedMixedArgs7(@SuppressWarnings("unused")
    final InvocationTypes type, final String[] strArgs, final int[] intArgs1,
                                final char[] charArgs1) {
        assertEquals("1", strArgs[0]);
        assertEquals("2", strArgs[1]);
        assertEquals("3", strArgs[2]);
        assertEquals("1", strArgs[3]);
        assertEquals(1, intArgs1[0]);
        assertEquals(2, intArgs1[1]);
        assertEquals(3, intArgs1[2]);
        assertEquals(1, intArgs1[3]);
        assertEquals('1', charArgs1[0]);
        assertEquals('2', charArgs1[1]);
        assertEquals('3', charArgs1[2]);
        assertEquals('1', charArgs1[3]);
        nbrAnnotatedMethodsSuccessfullyExcecuted++;
    }

    @Duplicate(stringArgs = {"1", "2", "3", "1"}, intArgs = {1, 2, 3, 1}, booleanArgs = {
            true, false, false, true})
    public void annotatedMixedArgs8(@SuppressWarnings("unused")
    final InvocationTypes type, final String[] strArgs, final int[] intArgs1,
                                final boolean[] booleanArgs1) {
        assertTrue(booleanArgs1[0]);
        assertFalse(booleanArgs1[1]);
        assertFalse(booleanArgs1[2]);
        assertTrue(booleanArgs1[3]);
        assertEquals("1", strArgs[0]);
        assertEquals("2", strArgs[1]);
        assertEquals("3", strArgs[2]);
        assertEquals("1", strArgs[3]);
        assertEquals(1, intArgs1[0]);
        assertEquals(2, intArgs1[1]);
        assertEquals(3, intArgs1[2]);
        assertEquals(1, intArgs1[3]);
        nbrAnnotatedMethodsSuccessfullyExcecuted++;
    }

    @Duplicate(stringArgs = {"1", "2", "3", "1"}, charArgs = {'1', '2', '3',
            '1'}, booleanArgs = {true, false, false, true})
    public void annotatedMixedArgs9(@SuppressWarnings("unused")
    final InvocationTypes type, final String[] strArgs, final char[] charArgs1,
                                final boolean[] booleanArgs1) {
        assertTrue(booleanArgs1[0]);
        assertFalse(booleanArgs1[1]);
        assertFalse(booleanArgs1[2]);
        assertTrue(booleanArgs1[3]);
        assertEquals("1", strArgs[0]);
        assertEquals("2", strArgs[1]);
        assertEquals("3", strArgs[2]);
        assertEquals("1", strArgs[3]);
        assertEquals('1', charArgs1[0]);
        assertEquals('2', charArgs1[1]);
        assertEquals('3', charArgs1[2]);
        assertEquals('1', charArgs1[3]);
        nbrAnnotatedMethodsSuccessfullyExcecuted++;
    }

    @Duplicate(invocationTypes = {InvocationTypes.Async}, intArgs = {1, 2, 3, 1}, charArgs = {
            '1', '2', '3', '1'}, booleanArgs = {true, false, false, true})
    public void annotatedMixedArgs10(final InvocationTypes type,
                                     final int[] intArgs1, final char[] charArgs1,
                                     final boolean[] booleanArgs1) {
        assertEquals(InvocationTypes.Async, type);
        assertTrue(booleanArgs1[0]);
        assertFalse(booleanArgs1[1]);
        assertFalse(booleanArgs1[2]);
        assertTrue(booleanArgs1[3]);
        assertEquals('1', charArgs1[0]);
        assertEquals('2', charArgs1[1]);
        assertEquals('3', charArgs1[2]);
        assertEquals('1', charArgs1[3]);
        assertEquals(1, intArgs1[0]);
        assertEquals(2, intArgs1[1]);
        assertEquals(3, intArgs1[2]);
        assertEquals(1, intArgs1[3]);
        nbrAnnotatedMethodsSuccessfullyExcecuted++;
    }

    @Duplicate(invocationTypes = {InvocationTypes.Sync}, stringArgs = {"1", "2", "3", "1"}, intArgs = {
            1, 2, 3, 1}, charArgs = {'1', '2', '3', '1'}, booleanArgs = {true,
            false, false, true})
    public void annotatedAllArgs(final InvocationTypes type,
                                 final String[] strArgs, final int[] intArgs1,
                                 final char[] charArgs1, final boolean[] booleanArgs1) {
        assertEquals(InvocationTypes.Sync, type);
        assertTrue(booleanArgs1[0]);
        assertFalse(booleanArgs1[1]);
        assertFalse(booleanArgs1[2]);
        assertTrue(booleanArgs1[3]);
        assertEquals("1", strArgs[0]);
        assertEquals("2", strArgs[1]);
        assertEquals("3", strArgs[2]);
        assertEquals("1", strArgs[3]);
        assertEquals('1', charArgs1[0]);
        assertEquals('2', charArgs1[1]);
        assertEquals('3', charArgs1[2]);
        assertEquals('1', charArgs1[3]);
        assertEquals(1, intArgs1[0]);
        assertEquals(2, intArgs1[1]);
        assertEquals(3, intArgs1[2]);
        assertEquals(1, intArgs1[3]);
        nbrAnnotatedMethodsSuccessfullyExcecuted++;
    }

    @TestingExternalAPI({"First API", "Second API"})
    private static class ClassWithTestingApiAnnotation {
        // no methods
    }

    @Author(name = "Klaus P. Berg", mailTo = "Klaus-Peter.berg@web.de")
    @TestingExternalAPI({"First API", "Second API"})
    private static class ClassWithMultipleClassLevelAnnotations {
        // no methods
    }

    // This class has NO class level annotations!
    private static class ClassWithMultipleMethodLevelAnnotations {

        @Ignore(initials = "KPB")
        public void method1() {
            // no code necessary
        }

        @Ignore(initials = "KPB")
        @Duplicate(invocationTypes = {InvocationTypes.Sync}, stringArgs = {"1", "2", "3", "1"}, intArgs = {
                1, 2, 3, 1}, charArgs = {'1', '2', '3', '1'}, booleanArgs = {
                true, false, false, true})
        public void method2(final InvocationTypes type, final String[] strArgs,
                            final int[] intArgs1, final char[] charArgs1,
                            final boolean[] booleanArgs1) {
            assertEquals(InvocationTypes.Sync, type);
            assertTrue(booleanArgs1[0]);
            assertFalse(booleanArgs1[1]);
            assertFalse(booleanArgs1[2]);
            assertTrue(booleanArgs1[3]);
            assertEquals("1", strArgs[0]);
            assertEquals("2", strArgs[1]);
            assertEquals("3", strArgs[2]);
            assertEquals("1", strArgs[3]);
            assertEquals('1', charArgs1[0]);
            assertEquals('2', charArgs1[1]);
            assertEquals('3', charArgs1[2]);
            assertEquals('1', charArgs1[3]);
            assertEquals(1, intArgs1[0]);
            assertEquals(2, intArgs1[1]);
            assertEquals(3, intArgs1[2]);
            assertEquals(1, intArgs1[3]);
        }
    }

    // This class has no class level annotations and the method annotations have default values only
    private static class ClassWithDefaultMethodLevelAnnotationsOnly {

        @Duplicate
        public void testMethod1(final InvocationTypes type) {
            assertTrue(type.equals(InvocationTypes.Async)
                    || type.equals(InvocationTypes.Sync));
        }

        @Duplicate
        public void testMethod2(final InvocationTypes type) {
            assertTrue(type.equals(InvocationTypes.Async)
                    || type.equals(InvocationTypes.Sync));
        }
    }

    ///////////////////////////////////////////////////////////////////////////////

    /* Annotation classes */

    /**
     * Author name annotation.
     *
     * @author Klaus Berg
     */

    @Documented
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
            @interface Author {
        /** the responsible author's name */
        String name();

        /** the responsible author's mail address */
        String mailTo();
    }

    /**
     * Duplicate annotation specifies that a particular test case will be duplicated for each
     * parameter type specified. Primarily, this annotation can be used to mark
     * tests that should be invoked in sync & async mode. But the same annotation
     * can also be used to provide additional arguments of type String, int, char,
     * and boolean to a test method. In general, these arguments will be used to
     * specify 'invalid' input arguments specific to a test method.
     *
     * @author Klaus Berg, Mike Stone
     */
    @Documented
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
            @interface Duplicate {
        /** the test method invocation type(s) */
        InvocationTypes[] invocationTypes() default {InvocationTypes.Async, InvocationTypes.Sync};

        /** invalid String args handed over as method parameters */
        String[] stringArgs() default {};

        /** invalid int args handed over as method parameters */
        int[] intArgs() default {};

        /** invalid char args handed over as method parameters */
        char[] charArgs() default {};

        /** invalid boolean args handed over as method parameters */
        boolean[] booleanArgs() default {};
    }

    /**
     * Ignore functional test annotation.
     *
     * @author Klaus Berg
     */
    @Documented
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
            @interface Ignore {
        /** the reasons why a test method should be ignored */
        String[] reasons() default {"test under construction"};

        /** the initials of the person who decided that the test method should be ignored */
        String initials();
    }

    /**
     * Test method invocation types.
     *
     * @author Klaus P. Berg
     */
    enum InvocationTypes {
        Sync, /**
     * Specifies a synchronous duplicated test.
     */
    Async /** Specifies an asynchronous duplicated test. */
    }

    /**
     * Declare external API classes that should be tested.
     *
     * @author Klaus Berg
     */

    @Documented
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
            @interface TestingExternalAPI {
        /** A list of all 'external interfaces' tested by the annotated class */
        String[] value() default {""};
    }
}
