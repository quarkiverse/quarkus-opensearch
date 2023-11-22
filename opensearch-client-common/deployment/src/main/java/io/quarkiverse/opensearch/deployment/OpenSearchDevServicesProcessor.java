package io.quarkiverse.opensearch.deployment;

import io.quarkus.deployment.annotations.BuildStep;

class OpenSearchDevServicesProcessor {

    static final String FEATURE = "opensearch-common";

    @BuildStep
    DevServicesOpenSearchBuildItem devServices() {
        return new DevServicesOpenSearchBuildItem("quarkus.opensearch.hosts");
    }

}
