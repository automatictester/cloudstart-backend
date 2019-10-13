package main

import (
	"errors"
	"fmt"
	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/aws/aws-sdk-go/service/ec2"
)

var ec2Svc *ec2.EC2

func init() {
	sess := session.Must(session.NewSessionWithOptions(session.Options{
		SharedConfigState: session.SharedConfigEnable,
	}))
	ec2Svc = ec2.New(sess)
}

func startInstance(instanceID string) {
	fmt.Println("Starting instance " + instanceID)

	input := &ec2.StartInstancesInput{
		InstanceIds: []*string{
			aws.String(instanceID),
		},
	}

	_, _ = ec2Svc.StartInstances(input)
}

func stopInstance(instanceID string) {
	fmt.Println("Stopping instance " + instanceID)

	input := &ec2.StopInstancesInput{
		InstanceIds: []*string{
			aws.String(instanceID),
		},
	}

	_, _ = ec2Svc.StopInstances(input)
}

func rebootInstance(instanceID string) {
	fmt.Println("Rebooting instance " + instanceID)

	input := &ec2.RebootInstancesInput{
		InstanceIds: []*string{
			aws.String(instanceID),
		},
	}

	_, _ = ec2Svc.RebootInstances(input)
}

func terminateInstance(instanceID string) {
	fmt.Println("Terminating instance " + instanceID)

	input := &ec2.TerminateInstancesInput{
		InstanceIds: []*string{
			aws.String(instanceID),
		},
	}

	_, _ = ec2Svc.TerminateInstances(input)
}

func waitUntilInstanceRunning(instanceID string) {
	fmt.Println("Waiting for instance " + instanceID)

	input := &ec2.DescribeInstancesInput{
		InstanceIds: []*string{
			aws.String(instanceID),
		},
	}

	_ = ec2Svc.WaitUntilInstanceRunning(input)
}

func waitUntilInstanceStopped(instanceID string) {
	fmt.Println("Waiting for instance " + instanceID)

	input := &ec2.DescribeInstancesInput{
		InstanceIds: []*string{
			aws.String(instanceID),
		},
	}

	_ = ec2Svc.WaitUntilInstanceStopped(input)
}

func waitUntilInstanceTerminated(instanceID string) {
	fmt.Println("Waiting for instance " + instanceID)

	input := &ec2.DescribeInstancesInput{
		InstanceIds: []*string{
			aws.String(instanceID),
		},
	}

	_ = ec2Svc.WaitUntilInstanceTerminated(input)
}

func getInstanceName(instanceID string) (string, error) {

	input := &ec2.DescribeInstancesInput{
		InstanceIds: []*string{
			aws.String(instanceID),
		},
	}

	result, err := ec2Svc.DescribeInstances(input)
	if err != nil {
		return "", err
	}

	for _, reservation := range result.Reservations {
		for _, instance := range reservation.Instances {
			for _, tag := range instance.Tags {
				if tag.Key != nil && *tag.Key == "Name" && tag.Value != nil && *tag.Value != "" {
					return *tag.Value, nil
				}
			}
		}
	}
	errorMessage := fmt.Sprintf("Unable to find Name tag for %s", instanceID)
	return "", errors.New(errorMessage)
}

func getPublicIPAddress(instanceID string) (string, error) {

	input := &ec2.DescribeInstancesInput{
		InstanceIds: []*string{
			aws.String(instanceID),
		},
	}

	result, err := ec2Svc.DescribeInstances(input)
	if err != nil {
		return "", err
	}

	for _, reservation := range result.Reservations {
		for _, instance := range reservation.Instances {
			if *instance.InstanceId == instanceID {
				return *instance.PublicIpAddress, nil
			}
		}
	}
	errorMessage := fmt.Sprintf("Unable to find public IP address for %s", instanceID)
	return "", errors.New(errorMessage)
}
