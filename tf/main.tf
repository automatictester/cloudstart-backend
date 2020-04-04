terraform {
  backend "s3" {
    bucket             = "automatictester.co.uk-cloudstart-backend-tf-state"
    key                = "lambda-test-runner.tfstate"
    region             = "eu-west-2"
  }
}

provider "aws" {
  region               = "eu-west-2"
  version              = "2.55"
}

resource "aws_s3_bucket" "jar" {
  bucket               = var.s3_bucket_jar
  acl                  = "private"
}

resource "aws_s3_bucket_object" "update_dns_jar" {
  bucket               = aws_s3_bucket.jar.bucket
  key                  = var.update_dns_jar_file_name
  source               = "${path.module}/../target/${var.update_dns_jar_file_name}"
  etag                 = filemd5("${path.module}/../target/${var.update_dns_jar_file_name}")
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

resource "aws_lambda_function" "update_dns" {
  function_name                  = "updateDns"
  handler                        = "uk.co.automatictester.cloudstart.backend.UpdateDnsHandler::handleRequest"
  runtime                        = "java11"
  s3_bucket                      = aws_s3_bucket.jar.bucket
  s3_key                         = aws_s3_bucket_object.update_dns_jar.key
  source_code_hash               = filebase64sha256("${path.module}/../target/${var.update_dns_jar_file_name}")
  role                           = "arn:aws:iam::${data.aws_caller_identity.current.account_id}:role/${aws_iam_role.cloudstart_lambda_update_dns.name}"
  memory_size                    = "1024"
  timeout                        = "30"
}

resource "aws_iam_role" "cloudstart_lambda_update_dns" {
  name                 = "CloudStartLambdaUpdateDns"
  assume_role_policy   = file("iam-policy/assume-role-policy.json")
}

resource "aws_iam_role_policy_attachment" "lambda_update_dns_cloudwatch_policy" {
  role                 = aws_iam_role.cloudstart_lambda_update_dns.name
  policy_arn           = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

resource "aws_iam_role_policy_attachment" "lambda_update_dns_functional_policy" {
  role                 = aws_iam_role.cloudstart_lambda_update_dns.name
  policy_arn           = aws_iam_policy.cloudstart_lambda_update_dns.arn
}

resource "aws_iam_policy" "cloudstart_lambda_update_dns" {
  name                 = "CloudStartLambdaUpdateDns"
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
                "ec2:DescribeInstances",
                "route53:ChangeResourceRecordSets"
            ],
            "Resource": "*"
        }
    ]
}
EOF
}

resource "aws_iam_policy" "cloudstart_mobile_app" {
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
                "arn:aws:lambda:${var.region}:*:function:updateDns"
            ]
        },
        {
            "Effect": "Allow",
            "Action": [
                "ec2:DescribeInstances",
                "ec2:RebootInstances",
                "ec2:StartInstances",
                "ec2:StopInstances",
                "ec2:TerminateInstances"
            ],
            "Resource": "*"
        }
    ]
}
EOF
}
