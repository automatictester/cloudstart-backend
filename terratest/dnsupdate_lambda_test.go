package terratest

import (
	"encoding/json"
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

	logger.Log(t, "Invoking Lambda...")
	_, err := aws.InvokeFunctionE(t, region, function, UpdateDnsRequest{InstanceId: "i-09dc014d370bc3cc6", Action: "upsert"})
	logger.Log(t, "Invoking Lambda done!")

	functionError, _ := err.(*aws.FunctionError)
	errorPayload := string(functionError.Payload)
	var result map[string]interface{}
	_ = json.Unmarshal([]byte(errorPayload), &result)
	errorMessage := result["errorMessage"]
	assert.Equal(t, "Hosted zone ID not set", errorMessage)
}

type UpdateDnsRequest struct {
	InstanceId string `json:"instanceId"`
	Action     string `json:"action"`
}
