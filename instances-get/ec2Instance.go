package main

import "github.com/aws/aws-sdk-go/service/ec2"

type ec2Instance ec2.Instance

func (i ec2Instance) convertToInstance() instance {
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
