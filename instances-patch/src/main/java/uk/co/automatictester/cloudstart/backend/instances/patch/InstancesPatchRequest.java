package uk.co.automatictester.cloudstart.backend.instances.patch;

public class InstancesPatchRequest {

    private String instanceId;
    private String action;

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
