Generate Boilerplate Classes via Annotation Processing at Compile Time
=======================

http://yegeniy.github.io/java_snippets.html#2014-08-02

We will use an annotation and an accompanying annotation processor, to generate
Java classes from a Mustache template at compile time. Useful for
generating boilerplate classes.

Reference: <http://deors.wordpress.com/2011/10/08/annotation-processors/>

In that example, the
[`com.example.MyAnnotationProcessor`][MyAnnotationProcessor] class is registered
as an annotation processor (in
[`javax.annotation.processing.Processor`][Processor]). Since it supports the
[`com.example.MarkedForProcessing`][MarkedForProcessing] annotation and
[`com.example.Thing`][Thing] is annotated with
[`@MarkedForProcessing`][MarkedForProcessing],
[`MyAnnotationProcessor`][MyAnnotationProcessor] creates the
`com.example.GeneratedThing` class used in [`com.example.MainClass`][MainClass].

If running `mvn clean install`, the top-level [`pom.xml`][pom] should compile with
the proper flags to write `GeneratedThing` into 
`client/target/generated-sources/annotations/com/example/GeneratedThing.java`.
As you can figure out from the local variable `template` inside
`MyAnnotationProcessor::process`, the source code of `GeneratedThing` looks
like:

```java
package com.example;

public class GeneratedThing {
    
}
```

[MarkedForProcessing]: annotations/src/main/java/com/example/MarkedForProcessing.java
[MainClass]: client/src/main/java/com/example/MainClass.java
[MyAnnotationProcessor]: processors/src/main/java/com/example/MyAnnotationProcessor.java
[Processor]: processors/src/main/resources/META-INF/services/javax.annotation.processing.Processor
[pom]: pom.xml
[Thing]: client/src/main/java/com/example/Thing.java

**A Checklist:**

1) Set up Maven Project for Annotation Processing

The most confusing part about this approach is setting up your environment to
kick off the annotation processing.

2) Create Annotation and Annotate your target Types

This is the step that you are most likely already familiar with. Your annotation
is created using the `@interface` keyword.

3) Create a template of the classes to generate

A tool like [jMustache](https://github.com/samskivert/jmustache) can be handy.

4) Use Java's Annotation Processing Tools to Generate the Classes

* The `javax.lang.model.*` packages are used to extract information about your
annotated types (the class, method, etc.. that you annotated).
* The `javax.annotation.processing` package is used to wire the Annotation and
Annotated classes to the Annotation Processor.
*  The `javax.tools` package has some useful tooling. For example
`JavaFileObject` generates the `.class` file, and `Diagnostic` can be useful for
sending messages at compile-time.
