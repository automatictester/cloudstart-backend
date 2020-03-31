package uk.co.automatictester.cloudstart.backend.instances.patch.route53;

import com.amazonaws.services.route53.model.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

public class Route53ManagerTest {

    private static String hostedZoneId = "A00AA0AAAAA00A";
    private static String hostname = "host.example.com";
    private static Route53Client client;
    private static Route53Manager route53Manager;

    @BeforeClass
    public void setup() {
        client = mock(Route53Client.class);
        route53Manager = new Route53Manager(client);
    }

    @BeforeMethod
    public void setupMethod() {
        reset(client);
    }

    @Test
    public void testUpsertDnsEntry() {
        var action = "UPSERT";
        var ipAddress = "10.0.0.1";
        var expectedArgument = getExpected(hostedZoneId, action, hostname, ipAddress);

        route53Manager.upsertDnsEntry(hostedZoneId, ipAddress, hostname);
        verify(client, times(1)).changeResourceRecordSets(expectedArgument);
    }

    @Test
    public void testDeleteDnsEntry() {
        var action = "DELETE";
        var ipAddress = Route53Manager.FAKE_IP_ADDRESS;
        var expectedArgument = getExpected(hostedZoneId, action, hostname, ipAddress);

        route53Manager.deleteDnsEntry(hostedZoneId, hostname);
        verify(client, times(1)).changeResourceRecordSets(expectedArgument);
    }

    private ChangeResourceRecordSetsRequest getExpected(
            String hostedZoneId, String action, String hostname, String ipAddress) {
        var resourceRecordSet = new ResourceRecordSet()
                .withType(RRType.A)
                .withName(hostname)
                .withTTL(60L)
                .withResourceRecords(new ResourceRecord().withValue(ipAddress));

        var change = new Change()
                .withAction(ChangeAction.fromValue(action))
                .withResourceRecordSet(resourceRecordSet);

        return new ChangeResourceRecordSetsRequest()
                .withHostedZoneId(hostedZoneId)
                .withChangeBatch(new ChangeBatch().withChanges(change));
    }
}
