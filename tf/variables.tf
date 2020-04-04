variable "s3_bucket_jar" {
  default = "automatictester.co.uk-cloudstart-backend-jar"
}

variable "update_dns_jar_file_name" {
  default = "cloudstart-backend-update-dns.jar"
}

variable "region" {
  default = "eu-west-2"
}

variable "env_suffix" {
  default = ""
  description = "Environment suffix to prevent resource name clashes between environments. Empty string for production"
}
