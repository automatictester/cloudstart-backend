#!/usr/bin/env bash

BUCKET_NAME=automatictester.co.uk-cloudstart-backend-tf-state

aws s3api create-bucket \
   --bucket ${BUCKET_NAME} \
   --acl private \
   --region eu-west-2 \
   --create-bucket-configuration LocationConstraint=eu-west-2

aws s3api put-public-access-block \
   --bucket ${BUCKET_NAME} \
   --public-access-block-configuration '{"BlockPublicAcls":true,"IgnorePublicAcls":true,"BlockPublicPolicy":true,"RestrictPublicBuckets":true}'
