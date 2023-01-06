package io.quarkiverse.opensearch.client.deployment;

import io.quarkiverse.opensearch.client.runtime.OpenSearchClientProducer;
import io.quarkiverse.opensearch.client.runtime.OpenSearchTransportProducer;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

class OpenSearchClientProcessor {

    private static final String FEATURE = "opensearch-java-client";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep()
    AdditionalBeanBuildItem buildOpenSearchTransportProducer() {
        return AdditionalBeanBuildItem.unremovableOf(OpenSearchTransportProducer.class);
    }

    @BuildStep()
    AdditionalBeanBuildItem buildOpenSearchClientProducer() {
        return AdditionalBeanBuildItem.unremovableOf(OpenSearchClientProducer.class);
    }

}
