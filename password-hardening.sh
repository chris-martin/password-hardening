#!/bin/bash

JAR=target/password-hardening.jar

if [ ! -e "$JAR" ]
then
  mvn install
fi

java -jar "$JAR" "$@"
