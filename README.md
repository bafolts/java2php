java2php
========

Java to PHP Translator.

Java is a beautiful programming language -- PHP is not. The compiled features of Java make maintaining the source code and extending functionality must simpler than PHP. The problem is that not all cheap web hosting companies currently provide a way to run Java code on their servers. The goal of this project is to provide a way for basic websites with minimal server side logic to run on hosting platforms that can only run PHP. Although it is only intended to be used for basic websites which need to run in PHP, this isn't to say that the project will not attempt to provide a complete java to php translation. Besides threading, or other very complicated features of java, most basic features should translate fine. If your java code compiles given the included java.* library that the translator supports than your PHP should work as the Java did. The compiler will throw warnings in cases where the PHP equivalent is not supported.


Example Usage:
--------------
You will need Maven to compile and run the provided example HelloWorld Servlet.

* Windows users can compile the example quickly with the [runExample batch script](runExample.bat).
* Linux users can compile the example quickly with the [runExample shell script](runExample.sh).

After the shell script has ran 'HelloWorld.class.php' should be created. To then run the servlet a server will have to be setup so that HelloWorld.class.php can be hit directly from a browser. If you happen to have a server setup at localhost then hitting (http://localhost/HelloWorld.class.php) should output the famous message.


