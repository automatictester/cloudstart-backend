package uk.co.automatictester.cloudstart.backend.instances.patch;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import lombok.experimental.UtilityClass;

import java.util.Map;
import java.util.Optional;

@UtilityClass
public class DataStore {

    private static final AmazonDynamoDB ddb = AmazonDynamoDBClientBuilder.defaultClient();

    public static String getHostedZoneId() {
        String hostedZoneId = "HOSTED_ZONE_ID";
        Optional<String> maybeHostedZoneId = getValue(hostedZoneId);
        return maybeHostedZoneId.orElseThrow(
                () -> new RuntimeException(hostedZoneId +
                        " item not found in DynamoDB table. Cannot update DNS entry.")
        );
    }

    public static Optional<String> getValue(String key) {
        if (key == null || key.trim().isEmpty()) {
            return Optional.empty();
        } else {
            GetItemResult getItemResult = getItem(key);
            Map<String, AttributeValue> item = getItemResult.getItem();
            if (item == null || item.get("Value") == null) {
                return Optional.empty();
            } else {
                return Optional.of(item.get("Value").getS());
            }
        }
    }

    private static GetItemResult getItem(String key) {
        String tableName = "CloudStartStore";
        GetItemRequest getItemRequest = new GetItemRequest();
        getItemRequest.setTableName(tableName);
        getItemRequest.addKeyEntry("Key", new AttributeValue(key));
        return ddb.getItem(getItemRequest);
    }
}
