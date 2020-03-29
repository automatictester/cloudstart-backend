#!/usr/bin/env bash

(cd update-dns || exit 1; go fmt)
(cd update-dns || exit 1; golint)
(cd update-dns || exit 1; GOOS=linux go build)
(cd update-dns || exit 1; mv update-dns main)
(cd update-dns || exit 1; zip cloudstart-backend-update-dns.zip main)

ls -lR | grep zip
