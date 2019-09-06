package uk.co.automatictester.cloudstart.backend.instances.patch;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;

public class DataStore {

    private static final AmazonDynamoDB DDB = AmazonDynamoDBClientBuilder.defaultClient();
    private static final String TABLE_NAME = "CloudStartStore";

    private DataStore() {
    }

    public static String getValue(String key) {
        GetItemRequest getItemRequest = new GetItemRequest();
        getItemRequest.setTableName(TABLE_NAME);
        getItemRequest.addKeyEntry("Key", new AttributeValue(key));
        GetItemResult getItemResult = DDB.getItem(getItemRequest);
        return getItemResult.getItem().get("Value").getS();
    }
}
