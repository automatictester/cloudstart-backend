package uk.co.automatictester.cloudstart.backend.instances.get;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InstancesGetHandler implements RequestHandler<InstancesGetRequest, InstancesGetResponse> {

    private static final Logger log = LogManager.getLogger(InstancesGetHandler.class);

    @Override
    public InstancesGetResponse handleRequest(InstancesGetRequest request, Context context) {
        log.info("all good");
        return null;
    }
}
