package uk.co.automatictester.cloudstart.backend.instances.patch;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateDnsResponse {

    private String instanceId;
    private String action;
}
