package main

import (
	"fmt"
	"github.com/aws/aws-lambda-go/lambda"
)

type updateDNSRequest struct {
	InstanceID string `json:"instanceId"`
	Action     string `json:"action"`
}

type updateDNSResponse struct {
	InstanceID string `json:"instanceId"`
	Action     string `json:"action"`
}

func main() {
	lambda.Start(handleRequest)
}

func handleRequest(request updateDNSRequest) (updateDNSResponse, error) {
	updateDNSResponseOnError := updateDNSResponse{}
	instanceID := request.InstanceID
	action := request.Action

	updateDNSResponse := updateDNSResponse{
		InstanceID: instanceID,
		Action:     action,
	}

	switch action {
	case "upsert":

		instanceName, err := getInstanceName(instanceID)
		if err == nil {
			hasMapping, err := hasCustomHostnameMapping(instanceName)
			if err != nil {
				fmt.Println(err.Error())
				return updateDNSResponseOnError, fmt.Errorf("instance %s started but cannot retrieve DNS mapping", instanceID)
			}
			if hasMapping {
				err := upsertDNSEntry(instanceID)
				if err != nil {
					fmt.Println(err.Error())
					return updateDNSResponseOnError, fmt.Errorf("instance %s started but cannot update DNS record", instanceID)
				}
			}
		}
	case "delete":

		instanceName, err := getInstanceName(instanceID)
		if err == nil {

			hasMapping, err := hasCustomHostnameMapping(instanceName)
			if err != nil {
				fmt.Println(err.Error())
				return updateDNSResponseOnError, fmt.Errorf("error retrieving DNS mapping for instance %s", instanceID)
			}

			if hasMapping {
				err = upsertFakeDNSEntry(instanceID)
				if err != nil {
					fmt.Println(err.Error())
					return updateDNSResponseOnError, fmt.Errorf("error upserting fake DNS entry for instance %s", instanceID)
				}

				err = deleteFakeDNSEntry(instanceID)
				if err != nil {
					fmt.Println(err.Error())
					return updateDNSResponseOnError, fmt.Errorf("error deleting fake DNS entry for instance %s", instanceID)
				}
			}
		}
	}

	return updateDNSResponse, nil
}
