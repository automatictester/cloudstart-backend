package uk.co.automatictester.cloudstart.backend.route53;

import com.amazonaws.services.route53.model.ChangeResourceRecordSetsRequest;

public interface Route53Client {

    void changeResourceRecordSets(ChangeResourceRecordSetsRequest changeResourceRecordSetsRequest);
}
