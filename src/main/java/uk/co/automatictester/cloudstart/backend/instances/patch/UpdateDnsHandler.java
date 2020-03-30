package uk.co.automatictester.cloudstart.backend.instances.patch;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UpdateDnsHandler implements RequestHandler<UpdateDnsRequest, UpdateDnsResponse> {

    private static final Logger log = LogManager.getLogger(UpdateDnsHandler.class);

    @Override
    public UpdateDnsResponse handleRequest(UpdateDnsRequest request, Context context) {
        String instanceId = request.getInstanceId();
        String action = request.getAction();

        switch (action) {
            case ("upsert"):
                InstanceManager.getInstanceName(instanceId).ifPresent((instanceName) -> {
                    DataStore.getValue(instanceName).ifPresent((hostname) -> {
                        InstanceManager.getPublicIpAddress(instanceId).ifPresent((ipAddress) -> {
                            DnsManager.upsertDnsEntry(ipAddress, hostname);
                        });
                    });
                });
                break;
            case ("delete"):
                InstanceManager.getInstanceName(instanceId).ifPresent((instanceName) -> {
                    DataStore.getValue(instanceName).ifPresent(DnsManager::deleteDnsEntry);
                });
                break;
            default:
                log.info("Unknown action: {}", action);
        }

        UpdateDnsResponse response = new UpdateDnsResponse();
        response.setAction(request.getAction());
        response.setInstanceId(instanceId);
        return response;
    }
}
