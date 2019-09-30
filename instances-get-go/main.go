package main

import (
	"fmt"
	"github.com/aws/aws-lambda-go/lambda"
	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/aws/aws-sdk-go/service/ec2"
)

type Instance struct {
	InstanceId   string `json:"instanceId"`
	InstanceType string `json:"instanceType"`
	State        string `json:"state"`
	Name         string `json:"name"`
}

type InstancesGetResponse struct {
	Instances []Instance `json:"instances"`
}

func main() {
	lambda.Start(handleRequest)
}

func handleRequest() (InstancesGetResponse, error) {
	return InstancesGetResponse{Instances: getInstances()}, nil
}

func getInstances() []Instance {
	sess := session.Must(session.NewSessionWithOptions(session.Options{
		SharedConfigState: session.SharedConfigEnable,
	}))
	svc := ec2.New(sess)

	result, err := svc.DescribeInstances(nil)
	if err != nil {
		panic(err.Error())
	}

	var instances []Instance
	for _, reservation := range result.Reservations {
		for _, instance := range reservation.Instances {
			i := convertInstance(*instance)
			instances = append(instances, i)
			fmt.Printf("%s\n", toString(i))
		}
	}
	return instances
}

func toString(instance Instance) string {
	return fmt.Sprintf("instanceId: %s, instanceType: %s, status: %s, name: %s", instance.InstanceId, instance.InstanceType, instance.State, instance.Name)
}

func convertInstance(instance ec2.Instance) Instance {
	var name = getName(instance.Tags)
	return Instance{*instance.InstanceId, *instance.InstanceType, *instance.State.Name, name}
}

func getName(tags []*ec2.Tag) string {
	var name = "NAME_NOT_FOUND"
	for _, tag := range tags {
		if *tag.Key == "Name" {
			name = *tag.Value
		}
	}
	return name
}
