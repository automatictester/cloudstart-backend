package uk.co.automatictester.cloudstart.backend.instances.patch;

import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.AmazonRoute53ClientBuilder;
import com.amazonaws.services.route53.model.*;

public class DnsManager {

    private static final AmazonRoute53 ROUTE_53 = AmazonRoute53ClientBuilder.defaultClient();
    private static final String HOSTED_ZONE_ID = DataStore.getValue("HOSTED_ZONE_ID");

    private DnsManager() {
    }

    public static void updateDnsEntry(String instanceId) {
        String publicIpAddress = InstanceManager.getPublicIpAddress(instanceId);
        String instanceName = InstanceManager.getName(instanceId);
        String instanceDnsName = DataStore.getValue(instanceName);

        ResourceRecordSet resourceRecordSet = new ResourceRecordSet()
                .withType(RRType.A)
                .withName(instanceDnsName)
                .withTTL(60L)
                .withResourceRecords(new ResourceRecord().withValue(publicIpAddress));

        Change change = new Change()
                .withAction(ChangeAction.UPSERT)
                .withResourceRecordSet(resourceRecordSet);

        ChangeResourceRecordSetsRequest changeResourceRecordSetsRequest = new ChangeResourceRecordSetsRequest()
                .withHostedZoneId(HOSTED_ZONE_ID)
                .withChangeBatch(new ChangeBatch().withChanges(change));

        ROUTE_53.changeResourceRecordSets(changeResourceRecordSetsRequest);
    }
}
