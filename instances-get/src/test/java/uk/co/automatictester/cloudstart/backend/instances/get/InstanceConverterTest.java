package uk.co.automatictester.cloudstart.backend.instances.get;

import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.Tag;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class InstanceConverterTest {

    private static final String INSTANCE_ID = "i-001cc8fd7dd3ac8f3";
    private static final String INSTANCE_TYPE = "t3.medium";
    private static final String STATE = "stopped";
    private static final String NAME = "CloudStart Test";

    @Test
    public void testWithoutName() {
        com.amazonaws.services.ec2.model.Instance ec2Instance = new com.amazonaws.services.ec2.model.Instance();
        ec2Instance.setInstanceId(INSTANCE_ID);
        ec2Instance.setInstanceType(INSTANCE_TYPE);
        ec2Instance.setState(new InstanceState().withCode(80).withName(STATE));

        Instance instance = InstanceConverter.from(ec2Instance);

        assertThat(instance.getInstanceId(), is(equalTo(INSTANCE_ID)));
        assertThat(instance.getInstanceType(), is(equalTo(INSTANCE_TYPE)));
        assertThat(instance.getState(), is(equalTo(STATE)));
        assertThat(instance.getName(), is(equalTo("NAME_NOT_FOUND")));
    }

    @Test
    public void testWithName() {
        com.amazonaws.services.ec2.model.Instance ec2Instance = new com.amazonaws.services.ec2.model.Instance();
        ec2Instance.setInstanceId(INSTANCE_ID);
        ec2Instance.setInstanceType(INSTANCE_TYPE);
        ec2Instance.setState(new InstanceState().withCode(80).withName(STATE));
        ec2Instance.setTags(Collections.singletonList(new Tag().withKey("Name").withValue(NAME)));

        Instance instance = InstanceConverter.from(ec2Instance);

        assertThat(instance.getInstanceId(), is(equalTo(INSTANCE_ID)));
        assertThat(instance.getInstanceType(), is(equalTo(INSTANCE_TYPE)));
        assertThat(instance.getState(), is(equalTo(STATE)));
        assertThat(instance.getName(), is(equalTo(NAME)));
    }
}
