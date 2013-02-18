#!/bin/bash

JAR=target/seclogin.jar

if [ ! -e "$JAR" ]
then
  mvn install
fi

java -jar "$JAR" "$@"
