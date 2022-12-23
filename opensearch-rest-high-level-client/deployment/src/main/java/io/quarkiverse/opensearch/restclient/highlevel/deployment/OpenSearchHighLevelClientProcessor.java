package io.quarkiverse.opensearch.restclient.highlevel.deployment;

import io.quarkiverse.opensearch.restclient.highlevel.runtime.OpenSearchRestHighLevelClientProducer;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

class OpenSearchHighLevelClientProcessor {

    private static final String FEATURE = "opensearch-rest-high-level-client";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep()
    AdditionalBeanBuildItem build() {
        return AdditionalBeanBuildItem.unremovableOf(OpenSearchRestHighLevelClientProducer.class);
    }

}
