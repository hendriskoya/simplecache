#!/bin/bash

if [ "$1" == "build" ]
then
  ./gradlew clean build -xtest -xdistZip -xdistTar -xbootDistTar
fi

docker build -t simplecache:latest .