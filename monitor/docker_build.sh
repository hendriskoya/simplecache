#!/bin/bash

if [ "$1" == "build" ]
then
  .././gradlew clean build -xtest -xdistTar
fi

docker build -t monitor:latest .