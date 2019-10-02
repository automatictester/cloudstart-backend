package main

import (
	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/service/ec2"
	"testing"
)

func TestString(t *testing.T) {
	exp := "instanceId: id, instanceType: type, state: state, name: name"

	i := instance{"id", "type", "state", "name"}
	got := i.String()

	if got != exp {
		t.Errorf("\nexp: %s\ngot: %s", exp, got)
	}
}

func TestNewInstance(t *testing.T) {
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

	got := newInstance(ec2Instance(testInstance))

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
	instance     ec2Instance
	instanceName string
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
		got := example.instance.getName()
		exp := example.instanceName
		if got != exp {
			t.Errorf("\nexp: %s\ngot: %s", exp, got)
		}
	}
}
