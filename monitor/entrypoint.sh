#!/bin/bash

# kubectl proxy --port=8080 &

# kubectl proxy --port=8080

nohup monitor/bin/monitor &

kubectl proxy --port=8080