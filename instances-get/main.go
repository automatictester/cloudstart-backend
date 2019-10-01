package main

import (
	"fmt"
	"github.com/aws/aws-lambda-go/lambda"
	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/aws/aws-sdk-go/service/ec2"
)

type instance struct {
	InstanceID   string `json:"instanceId"`
	InstanceType string `json:"instanceType"`
	State        string `json:"state"`
	Name         string `json:"name"`
}

type instancesGetResponse struct {
	Instances []instance `json:"instances"`
}

func main() {
	lambda.Start(handleRequest)
}

func handleRequest() (instancesGetResponse, error) {
	return instancesGetResponse{getInstances()}, nil
}

func getInstances() []instance {
	sess := session.Must(session.NewSessionWithOptions(session.Options{
		SharedConfigState: session.SharedConfigEnable,
	}))
	svc := ec2.New(sess)

	result, err := svc.DescribeInstances(nil)
	if err != nil {
		panic(err.Error())
	}

	var instances []instance
	for _, reservation := range result.Reservations {
		for _, instance := range reservation.Instances {
			i := convertInstance(*instance)
			instances = append(instances, i)
			fmt.Printf("%s\n", toString(i))
		}
	}
	return instances
}

func toString(instance instance) string {
	return fmt.Sprintf("instanceId: %s, instanceType: %s, status: %s, name: %s", instance.InstanceID, instance.InstanceType, instance.State, instance.Name)
}

func convertInstance(ec2instance ec2.Instance) instance {
	var name = getName(ec2instance.Tags)
	return instance{*ec2instance.InstanceId, *ec2instance.InstanceType, *ec2instance.State.Name, name}
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
