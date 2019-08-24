package uk.co.automatictester.cloudstart.backend.instances.get;

import java.util.Optional;

public class Instance {

    private String instanceId;
    private String instanceType;
    private String state;
    private Optional<String> name = Optional.empty();

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

    public Optional<String> getName() {
        return name;
    }

    @Override
    public String toString() {
        if (name.isPresent()) {
            return String.format("instanceId: %s, instanceType: %s, status: %s, name: %s", instanceId, instanceType, state, name);
        } else {
            return String.format("instanceId: %s, instanceType: %s, status: %s", instanceId, instanceType, state);
        }
    }

    public static class Builder {
        private String instanceId;
        private String instanceType;
        private String state;
        private Optional<String> name = Optional.empty();

        public Builder(String instanceId, String instanceType, String state) {
            this.instanceId = instanceId;
            this.instanceType = instanceType;
            this.state = state;
        }

        public Instance.Builder withName(String name) {
            this.name = Optional.of(name);
            return this;
        }

        public Instance build() {
            Instance instance = new Instance(instanceId, instanceType, state);
            instance.name = name;
            return instance;
        }
    }
}
