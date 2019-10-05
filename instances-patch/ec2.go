package main

import (
	"errors"
	"fmt"
	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/aws/aws-sdk-go/service/ec2"
)

func getEC2() *ec2.EC2 {
	sess := session.Must(session.NewSessionWithOptions(session.Options{
		SharedConfigState: session.SharedConfigEnable,
	}))
	return ec2.New(sess)
}

func startInstance(instanceID string) {
	fmt.Println("Starting instance " + instanceID)
	svc := getEC2()

	input := &ec2.StartInstancesInput{
		InstanceIds: []*string{
			aws.String(instanceID),
		},
	}

	_, _ = svc.StartInstances(input)
}

func stopInstance(instanceID string) {
	fmt.Println("Stopping instance " + instanceID)
	svc := getEC2()

	input := &ec2.StopInstancesInput{
		InstanceIds: []*string{
			aws.String(instanceID),
		},
	}

	_, _ = svc.StopInstances(input)
}

func rebootInstance(instanceID string) {
	fmt.Println("Rebooting instance " + instanceID)
	svc := getEC2()

	input := &ec2.RebootInstancesInput{
		InstanceIds: []*string{
			aws.String(instanceID),
		},
	}

	_, _ = svc.RebootInstances(input)
}

func terminateInstance(instanceID string) {
	fmt.Println("Terminating instance " + instanceID)
	svc := getEC2()

	input := &ec2.TerminateInstancesInput{
		InstanceIds: []*string{
			aws.String(instanceID),
		},
	}

	_, _ = svc.TerminateInstances(input)
}

func waitUntilInstanceRunning(instanceID string) {
	fmt.Println("Waiting for instance " + instanceID)
	svc := getEC2()

	input := &ec2.DescribeInstancesInput{
		InstanceIds: []*string{
			aws.String(instanceID),
		},
	}

	_ = svc.WaitUntilInstanceRunning(input)
}

func waitUntilInstanceStopped(instanceID string) {
	fmt.Println("Waiting for instance " + instanceID)
	svc := getEC2()

	input := &ec2.DescribeInstancesInput{
		InstanceIds: []*string{
			aws.String(instanceID),
		},
	}

	_ = svc.WaitUntilInstanceStopped(input)
}

func waitUntilInstanceTerminated(instanceID string) {
	fmt.Println("Waiting for instance " + instanceID)
	svc := getEC2()

	input := &ec2.DescribeInstancesInput{
		InstanceIds: []*string{
			aws.String(instanceID),
		},
	}

	_ = svc.WaitUntilInstanceTerminated(input)
}

func getInstanceName(instanceID string) (string, error) {
	svc := getEC2()

	input := &ec2.DescribeInstancesInput{
		InstanceIds: []*string{
			aws.String(instanceID),
		},
	}

	result, err := svc.DescribeInstances(input)
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
	svc := getEC2()

	input := &ec2.DescribeInstancesInput{
		InstanceIds: []*string{
			aws.String(instanceID),
		},
	}

	result, err := svc.DescribeInstances(input)
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
