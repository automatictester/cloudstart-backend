package main

import (
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

var ddb *dynamodb.DynamoDB

func init() {
	sess := session.Must(session.NewSessionWithOptions(session.Options{
		SharedConfigState: session.SharedConfigEnable,
	}))
	ddb = dynamodb.New(sess)
}

func hasCustomHostnameMapping(key string) (bool, error) {
	item, err := getItem(key)
	if err != nil {
		return false, err
	}
	if item == "" {
		return false, nil
	}
	return true, nil
}

func getItem(key string) (string, error) {
	result, err := ddb.GetItem(&dynamodb.GetItemInput{
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
		return "", err
	}

	if item.Value == "" {
		fmt.Printf("No item found for key: %s", key)
	}

	return item.Value, nil
}
