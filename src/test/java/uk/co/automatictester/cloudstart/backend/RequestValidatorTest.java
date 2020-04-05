package uk.co.automatictester.cloudstart.backend;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.*;

public class RequestValidatorTest {

    @DataProvider(name = "invalidRequest")
    public Object[][] getInvalidRequest() {
        String instanceId = "i-04cdbf3adda6f58bf";
        return new Object[][]{
                {instanceId, "merge"}, // invalid action
                {instanceId, " "},     // blank action
                {instanceId, null},    // null action
                {" ", "upsert"},       // blank instanceId
                {null, "upsert"}       // null instanceId
        };
    }

    @Test(dataProvider = "invalidRequest")
    public void testIsValid(String instanceId, String action) {
        var request = new UpdateDnsRequest();
        request.setInstanceId(instanceId);
        request.setAction(action);

        var isValid = RequestValidator.isValid(request);

        assertFalse(isValid);
    }
}
