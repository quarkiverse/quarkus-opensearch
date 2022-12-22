package io.quarkiverse.opensearch.restclient.highlevel.deployment;

import io.quarkiverse.opensearch.restclient.highlevel.runtime.OpenSearchRestHighLevelClientProducer;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.Feature;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

class OpenSearchHighLevelClientProcessor {

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(Feature.ELASTICSEARCH_REST_HIGH_LEVEL_CLIENT);
    }

    @BuildStep()
    AdditionalBeanBuildItem build() {
        return AdditionalBeanBuildItem.unremovableOf(OpenSearchRestHighLevelClientProducer.class);
    }

}
