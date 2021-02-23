#!/bin/bash

# kubectl proxy --port=8080 &

# kubectl proxy --port=8080

nohup monitorWorker/bin/monitorWorker &

kubectl proxy --port=8080