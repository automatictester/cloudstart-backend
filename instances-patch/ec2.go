package main

import (
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

func startInstance(instanceID string) error {
	fmt.Println("Starting instance " + instanceID)

	input := &ec2.StartInstancesInput{
		InstanceIds: []*string{
			aws.String(instanceID),
		},
	}

	_, err := ec2Svc.StartInstances(input)
	if err != nil {
		return err
	}
	return nil
}

func stopInstance(instanceID string) error {
	fmt.Println("Stopping instance " + instanceID)

	input := &ec2.StopInstancesInput{
		InstanceIds: []*string{
			aws.String(instanceID),
		},
	}

	_, err := ec2Svc.StopInstances(input)
	if err != nil {
		return err
	}
	return nil
}

func rebootInstance(instanceID string) error {
	fmt.Println("Rebooting instance " + instanceID)

	input := &ec2.RebootInstancesInput{
		InstanceIds: []*string{
			aws.String(instanceID),
		},
	}

	_, err := ec2Svc.RebootInstances(input)
	if err != nil {
		return err
	}
	return nil
}

func terminateInstance(instanceID string) error {
	fmt.Println("Terminating instance " + instanceID)

	input := &ec2.TerminateInstancesInput{
		InstanceIds: []*string{
			aws.String(instanceID),
		},
	}

	_, err := ec2Svc.TerminateInstances(input)
	if err != nil {
		return err
	}
	return nil
}

func waitUntilInstanceRunning(instanceID string) error {
	fmt.Println("Waiting for instance " + instanceID)

	input := &ec2.DescribeInstancesInput{
		InstanceIds: []*string{
			aws.String(instanceID),
		},
	}

	err := ec2Svc.WaitUntilInstanceRunning(input)
	if err != nil {
		return err
	}
	return nil
}

func waitUntilInstanceStopped(instanceID string) error {
	fmt.Println("Waiting for instance " + instanceID)

	input := &ec2.DescribeInstancesInput{
		InstanceIds: []*string{
			aws.String(instanceID),
		},
	}

	err := ec2Svc.WaitUntilInstanceStopped(input)
	if err != nil {
		return err
	}
	return nil
}

func waitUntilInstanceTerminated(instanceID string) error {
	fmt.Println("Waiting for instance " + instanceID)

	input := &ec2.DescribeInstancesInput{
		InstanceIds: []*string{
			aws.String(instanceID),
		},
	}

	err := ec2Svc.WaitUntilInstanceTerminated(input)
	if err != nil {
		return err
	}
	return nil
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
	err = fmt.Errorf("unable to find Name tag for %s", instanceID)
	return "", err
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
	err = fmt.Errorf("unable to find public IP address for %s", instanceID)
	return "", err
}
