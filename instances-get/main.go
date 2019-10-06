package main

import (
	"github.com/aws/aws-lambda-go/lambda"
)

type instancesGetResponse struct {
	Instances []instance `json:"instances"`
}

func main() {
	lambda.Start(handleRequest)
}

func handleRequest() (instancesGetResponse, error) {
	return instancesGetResponse{getInstances()}, nil
}
