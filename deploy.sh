#!/usr/bin/env bash

./build.sh
(cd tf || exit 1; terraform apply -auto-approve)
