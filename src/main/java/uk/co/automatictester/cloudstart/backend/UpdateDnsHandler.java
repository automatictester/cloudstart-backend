package uk.co.automatictester.cloudstart.backend;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.co.automatictester.cloudstart.backend.ddb.AwsDdbClient;
import uk.co.automatictester.cloudstart.backend.ddb.DdbManager;
import uk.co.automatictester.cloudstart.backend.ec2.AwsEc2Client;
import uk.co.automatictester.cloudstart.backend.ec2.Ec2Manager;
import uk.co.automatictester.cloudstart.backend.route53.AwsRoute53Client;
import uk.co.automatictester.cloudstart.backend.route53.Route53Manager;

public class UpdateDnsHandler {

    private static final Logger log = LogManager.getLogger(UpdateDnsHandler.class);
    private final Route53Manager route53Manager;
    private final Ec2Manager ec2Manager;
    private final DdbManager ddbManager;

    public UpdateDnsHandler() {
        this.route53Manager = new Route53Manager(new AwsRoute53Client());
        this.ec2Manager = new Ec2Manager(new AwsEc2Client());
        this.ddbManager = new DdbManager(new AwsDdbClient());
    }

    public UpdateDnsHandler(Route53Manager route53Manager, Ec2Manager ec2Manager, DdbManager ddbManager) {
        this.route53Manager = route53Manager;
        this.ec2Manager = ec2Manager;
        this.ddbManager = ddbManager;
    }

    public void handleRequest(UpdateDnsRequest request) {
        var instanceId = request.getInstanceId();
        var action = request.getAction();
        var hostedZoneId = ddbManager.getHostedZoneId();

        if (hostedZoneId.isPresent()) {
            switch (action.toLowerCase()) {
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
