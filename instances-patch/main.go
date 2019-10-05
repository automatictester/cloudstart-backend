package main

import "github.com/aws/aws-lambda-go/lambda"

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

func handleRequest(request instancesPatchResponse) (instancesPatchResponse, error) {
	instanceID := request.InstanceID
	action := request.Action

	instancesPatchResponse := instancesPatchResponse{
		InstanceID: instanceID,
		Action:     action,
		Status:     "",
		Message:    "",
	}

	switch action {
	case "start":
		startInstance(instanceID)
		waitUntilInstanceRunning(instanceID)
		name, err := getInstanceName(instanceID)
		if err == nil {
			if hasCustomHostnameMapping(name) {
				updateDNSEntry(instanceID)
			}
		}
	case "stop":
		stopInstance(instanceID)
		waitUntilInstanceStopped(instanceID)
	case "reboot":
		rebootInstance(instanceID)
	case "terminate":
		terminateInstance(instanceID)
		waitUntilInstanceTerminated(instanceID)
	}

	return instancesPatchResponse, nil
}
