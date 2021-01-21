#!/bin/bash

docker run \
  --name simplecache \
  --network host \
  --rm \
  simplecache:latest