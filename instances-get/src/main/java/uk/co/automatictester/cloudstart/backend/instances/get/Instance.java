package uk.co.automatictester.cloudstart.backend.instances.get;

public class Instance {

    private String instanceId;
    private String instanceType;
    private String state;
    private String name;

    private Instance(String instanceId, String instanceType, String state) {
        this.instanceId = instanceId;
        this.instanceType = instanceType;
        this.state = state;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getInstanceType() {
        return instanceType;
    }

    public String getState() {
        return state;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("instanceId: %s, instanceType: %s, status: %s, name: %s", instanceId, instanceType, state, name);
    }

    public static class Builder {
        private String instanceId;
        private String instanceType;
        private String state;
        private String name;

        public Builder(String instanceId, String instanceType, String state) {
            this.instanceId = instanceId;
            this.instanceType = instanceType;
            this.state = state;
            this.name = "NAME_NOT_FOUND";
        }

        public Instance.Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Instance build() {
            Instance instance = new Instance(instanceId, instanceType, state);
            instance.name = name;
            return instance;
        }
    }
}
