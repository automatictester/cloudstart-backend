package main

import (
	"fmt"
	"github.com/aws/aws-lambda-go/lambda"
)

type instancesPatchRequest struct {
	InstanceID string `json:"instanceId"`
	Action     string `json:"action"`
}

type instancesPatchResponse struct {
	InstanceID string `json:"instanceId"`
	Action     string `json:"action"`
	Status     string `json:"status"`
	Message    string `json:"message"`
}

func main() {
	lambda.Start(handleRequest)
}

func handleRequest(request instancesPatchRequest) (instancesPatchResponse, error) {
	instancesPatchResponseOnError := instancesPatchResponse{}
	instanceID := request.InstanceID
	action := request.Action

	instancesPatchResponse := instancesPatchResponse{
		InstanceID: instanceID,
		Action:     action,
	}

	switch action {
	case "start":
		err := startInstance(instanceID)
		if err != nil {
			fmt.Println(err.Error())
			return instancesPatchResponseOnError, fmt.Errorf("cannot start instance %s", instanceID)
		}

		err = waitUntilInstanceRunning(instanceID)
		if err != nil {
			fmt.Println(err.Error())
			return instancesPatchResponseOnError, fmt.Errorf("instance %s still not running", instanceID)
		}

		instanceName, err := getInstanceName(instanceID)
		if err == nil {
			hasMapping, err := hasCustomHostnameMapping(instanceName)
			if err != nil {
				fmt.Println(err.Error())
				return instancesPatchResponseOnError, fmt.Errorf("instance %s started but cannot retrieve DNS mapping", instanceID)
			}
			if hasMapping {
				err := upsertDNSEntry(instanceID)
				if err != nil {
					fmt.Println(err.Error())
					return instancesPatchResponseOnError, fmt.Errorf("instance %s started but cannot update DNS record", instanceID)
				}
			}
		}
	case "stop":
		err := stopInstance(instanceID)
		if err != nil {
			fmt.Println(err.Error())
			return instancesPatchResponseOnError, fmt.Errorf("cannot stop instance %s", instanceID)
		}

		instanceName, err := getInstanceName(instanceID)
		if err == nil {

			hasMapping, err := hasCustomHostnameMapping(instanceName)
			if err != nil {
				fmt.Println(err.Error())
				return instancesPatchResponseOnError, fmt.Errorf("error retrieving DNS mapping for instance %s", instanceID)
			}

			if hasMapping {
				err = upsertFakeDNSEntry(instanceID)
				if err != nil {
					fmt.Println(err.Error())
					return instancesPatchResponseOnError, fmt.Errorf("error upserting fake DNS entry for instance %s", instanceID)
				}

				err = deleteFakeDNSEntry(instanceID)
				if err != nil {
					fmt.Println(err.Error())
					return instancesPatchResponseOnError, fmt.Errorf("error deleting fake DNS entry for instance %s", instanceID)
				}
			}
		}

		err = waitUntilInstanceStopped(instanceID)
		if err != nil {
			fmt.Println(err.Error())
			return instancesPatchResponseOnError, fmt.Errorf("instance %s still not stopped", instanceID)
		}
	case "reboot":
		err := rebootInstance(instanceID)
		if err != nil {
			fmt.Println(err.Error())
			return instancesPatchResponseOnError, fmt.Errorf("cannot reboot instance %s", instanceID)
		}
	case "terminate":
		err := terminateInstance(instanceID)
		if err != nil {
			fmt.Println(err.Error())
			return instancesPatchResponseOnError, fmt.Errorf("cannot terminate instance %s", instanceID)
		}

		instanceName, err := getInstanceName(instanceID)
		if err == nil {

			hasMapping, err := hasCustomHostnameMapping(instanceName)
			if err != nil {
				fmt.Println(err.Error())
				return instancesPatchResponseOnError, fmt.Errorf("error retrieving DNS mapping for instance %s", instanceID)
			}

			if hasMapping {
				err = upsertFakeDNSEntry(instanceID)
				if err != nil {
					fmt.Println(err.Error())
					return instancesPatchResponseOnError, fmt.Errorf("error upserting fake DNS entry for instance %s", instanceID)
				}

				err = deleteFakeDNSEntry(instanceID)
				if err != nil {
					fmt.Println(err.Error())
					return instancesPatchResponseOnError, fmt.Errorf("error deleting fake DNS entry for instance %s", instanceID)
				}
			}
		}

		err = waitUntilInstanceTerminated(instanceID)
		if err != nil {
			fmt.Println(err.Error())
			return instancesPatchResponseOnError, fmt.Errorf("instance %s still not terminated", instanceID)
		}
	}

	return instancesPatchResponse, nil
}
