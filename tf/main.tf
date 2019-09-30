terraform {
  backend "s3" {
    bucket             = "automatictester.co.uk-cloudstart-backend-tf-state"
    key                = "lambda-test-runner.tfstate"
    region             = "eu-west-2"
  }
}

provider "aws" {
  region               = "eu-west-2"
}

resource "aws_s3_bucket" "jar" {
  bucket               = "${var.s3_bucket_jar}"
  acl                  = "private"
}

resource "aws_s3_bucket_object" "instances_get_zip" {
  bucket               = "${aws_s3_bucket.jar.bucket}"
  key                  = "${var.instances_get_zip_file_name}"
  source               = "${path.module}/../instances-get/${var.instances_get_zip_file_name}"
  etag                 = "${md5(file("${path.module}/../instances-get/${var.instances_get_zip_file_name}"))}"
}

resource "aws_s3_bucket_object" "instances_patch_jar" {
  bucket               = "${aws_s3_bucket.jar.bucket}"
  key                  = "${var.instances_patch_jar_file_name}"
  source               = "${path.module}/../instances-patch/target/${var.instances_patch_jar_file_name}"
  etag                 = "${md5(file("${path.module}/../instances-patch/target/${var.instances_patch_jar_file_name}"))}"
}

resource "aws_iam_role" "cloudstart_lambda_instances_get" {
  name                 = "CloudStartLambdaInstancesGet"
  assume_role_policy   = "${file("iam-policy/assume-role-policy.json")}"
}

resource "aws_iam_role" "cloudstart_lambda_instances_patch" {
  name                 = "CloudStartLambdaInstancesPatch"
  assume_role_policy   = "${file("iam-policy/assume-role-policy.json")}"
}

resource "aws_iam_role" "cloudstart_cognito" {
  name                 = "CloudStartMobileApp"
  assume_role_policy   = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Federated": "cognito-identity.amazonaws.com"
      },
      "Action": "sts:AssumeRoleWithWebIdentity",
      "Condition": {
        "StringEquals": {
          "cognito-identity.amazonaws.com:aud": "${aws_cognito_identity_pool.cognito_identity_pool.id}"
        },
        "ForAnyValue:StringLike": {
          "cognito-identity.amazonaws.com:amr": "unauthenticated"
        }
      }
    }
  ]
}
EOF
}

resource "aws_iam_policy" "cloudstart_lambda_instances_get" {
  name                 = "CloudStartLambdaInstancesGet"
  path                 = "/"
  policy               = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": "ec2:DescribeInstances",
            "Resource": "*"
        }
    ]
}
EOF
}

resource "aws_iam_policy" "cloudstart_lambda_instances_patch" {
  name                 = "CloudStartLambdaInstancesPatch"
  path                 = "/"
  policy               = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": "dynamodb:GetItem",
            "Resource": "arn:aws:dynamodb:${var.region}:*:table/${aws_dynamodb_table.cloudstartstore.name}"
        },
        {
            "Effect": "Allow",
            "Action": [
                "ec2:RebootInstances",
                "ec2:DescribeInstances",
                "ec2:TerminateInstances",
                "ec2:StartInstances",
                "ec2:StopInstances",
                "route53:ChangeResourceRecordSets"
            ],
            "Resource": "*"
        }
    ]
}
EOF
}

resource "aws_iam_policy" "cloudstart_cognito" {
  name                 = "CloudStartMobileApp"
  path                 = "/"
  policy               = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": "lambda:InvokeFunction",
            "Resource": [
                "arn:aws:lambda:${var.region}:*:function:instancesGet",
                "arn:aws:lambda:${var.region}:*:function:instancesPatch"
            ]
        }
    ]
}
EOF
}

resource "aws_cognito_identity_pool" "cognito_identity_pool" {
  identity_pool_name               = "CloudStart"
  allow_unauthenticated_identities = true
}

resource "aws_cognito_identity_pool_roles_attachment" "cognito_identity_pool" {
  identity_pool_id = "${aws_cognito_identity_pool.cognito_identity_pool.id}"

  roles = {
    "unauthenticated" = "${aws_iam_role.cloudstart_cognito.arn}"
  }
}

resource "aws_dynamodb_table" "cloudstartstore" {
  name           = "CloudStartStore"
  billing_mode   = "PAY_PER_REQUEST"
  hash_key       = "Key"

  attribute {
    name = "Key"
    type = "S"
  }
}

resource "aws_iam_role_policy_attachment" "lambda_instances_get_functional_policy" {
  role                 = "${aws_iam_role.cloudstart_lambda_instances_get.name}"
  policy_arn           = "${aws_iam_policy.cloudstart_lambda_instances_get.arn}"
}

resource "aws_iam_role_policy_attachment" "lambda_instances_patch_functional_policy" {
  role                 = "${aws_iam_role.cloudstart_lambda_instances_patch.name}"
  policy_arn           = "${aws_iam_policy.cloudstart_lambda_instances_patch.arn}"
}

resource "aws_iam_role_policy_attachment" "lambda_instances_get_cloudwatch_policy" {
  role                 = "${aws_iam_role.cloudstart_lambda_instances_get.name}"
  policy_arn           = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

resource "aws_iam_role_policy_attachment" "lambda_instances_patch_cloudwatch_policy" {
  role                 = "${aws_iam_role.cloudstart_lambda_instances_patch.name}"
  policy_arn           = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

resource "aws_iam_role_policy_attachment" "cognito_policy" {
  role                 = "${aws_iam_role.cloudstart_cognito.name}"
  policy_arn           = "${aws_iam_policy.cloudstart_cognito.arn}"
}

resource "aws_lambda_function" "instances_get" {
  function_name                  = "instancesGet"
  handler                        = "main"
  runtime                        = "go1.x"
  s3_bucket                      = "${aws_s3_bucket.jar.bucket}"
  s3_key                         = "${aws_s3_bucket_object.instances_get_zip.key}"
  source_code_hash               = "${base64sha256(file("${path.module}/../instances-get/${var.instances_get_zip_file_name}"))}"
  role                           = "arn:aws:iam::${data.aws_caller_identity.current.account_id}:role/${aws_iam_role.cloudstart_lambda_instances_get.name}"
  memory_size                    = "1024"
  timeout                        = "900"
}

resource "aws_lambda_function" "instances_patch" {
  function_name                  = "instancesPatch"
  handler                        = "uk.co.automatictester.cloudstart.backend.instances.patch.InstancesPatchHandler::handleRequest"
  runtime                        = "java8"
  s3_bucket                      = "${aws_s3_bucket.jar.bucket}"
  s3_key                         = "${aws_s3_bucket_object.instances_patch_jar.key}"
  source_code_hash               = "${base64sha256(file("${path.module}/../instances-patch/target/${var.instances_patch_jar_file_name}"))}"
  role                           = "arn:aws:iam::${data.aws_caller_identity.current.account_id}:role/${aws_iam_role.cloudstart_lambda_instances_patch.name}"
  memory_size                    = "3008"
  timeout                        = "900"
}
