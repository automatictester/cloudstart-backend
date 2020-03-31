package uk.co.automatictester.cloudstart.backend.instances.patch.ddb;

import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;

public interface DdbClient {

    GetItemResult getItem(GetItemRequest getItemRequest);
}
