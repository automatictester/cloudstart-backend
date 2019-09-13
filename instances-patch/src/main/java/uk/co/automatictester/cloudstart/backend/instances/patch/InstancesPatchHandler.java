package uk.co.automatictester.cloudstart.backend.instances.patch;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.waiters.WaiterParameters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static uk.co.automatictester.cloudstart.backend.instances.patch.DataStore.hasCustomHostnameMapping;
import static uk.co.automatictester.cloudstart.backend.instances.patch.DnsManager.updateDnsEntry;
import static uk.co.automatictester.cloudstart.backend.instances.patch.InstanceManager.getInstanceName;
import static uk.co.automatictester.cloudstart.backend.instances.patch.InstanceManager.hasNameTag;

public class InstancesPatchHandler implements RequestHandler<InstancesPatchRequest, InstancesPatchResponse> {

    private static final Logger log = LogManager.getLogger(InstancesPatchHandler.class);
    private AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

    @Override
    public InstancesPatchResponse handleRequest(InstancesPatchRequest request, Context context) {
        String instanceId = request.getInstanceId();
        String action = request.getAction();

        switch (action) {
            case ("start"):
                startInstance(instanceId);
                if (hasNameTag(instanceId)) {
                    String instanceName = getInstanceName(instanceId);
                    if (hasCustomHostnameMapping(instanceName)) {
                        waitForInstanceRunning(instanceId);
                        updateDns(instanceId);
                    }
                }
                break;
            case ("stop"):
                stopInstance(instanceId);
                break;
            case ("reboot"):
                rebootInstance(instanceId);
                break;
            case ("terminate"):
                terminateInstance(instanceId);
                break;
        }

        InstancesPatchResponse response = new InstancesPatchResponse();
        response.setAction(request.getAction());
        response.setInstanceId(instanceId);
        response.setStatus("OK");
        response.setMessage("");
        return response;
    }

    private void startInstance(String instanceId) {
        log.info("Starting instance {}", instanceId);
        StartInstancesRequest startInstancesRequest = new StartInstancesRequest()
                .withInstanceIds(instanceId);
        ec2.startInstances(startInstancesRequest);
    }

    private void stopInstance(String instanceId) {
        log.info("Stopping instance {}", instanceId);
        StopInstancesRequest stopInstancesRequest = new StopInstancesRequest()
                .withInstanceIds(instanceId);
        ec2.stopInstances(stopInstancesRequest);
    }

    private void rebootInstance(String instanceId) {
        log.info("Rebooting instance {}", instanceId);
        RebootInstancesRequest rebootInstancesRequest = new RebootInstancesRequest()
                .withInstanceIds(instanceId);
        ec2.rebootInstances(rebootInstancesRequest);
    }

    private void terminateInstance(String instanceId) {
        log.info("Terminating instance {}", instanceId);
        TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest()
                .withInstanceIds(instanceId);
        ec2.terminateInstances(terminateInstancesRequest);
    }

    private void waitForInstanceRunning(String instanceId) {
        DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest().withInstanceIds(instanceId);
        ec2.waiters().instanceRunning().run(
                new WaiterParameters().withRequest(describeInstancesRequest)
        );
    }

    private void updateDns(String instanceId) {
        log.info("Updating instance {} DNS entry", instanceId);
        updateDnsEntry(instanceId);
    }
}
