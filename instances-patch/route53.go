package main

import (
	"fmt"
	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/aws/aws-sdk-go/service/route53"
)

var ROUTE53 *route53.Route53

func init() {
	sess := session.Must(session.NewSessionWithOptions(session.Options{
		SharedConfigState: session.SharedConfigEnable,
	}))
	ROUTE53 = route53.New(sess)
}

func updateDNSEntry(instanceID string) {
	fmt.Println("Updating instance " + instanceID + " DNS entry")

	publicIPAddress, _ := getPublicIPAddress(instanceID)
	instanceName, _ := getInstanceName(instanceID)
	instanceDNSName, _ := getItem(instanceName)
	hostedZoneID, _ := getItem("HOSTED_ZONE_ID")

	resourceRecord := []*route53.ResourceRecord{
		{
			Value: aws.String(publicIPAddress),
		},
	}

	resourceRecordSet := &route53.ResourceRecordSet{
		Type:            aws.String("A"),
		Name:            aws.String(instanceDNSName),
		TTL:             aws.Int64(60),
		ResourceRecords: resourceRecord,
	}

	change := []*route53.Change{
		{
			Action:            aws.String("UPSERT"),
			ResourceRecordSet: resourceRecordSet,
		},
	}

	changeBatch := &route53.ChangeBatch{
		Changes: change,
	}

	changeResourceRecordSetsInput := &route53.ChangeResourceRecordSetsInput{
		HostedZoneId: aws.String(hostedZoneID),
		ChangeBatch:  changeBatch,
	}

	_, err := ROUTE53.ChangeResourceRecordSets(changeResourceRecordSetsInput)
	if err != nil {
		fmt.Println(err.Error())
		return
	}
}
