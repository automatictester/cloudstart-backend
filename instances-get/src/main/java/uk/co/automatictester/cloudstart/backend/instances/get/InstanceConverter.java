package uk.co.automatictester.cloudstart.backend.instances.get;

import com.amazonaws.services.ec2.model.Tag;

import java.util.Optional;

public class InstanceConverter {

    private InstanceConverter() {
    }

    public static Instance from(com.amazonaws.services.ec2.model.Instance instance) {
        Instance.Builder instanceBuilder = new Instance.Builder(
                instance.getInstanceId(),
                instance.getInstanceType(),
                instance.getState().getName()
        );
        Optional<String> name = getInstanceName(instance);
        name.ifPresent(instanceBuilder::withName);
        return instanceBuilder.build();
    }

    private static Optional<String> getInstanceName(com.amazonaws.services.ec2.model.Instance instance) {
        return instance.getTags().stream()
                .filter(x -> x.getKey().equals("Name"))
                .findFirst()
                .map(Tag::getValue);
    }
}
