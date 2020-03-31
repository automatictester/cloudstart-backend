package uk.co.automatictester.cloudstart.backend.instances.patch.ec2;

import com.amazonaws.services.ec2.model.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

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

    @Test
    public void testGetInstanceName() {
        var nameTag = new Tag("Name", "My Instance");
        var instance = new Instance().withTags(nameTag);
        var describeInstancesResult = new DescribeInstancesResult()
                .withReservations(new Reservation().withInstances(instance));
        when(client.describeInstance(instanceId)).thenReturn(describeInstancesResult);
        var actualName = ec2Manager.getInstanceName(instanceId);
        var expectedName = Optional.of(nameTag.getValue());
        assertThat(actualName, equalTo(expectedName));
    }

    @Test
    public void testGetInstanceNameWithNameTagButWhitespaceValue() {
        var nameTag = new Tag("Name", " ");
        var instance = new Instance().withTags(nameTag);
        var describeInstancesResult = new DescribeInstancesResult()
                .withReservations(new Reservation().withInstances(instance));
        when(client.describeInstance(instanceId)).thenReturn(describeInstancesResult);
        var actualName = ec2Manager.getInstanceName(instanceId);
        var expectedName = Optional.empty();
        assertThat(actualName, equalTo(expectedName));
    }

    @Test
    public void testGetInstanceNameWithNameTagButNullValue() {
        var nameTag = new Tag("Name", null);
        var instance = new Instance().withTags(nameTag);
        var describeInstancesResult = new DescribeInstancesResult()
                .withReservations(new Reservation().withInstances(instance));
        when(client.describeInstance(instanceId)).thenReturn(describeInstancesResult);
        var actualName = ec2Manager.getInstanceName(instanceId);
        var expectedName = Optional.empty();
        assertThat(actualName, equalTo(expectedName));
    }

    @Test
    public void testGetInstanceNameWithTagsButNotName() {
        var nameTag = new Tag("name", "My Instance"); // wrong case, should be 'Name'
        var instance = new Instance().withTags(nameTag);
        var describeInstancesResult = new DescribeInstancesResult()
                .withReservations(new Reservation().withInstances(instance));
        when(client.describeInstance(instanceId)).thenReturn(describeInstancesResult);
        var actualName = ec2Manager.getInstanceName(instanceId);
        var expectedName = Optional.empty();
        assertThat(actualName, equalTo(expectedName));
    }

    @Test
    public void testGetInstanceNameNoTags() {
        var instance = new Instance();
        var describeInstancesResult = new DescribeInstancesResult()
                .withReservations(new Reservation().withInstances(instance));
        when(client.describeInstance(instanceId)).thenReturn(describeInstancesResult);
        var actualName = ec2Manager.getInstanceName(instanceId);
        var expectedName = Optional.empty();
        assertThat(actualName, equalTo(expectedName));
    }

    @Test
    public void testGetInstanceNameNoInstance() {
        var describeInstancesResult = new DescribeInstancesResult()
                .withReservations(new Reservation());
        when(client.describeInstance(instanceId)).thenReturn(describeInstancesResult);
        var actualName = ec2Manager.getInstanceName(instanceId);
        var expectedName = Optional.empty();
        assertThat(actualName, equalTo(expectedName));
    }

    @Test
    public void testGetPublicIpAddress() {
        var expectedIpAddress = Optional.of("10.0.0.1");
        var instance = new Instance().withPublicIpAddress(expectedIpAddress.get());
        var describeInstancesResult = new DescribeInstancesResult()
                .withReservations(new Reservation().withInstances(instance));
        when(client.describeInstance(instanceId)).thenReturn(describeInstancesResult);
        var actualIpAddress = ec2Manager.getPublicIpAddress(instanceId);
        assertThat(actualIpAddress, equalTo(expectedIpAddress));
    }

    @Test
    public void testGetPublicIpAddressNoPublicIpAddress() {
        var expectedIpAddress = Optional.empty();
        var instance = new Instance();
        var describeInstancesResult = new DescribeInstancesResult()
                .withReservations(new Reservation().withInstances(instance));
        when(client.describeInstance(instanceId)).thenReturn(describeInstancesResult);
        var actualIpAddress = ec2Manager.getPublicIpAddress(instanceId);
        assertThat(actualIpAddress, equalTo(expectedIpAddress));
    }

    @Test
    public void testGetPublicIpAddressNoInstance() {
        var expectedIpAddress = Optional.empty();
        var describeInstancesResult = new DescribeInstancesResult()
                .withReservations(new Reservation());
        when(client.describeInstance(instanceId)).thenReturn(describeInstancesResult);
        var actualIpAddress = ec2Manager.getPublicIpAddress(instanceId);
        assertThat(actualIpAddress, equalTo(expectedIpAddress));
    }

    @Test
    public void testGetPublicIpAddressInvalidInstanceId() {
        var expectedIpAddress = Optional.empty();
        when(client.describeInstance(instanceId)).thenThrow(new AmazonEC2Exception("Invalid id: \"invalidInstanceId\""));
        var actualIpAddress = ec2Manager.getPublicIpAddress(instanceId);
        assertThat(actualIpAddress, equalTo(expectedIpAddress));
    }
}
