package uk.co.automatictester.cloudstart.backend.instances.patch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.co.automatictester.cloudstart.backend.instances.patch.ddb.AwsDdbClient;
import uk.co.automatictester.cloudstart.backend.instances.patch.ddb.DdbManager;
import uk.co.automatictester.cloudstart.backend.instances.patch.ec2.AwsEc2Client;
import uk.co.automatictester.cloudstart.backend.instances.patch.ec2.Ec2Manager;
import uk.co.automatictester.cloudstart.backend.instances.patch.route53.AwsRoute53Client;
import uk.co.automatictester.cloudstart.backend.instances.patch.route53.Route53Manager;

public class UpdateDnsHandler {

    private static final Logger log = LogManager.getLogger(UpdateDnsHandler.class);
    private static final Route53Manager route53Manager = new Route53Manager(new AwsRoute53Client());
    private static final Ec2Manager ec2Manager = new Ec2Manager(new AwsEc2Client());
    private static final DdbManager ddbManager = new uk.co.automatictester.cloudstart.backend.instances.patch.ddb.DdbManager(new AwsDdbClient());

    public void handleRequest(UpdateDnsRequest request) {
        var instanceId = request.getInstanceId();
        var action = request.getAction();
        var hostedZoneId = ddbManager.getHostedZoneId();

        if (hostedZoneId.isPresent()) {
            switch (action) {
                case ("upsert"):
                    ec2Manager.getInstanceName(instanceId).ifPresent((instanceName) -> {
                        ddbManager.getValue(instanceName).ifPresent((hostname) -> {
                            ec2Manager.getPublicIpAddress(instanceId).ifPresent((ipAddress) -> {
                                route53Manager.upsertDnsEntry(hostedZoneId.get(), ipAddress, hostname);
                            });
                        });
                    });
                    break;
                case ("delete"):
                    ec2Manager.getInstanceName(instanceId).ifPresent((instanceName) -> {
                        ddbManager.getValue(instanceName).ifPresent(hostname -> {
                            route53Manager.deleteDnsEntry(hostedZoneId.get(), hostname);
                        });
                    });
                    break;
                default:
                    log.info("Unknown action: {}", action);
            }
        } else {
            log.info("Hosted zone ID not defined");
        }
    }
}
