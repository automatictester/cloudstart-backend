package uk.co.automatictester.cloudstart.backend.instances.patch;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.waiters.WaiterParameters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InstancesPatchHandler implements RequestHandler<InstancesPatchRequest, InstancesPatchResponse> {

    private static final Logger log = LogManager.getLogger(InstancesPatchHandler.class);

    @Override
    public InstancesPatchResponse handleRequest(InstancesPatchRequest request, Context context) {
        AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();
        switch (request.getAction()) {
            case ("start"):
                log.info("Starting instance {}", request.getInstanceId());
                StartInstancesRequest startInstancesRequest = new StartInstancesRequest()
                        .withInstanceIds(request.getInstanceId());
                ec2.startInstances(startInstancesRequest);

                if (InstanceManager.hasName(request.getInstanceId())) {
                    String instanceName = InstanceManager.getName(request.getInstanceId());
                    if (DataStore.hasKey(instanceName)) {

                        DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest().withInstanceIds(request.getInstanceId());
                        ec2.waiters().instanceRunning().run(
                                new WaiterParameters().withRequest(describeInstancesRequest)
                        );

                        log.info("Updating instance {} DNS entry", request.getInstanceId());
                        DnsManager.updateDnsEntry(request.getInstanceId());
                    }
                }
                break;
            case ("stop"):
                log.info("Stopping instance {}", request.getInstanceId());
                StopInstancesRequest stopInstancesRequest = new StopInstancesRequest()
                        .withInstanceIds(request.getInstanceId());
                ec2.stopInstances(stopInstancesRequest);
                break;
            case ("reboot"):
                log.info("Rebooting instance {}", request.getInstanceId());
                RebootInstancesRequest rebootInstancesRequest = new RebootInstancesRequest()
                        .withInstanceIds(request.getInstanceId());
                ec2.rebootInstances(rebootInstancesRequest);
                break;
            case ("terminate"):
                log.info("Terminating instance {}", request.getInstanceId());
                TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest()
                        .withInstanceIds(request.getInstanceId());
                ec2.terminateInstances(terminateInstancesRequest);
                break;
        }

        InstancesPatchResponse response = new InstancesPatchResponse();
        response.setAction(request.getAction());
        response.setInstanceId(request.getInstanceId());
        response.setStatus("OK");
        response.setMessage("");
        return response;
    }
}
