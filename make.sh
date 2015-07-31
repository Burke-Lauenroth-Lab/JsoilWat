#!/bin/bash

rm -rf bin
mkdir bin
javac -d bin/ -sourcepath src/ src/Main.java
echo "Main-Class: Main" > Manifest.txt
jar cfm soilwat.jar Manifest.txt -C bin/ .
chmod +x soilwat.jar
