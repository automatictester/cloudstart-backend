#!/usr/bin/env bash

(cd instances-get; go test)
(cd instances-get; GOOS=linux go build main.go)
(cd instances-get; zip cloudstart-backend-instances-get.zip main)

(cd instances-patch; GOOS=linux go build)
(cd instances-patch; mv instances-patch main)
(cd instances-patch; zip cloudstart-backend-instances-patch.zip main)

ls -lR | grep zip
