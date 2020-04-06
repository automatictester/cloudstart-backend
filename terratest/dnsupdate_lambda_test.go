package terratest

import (
	"encoding/json"
	"fmt"
	awsCore "github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/aws/aws-sdk-go/service/dynamodb"
	"github.com/aws/aws-sdk-go/service/dynamodb/dynamodbattribute"
	"github.com/gruntwork-io/terratest/modules/aws"
	"github.com/gruntwork-io/terratest/modules/logger"
	"github.com/gruntwork-io/terratest/modules/shell"
	"github.com/gruntwork-io/terratest/modules/terraform"
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestUpdateDnsLambda(t *testing.T) {

	shell.RunCommand(t, shell.Command{
		Command:    "./mvnw",
		Args:       []string{"clean", "package"},
		WorkingDir: "..",
	})

	shell.RunCommand(t, shell.Command{
		Command:    "rm",
		Args:       []string{"-rf", "tf/.terraform"},
		WorkingDir: "..",
	})

	terraformOptions := &terraform.Options{
		TerraformDir: "../tf",
		BackendConfig: map[string]interface{}{
			"key": "cloudstart-backend-test.tfstate",
		},
		Vars: map[string]interface{}{
			"env_suffix": "Test",
		},
	}

	defer terraform.Destroy(t, terraformOptions)
	terraform.InitAndApply(t, terraformOptions)
	region := terraform.Output(t, terraformOptions, "region")
	function := terraform.Output(t, terraformOptions, "function")
	table := terraform.Output(t, terraformOptions, "table")

	putHostedZoneId(table)

	instanceId := "i-09dc014d370bc3cc6"
	logger.Log(t, "Invoking Lambda...")
	_, err := aws.InvokeFunctionE(t, region, function, UpdateDnsRequest{InstanceId: instanceId, Action: "upsert"})
	logger.Log(t, "Invoking Lambda done!")

	functionError, _ := err.(*aws.FunctionError)
	errorPayload := string(functionError.Payload)
	var result map[string]interface{}
	_ = json.Unmarshal([]byte(errorPayload), &result)
	actualErrorMessage := result["errorMessage"]
	expectedErrorMessage := fmt.Sprintf("Instance with instanceId '%s' does not exist", instanceId)
	assert.Equal(t, expectedErrorMessage, actualErrorMessage)
}

func putHostedZoneId(table string) {
	sess := session.Must(session.NewSessionWithOptions(session.Options{
		SharedConfigState: session.SharedConfigEnable,
	}))

	svc := dynamodb.New(sess)

	item := DdbItem{
		Key:   "HOSTED_ZONE_ID",
		Value: "A00AA0AAAAA00A",
	}
	av, _ := dynamodbattribute.MarshalMap(item)

	input := &dynamodb.PutItemInput{
		Item:      av,
		TableName: awsCore.String(table),
	}

	_, _ = svc.PutItem(input)
}

type DdbItem struct {
	Key   string
	Value string
}

type UpdateDnsRequest struct {
	InstanceId string `json:"instanceId"`
	Action     string `json:"action"`
}
