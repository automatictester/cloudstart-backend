package uk.co.automatictester.cloudstart.backend.instances.patch;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.waiters.WaiterParameters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InstanceManager {

    private static final Logger log = LogManager.getLogger(InstanceManager.class);
    private static final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

    private InstanceManager() {
    }

    public static void startInstance(String instanceId) {
        log.info("Starting instance {}", instanceId);
        StartInstancesRequest startInstancesRequest = new StartInstancesRequest()
                .withInstanceIds(instanceId);
        ec2.startInstances(startInstancesRequest);
    }

    public static  void stopInstance(String instanceId) {
        log.info("Stopping instance {}", instanceId);
        StopInstancesRequest stopInstancesRequest = new StopInstancesRequest()
                .withInstanceIds(instanceId);
        ec2.stopInstances(stopInstancesRequest);
    }

    public static  void rebootInstance(String instanceId) {
        log.info("Rebooting instance {}", instanceId);
        RebootInstancesRequest rebootInstancesRequest = new RebootInstancesRequest()
                .withInstanceIds(instanceId);
        ec2.rebootInstances(rebootInstancesRequest);
    }

    public static  void terminateInstance(String instanceId) {
        log.info("Terminating instance {}", instanceId);
        TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest()
                .withInstanceIds(instanceId);
        ec2.terminateInstances(terminateInstancesRequest);
    }

    public static  void waitForInstanceRunning(String instanceId) {
        DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest().withInstanceIds(instanceId);
        ec2.waiters().instanceRunning().run(
                new WaiterParameters().withRequest(describeInstancesRequest)
        );
    }

    public static  void waitForInstanceStopped(String instanceId) {
        DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest().withInstanceIds(instanceId);
        ec2.waiters().instanceStopped().run(
                new WaiterParameters().withRequest(describeInstancesRequest)
        );
    }

    public static  void waitForInstanceTerminated(String instanceId) {
        DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest().withInstanceIds(instanceId);
        ec2.waiters().instanceTerminated().run(
                new WaiterParameters().withRequest(describeInstancesRequest)
        );
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
