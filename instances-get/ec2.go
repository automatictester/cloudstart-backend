package main

import (
	"fmt"
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

func getInstances() ([]instance, error) {
	result, err := ec2Svc.DescribeInstances(nil)
	if err != nil {
		return nil, err
	}

	var instances []instance
	for _, reservation := range result.Reservations {
		for _, instance := range reservation.Instances {
			i := ec2Instance(*instance).convertToInstance()
			instances = append(instances, i)
			fmt.Println(i)
		}
	}
	return instances, nil
}
