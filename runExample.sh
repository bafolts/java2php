#!/bin/bash
mvn clean
mvn package

java -jar target/java2php.jar -d php/classes -sourcepath php/src;php/src/javax/servlet/http examples/HelloWorldServlet.java

cd php/classes
php HelloWorld.class.php
