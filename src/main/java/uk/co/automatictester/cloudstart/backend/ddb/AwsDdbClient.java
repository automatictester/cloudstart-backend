package uk.co.automatictester.cloudstart.backend.ddb;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;

public class AwsDdbClient implements DdbClient {

    private static final AmazonDynamoDB ddb = AmazonDynamoDBClientBuilder.defaultClient();

    @Override
    public GetItemResult getItem(GetItemRequest getItemRequest) {
        return ddb.getItem(getItemRequest);
    }
}
