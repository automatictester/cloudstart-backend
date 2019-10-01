#!/usr/bin/env bash

cd instances-get || exit 1
go test
GOOS=linux go build main.go
zip cloudstart-backend-instances-get.zip main
