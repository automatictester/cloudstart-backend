#!/usr/bin/env bash

(cd instances-get || exit 1; go test)
(cd instances-get || exit 1; GOOS=linux go build)
(cd instances-get || exit 1; zip cloudstart-backend-instances-get.zip main)

(cd instances-patch || exit 1; GOOS=linux go build)
(cd instances-patch || exit 1; mv instances-patch main)
(cd instances-patch || exit 1; zip cloudstart-backend-instances-patch.zip main)

ls -lR | grep zip
