package uk.co.automatictester.cloudstart.backend.instances.patch;

import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.AmazonRoute53ClientBuilder;
import com.amazonaws.services.route53.model.*;
import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@UtilityClass
public class DnsManager {

    private static final Logger log = LogManager.getLogger(DnsManager.class);
    private static final AmazonRoute53 route53 = AmazonRoute53ClientBuilder.defaultClient();

    public static void upsertDnsEntry(String ipAddress, String hostname) {
        log.info("Updating instance DNS entry '{}' -> '{}'", hostname, ipAddress);
        upsert(ipAddress, hostname);
    }

    public static void deleteDnsEntry(String hostname) {
        log.info("Deleting instance DNS entry '{}'", hostname);
        String fakeIpAddress = "192.168.0.123";
        upsert(fakeIpAddress, hostname);
        ResourceRecordSet resourceRecordSet = getResourceRecordSet(fakeIpAddress, hostname);
        Change change = getChange("DELETE", resourceRecordSet);
        ChangeResourceRecordSetsRequest changeResourceRecordSetsRequest = getChangeResourceRecordSetsRequest(change);
        changeResourceRecordSets(changeResourceRecordSetsRequest);
    }

    private static void upsert(String ipAddress, String hostname) {
        ResourceRecordSet resourceRecordSet = getResourceRecordSet(ipAddress, hostname);
        Change change = getChange("UPSERT", resourceRecordSet);
        ChangeResourceRecordSetsRequest changeResourceRecordSetsRequest = getChangeResourceRecordSetsRequest(change);
        changeResourceRecordSets(changeResourceRecordSetsRequest);
    }

    private static ResourceRecordSet getResourceRecordSet(String ipAddress, String hostname) {
        return new ResourceRecordSet()
                .withType(RRType.A)
                .withName(hostname)
                .withTTL(60L)
                .withResourceRecords(new ResourceRecord().withValue(ipAddress));
    }

    private static Change getChange(String action, ResourceRecordSet resourceRecordSet) {
        ChangeAction changeAction = ChangeAction.fromValue(action);
        return new Change()
                .withAction(changeAction)
                .withResourceRecordSet(resourceRecordSet);
    }

    private static ChangeResourceRecordSetsRequest getChangeResourceRecordSetsRequest(Change change) {
        return new ChangeResourceRecordSetsRequest()
                .withHostedZoneId(DataStore.getHostedZoneId())
                .withChangeBatch(new ChangeBatch().withChanges(change));
    }

    private static void changeResourceRecordSets(ChangeResourceRecordSetsRequest changeResourceRecordSetsRequest) {
        route53.changeResourceRecordSets(changeResourceRecordSetsRequest);
    }
}
