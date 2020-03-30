package uk.co.automatictester.cloudstart.backend.instances.patch;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateDnsRequest {

    private String instanceId;
    private String action;
}
