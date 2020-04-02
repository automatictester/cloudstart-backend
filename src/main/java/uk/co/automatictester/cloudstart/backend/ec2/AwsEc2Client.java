package uk.co.automatictester.cloudstart.backend.ec2;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;

public class AwsEc2Client implements Ec2Client {

    private static final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

    @Override
    public DescribeInstancesResult describeInstance(String instanceId) {
        DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest().withInstanceIds(instanceId);
        return ec2.describeInstances(describeInstancesRequest);
    }
}
