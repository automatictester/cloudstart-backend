package uk.co.automatictester.cloudstart.backend.instances.patch;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;

public class InstanceManager {

    private static final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

    private InstanceManager() {
    }

    public static boolean hasNameTag(String instanceId) {
        return getDescribeInstanceResult(instanceId)
                .getReservations()
                .stream()
                .flatMap(r -> r.getInstances().stream())
                .findAny()
                .get()
                .getTags()
                .stream()
                .anyMatch(t -> t.getKey().equals("Name"));
    }

    public static String getInstanceName(String instanceId) {
        String nameTag = getDescribeInstanceResult(instanceId)
                .getReservations()
                .stream()
                .flatMap(r -> r.getInstances().stream())
                .findAny()
                .get()
                .getTags()
                .stream()
                .filter(t -> t.getKey().equals("Name"))
                .findAny()
                .get()
                .getValue();
        return nameTag;
    }

    public static String getPublicIpAddress(String instanceId) {
        String publicIpAddress = getDescribeInstanceResult(instanceId)
                .getReservations()
                .stream()
                .flatMap(r -> r.getInstances().stream())
                .findFirst()
                .get()
                .getPublicIpAddress();
        return publicIpAddress;
    }

    private static DescribeInstancesResult getDescribeInstanceResult(String instanceId) {
        DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest().withInstanceIds(instanceId);
        return ec2.describeInstances(describeInstancesRequest);
    }
}
