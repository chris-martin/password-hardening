#!/bin/bash

JAR=target/seclogin.jar

if [ ! -e "$JAR" ]
then
  java -jar sbt-launch.jar assembly
fi

java -jar "$JAR" "$@"
