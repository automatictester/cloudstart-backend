package uk.co.automatictester.cloudstart.backend.instances.patch;

import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.AmazonRoute53ClientBuilder;
import com.amazonaws.services.route53.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

import static uk.co.automatictester.cloudstart.backend.instances.patch.DataStore.getValueFromDataStore;
import static uk.co.automatictester.cloudstart.backend.instances.patch.InstanceManager.getInstanceName;
import static uk.co.automatictester.cloudstart.backend.instances.patch.InstanceManager.getPublicIpAddress;

public class DnsManager {

    private static final Logger log = LogManager.getLogger(DnsManager.class);
    private static final AmazonRoute53 route53 = AmazonRoute53ClientBuilder.defaultClient();
    private static final String HOSTED_ZONE_ID = "HOSTED_ZONE_ID";

    private DnsManager() {
    }

    public static void updateDnsEntry(String instanceId) {
        log.info("Updating instance {} DNS entry", instanceId);
        String publicIpAddress = getPublicIpAddress(instanceId);
        String instanceName = getInstanceName(instanceId);
        String instanceDnsName = getValueFromDataStore(instanceName).get();

        ResourceRecordSet resourceRecordSet = new ResourceRecordSet()
                .withType(RRType.A)
                .withName(instanceDnsName)
                .withTTL(60L)
                .withResourceRecords(new ResourceRecord().withValue(publicIpAddress));

        Change change = new Change()
                .withAction(ChangeAction.UPSERT)
                .withResourceRecordSet(resourceRecordSet);

        ChangeResourceRecordSetsRequest changeResourceRecordSetsRequest = new ChangeResourceRecordSetsRequest()
                .withHostedZoneId(getHostedZoneId())
                .withChangeBatch(new ChangeBatch().withChanges(change));

        route53.changeResourceRecordSets(changeResourceRecordSetsRequest);
    }

    private static String getHostedZoneId() {
        Optional<String> maybeHostedZoneId = getValueFromDataStore(HOSTED_ZONE_ID);
        return maybeHostedZoneId.orElseThrow(
                () -> new RuntimeException(HOSTED_ZONE_ID + " item not found in DynamoDB table. Cannot update DNS entry for this EC2 instance.")
        );
    }
}
