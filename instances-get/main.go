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

type ec2Instance ec2.Instance

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
			i := newInstance(ec2Instance(*instance))
			instances = append(instances, i)
			fmt.Println(i)
		}
	}
	return instances
}

func (i instance) String() string {
	return fmt.Sprintf("instanceId: %s, instanceType: %s, state: %s, name: %s",
		i.InstanceID,
		i.InstanceType,
		i.State,
		i.Name)
}

func newInstance(i ec2Instance) instance {
	return instance{
		*i.InstanceId,
		*i.InstanceType,
		*i.State.Name,
		i.getName()}
}

func (i ec2Instance) getName() string {
	name := "NAME_NOT_FOUND"
	for _, tag := range i.Tags {
		if tag.Key != nil && *tag.Key == "Name" && tag.Value != nil && *tag.Value != "" {
			name = *tag.Value
		}
	}
	return name
}
