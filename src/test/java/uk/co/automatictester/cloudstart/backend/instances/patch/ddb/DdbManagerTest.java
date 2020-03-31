package uk.co.automatictester.cloudstart.backend.instances.patch.ddb;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

public class DdbManagerTest {

    private static String instanceName = "My Instance Name";
    private static DdbClient client;
    private static DdbManager ddbManager;

    @BeforeClass
    public void setup() {
        client = mock(DdbClient.class);
        ddbManager = new DdbManager(client);
    }

    @BeforeMethod
    public void setupMethod() {
        reset(client);
    }

    @Test
    public void testGetHostedZoneId() {
        var hostedZoneId = getItemRequest("HOSTED_ZONE_ID");
        var expectedHostedZoneId = "A00AA0AAAAA00A";
        var getItemResult = new GetItemResult().withItem(Map.of("Value", new AttributeValue().withS(expectedHostedZoneId)));
        when(client.getItem(hostedZoneId)).thenReturn(getItemResult);
        var actualHostname = ddbManager.getHostedZoneId();
        assertThat(actualHostname, equalTo(Optional.of(expectedHostedZoneId)));
    }

    @Test
    public void testGetValue() {
        var itemRequest = getItemRequest(instanceName);
        var expectedHostname = "host.example.com";
        var getItemResult = new GetItemResult().withItem(Map.of("Value", new AttributeValue().withS(expectedHostname)));
        when(client.getItem(itemRequest)).thenReturn(getItemResult);
        var actualHostname = ddbManager.getValue(instanceName);
        assertThat(actualHostname, equalTo(Optional.of(expectedHostname)));
    }

    @Test
    public void testGetValueButNullValue() {
        var itemRequest = getItemRequest(instanceName);
        String expectedHostname = null;
        var getItemResult = new GetItemResult().withItem(Map.of("Value", new AttributeValue().withS(expectedHostname)));
        when(client.getItem(itemRequest)).thenReturn(getItemResult);
        var actualHostname = ddbManager.getValue(instanceName);
        assertThat(actualHostname, equalTo(Optional.empty()));
    }

    @Test
    public void testGetValueButBlankValue() {
        var itemRequest = getItemRequest(instanceName);
        String expectedHostname = " ";
        var getItemResult = new GetItemResult().withItem(Map.of("Value", new AttributeValue().withS(expectedHostname)));
        when(client.getItem(itemRequest)).thenReturn(getItemResult);
        var actualHostname = ddbManager.getValue(instanceName);
        assertThat(actualHostname, equalTo(Optional.empty()));
    }

    @Test
    public void testGetValueButNoSuchItem() {
        var itemRequest = getItemRequest(instanceName);
        var getItemResult = new GetItemResult();
        when(client.getItem(itemRequest)).thenReturn(getItemResult);
        var actualHostname = ddbManager.getValue(instanceName);
        assertThat(actualHostname, equalTo(Optional.empty()));
    }

    @Test
    public void testGetValueButBlankKey() {
        var itemRequest = getItemRequest(instanceName);
        var instanceName = " ";
        var getItemResult = new GetItemResult();
        when(client.getItem(itemRequest)).thenReturn(getItemResult);
        var actualHostname = ddbManager.getValue(instanceName);
        assertThat(actualHostname, equalTo(Optional.empty()));
    }

    @Test
    public void testGetValueButNullKey() {
        var itemRequest = getItemRequest(instanceName);
        String instanceName = null;
        var getItemResult = new GetItemResult();
        when(client.getItem(itemRequest)).thenReturn(getItemResult);
        var actualHostname = ddbManager.getValue(instanceName);
        assertThat(actualHostname, equalTo(Optional.empty()));
    }

    private GetItemRequest getItemRequest(String key) {
        return new GetItemRequest()
                .withTableName("CloudStartStore")
                .withKey(Map.of("Key", new AttributeValue(key)));
    }
}
