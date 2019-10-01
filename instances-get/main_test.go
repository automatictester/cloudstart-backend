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

func TestGetName(t *testing.T) {
	exp := "My Instance"

	tags := []*ec2.Tag{
		{
			Key:   aws.String("Name"),
			Value: aws.String("My Instance"),
		},
	}

	got := getName(tags)

	if got != exp {
		t.Errorf("\nexp: %s\ngot: %s", exp, got)
	}
}

func TestGetNameNoTag(t *testing.T) {
	exp := "NAME_NOT_FOUND"

	tags := []*ec2.Tag{
		{},
	}

	got := getName(tags)

	if got != exp {
		t.Errorf("\nexp: %s\ngot: %s", exp, got)
	}
}

func TestGetNameTagNoValue(t *testing.T) {
	exp := "NAME_NOT_FOUND"

	tags := []*ec2.Tag{
		{
			Key: aws.String("Name"),
		},
	}

	got := getName(tags)

	if got != exp {
		t.Errorf("\nexp: %s\ngot: %s", exp, got)
	}
}

func TestGetNameTagEmptyValue(t *testing.T) {
	exp := "NAME_NOT_FOUND"

	tags := []*ec2.Tag{
		{
			Key:   aws.String("Name"),
			Value: aws.String(""),
		},
	}

	got := getName(tags)

	if got != exp {
		t.Errorf("\nexp: %s\ngot: %s", exp, got)
	}
}
