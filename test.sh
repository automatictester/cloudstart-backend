#!/usr/bin/env bash

set -x
set -e
(cd terratest || exit 1; go test)
