package main

import (
	"errors"
	"fmt"
	"github.com/aws/aws-lambda-go/lambda"
)

type instancesGetResponse struct {
	Instances []instance `json:"instances"`
}

func main() {
	lambda.Start(handleRequest)
}

func handleRequest() (instancesGetResponse, error) {
	instances, err := getInstances()
	if err != nil {
		fmt.Println(err.Error())
		return instancesGetResponse{instances}, errors.New("cannot retrieve list of EC2 instances")
	}
	return instancesGetResponse{instances}, nil
}
