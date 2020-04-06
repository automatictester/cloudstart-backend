#!/usr/bin/env bash

set -x
set -e
./mvnw clean package
rm -rf tf/.terraform
(cd tf || exit 1; terraform init)
(cd tf || exit 1; terraform apply -auto-approve)
