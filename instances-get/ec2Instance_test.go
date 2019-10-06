package main

import (
	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/service/ec2"
	"testing"
)

func TestConvertToInstance(t *testing.T) {
	testInstance := ec2.Instance{
		InstanceId:   aws.String("instanceId"),
		InstanceType: aws.String("instanceType"),
		Tags: []*ec2.Tag{
			{
				Key:   aws.String("Name"),
				Value: aws.String("name"),
			},
		},
		State: &ec2.InstanceState{
			Name: aws.String("stopped"),
		},
	}

	got := ec2Instance(testInstance).convertToInstance()

	if got.InstanceID != "instanceId" {
		t.Errorf("\nexp: %s\ngot: %s", "instanceId", got.InstanceID)
	}
	if got.InstanceType != "instanceType" {
		t.Errorf("\nexp: %s\ngot: %s", "instanceType", got.InstanceType)
	}
	if got.Name != "name" {
		t.Errorf("\nexp: %s\ngot: %s", "name", got.Name)
	}
	if got.State != "stopped" {
		t.Errorf("\nexp: %s\ngot: %s", "state", got.State)
	}
}

var instanceNameTestData = []struct {
	ec2Instance          ec2Instance
	expectedInstanceName string
}{
	{
		ec2Instance(ec2.Instance{
			InstanceId:   aws.String("instanceId"),
			InstanceType: aws.String("instanceType"),
			Tags: []*ec2.Tag{
				{
					Key:   aws.String("Name"),
					Value: aws.String("My Instance"),
				},
			},
			State: &ec2.InstanceState{
				Name: aws.String("stopped"),
			},
		}),
		"My Instance",
	},
	{
		ec2Instance(ec2.Instance{
			InstanceId:   aws.String("instanceId"),
			InstanceType: aws.String("instanceType"),
			Tags: []*ec2.Tag{
				{},
			},
			State: &ec2.InstanceState{
				Name: aws.String("stopped"),
			},
		}),
		"NAME_NOT_FOUND",
	},
	{
		ec2Instance(ec2.Instance{
			InstanceId:   aws.String("instanceId"),
			InstanceType: aws.String("instanceType"),
			Tags: []*ec2.Tag{
				{
					Key: aws.String("Name"),
				},
			},
			State: &ec2.InstanceState{
				Name: aws.String("stopped"),
			},
		}),
		"NAME_NOT_FOUND",
	},
	{
		ec2Instance(ec2.Instance{
			InstanceId:   aws.String("instanceId"),
			InstanceType: aws.String("instanceType"),
			Tags: []*ec2.Tag{
				{
					Key:   aws.String("Name"),
					Value: aws.String(""),
				},
			},
			State: &ec2.InstanceState{
				Name: aws.String("stopped"),
			},
		}),
		"NAME_NOT_FOUND",
	},
}

func TestGetName(t *testing.T) {
	for _, example := range instanceNameTestData {
		got := example.ec2Instance.getName()
		exp := example.expectedInstanceName
		if got != exp {
			t.Errorf("\nexp: %s\ngot: %s", exp, got)
		}
	}
}