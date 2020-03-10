package main

import (
	"fmt"
	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/aws/aws-sdk-go/service/route53"
)

var route53Svc *route53.Route53
var fakeIPAddress = "192.168.0.1"

func init() {
	sess := session.Must(session.NewSessionWithOptions(session.Options{
		SharedConfigState: session.SharedConfigEnable,
	}))
	route53Svc = route53.New(sess)
}

func upsertDNSEntry(instanceID string) error {
	fmt.Println("Upserting instance " + instanceID + " DNS entry")

	publicIPAddress, err := getPublicIPAddress(instanceID)
	if err != nil {
		return err
	}

	return updateDNSEntry(instanceID, publicIPAddress, "UPSERT")
}

func upsertFakeDNSEntry(instanceID string) error {
	fmt.Println("Upserting instance " + instanceID + " fake DNS entry")
	return updateDNSEntry(instanceID, fakeIPAddress, "UPSERT")
}

func deleteFakeDNSEntry(instanceID string) error {
	fmt.Println("Deleting instance " + instanceID + " fake DNS entry")
	return updateDNSEntry(instanceID, fakeIPAddress, "DELETE")
}

func updateDNSEntry(instanceID string, publicIPAddress string, action string) error {
	if action != "UPSERT" && action != "DELETE" {
		return fmt.Errorf("action not in set [UPSERT, DELETE]: %s", action)
	}

	instanceName, err := getInstanceName(instanceID)
	if err != nil {
		return err
	}

	instanceDNSName, err := getCustomDNSMapping(instanceName)
	if err != nil {
		return err
	}

	hostedZoneID, err := getCustomDNSMapping("HOSTED_ZONE_ID")
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
			Action:            aws.String(action),
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
