package uk.co.automatictester.cloudstart.backend.ec2;

import com.amazonaws.services.ec2.model.AmazonEC2Exception;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.co.automatictester.cloudstart.backend.route53.Route53Manager;

import java.util.Optional;

public class Ec2Manager {

    private static final Logger log = LogManager.getLogger(Route53Manager.class);
    private final Ec2Client client;

    public Ec2Manager(Ec2Client client) {
        this.client = client;
    }

    public Optional<String> getInstanceName(String instanceId) {
        Optional<Instance> instance = getInstance(instanceId);
        if (instance.isPresent()) {
            Optional<Tag> nameTag = instance
                    .get()
                    .getTags()
                    .stream()
                    .filter(t -> t.getKey().equals("Name") && t.getValue() != null && !t.getValue().isBlank())
                    .findAny();
            return nameTag.map(tag -> tag.getValue());
        } else {
            return Optional.empty();
        }
    }

    public Optional<String> getPublicIpAddress(String instanceId) {
        Optional<Instance> instance = getInstance(instanceId);
        if (instance.isPresent()) {
            var publicIpAddress = instance.get().getPublicIpAddress();
            return (publicIpAddress == null) ? Optional.empty() : Optional.of(publicIpAddress);
        } else {
            return Optional.empty();
        }
    }

    private Optional<Instance> getInstance(String instanceId) {
        try {
            return client.describeInstance(instanceId)
                    .getReservations()
                    .stream()
                    .flatMap(r -> r.getInstances().stream())
                    .findFirst();
        } catch (AmazonEC2Exception e) {
            log.error("Error retrieving instance details: {}", e.getErrorMessage());
            return Optional.empty();
        }
    }
}
