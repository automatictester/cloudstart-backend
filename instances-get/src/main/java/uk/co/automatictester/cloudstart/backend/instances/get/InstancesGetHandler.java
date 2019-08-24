package uk.co.automatictester.cloudstart.backend.instances.get;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class InstancesGetHandler implements RequestHandler<InstancesGetRequest, InstancesGetResponse> {

    private static final Logger log = LogManager.getLogger(InstancesGetHandler.class);

    @Override
    public InstancesGetResponse handleRequest(InstancesGetRequest request, Context context) {
        List<Instance> instances = new ArrayList<>();
        AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();
        DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest().withMaxResults(1_000);
        DescribeInstancesResult describeInstancesResult = ec2.describeInstances(describeInstancesRequest);

        for (Reservation reservation : describeInstancesResult.getReservations()) {
            for (com.amazonaws.services.ec2.model.Instance ec2Instance : reservation.getInstances()) {
                Instance instance = InstanceConverter.from(ec2Instance);
                instances.add(instance);
                log.info(instance.toString());
            }
        }

        return new InstancesGetResponse(instances);
    }
}
