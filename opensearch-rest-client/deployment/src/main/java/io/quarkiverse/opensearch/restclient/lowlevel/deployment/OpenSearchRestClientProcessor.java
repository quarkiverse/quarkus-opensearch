package io.quarkiverse.opensearch.restclient.lowlevel.deployment;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import org.opensearch.client.RestClient;

class OpenSearchRestClientProcessor {

    private static final String FEATURE = "opensearch-rest-lowlevel-client";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep()
    AdditionalBeanBuildItem buildOpenSearchTransportProducer() {
        return AdditionalBeanBuildItem.unremovableOf(RestClient.class);
    }


}
