#!/bin/bash

JAR=target/seclogin.jar

if [ ! -e "$JAR" ]
then
  sbt assembly
fi

java -jar "$JAR" "$@"
