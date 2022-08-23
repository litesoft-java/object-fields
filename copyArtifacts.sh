#!/bin/bash
if [ ! -f ./pom.xml ]
then
    echo "No pom file: ./pom.xml"
    exit 1
fi
if [ ! -f ./target/object-fields.jar ]
then
    echo "No jar file: ./target/object-fields.jar"
    exit 2
fi
if [ ! -d ./artifacts ]
then
    mkdir artifacts
fi
cp target/object-fields.jar artifacts/object-fields.jar
cp pom.xml artifacts/pom.xml