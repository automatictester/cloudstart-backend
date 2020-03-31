#!/usr/bin/env bash

set -x
set -e
./mvnw clean verify
(cd tf || exit 1; terraform apply -auto-approve)
