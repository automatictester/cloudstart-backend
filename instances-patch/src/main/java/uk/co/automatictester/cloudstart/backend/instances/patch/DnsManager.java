package uk.co.automatictester.cloudstart.backend.instances.patch;

import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.AmazonRoute53ClientBuilder;
import com.amazonaws.services.route53.model.*;

import static uk.co.automatictester.cloudstart.backend.instances.patch.DataStore.getValueFromDataStore;
import static uk.co.automatictester.cloudstart.backend.instances.patch.InstanceManager.getInstanceName;
import static uk.co.automatictester.cloudstart.backend.instances.patch.InstanceManager.getPublicIpAddress;

public class DnsManager {

    private static final String HOSTED_ZONE_ID = getValueFromDataStore("HOSTED_ZONE_ID");
    private static final AmazonRoute53 route53 = AmazonRoute53ClientBuilder.defaultClient();

    private DnsManager() {
    }

    public static void updateDnsEntry(String instanceId) {
        String publicIpAddress = getPublicIpAddress(instanceId);
        String instanceName = getInstanceName(instanceId);
        String instanceDnsName = getValueFromDataStore(instanceName);

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

        route53.changeResourceRecordSets(changeResourceRecordSetsRequest);
    }
}
