package uk.co.automatictester.cloudstart.backend.ec2;

import com.amazonaws.services.ec2.model.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class Ec2ManagerTest {

    private static String instanceId = "i-04cdbf3adda6f58bf";
    private static Ec2Client client;
    private static Ec2Manager ec2Manager;

    @BeforeClass
    public void setup() {
        client = mock(Ec2Client.class);
        ec2Manager = new Ec2Manager(client);
    }

    @BeforeMethod
    public void setupMethod() {
        reset(client);
    }

    @DataProvider(name = "instanceNameTestingInput")
    public Object[][] getInstanceNameTestingInput() {
        return new Object[][]{
                {new Tag("Name", "My Instance"), Optional.of("My Instance")},
                {new Tag("name", "My Instance"), Optional.empty()},
                {new Tag("Name", " "), Optional.empty()},
                {new Tag("Name", null), Optional.empty()}
        };
    }

    @Test(dataProvider = "instanceNameTestingInput")
    public void testGetInstanceName(Tag nameTag, Optional<String> expectedName) {
        var instance = new Instance().withTags(nameTag);
        var describeInstancesResult = new DescribeInstancesResult()
                .withReservations(new Reservation().withInstances(instance));
        when(client.describeInstance(instanceId)).thenReturn(describeInstancesResult);
        var actualName = ec2Manager.getInstanceName(instanceId);
        assertThat(actualName, equalTo(expectedName));
    }

    @DataProvider(name = "instanceTestingInput")
    public Object[][] getInstanceTestingInput() {
        return new Object[][]{
                {new Reservation().withInstances(new Instance())},
                {new Reservation()}
        };
    }

    @Test(dataProvider = "instanceTestingInput")
    public void testGetInstanceNameNoInstanceWithSuchName(Reservation reservation) {
        var describeInstancesResult = new DescribeInstancesResult().withReservations(reservation);
        when(client.describeInstance(instanceId)).thenReturn(describeInstancesResult);
        var actualName = ec2Manager.getInstanceName(instanceId);
        var expectedName = Optional.empty();
        assertThat(actualName, equalTo(expectedName));
    }

    @DataProvider(name = "publicIpAddressTestingInput")
    public Object[][] getPublicIpAddressTestingInput() {
        return new Object[][]{
                {new Reservation().withInstances(new Instance().withPublicIpAddress("35.176.100.147")), Optional.of("35.176.100.147")},
                {new Reservation().withInstances(new Instance()), Optional.empty()},
                {new Reservation(), Optional.empty()}
        };
    }

    @Test(dataProvider = "publicIpAddressTestingInput")
    public void testGetPublicIpAddress(Reservation reservation, Optional<String> ipAddress) {
        var describeInstancesResult = new DescribeInstancesResult()
                .withReservations(reservation);
        when(client.describeInstance(instanceId)).thenReturn(describeInstancesResult);
        var actualIpAddress = ec2Manager.getPublicIpAddress(instanceId);
        assertThat(actualIpAddress, equalTo(ipAddress));
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testGetPublicIpAddressInvalidInstanceId() {
        var invalidInstanceId = "invalidInstanceId";
        when(client.describeInstance(invalidInstanceId)).thenThrow(AmazonEC2Exception.class);
        ec2Manager.getPublicIpAddress(invalidInstanceId);
    }
}
