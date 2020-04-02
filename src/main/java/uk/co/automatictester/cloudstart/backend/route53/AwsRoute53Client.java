package uk.co.automatictester.cloudstart.backend.route53;

import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.AmazonRoute53ClientBuilder;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsRequest;

public class AwsRoute53Client implements Route53Client {

    private static final AmazonRoute53 route53 = AmazonRoute53ClientBuilder.defaultClient();

    @Override
    public void changeResourceRecordSets(ChangeResourceRecordSetsRequest changeResourceRecordSetsRequest) {
        route53.changeResourceRecordSets(changeResourceRecordSetsRequest);
    }
}
