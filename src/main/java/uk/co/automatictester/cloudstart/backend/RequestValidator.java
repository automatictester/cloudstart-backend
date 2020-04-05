package uk.co.automatictester.cloudstart.backend;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

public class RequestValidator {

    private static final Logger log = LogManager.getLogger(RequestValidator.class);

    private RequestValidator() {
    }

    public static boolean isValid(UpdateDnsRequest request) {
        var instanceId = request.getInstanceId();
        var action = request.getAction();

        if (action == null || !Set.of("upsert", "delete").contains(action.toLowerCase())) {
            log.error("Invalid action: '{}'", action);
            return false;
        } else if (instanceId == null || instanceId.isBlank()) {
            log.error("Invalid instanceId: '{}'", instanceId);
            return false;
        }
        return true;
    }
}
