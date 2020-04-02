package uk.co.automatictester.cloudstart.backend;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uk.co.automatictester.cloudstart.backend.ddb.DdbManager;
import uk.co.automatictester.cloudstart.backend.ec2.Ec2Manager;
import uk.co.automatictester.cloudstart.backend.route53.Route53Manager;

import java.util.Optional;

import static org.mockito.Mockito.*;

public class UpdateDnsHandlerTest {

    private String hostedZoneId = "A00AA0AAAAA00A";
    private String instanceId = "i-04cdbf3adda6f58bf";
    private String instanceName = "My Instance";
    private String ipAddress = "35.176.100.147";
    private String hostname = "host.example.com";

    private DdbManager ddbManager;
    private Ec2Manager ec2Manager;
    private Route53Manager route53Manager;

    @BeforeClass
    public void setup() {
        ddbManager = mock(DdbManager.class);
        when(ddbManager.getHostedZoneId()).thenReturn(Optional.of(hostedZoneId));
        when(ddbManager.getValue(instanceName)).thenReturn(Optional.of(hostname));

        ec2Manager = mock(Ec2Manager.class);
        when(ec2Manager.getInstanceName(instanceId)).thenReturn(Optional.of(instanceName));
        when(ec2Manager.getPublicIpAddress(instanceId)).thenReturn(Optional.of(ipAddress));

        route53Manager = mock(Route53Manager.class);
    }

    @Test
    public void testHandleRequestUpsert() {
        UpdateDnsRequest request = new UpdateDnsRequest();
        request.setInstanceId(instanceId);
        request.setAction("upsert");

        new UpdateDnsHandler(route53Manager, ec2Manager, ddbManager).handleRequest(request);

        verify(route53Manager, times(1)).upsertDnsEntry(hostedZoneId, ipAddress, hostname);
    }

    @Test
    public void testHandleRequestDelete() {
        UpdateDnsRequest request = new UpdateDnsRequest();
        request.setInstanceId(instanceId);
        request.setAction("delete");

        new UpdateDnsHandler(route53Manager, ec2Manager, ddbManager).handleRequest(request);

        verify(route53Manager, times(1)).deleteDnsEntry(hostedZoneId, hostname);
    }
}
