<a href="http://www.jetbrains.com/idea/"><img
                src="http://resources.jetbrains.com/storage/products/intellij-idea/img/meta/intellij-idea_logo_300x300.png"
                width="100" height="100" border="0" alt="The best Java IDE" align="right"/></a>

## Retrotranslator


#### Contents

1.  [What is Retrotranslator?](#what)
2.  [What Java 5.0 features are supported?](#features)
3.  [How to use Retrotranslator from the command line?](#commandline)
4.  [How to use Retrotranslator from Apache Ant or Maven?](#ant)
5.  [How to use Retrotranslator from IntelliJ IDEA?](#idea)
6.  [How to produce a jar file compatible with Java 1.4?](#jarfile)
7.  [How to use Just-in-Time Retrotranslator?](#jit)
8.  [What API is supported on Java 1.4 and Java 1.3?](#supported)
9.  [How to write an extension?](#extension)
10.  [What are the limitations?](#limitations)
11.  [Alternative tools](#alternative)
12.  [Contact](#contact)
13.  [License](#license)

#### <a name="what">What is Retrotranslator?</a>

Retrotranslator is a tool that makes Java applications compatible with Java 1.4, Java 1.3 and other environments. It supports all Java 5.0 language features and a significant part of the Java 5.0 API on both J2SE 1.4 and J2SE 1.3. In other Java environments only the Java 5.0 features that don't depend on the new API are supported. Retrotranslator employs the [ASM](http://asm.objectweb.org/) bytecode manipulation framework to transform compiled Java classes and the [backport](http://backport-jsr166.sourceforge.net/) of the Java 5.0 concurrency utilities to emulate the Java 5.0 API.

#### <a name="features">What Java 5.0 features are supported?</a>

*   Generics
*   Annotations
*   Reflection on generics and annotations
*   Typesafe enums
*   Autoboxing/unboxing
*   Enhanced for loop
*   Varargs
*   Covariant return types
*   Formatted output
*   Static import
*   Concurrency utilities
*   Collections framework enhancements

#### <a name="commandline">How to use Retrotranslator from the command line?</a>

1.  [Download](http://sourceforge.net/project/showfiles.php?group_id=153566) and unzip the binary distribution file `Retrotranslator-_n.n.n_-bin.zip`, where _n.n.n_ is the latest Retrotranslator release number.
2.  Compile your classes with Java 5.0 or Java 6 and put them into some directory, e.g. `myclasses`.
3.  Go to the unzipped directory and execute:  
    `java -jar retrotranslator-transformer-_n.n.n_.jar -srcdir myclasses`  
    Use appropriate options to verify the result and for troubleshooting, e.g. -[verify](#option_verify), -[classpath](#option_classpath), -[advanced](#option_advanced), and -[smart](#option_smart).
4.  Put `retrotranslator-runtime-_n.n.n_.jar` and `backport-util-concurrent-_n.n_.jar` into the classpath of your application if you use the Java 5.0 API.
5.  Run or debug the application as usual on Java 1.4.

The command line syntax:  
`java -jar retrotranslator-transformer-_n.n.n_.jar <options>`  
or  
`java -cp retrotranslator-transformer-_n.n.n_.jar net.sf.retrotranslator.transformer.Retrotranslator <options>`

<table border="1" cellspacing="0" cellpadding="5">

<tbody>

<tr>

<th>Option</th>

<th>Description</th>

<th>Default</th>

</tr>

<tr>

<td nowrap="">

`-srcdir <path>`

</td>

<td>The directory with the files to process (can be specified several times).</td>

<td>-</td>

</tr>

<tr>

<td nowrap="">

`-srcjar <file>`

</td>

<td>The jar file with the files to process (can be specified several times).</td>

<td>-</td>

</tr>

<tr>

<td nowrap="">

`-destdir <path>`

</td>

<td>The directory to place processed files.</td>

<td nowrap="">The source.</td>

</tr>

<tr>

<td nowrap="">

`-destjar <file>`

</td>

<td>The jar file to place processed files.</td>

<td>The source.</td>

</tr>

<tr>

<td nowrap="">

`-srcmask <mask>`

</td>

<td>

The wildcard pattern specifying the files to transform rather than copy (classes or UTF-8 text files), e.g. `*.class;?*.tld`.

</td>

<td>

`*.class`

</td>

</tr>

<tr>

<td nowrap="">

<a name="option_target">`-target <version>`</a>

</td>

<td>

The version of the JVM where the translated application should be able to run (1.1, 1.2, 1.3, 1.4, or 1.5). The Java 5.0 API is supported only for the 1.4 and 1.3 targets, but user-defined [backport](#option_backport) classes can be used for other targets as well.

</td>

<td>

`1.4`

</td>

</tr>

<tr>

<td nowrap="">

<a name="option_classpath">`-classpath <path>`</a>

</td>

<td>

The dependencies of the translated classes including backport packages and jar files from the target JVM. For the 1.4 target the following files should be specified among others: `rt.jar`, `jce.jar`, and `jsse.jar` from Sun JRE 1.4; `retrotranslator-runtime-_n.n.n_.jar` and `backport-util-concurrent-_n.n_.jar`. For the 1.3 target the files are: `rt.jar` from Sun JRE 1.3, `retrotranslator-runtime13-_n.n.n_.jar` and `backport-util-concurrent-java12-_n.n_.jar`. This option can be omitted if the current Java environment matches the target one.

</td>

<td>The current classpath.</td>

</tr>

<tr>

<td nowrap="">

<a name="option_verify">`-verify`</a>

</td>

<td>

Asks the translator to examine translated bytecode for references to classes, methods, or fields that cannot be found in the [classpath](#option_classpath). Every use of Java 5.0 API will result in a warning message if the [classpath](#option_classpath) contains jar files from JRE 1.4.

</td>

<td>

`false`

</td>

</tr>

<tr>

<td nowrap="">

<a name="option_support">`-support <features>`</a>

</td>

<td>

Enables advanced features. The names specified should be separated with semicolons, e.g. `ThreadLocal.remove;BigDecimal.setScale`.

</td>

<td>-</td>

</tr>

<tr>

<td nowrap="">

<a name="option_advanced">`-advanced`</a>

</td>

<td>

Enables all advanced features at once, but it's recommended to enable only required features in order to avoid compatibility issues.

</td>

<td>

`false`

</td>

</tr>

<tr>

<td nowrap="">

<a name="option_smart">`-smart`</a>

</td>

<td>

Makes all backport classes inheritable provided that the [classpath](#option_classpath) correctly reflects the target environment. For example, the backport of the `Writer.append(String)` method can be used to translate the following expression: `new FileWriter("file.tmp").append("Hello")`.

</td>

<td>

`false`

</td>

</tr>

<tr>

<td nowrap="">

<a name="option_syncvolatile">`-syncvolatile`</a>

</td>

<td>

Makes access to volatile fields of Java 5.0 classes compliant with Java 5.0 memory model at the cost of decreased performance. Highly recommended to use.

</td>

<td>

`false`

</td>

</tr>

<tr>

<td nowrap="">

<a name="option_syncfinal">`-syncfinal`</a>

</td>

<td>

Makes access to final instance fields of Java 5.0 classes compliant with Java 5.0 memory model at the cost of decreased performance. Not recommended to use.

</td>

<td>

`false`

</td>

</tr>

<tr>

<td nowrap="">

<a name="option_backport">`-backport <names>`</a>

</td>

<td>

The [backport](#extension) names separated with semicolons, e.g. `com.myco.all;java.util:com.myco.ju;javax.net.SocketFactory:com.myco.jsse.Factory`. The corresponding backport classes must be present in the [classpath](#option_classpath).

</td>

<td>-</td>

</tr>

<tr>

<td nowrap="">

`-embed <package>`

</td>

<td>

The unique package name for a partial copy of `retrotranslator-runtime-_n.n.n_.jar` and `backport-util-concurrent-_n.n_.jar` to be put along with translated classes. This makes the translated application independent of other versions of Retrotranslator present in the classpath.

</td>

<td>-</td>

</tr>

<tr>

<td nowrap="">

`-lazy`

</td>

<td>

Asks the translator to transform and verify only the classes compiled with a target greater than the [current](#option_target) one.

</td>

<td>

`false`

</td>

</tr>

<tr>

<td nowrap="">

`-stripsign`

</td>

<td>Whether to remove signature (generics) information.</td>

<td>

`false`

</td>

</tr>

<tr>

<td nowrap="">

`-stripannot`

</td>

<td>Whether to remove annotations and related attributes.</td>

<td>

`false`

</td>

</tr>

<tr>

<td nowrap="">

`-verbose`

</td>

<td>Asks the translator for verbose output.</td>

<td>

`false`

</td>

</tr>

<tr>

<td nowrap="">

`-retainapi`

</td>

<td>

Asks the translator to modify classes for JVM compatibility but keep use of API unless overriden with the [backport](#option_backport) option. Any references introduced by a compiler remain unchanged, like the use of `java.lang.StringBuilder` for string concatenation or the implicit `valueOf` method calls for autoboxing.

</td>

<td>

`false`

</td>

</tr>

<tr>

<td nowrap="">

<a name="option_keepclasslit">`-keepclasslit`</a>

</td>

<td>

Prevents replacement of certain types like `java.lang.Iterable` or `java.util.Queue` with their base types in class literals.

</td>

<td>

`false`

</td>

</tr>

<tr>

<td nowrap="">

`-retainflags`

</td>

<td>Whether to keep Java 5.0 specific access modifiers.</td>

<td>

`false`

</td>

</tr>

<tr>

<td nowrap="">

`-uptodatecheck`

</td>

<td>Asks the translator to skip processing of files if the destination files are newer.</td>

<td>

`false`

</td>

</tr>

<tr>

<td nowrap="">

`-reflection <mode>`

</td>

<td>

Setting this option to `safe` makes the translator include additional metadata into classes, so reflection works even if the classes are unavailable as resources.

</td>

<td>

`normal`

</td>

</tr>

</tbody>

</table>

#### <a name="ant">How to use Retrotranslator from Apache Ant or Maven?</a>

The distribution contains an [Apache Ant](http://ant.apache.org/) task `net.sf.retrotranslator.transformer.RetrotranslatorTask`. Every [command line](#commandline) option can be set using the corresponding attribute. In addition the source files can be specified with nested `fileset`, `jarfileset`, and `dirset` elements and the [classpath](#option_classpath) can be set with nested `classpath` elements or the `classpathref` attribute. The source directories specified with `srcdir`, `dirset`, and the `dir` attribute of `fileset` should contain the root package of the classes. In case of warnings the build fails unless the value of the `failonwarning` attribute is set to `false`. The following script can be used to build one jar compatible with Java 1.4 and another one compatible with Java 1.3.

```xml
    <taskdef name="retrotranslator" classname="net.sf.retrotranslator.transformer.RetrotranslatorTask">
        <classpath>
            <fileset dir="../Retrotranslator-n.n.n-bin">
                <include name="retrotranslator-transformer-n.n.n.jar" />
                <include name="retrotranslator-runtime-n.n.n.jar" />
                <include name="backport-util-concurrent-n.n.jar" />
            </fileset>
        </classpath>
    </taskdef>

    <retrotranslator target="1.4" destjar="build/application14.jar"
                     smart="true" verify="true" failonwarning="false">
        <fileset dir="build/classes" includes="**/*.class" />
        <jarfileset dir="build/lib" includes="**/*.jar" />
        <classpath>
            <fileset dir="../j2sdk1.4.2_17/jre/lib" includes="**/*.jar"/>
            <fileset dir="../Retrotranslator-n.n.n-bin">
                <include name="retrotranslator-runtime-n.n.n.jar" />
                <include name="backport-util-concurrent-n.n.jar" />
            </fileset>
            <fileset dir="lib" includes="**/*.jar"/>
        </classpath>
    </retrotranslator>

    <retrotranslator target="1.3" destjar="build/application13.jar"
                     smart="true" verify="true" failonwarning="false">
        <fileset dir="build/classes" includes="**/*.class" />
        <jarfileset dir="build/lib" includes="**/*.jar" />
        <classpath>
            <fileset dir="../jdk1.3.1_20/jre/lib" includes="**/*.jar"/>
            <fileset dir="../Retrotranslator-n.n.n-bin">
                <include name="retrotranslator-runtime13-n.n.n.jar" />
                <include name="backport-util-concurrent-java12-n.n.jar" />
            </fileset>
            <fileset dir="lib" includes="**/*.jar"/>
        </classpath>
    </retrotranslator>
```

[Maven](http://maven.apache.org/) users can use the [Retrotranslator plugin](http://mojo.codehaus.org/retrotranslator-maven-plugin/) from the [Mojo Project](http://mojo.codehaus.org/).

#### <a name="idea">How to use Retrotranslator from IntelliJ IDEA?</a>

There is a [plugin](http://plugins.intellij.net/plugin/?id=145) to automatically translate and verify classes compiled by [IntelliJ IDEA](http://www.jetbrains.com/idea/), so you can develop in Java 5.0 but run and debug on Java 1.4.

#### <a name="jarfile">How to produce a jar file compatible with Java 1.4?</a>

If you have a `myapplication5.jar` file built with Java 5.0 you can use the following command to produce `myapplication4.jar`. The jar file will be compatible with Java 1.4 and independent of Retrotranslator because backport classes are added to the translated application with a different package name.

`java -jar retrotranslator-transformer-_n.n.n_.jar -srcjar myapplication5.jar -destjar myapplication4.jar -embed com.mycompany.internal`  

Also it is recommended to specify the [classpath](#option_classpath), [verify](#option_verify), and [smart](#option_smart) options. In case of verification failure try the [support](#option_support) option.

#### <a name="jit">How to use Just-in-Time Retrotranslator?</a>

In order to run a Java 5.0 application on Java 1.4 start it with JIT Retrotranslator using one of the following commands:

*   `java -cp retrotranslator-transformer-_n.n.n_.jar net.sf.retrotranslator.transformer.JITRetrotranslator <options> -jar <jarfile> [<args...>]`
*   `java -cp <classpath including Retrotranslator> net.sf.retrotranslator.transformer.JITRetrotranslator <options> <class> [<args...>]`

The options include [-support](#option_support), [-advanced](#option_advanced), [-smart](#option_smart), [-backport](#option_backport), [-syncvolatile](#option_syncvolatile), [-syncfinal](#option_syncfinal), and [-keepclasslit](#option_keepclasslit). When running on J2SE 5.0 JIT Retrotranslator simply calls the application, but on J2SE 1.4 it also translates classes compiled for Java 5.0 or later. However this capability depends on the current JVM and the application itself, so under certain conditions JIT Retrotranslator may be unable to translate either a jar file or classes from the classpath or both.

#### <a name="supported">What API is supported on Java 1.4 and Java 1.3?</a>

An asterisk designates all API members introduced in Java 5.0. Most classes and methods added in Java 1.4 and in Java 6.0 cannot be used on earlier VMs.

<table border="1" cellspacing="0" cellpadding="5">

<tbody>

<tr>

<th>Package</th>

<th>Class</th>

<th>Members</th>

<th>Compatibility notes</th>

</tr>

<tr>

<td rowspan="6">

`java.io`

</td>

<td>

`Closeable`<sup>[2](#2)</sup>

</td>

<td>* (all members introduced in Java 5.0)</td>

<td> </td>

</tr>

<tr>

<td>

`Flushable`<sup>[2](#2)</sup>

</td>

<td>*</td>

<td> </td>

</tr>

<tr>

<td>

`PrintStream`

</td>

<td>

\* (11 new methods and constructors)  
`PrintStream(File, String)`<sup>[5](#5)</sup>,  
`PrintStream(String, String)`<sup>[5](#5)</sup>

</td>

<td> </td>

</tr>

<tr>

<td>

`PrintWriter`

</td>

<td>

\* (11 new methods and constructors)

</td>

<td>

The `PrintWriter.format` and `PrintWriter.printf` methods always flush the output buffer.

</td>

</tr>

<tr>

<td>

`Reader`<sup>[5](#5)</sup>

</td>

<td>

\* (1 new method)

</td>

<td> </td>

</tr>

<tr>

<td>

`Writer`

</td>

<td>

\* (3 new methods)

</td>

<td> </td>

</tr>

<tr>

<td rowspan="32">

`java.lang`

</td>

<td>

`Appendable`<sup>[2](#2)</sup>

</td>

<td>*</td>

<td> </td>

</tr>

<tr>

<td>

`Boolean`

</td>

<td>

\* (2 new methods)  
`toString(boolean)`<sup>[6](#6)</sup>,  
`valueOf(boolean)`<sup>[6](#6)</sup>

</td>

<td> </td>

</tr>

<tr>

<td>

`Byte`

</td>

<td>

\* (1 new method)

</td>

<td> </td>

</tr>

<tr>

<td>

`Character`

</td>

<td>

\* (44 new methods)  
`getDirectionality(int)`<sup>[5](#5)</sup>,  
`isMirrored(int)`<sup>[5](#5)</sup>,  
`toString(char)`<sup>[6](#6)</sup>

</td>

<td>

New members of `Character.UnicodeBlock` are not supported. All supplementary code points are considered as unassigned.

</td>

</tr>

<tr>

<td>

`CharSequence`<sup>[6](#6)</sup>

</td>

<td>*</td>

<td> </td>

</tr>

<tr>

<td>

`Class`

</td>

<td>

\* (21 new methods)

</td>

<td>

Enable feature "`Class.forName`" to use backported classes as a fallback when calling this method<sup>[3](#3)</sup>.  
Enable features "`Class.getMethod`" and "`Class.getDeclaredMethod`" to use most of backported static methods as a fallback when calling these methods and for more uniform support of generics and covariant return types on different platforms<sup>[3](#3)</sup>.

</td>

</tr>

<tr>

<td>

`Deprecated`

</td>

<td>

\*

</td>

<td> </td>

</tr>

<tr>

<td>

`Double`

</td>

<td>

`valueOf(double)`

</td>

<td> </td>

</tr>

<tr>

<td>

`Enum`

</td>

<td>*</td>

<td> </td>

</tr>

<tr>

<td>

`Float`

</td>

<td>

`valueOf(float)`

</td>

<td> </td>

</tr>

<tr>

<td>

`IllegalArgumentException`

</td>

<td>

\* (2 new constructors)

</td>

<td> </td>

</tr>

<tr>

<td>

`IllegalStateException`

</td>

<td>

\* (2 new constructors)

</td>

<td> </td>

</tr>

<tr>

<td>

`Integer`

</td>

<td>

`valueOf(int),  
signum(int)`

</td>

<td> </td>

</tr>

<tr>

<td>

`Iterable`<sup>[2](#2)</sup>

</td>

<td>*</td>

<td> </td>

</tr>

<tr>

<td>

`Long`

</td>

<td>

`valueOf(long),  
signum(long)`

</td>

<td> </td>

</tr>

<tr>

<td>

`Math`

</td>

<td>

`cbrt(double), cosh(double),  
expm1(double), log10(double),  
log1p(double), signum(double),  
signum(float), sinh(double),  
tanh(double)`

</td>

<td>The quality of computation may be insufficient for applications of certain types.</td>

</tr>

<tr>

<td>

`Package`

</td>

<td>

\* (4 new methods)

</td>

<td>

Enable feature "`Retrotranslator.fixHyphen`" to change the name of `package-info` classes to `package$info`<sup>[3](#3)</sup>.

</td>

</tr>

<tr>

<td>

`Readable`<sup>[2](#2),[5](#5)</sup>

</td>

<td>*</td>

<td> </td>

</tr>

<tr>

<td>

`Short`

</td>

<td>

\* (2 new methods)

</td>

<td> </td>

</tr>

<tr>

<td>

`StackTraceElement`<sup>[6](#6)</sup>

</td>

<td>

\* (1 new constructor)

</td>

<td> </td>

</tr>

<tr>

<td>

`String`

</td>

<td>

\* (10 new methods and constructors)  
`isEmpty()`<sup>[4](#4)</sup>

</td>

<td>

Enable features "`String.matches`", "`String.replaceAll`", "`String.replaceFirst`" and "`String.split`" to support Java 5.0 features in regular expressions when calling these methods on Java 1.4<sup>[3](#3)</sup> (they are unavailable on Java 1.3).

</td>

</tr>

<tr>

<td>

`StringBuffer`

</td>

<td>

\* (11 new methods and constructors) `append(StringBuffer)`<sup>[6](#6)</sup>

</td>

<td>

Enable feature "`StringBuffer.trimToSize`" to use an empty implementation of the `StringBuffer.trimToSize()` method<sup>[3](#3)</sup>.

</td>

</tr>

<tr>

<td>

`StringBuilder`

</td>

<td>*</td>

<td>

`StringBuilder` is being replaced with `StringBuffer`.

</td>

</tr>

<tr>

<td>

`SuppressWarnings`

</td>

<td>*</td>

<td> </td>

</tr>

<tr>

<td>

`System`

</td>

<td>

`nanoTime()`<sup>[1](#1)</sup>,  
`clearProperty(String)`

</td>

<td>

The `System.nanoTime()` method precision [may vary](http://dcl.mathcs.emory.edu/util/backport-util-concurrent/doc/api/edu/emory/mathcs/backport/java/util/concurrent/helpers/Utils.html#nanoTime()) on different platforms.  
Enable feature "`System.getProperty`" to identify the current JVM as compatible with Java 5.0 when calling this method at runtime<sup>[3](#3)</sup>.

</td>

</tr>

<tr>

<td>

`Thread`

</td>

<td>

\* (8 new methods)

</td>

<td>

The `Thread.getId()` method does not reflect the order in which threads have been created.  
The `Thread.getStackTrace()` and `Thread.getAllStackTraces()` methods return non-empty stack trace only for the current thread.  
Enable feature "`Thread.getState`" to support this method, but it will detect only `NEW`, `RUNNABLE` and `TERMINATED` states.<sup>[3](#3)</sup>  
Enable features "`Thread.setUncaughtExceptionHandler`" and "`Thread.setDefaultUncaughtExceptionHandler`" to support exception handlers for threads created by translated code (in contrast to Java 5.0 the default `UncaughtExceptionHandler` takes precedence over `ThreadGroup`)<sup>[3](#3)</sup>.

</td>

</tr>

<tr>

<td>

`Thread.State`

</td>

<td>*</td>

<td> </td>

</tr>

<tr>

<td>

`Thread.UncaughtExceptionHandler`<sup>[2](#2)</sup>

</td>

<td>*</td>

<td> </td>

</tr>

<tr>

<td>

`ThreadLocal`

</td>

<td>

\* (1 new method)

</td>

<td>

Enable feature "`ThreadLocal.remove`" to use alternative `ThreadLocal` and `InheritableThreadLocal` implementations with method `remove()`<sup>[3](#3)</sup>.

</td>

</tr>

<tr>

<td>

`TypeNotPresentException`

</td>

<td>*</td>

<td> </td>

</tr>

<tr>

<td>

`Throwable`

</td>

<td>

`Throwable(String, Throwable)`<sup>[6](#6)</sup>,  
`Throwable(String, Throwable)`<sup>[6](#6)</sup>,  
`getCause()`<sup>[6](#6)</sup>,  
`getStackTrace()`<sup>[6](#6)</sup>,  
`initCause(Throwable)`<sup>[6](#6)</sup>

</td>

<td>

The additional constructors and methods are available for all Java 1.3 exception classes if the [-smart](#option_smart) option has been used.

</td>

</tr>

<tr>

<td>

`UnsupportedOperationException`

</td>

<td>

\* (2 new constructors)

</td>

<td> </td>

</tr>

<tr>

<td>

`java.lang.annotation`

</td>

<td>

\* (all classes)

</td>

<td>*</td>

<td> </td>

</tr>

<tr>

<td>

`java.lang.instrument`

</td>

<td>*</td>

<td>*</td>

<td>Bytecode instrumentation is not implemented.</td>

</tr>

<tr>

<td>

`java.lang.management`

</td>

<td>

`ManagementFactory`

</td>

<td>

`getPlatformMBeanServer()`

</td>

<td>

The `ManagementFactory.getPlatformMBeanServer()` method simply returns the first registered `MBeanServer` or creates it when no one exists. Requires an [implementation of JMX 1.2](http://mx4j.sourceforge.net/).

</td>

</tr>

<tr>

<td rowspan="2">

`java.lang.ref`

</td>

<td>

`SoftReference`

</td>

<td>*</td>

<td>

Enable feature "`SoftReference.NullReferenceQueue`" to support `null` for the second parameter of `SoftReference(Object,ReferenceQueue)` on all platforms<sup>[3](#3)</sup>.

</td>

</tr>

<tr>

<td>

`WeakReference`

</td>

<td>*</td>

<td>

Enable feature "`WeakReference.NullReferenceQueue`" to support `null` for the second parameter of `WeakReference(Object,ReferenceQueue)` on all platforms<sup>[3](#3)</sup>.

</td>

</tr>

<tr>

<td rowspan="14">

`java.lang.reflect`

</td>

<td>

`AccessibleObject`

</td>

<td>

\* (4 new methods)

</td>

<td> </td>

</tr>

<tr>

<td>

`AnnotatedElement`<sup>[2](#2)</sup>

</td>

<td>*</td>

<td> </td>

</tr>

<tr>

<td>

`Constructor`

</td>

<td>

\* (11 new methods)

</td>

<td> </td>

</tr>

<tr>

<td>

`Field`

</td>

<td>

\* (8 new methods)

</td>

<td> </td>

</tr>

<tr>

<td>

`GenericArrayType`

</td>

<td>*</td>

<td> </td>

</tr>

<tr>

<td>

`GenericDeclaration`<sup>[2](#2)</sup>

</td>

<td>*</td>

<td> </td>

</tr>

<tr>

<td>

`GenericSignatureFormatError`

</td>

<td>*</td>

<td> </td>

</tr>

<tr>

<td>

`MalformedParameterizedTypeException`

</td>

<td>*</td>

<td> </td>

</tr>

<tr>

<td>

`Member`

</td>

<td>

\* (1 new method)

</td>

<td> </td>

</tr>

<tr>

<td>

`Method`

</td>

<td>

\* (14 new methods)

</td>

<td> </td>

</tr>

<tr>

<td>

`ParameterizedType`

</td>

<td>*</td>

<td> </td>

</tr>

<tr>

<td>

`Type`<sup>[2](#2)</sup>

</td>

<td>*</td>

<td> </td>

</tr>

<tr>

<td>

`TypeVariable`

</td>

<td>*</td>

<td> </td>

</tr>

<tr>

<td>

`WildcardType`

</td>

<td>*</td>

<td> </td>

</tr>

<tr>

<td rowspan="2">

`java.math`

</td>

<td>

`BigDecimal`

</td>

<td>

`ZERO, ONE, TEN,  
BigDecimal(int),  
BigDecimal(long),  
BigDecimal(char[]),  
BigDecimal(char[], int, int),  
divide(BigDecimal),  
divideAndRemainder(BigDecimal),  
divideToIntegralValue(BigDecimal),  
pow(int),  
remainder(BigDecimal),  
stripTrailingZeros(),  
toPlainString(),  
valueOf(double),  
valueOf(long)`

</td>

<td>

Enable feature "`BigDecimal.setScale`" to support negative scales in the `BigDecimal.setScale(int, int)` method<sup>[3](#3)</sup>.

</td>

</tr>

<tr>

<td>

`BigInteger`

</td>

<td>

`TEN`

</td>

<td> </td>

</tr>

<tr>

<td rowspan="6">

`java.net`

</td>

<td>

`HttpURLConnection`

</td>

<td>

\* (6 new methods)

</td>

<td>

Enable features "`HttpURLConnection.setChunkedStreamingMode`", "`HttpURLConnection.setFixedLengthStreamingMode`" to use the corresponding methods, but on Java 1.4 they will simply return. Consider using alternative or writing own protocol handlers.

</td>

</tr>

<tr>

<td>

`Proxy`<sup>[5](#5)</sup>

</td>

<td>*</td>

<td> </td>

</tr>

<tr>

<td>

`ProxySelector`<sup>[5](#5)</sup>

</td>

<td>*</td>

<td> </td>

</tr>

<tr>

<td>

`Socket`<sup>[3](#3),[5](#5)</sup>

</td>

<td>

`Socket(Proxy)`

</td>

<td>

Enable feature "`Socket.New`" to to call the `Socket(Proxy)` constructor, but the `Proxy` parameter will be ignored.

</td>

</tr>

<tr>

<td>

`URL`<sup>[5](#5)</sup>

</td>

<td>

\* (2 new methods)

</td>

<td>

The `Proxy` parameter is ignored by the `URL.openConnection(Proxy)` method.

</td>

</tr>

<tr>

<td>

`URLConnection`

</td>

<td>

\* (4 new methods)

</td>

<td>

Enable features "`URLConnection.getConnectTimeout`", "`URLConnection.setConnectTimeout`", "`URLConnection.getReadTimeout`", "`URLConnection.setReadTimeout`" to use the corresponding methods, but on Java 1.4 they will simply return. Consider using alternative or writing own protocol handlers.

</td>

</tr>

<tr>

<td rowspan="2">

`java.nio`<sup>[5](#5)</sup>

</td>

<td>

`CharBuffer`

</td>

<td>

\* (4 new methods)

</td>

<td> </td>

</tr>

<tr>

<td>

`Charset`

</td>

<td>

\* (1 new method)

</td>

<td>

The `Charset.defaultCharset()` method returns UTF-8 if the default charset is unavailable (occurs on JDK 1.4.0).

</td>

</tr>

<tr>

<td>

`java.rmi.server`

</td>

<td>

`RemoteObjectInvocationHandler`

</td>

<td>*</td>

<td> </td>

</tr>

<tr>

<td>

`java.text`

</td>

<td>

`DecimalFormat`

</td>

<td>

\* (2 new methods)

</td>

<td>

Enable feature "`DecimalFormat.setParseBigDecimal`" to support the `DecimalFormat.setParseBigDecimal(boolean)` method, but parsing and formatting precision will still be limited by the `java.lang.Double` or `java.lang.Long` precision<sup>[3](#3)</sup>.

</td>

</tr>

<tr>

<td rowspan="15">

`java.util`

</td>

<td>

`AbstractQueue`<sup>[1](#1)</sup>

</td>

<td>*</td>

<td> </td>

</tr>

<tr>

<td>

`ArrayDeque`<sup>[1](#1),[4](#4)</sup>

</td>

<td>*</td>

<td> </td>

</tr>

<tr>

<td>

`Arrays`

</td>

<td>

\* (21 new methods)  
`copyOf(...)`<sup>[4](#4)</sup> (10 methods)  
`copyOfRange(...)`<sup>[4](#4)</sup> (10 methods)

</td>

<td> </td>

</tr>

<tr>

<td>

`Calendar`

</td>

<td>

`getTimeInMillis()`<sup>[6](#6)</sup>,  
`setTimeInMillis(long)`<sup>[6](#6)</sup>

</td>

<td> </td>

</tr>

<tr>

<td>

`Collections`<sup>[1](#1)</sup>

</td>

<td>

\* (13 new methods)  
`newSetFromMap(Map)`<sup>[4](#4)</sup>,  
`list(Enumeration)`<sup>[6](#6)</sup>,  
`replaceAll(List, Object, Object)`<sup>[6](#6)</sup>,  
`swap(List, int, int)`<sup>[6](#6)</sup>

</td>

<td> </td>

</tr>

<tr>

<td>

`Deque`<sup>[1](#1),[2](#2),[4](#4)</sup>

</td>

<td>*</td>

<td> </td>

</tr>

<tr>

<td>

`EnumMap`

</td>

<td>*</td>

<td> </td>

</tr>

<tr>

<td>

`EnumSet`

</td>

<td>*</td>

<td> </td>

</tr>

<tr>

<td>

`Formatter`

</td>

<td>*</td>

<td> </td>

</tr>

<tr>

<td>

`LinkedList`

</td>

<td>

\* (5 new methods)

</td>

<td> </td>

</tr>

<tr>

<td>

`PriorityQueue`<sup>[1](#1)</sup>

</td>

<td>*</td>

<td> </td>

</tr>

<tr>

<td>

`Properties`

</td>

<td>*</td>

<td>

Requires an [implementation of JAXP 1.3](http://xml.apache.org/xalan-j/) for J2SE 1.3.

</td>

</tr>

<tr>

<td>

`Queue`<sup>[1](#1),[2](#2)</sup>

</td>

<td>*</td>

<td> </td>

</tr>

<tr>

<td>

`Timer`

</td>

<td>

\* (3 new methods and constructors)

</td>

<td>

Enable feature "`Timer.All`" to use alternative `Timer` and `TimerTask` implementations in order to be able to call `Timer(String)`, `Timer(String, boolean)`, and `Timer.purge()`<sup>[3](#3)</sup>.

</td>

</tr>

<tr>

<td>

`UUID`

</td>

<td>*</td>

<td> </td>

</tr>

<tr>

<td>

`java.util.concurrent,  
java.util.concurrent.atomic,  
java.util.concurrent.locks`

</td>

<td>

almost all classes<sup>[1](#1)</sup>

</td>

<td>almost all methods</td>

<td>

The `LockSupport` class may be unusable due to insufficient performance. The `Condition.awaitNanos(long)` method has [little](http://dcl.mathcs.emory.edu/util/backport-util-concurrent/doc/api/edu/emory/mathcs/backport/java/util/concurrent/helpers/Utils.html#awaitNanos(edu.emory.mathcs.backport.java.util.concurrent.locks.Condition,%20long)) accuracy guarantees.

</td>

</tr>

<tr>

<td rowspan="3">

`java.util.regex`<sup>[5](#5)</sup>

</td>

<td>

`Matcher`

</td>

<td>

`quoteReplacement(String),  
toMatchResult()`

</td>

<td> </td>

</tr>

<tr>

<td>

`MatchResult`<sup>[2](#2)</sup>

</td>

<td>*</td>

<td> </td>

</tr>

<tr>

<td>

`Pattern`

</td>

<td>

`LITERAL`<sup>[3](#3)</sup>,  
`quote(String)`

</td>

<td>

Enable features "`Pattern.compile`" and "`Pattern.matches`" to support Java 5.0 features in regular expressions when calling these methods<sup>[3](#3)</sup>.

</td>

</tr>

<tr>

<td>

`javax.net.ssl`<sup>[5](#5)</sup>

</td>

<td>

`HttpsURLConnection`

</td>

<td>

\* (2 new methods)

</td>

<td> </td>

</tr>

</tbody>

</table>

<a name="1"><sup>1</sup></a> Supported via the [Backport of JSR 166](http://backport-jsr166.sourceforge.net/).  
<a name="2"><sup>2</sup></a> In most cases this type is being replaced with its base type.  
<a name="3"><sup>3</sup></a> Supported only when the corresponding feature is enabled via the [-support](#option_support) or [-advanced](#option_advanced) options.
<a name="4"><sup>4</sup></a> Introduced in Java 6.  
<a name="5"><sup>5</sup></a> Not supported on J2SE 1.3.  
<a name="6"><sup>6</sup></a> Introduced in Java 1.4 and supported on J2SE 1.3.  

#### <a name="extension">How to write an extension?</a>

In order to support the API unavailable on the target platform Retrotranslator should be able to replace all references to new classes, constructors, methods, and fields with references to backports compatible with the platform. The location of the backports must be specified with the [classpath](#option_classpath) option. The [default](http://retrotranslator.cvs.sourceforge.net/retrotranslator/Retrotranslator/src/net/sf/retrotranslator/registry/backport14.properties?view=markup) backports for the 1.4 target have been packaged into the `retrotranslator-runtime-_n.n.n_.jar` and `backport-util-concurrent-_n.n_.jar` files, and to complement or override them additional backport names can be specified via the [backport](#option_backport) option or a [properties](http://retrotranslator.cvs.sourceforge.net/retrotranslator/Retrotranslator/src/net/sf/retrotranslator/registry/backport14.properties?view=markup) file. The backport names can have five different forms, the first one declares a universal backport package and the others allow reusing existing backports.

<table border="1" cellspacing="0" cellpadding="5">

<tbody>

<tr>

<th>Backport name form</th>

<th>Example</th>

</tr>

<tr>

<td>

`<universal backport package name>`

</td>

<td>

`net.sf.retrotranslator.runtime  
com.mycompany.backport`

</td>

</tr>

<tr>

<td>

`<original package name>:<backport package name>`

</td>

<td>

`java.util.concurrent:edu.emory.mathcs.backport.java.util.concurrent  
com.sun.org.apache.xerces.internal:org.apache.xerces`

</td>

</tr>

<tr>

<td>

`<original class name>:<backport class name>`

</td>

<td>

`java.lang.StringBuilder:java.lang.StringBuffer  
java.util.LinkedHashMap:org.apache.commons.collections.map.LinkedMap`

</td>

</tr>

<tr>

<td>

`<original method name>:<backport method name>`

</td>

<td>

`java.lang.System.nanoTime:edu.emory.mathcs.backport.java.util.concurrent.helpers.Utils.nanoTime`

</td>

</tr>

<tr>

<td>

`<original field name>:<backport field name>`

</td>

<td>

`java.util.Collections.EMPTY_MAP:edu.emory.mathcs.backport.java.util.Collections.EMPTY_MAP`

</td>

</tr>

</tbody>

</table>

The names of backport classes in a universal backport package consist of the backport package name, the name of the original class, and an optional trailing underscore. For example, [`net.sf.retrotranslator.runtime.java.util.EnumSet**_**`](http://retrotranslator.cvs.sourceforge.net/retrotranslator/Retrotranslator/src/net/sf/retrotranslator/runtime/java/util/EnumSet_.java?view=markup) is a complete backport of [`java.util.EnumSet`](http://java.sun.com/j2se/1.5.0/docs/api/java/util/EnumSet.html). But when classes exist on the target platform then the backports of their new fields, constructors and methods are grouped into classes with a leading underscore in their names. Look at the [`net.sf.retrotranslator.runtime.java.math.**_**BigDecimal`](http://retrotranslator.cvs.sourceforge.net/retrotranslator/Retrotranslator/src/net/sf/retrotranslator/runtime/java/math/_BigDecimal.java?view=markup) class:

*   For a static field there is a public static field with the same name and type.
*   For a static method there is a public static method with the same signature.
*   For an instance method there is a public static method with the same signature but with an additional first parameter representing the instance.
*   For a constructor there is a public static `convertConstructorArguments` method that accepts constructor's arguments and returns an argument for a Java 1.4 constructor.

The [`net.sf.retrotranslator.runtime.java.io.**_**PrintStream`](http://retrotranslator.cvs.sourceforge.net/retrotranslator/Retrotranslator/src/net/sf/retrotranslator/runtime/java/io/_PrintStream.java?view=markup) and [`net.sf.retrotranslator.runtime.java.lang.**_**SecurityException`](http://retrotranslator.cvs.sourceforge.net/retrotranslator/Retrotranslator/src/net/sf/retrotranslator/runtime/java/lang/_SecurityException.java?view=markup) classes use another type of constructor backports. There is a public static `createInstanceBuilder` method that accepts constructor's arguments an returns an object with public `argument1`...`argumentN` methods and an optional public void `initialize` method. All the `argumentX` methods don't have any parameters and provide arguments for a constructor that exists on the target platform. The `initialize` method has a single parameter for the created instance and can be used for postprocessing. And the most flexible but unsupported in certain cases way has been used by [`net.sf.retrotranslator.runtime.java.lang.**_**StackTraceElement`](http://retrotranslator.cvs.sourceforge.net/retrotranslator/Retrotranslator/src/net/sf/retrotranslator/runtime/java/lang/_StackTraceElement.java?view=markup).

If backported methods require access to non-public methods or fields of the instance, they can do it with reflection when the security manager allows such access. The backports of public instance fields are not supported, but private instance fields can be emulated using a weak identity map, see [`net.sf.retrotranslator.runtime.java.lang.**_**Thread`](http://retrotranslator.cvs.sourceforge.net/retrotranslator/Retrotranslator/src/net/sf/retrotranslator/runtime/java/lang/_Thread.java?view=markup) for an example.

#### <a name="limitations">What are the limitations?</a>

*   The Java 5.0 memory model is not fully supported even with the [syncvolatile](#option_syncvolatile) and [syncfinal](#option_syncfinal) options if the fields are being accessed via reflection.
*   Only the classes, methods, and fields listed [above](#supported) should work and the other features, like formatted input, are not supported.
*   The Java 1.4 compilers and applications performing bean introspection may fail to work correctly with covariant return types.
*   The backported implementation of the Java 5.0 API is not interoperable with the original implementation.
*   Reflection-based tools may be unable to discover the backported implementation of the Java 5.0 API.
*   The Java 5.0 reflection methods may return incomplete information for dynamically generated classes.
*   The constants inlined by a compiler and access modifiers are ignored during the verification.

#### <a name="alternative">Alternative tools</a>

*   [Retroweaver](http://retroweaver.sourceforge.net/)
*   [Declawer](http://tinyurl.com/r8xba)
*   [JBossRetro](http://wiki.jboss.org/wiki/Wiki.jsp?page=JBossRetro)

#### <a name="contact">Contact</a>

*   [Project summary](http://sourceforge.net/projects/retrotranslator)
*   [Latest documentation](http://retrotranslator.sourceforge.net/)
*   [Open discussion](http://sourceforge.net/forum/forum.php?forum_id=513539)
*   [Help](http://sourceforge.net/forum/forum.php?forum_id=513540)
*   [Bugs](http://sourceforge.net/tracker/?group_id=153566&atid=788279)
*   [Feature requests](http://sourceforge.net/tracker/?group_id=153566&atid=788282)
*   [Author](http://sourceforge.net/users/tarasp/)

#### <a name="license">License</a>

    Copyright (c) 2005 - 2009 Taras Puchko
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:
    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.
    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.
    3. Neither the name of the copyright holders nor the names of its
       contributors may be used to endorse or promote products derived from
       this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
    THE POSSIBILITY OF SUCH DAMAGE.
