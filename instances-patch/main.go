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

		name, err := getInstanceName(instanceID)
		if err == nil {
			hasMapping, err := hasCustomHostnameMapping(name)
			if err != nil {
				fmt.Println(err.Error())
				return instancesPatchResponseOnError, fmt.Errorf("instance %s started but cannot retrieve DNS mapping", instanceID)
			}
			if hasMapping {
				err := updateDNSEntry(instanceID)
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

		err = waitUntilInstanceTerminated(instanceID)
		if err != nil {
			fmt.Println(err.Error())
			return instancesPatchResponseOnError, fmt.Errorf("instance %s still not terminated", instanceID)
		}
	}

	return instancesPatchResponse, nil
}
