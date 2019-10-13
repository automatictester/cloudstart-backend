package main

import (
	"errors"
	"fmt"
	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/aws/aws-sdk-go/service/dynamodb"
	"github.com/aws/aws-sdk-go/service/dynamodb/dynamodbattribute"
)

type cloudStartStoreItem struct {
	Key   string
	Value string
}

const dynamoDBTable = "CloudStartStore"

var DDB *dynamodb.DynamoDB

func init() {
	sess := session.Must(session.NewSessionWithOptions(session.Options{
		SharedConfigState: session.SharedConfigEnable,
	}))
	DDB = dynamodb.New(sess)
}

func hasCustomHostnameMapping(key string) bool {
	item, err := getItem(key)
	if err != nil {
		fmt.Println(err.Error())
	}
	if item == "" {
		return false
	}
	return true
}

func getItem(key string) (string, error) {
	result, err := DDB.GetItem(&dynamodb.GetItemInput{
		TableName: aws.String(dynamoDBTable),
		Key: map[string]*dynamodb.AttributeValue{
			"Key": {
				S: aws.String(key),
			},
		},
	})

	if err != nil {
		return "", err
	}

	item := cloudStartStoreItem{}
	if err = dynamodbattribute.UnmarshalMap(result.Item, &item); err != nil {
		fmt.Println(err.Error())
		return "", err
	}

	if item.Value == "" {
		errorMessage := fmt.Sprintf("No item found for key: %s", key)
		return "", errors.New(errorMessage)
	}

	return item.Value, nil
}
