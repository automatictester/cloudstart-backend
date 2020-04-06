package uk.co.automatictester.cloudstart.backend;

public class UpdateDnsRequest {

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

    @Override
    public String toString() {
        return String.format("UpdateDnsRequest[instanceId=%s, action=%s]", instanceId, action);
    }
}
