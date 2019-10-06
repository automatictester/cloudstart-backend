package main

import (
	"fmt"
	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/aws/aws-sdk-go/service/ec2"
)

func getEC2() *ec2.EC2 {
	sess := session.Must(session.NewSessionWithOptions(session.Options{
		SharedConfigState: session.SharedConfigEnable,
	}))
	return ec2.New(sess)
}

func getInstances() []instance {
	svc := getEC2()

	result, err := svc.DescribeInstances(nil)
	if err != nil {
		panic(err.Error())
	}

	var instances []instance
	for _, reservation := range result.Reservations {
		for _, instance := range reservation.Instances {
			i := ec2Instance(*instance).convertToInstance()
			instances = append(instances, i)
			fmt.Println(i)
		}
	}
	return instances
}
