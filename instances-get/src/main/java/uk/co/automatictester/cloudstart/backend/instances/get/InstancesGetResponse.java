package uk.co.automatictester.cloudstart.backend.instances.get;

import java.util.List;

public class InstancesGetResponse {

    private List<Instance> instances;

    public InstancesGetResponse(List<Instance> instances) {
        this.instances = instances;
    }

    public List<Instance> getInstances() {
        return instances;
    }

    public void setInstances(List<Instance> instances) {
        this.instances = instances;
    }
}
