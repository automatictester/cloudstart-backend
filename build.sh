#!/usr/bin/env bash

cd instances-get-go || exit
GOOS=linux go build main.go
zip cloudstart-backend-instances-get.zip main
