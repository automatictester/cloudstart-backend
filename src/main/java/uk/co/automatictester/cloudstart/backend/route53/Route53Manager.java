package uk.co.automatictester.cloudstart.backend.route53;

import com.amazonaws.services.route53.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Route53Manager {

    private static final Logger log = LogManager.getLogger(Route53Manager.class);
    static final String FAKE_IP_ADDRESS = "192.168.0.123";
    private final Route53Client client;

    public Route53Manager(Route53Client client) {
        this.client = client;
    }

    public void upsertDnsEntry(String hostedZoneId, String ipAddress, String hostname) {
        upsert(hostedZoneId, ipAddress, hostname);
    }

    public void deleteDnsEntry(String hostedZoneId, String hostname) {
        upsert(hostedZoneId, FAKE_IP_ADDRESS, hostname);
        ResourceRecordSet resourceRecordSet = getResourceRecordSet(FAKE_IP_ADDRESS, hostname);
        Change change = getChange("DELETE", resourceRecordSet);
        var changeResourceRecordSetsRequest = getChangeResourceRecordSetsRequest(hostedZoneId, change);
        client.changeResourceRecordSets(changeResourceRecordSetsRequest);
    }

    private void upsert(String hostedZoneId, String ipAddress, String hostname) {
        ResourceRecordSet resourceRecordSet = getResourceRecordSet(ipAddress, hostname);
        Change change = getChange("UPSERT", resourceRecordSet);
        var changeResourceRecordSetsRequest = getChangeResourceRecordSetsRequest(hostedZoneId, change);
        client.changeResourceRecordSets(changeResourceRecordSetsRequest);
    }

    private ResourceRecordSet getResourceRecordSet(String ipAddress, String hostname) {
        return new ResourceRecordSet()
                .withType(RRType.A)
                .withName(hostname)
                .withTTL(60L)
                .withResourceRecords(new ResourceRecord().withValue(ipAddress));
    }

    private Change getChange(String action, ResourceRecordSet resourceRecordSet) {
        ChangeAction changeAction = ChangeAction.fromValue(action);
        return new Change()
                .withAction(changeAction)
                .withResourceRecordSet(resourceRecordSet);
    }

    private ChangeResourceRecordSetsRequest getChangeResourceRecordSetsRequest(String hostedZoneId, Change change) {
        return new ChangeResourceRecordSetsRequest()
                .withHostedZoneId(hostedZoneId)
                .withChangeBatch(new ChangeBatch().withChanges(change));
    }
}
