package uk.co.automatictester.cloudstart.backend.instances.patch;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static uk.co.automatictester.cloudstart.backend.instances.patch.DataStore.hasCustomHostnameMapping;
import static uk.co.automatictester.cloudstart.backend.instances.patch.DnsManager.updateDnsEntry;
import static uk.co.automatictester.cloudstart.backend.instances.patch.InstanceManager.*;

public class InstancesPatchHandler implements RequestHandler<InstancesPatchRequest, InstancesPatchResponse> {

    private static final Logger log = LogManager.getLogger(InstancesPatchHandler.class);

    @Override
    public InstancesPatchResponse handleRequest(InstancesPatchRequest request, Context context) {
        String instanceId = request.getInstanceId();
        String action = request.getAction();

        InstancesPatchResponse response = new InstancesPatchResponse();
        response.setAction(request.getAction());
        response.setInstanceId(instanceId);

        try {
            switch (action) {
                case ("start"):
                    startInstance(instanceId);
                    waitForInstanceRunning(instanceId);
                    if (hasNameTag(instanceId)) {
                        String instanceName = getInstanceName(instanceId);
                        if (hasCustomHostnameMapping(instanceName)) {
                            updateDnsEntry(instanceId);
                        }
                    }
                    break;
                case ("stop"):
                    stopInstance(instanceId);
                    waitForInstanceStopped(instanceId);
                    break;
                case ("reboot"):
                    rebootInstance(instanceId);
                    break;
                case ("terminate"):
                    terminateInstance(instanceId);
                    waitForInstanceTerminated(instanceId);
                    break;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            response.setStatus("ERROR");
            response.setMessage(e.getMessage());
            return response;
        }

        response.setStatus("OK");
        response.setMessage("");
        return response;
    }
}
