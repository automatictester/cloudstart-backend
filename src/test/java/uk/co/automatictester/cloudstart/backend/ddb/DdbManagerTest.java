package uk.co.automatictester.cloudstart.backend.ddb;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class DdbManagerTest {

    private static DdbClient client;
    private static DdbManager ddbManager;

    @BeforeClass
    public void setup() {
        client = mock(DdbClient.class);
        ddbManager = new DdbManager(client, "CloudStartStore");
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
        assertThat(actualHostname, equalTo(expectedHostedZoneId));
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testGetHostedZoneIdException() {
        var hostedZoneId = getItemRequest("HOSTED_ZONE_ID");
        var getItemResult = new GetItemResult();
        when(client.getItem(hostedZoneId)).thenReturn(getItemResult);
        ddbManager.getHostedZoneId();
    }

    @DataProvider(name = "valueTestingInput")
    public Object[][] getValueTestingInput() {
        return new Object[][]{
                {"host.example.com", Optional.of("host.example.com")},
                {null, Optional.empty()},
                {" ", Optional.empty()}
        };
    }

    @Test(dataProvider = "valueTestingInput")
    public void testGetValue(String value, Optional<String> expectedResult) {
        var key = "My Instance Name";
        var itemRequest = getItemRequest(key);
        var getItemResult = new GetItemResult().withItem(Map.of("Value", new AttributeValue().withS(value)));
        when(client.getItem(itemRequest)).thenReturn(getItemResult);
        var actualResult = ddbManager.getValue(key);
        assertThat(actualResult, equalTo(expectedResult));
    }

    @Test
    public void testGetValueButNoSuchItem() {
        var key = "My Instance Name";
        var itemRequest = getItemRequest(key);
        var getItemResult = new GetItemResult();
        when(client.getItem(itemRequest)).thenReturn(getItemResult);
        var actualResult = ddbManager.getValue(key);
        assertThat(actualResult, equalTo(Optional.empty()));
    }

    @DataProvider(name = "keyTestingInput")
    public Object[][] getKeyTestingInput() {
        return new Object[][]{
                {" "},
                {null}
        };
    }

    @Test(dataProvider = "keyTestingInput")
    public void testGetValueButInvalidKey(String key) {
        var actualResult = ddbManager.getValue(key);
        assertThat(actualResult, equalTo(Optional.empty()));
    }

    private GetItemRequest getItemRequest(String key) {
        return new GetItemRequest()
                .withTableName("CloudStartStore")
                .withKey(Map.of("Key", new AttributeValue(key)));
    }
}
