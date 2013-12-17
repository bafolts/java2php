call mvn clean
call mvn package

java -jar target/java2php.jar -d php/classes -sourcepath php/src examples/HelloWorld.java

cd php/classes
php HelloWorld.class.php
