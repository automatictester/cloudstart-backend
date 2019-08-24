package uk.co.automatictester.cloudstart.backend.instances.get;

import org.testng.annotations.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class InstanceTest {

    private static final String INSTANCE_ID = "i-001cc8fd7dd3ac8f3";
    private static final String INSTANCE_TYPE = "t3.medium";
    private static final String STATE = "stopped";
    private static final String NAME = "CloudStart Test";

    @Test
    public void createInstanceWithoutName() {
        Instance instance = new Instance.Builder(INSTANCE_ID, INSTANCE_TYPE, STATE).build();
        assertThat(instance.getInstanceId(), is(equalTo(INSTANCE_ID)));
        assertThat(instance.getInstanceType(), is(equalTo(INSTANCE_TYPE)));
        assertThat(instance.getState(), is(equalTo(STATE)));
        assertThat(instance.getName(), is(equalTo(Optional.empty())));
    }

    @Test
    public void createInstanceWithName() {
        Instance instance = new Instance.Builder(INSTANCE_ID, INSTANCE_TYPE, STATE).withName(NAME).build();
        assertThat(instance.getInstanceId(), is(equalTo(INSTANCE_ID)));
        assertThat(instance.getInstanceType(), is(equalTo(INSTANCE_TYPE)));
        assertThat(instance.getState(), is(equalTo(STATE)));
        assertThat(instance.getName(), is(equalTo(Optional.of(NAME))));
    }
}
