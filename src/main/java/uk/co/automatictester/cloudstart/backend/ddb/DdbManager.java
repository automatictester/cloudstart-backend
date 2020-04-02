package uk.co.automatictester.cloudstart.backend.ddb;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;

import java.util.Map;
import java.util.Optional;

public class DdbManager {

    private final DdbClient client;

    public DdbManager(DdbClient client) {
        this.client = client;
    }

    public Optional<String> getHostedZoneId() {
        return getValue("HOSTED_ZONE_ID");
    }

    public Optional<String> getValue(String key) {
        if (key == null || key.isBlank()) {
            return Optional.empty();
        } else {
            GetItemRequest getItemRequest = getItemRequest(key);
            GetItemResult getItemResult = client.getItem(getItemRequest);
            Map<String, AttributeValue> item = getItemResult.getItem();
            if (item == null || item.get("Value") == null || item.get("Value").getS() == null
                    || item.get("Value").getS().isBlank()) {
                return Optional.empty();
            } else {
                return Optional.of(item.get("Value").getS());
            }
        }
    }

    private GetItemRequest getItemRequest(String key) {
        return new GetItemRequest()
                .withTableName("CloudStartStore")
                .withKey(Map.of("Key", new AttributeValue(key)));
    }
}
