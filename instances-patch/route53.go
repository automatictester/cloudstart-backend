package main

import (
	"fmt"
	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/aws/aws-sdk-go/service/route53"
)

var route53Svc *route53.Route53

func init() {
	sess := session.Must(session.NewSessionWithOptions(session.Options{
		SharedConfigState: session.SharedConfigEnable,
	}))
	route53Svc = route53.New(sess)
}

func updateDNSEntry(instanceID string) error {
	fmt.Println("Updating instance " + instanceID + " DNS entry")

	publicIPAddress, err := getPublicIPAddress(instanceID)
	if err != nil {
		return err
	}

	instanceName, err := getInstanceName(instanceID)
	if err != nil {
		return err
	}

	instanceDNSName, err := getItem(instanceName)
	if err != nil {
		return err
	}

	hostedZoneID, err := getItem("HOSTED_ZONE_ID")
	if err != nil {
		return err
	}

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

	_, err = route53Svc.ChangeResourceRecordSets(changeResourceRecordSetsInput)
	return err
}
