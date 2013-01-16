====================
== Cascading Demo ==
====================

1. MOTIVATION

Demo showcasing the Cascading integration in Spring for Apache Hadoop.
Built on the 'official' Cascading example, Cascading for the Impatient, Part 3:

Original Source: http://github.com/Cascading/Impatient/tree/master/part3#cascading-for-the-impatient-part-3
Original Doc   : http://www.cascading.org/2012/07/17/cascading-for-the-impatient-part-3/


The code-base features and structure resembles the original as much as possible to highlight the minimal
effort required to introduce Spring for Apache Hadoop. Where possible, the original code was not removed
but commented out.

2. BUILD AND DEPLOYMENT

This directory contains the source files.
For building, JDK 1.6+ are required

a) To build, test and run the sample, use the following command:

*nix/BSD OS:
$ ../gradlew

Windows OS:
$ ..\gradlew

If you have Gradle installed and available in your classpath, you can simply type:
$ gradle

3. IDE IMPORT

To import the code inside an IDE run the command

For Eclipse 
$ ../gradlew eclipse

For IDEA
$ ../gradlew idea

This will generate the IDE specific project files.
