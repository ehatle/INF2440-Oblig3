#!/bin/sh
mkdir ./bin
javac -d ./bin ./src/*.java
echo "Compilation completed, now to run the code..."
java -Xmx4000m -cp bin/ Oblig3
echo "Run completed, now to remove the .class files"
rm -rf ./bin