package uk.co.automatictester.cloudstart.backend.instances.patch;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.AmazonEC2Exception;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Tag;
import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

@UtilityClass
public class InstanceManager {

    private static final Logger log = LogManager.getLogger(InstanceManager.class);
    private static final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

    public static Optional<String> getInstanceName(String instanceId) {
        Optional<Instance> instance = getInstance(instanceId);
        if (instance.isPresent()) {
            Optional<Tag> nameTag = instance
                    .get()
                    .getTags()
                    .stream()
                    .filter(t -> t.getKey().equals("Name"))
                    .findAny();
            return nameTag.map(tag -> tag.getValue());
        } else {
            return Optional.empty();
        }
    }

    public static Optional<String> getPublicIpAddress(String instanceId) {
        Optional<Instance> instance = getInstance(instanceId);
        if (instance.isPresent()) {
            String publicIpAddress = instance.get().getPublicIpAddress();
            return (publicIpAddress == null) ? Optional.empty() : Optional.of(publicIpAddress);
        } else {
            return Optional.empty();
        }
    }

    private static Optional<Instance> getInstance(String instanceId) {
        DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest().withInstanceIds(instanceId);
        try {
            return ec2.describeInstances(describeInstancesRequest)
                    .getReservations()
                    .stream()
                    .flatMap(r -> r.getInstances().stream())
                    .findFirst();
        } catch (AmazonEC2Exception e) {
            log.info("{}", e.getErrorMessage());
            return Optional.empty();
        }
    }
}
