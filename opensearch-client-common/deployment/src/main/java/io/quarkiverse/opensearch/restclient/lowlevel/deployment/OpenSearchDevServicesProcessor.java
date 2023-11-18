package io.quarkiverse.opensearch.restclient.lowlevel.deployment;

import io.quarkus.deployment.annotations.BuildStep;

class OpenSearchDevServicesProcessor {

    static final String FEATURE = "opensearch-common";

    @BuildStep
    DevservicesOpenSearchBuildItem devServices() {
        return new DevservicesOpenSearchBuildItem("quarkus.opensearch.hosts");
    }

}
