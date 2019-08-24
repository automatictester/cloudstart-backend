package uk.co.automatictester.cloudstart.backend.instances.get;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Reservation;
import org.testng.annotations.Test;

public class SampleTest {

    @Test
    public void getInstancesTest() {
        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();
        DescribeInstancesRequest request = new DescribeInstancesRequest().withMaxResults(1_000);
        DescribeInstancesResult response = ec2.describeInstances(request);

        for (Reservation reservation : response.getReservations()) {
            for (com.amazonaws.services.ec2.model.Instance instance : reservation.getInstances()) {
                Instance ec2Instance = InstanceConverter.from(instance);
                System.out.println(ec2Instance.toString());
            }
        }
    }
}
