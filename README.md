java2php
========

Java to PHP Translator.

An old project I had collecting dust. Decided to get it out into the wild to see if anyone actually needs it. More to come....


Example Usage:
--------------
'''bat
      mvn clean
      mvn install
      java -jar target/java2php.jar -d php/classes -sourcepath php/src examples/HelloWorld.java
      cd php/classes
      php HelloWorld.class.php
'''

* Windows users can run this command quickly with runExample.bat
* Linux users can run this command quickly with runExample.sh


