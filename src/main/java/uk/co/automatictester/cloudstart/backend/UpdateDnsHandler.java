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
        this.ddbManager = new DdbManager(new AwsDdbClient(), getDdbTable());
    }

    public UpdateDnsHandler(Route53Manager route53Manager, Ec2Manager ec2Manager, DdbManager ddbManager) {
        this.route53Manager = route53Manager;
        this.ec2Manager = ec2Manager;
        this.ddbManager = ddbManager;
    }

    public void handleRequest(UpdateDnsRequest request) {
        var hostedZoneId = ddbManager.getHostedZoneId();

        if (hostedZoneId.isEmpty()) {
            log.error("Hosted zone ID not set");
            return;
        } else if (!RequestValidator.isValid(request)) {
            return;
        }

        var instanceId = request.getInstanceId();
        var action = request.getAction();

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
        }
    }

    private String getDdbTable() {
        var dynamodbTable = "DYNAMODB_TABLE";
        var table = System.getenv(dynamodbTable);
        if (table == null) {
            throw new IllegalStateException("Environment variable '" + dynamodbTable + "' is not set");
        }
        return table;
    }
}
