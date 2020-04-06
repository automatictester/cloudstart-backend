package uk.co.automatictester.cloudstart.backend;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.co.automatictester.cloudstart.backend.ddb.AwsDdbClient;
import uk.co.automatictester.cloudstart.backend.ddb.DdbManager;
import uk.co.automatictester.cloudstart.backend.ec2.AwsEc2Client;
import uk.co.automatictester.cloudstart.backend.ec2.Ec2Manager;
import uk.co.automatictester.cloudstart.backend.route53.AwsRoute53Client;
import uk.co.automatictester.cloudstart.backend.route53.Route53Manager;

import java.util.Optional;

public class UpdateDnsHandler {

    private static final Logger log = LogManager.getLogger(UpdateDnsHandler.class);
    private final Route53Manager route53Manager;
    private final Ec2Manager ec2Manager;
    private final DdbManager ddbManager;

    public UpdateDnsHandler() {
        this.route53Manager = new Route53Manager(new AwsRoute53Client());
        this.ec2Manager = new Ec2Manager(new AwsEc2Client());
        this.ddbManager = new DdbManager(new AwsDdbClient(), getDdbTable());
    }

    public UpdateDnsHandler(Route53Manager route53Manager, Ec2Manager ec2Manager, DdbManager ddbManager) {
        this.route53Manager = route53Manager;
        this.ec2Manager = ec2Manager;
        this.ddbManager = ddbManager;
    }

    public void handleRequest(UpdateDnsRequest request) {
        var hostedZoneId = ddbManager.getHostedZoneId();

        if (!RequestValidator.isValid(request)) {
            throw new RuntimeException("Invalid request: " + request);
        }

        var instanceId = request.getInstanceId();
        var action = request.getAction().toLowerCase();

        if (action.equals("upsert")) {
            Optional<String> instanceName = ec2Manager.getInstanceName(instanceId);
            if (instanceName.isPresent()) {
                Optional<String> hostname = ddbManager.getValue(instanceName.get());
                if (hostname.isPresent()) {
                    Optional<String> ipAddress = ec2Manager.getPublicIpAddress(instanceId);
                    if (ipAddress.isPresent()) {
                        log.info("Updating instance DNS entry '{}' -> '{}'", hostname.get(), ipAddress.get());
                        route53Manager.upsertDnsEntry(hostedZoneId, ipAddress.get(), hostname.get());
                    } else {
                        log.info("Instance '{}' has name '{}' and DNS mapping '{}' but no public IP address",
                                instanceId, instanceName.get(), hostname.get());
                    }
                } else {
                    log.info("Instance '{}' has name '{}' but no DNS mapping", instanceId, instanceName.get());
                }
            } else {
                log.info("Instance '{}' has no name", instanceId);
            }
        } else if (action.equals("delete")) {
            Optional<String> instanceName = ec2Manager.getInstanceName(instanceId);
            if (instanceName.isPresent()) {
                Optional<String> hostname = ddbManager.getValue(instanceName.get());
                if (hostname.isPresent()) {
                    log.info("Deleting instance DNS entry '{}'", hostname.get());
                    route53Manager.deleteDnsEntry(hostedZoneId, hostname.get());
                } else {
                    log.info("Instance '{}' has name '{}' but no DNS mapping", instanceId, instanceName.get());
                }
            } else {
                log.info("Instance '{}' has no name", instanceId);
            }
        }
    }

    private String getDdbTable() {
        var ddbTable = "DYNAMODB_TABLE";
        var table = System.getenv(ddbTable);
        if (table == null) {
            throw new RuntimeException("Environment variable '" + ddbTable + "' is not set");
        }
        return table;
    }
}
