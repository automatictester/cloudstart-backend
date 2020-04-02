package uk.co.automatictester.cloudstart.backend.ec2;

import com.amazonaws.services.ec2.model.DescribeInstancesResult;

public interface Ec2Client {

    DescribeInstancesResult describeInstance(String instanceId);
}
