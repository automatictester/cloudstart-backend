package main

import (
	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/service/ec2"
	"testing"
)

func TestToString(t *testing.T) {
	exp := "instanceId: id, instanceType: type, state: state, name: name"

	i := instance{"id", "type", "state", "name"}
	got := toString(i)

	if got != exp {
		t.Errorf("\nexp: %s\ngot: %s", exp, got)
	}
}

func TestConvertInstance(t *testing.T) {
	var ec2Instance = ec2.Instance{
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

	got := convertInstance(ec2Instance)

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

var nameTests = []struct {
	tag          []*ec2.Tag
	instanceName string
}{
	{
		[]*ec2.Tag{
			{
				Key:   aws.String("Name"),
				Value: aws.String("My Instance"),
			},
		},
		"My Instance",
	},
	{
		[]*ec2.Tag{
			{},
		},
		"NAME_NOT_FOUND",
	},
	{
		[]*ec2.Tag{
			{
				Key: aws.String("Name"),
			},
		},
		"NAME_NOT_FOUND",
	},
	{
		[]*ec2.Tag{
			{
				Key:   aws.String("Name"),
				Value: aws.String(""),
			},
		},
		"NAME_NOT_FOUND",
	},
}

func TestGetName(t *testing.T) {
	for _, testDataItem := range nameTests {
		got := getName(testDataItem.tag)
		exp := testDataItem.instanceName
		if got != exp {
			t.Errorf("\nexp: %s\ngot: %s", exp, got)
		}
	}
}
