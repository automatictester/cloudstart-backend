package uk.co.automatictester.cloudstart.backend.instances.get;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import org.testng.annotations.Test;

public class SampleTest {

    @Test
    public void getInstancesTest() {
        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();
        boolean done = false;

        DescribeInstancesRequest request = new DescribeInstancesRequest();
        while (!done) {
            DescribeInstancesResult response = ec2.describeInstances(request);

            for (Reservation reservation : response.getReservations()) {
                for (Instance instance : reservation.getInstances()) {
                    String instanceInfo = String.format(
                            "Found instance with id %s, " +
                                    "type %s, " +
                                    "state %s",
                            instance.getInstanceId(),
                            instance.getInstanceType(),
                            instance.getState().getName()
                    );

                    if (instance.getTags() != null) {
                        for (Tag tag : instance.getTags()) {
                            System.out.println(String.format(
                                    "%s: %s",
                                    tag.getKey(),
                                    tag.getValue()
                            ));
                        }
                    }
                    System.out.println(instanceInfo);
                }
            }

            request.setNextToken(response.getNextToken());

            if (response.getNextToken() == null) {
                done = true;
            }
        }
    }
}
