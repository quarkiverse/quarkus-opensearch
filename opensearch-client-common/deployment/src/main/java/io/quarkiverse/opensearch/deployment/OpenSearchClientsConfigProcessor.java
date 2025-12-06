package io.quarkiverse.opensearch.deployment;

import io.quarkiverse.opensearch.OpenSearchClientsConfig;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.builditem.ConfigMappingBuildItem;

@BuildSteps
public class OpenSearchClientsConfigProcessor {

    @BuildStep
    void registerOpenSearchClientsConfig(BuildProducer<ConfigMappingBuildItem> configMappings) {
        configMappings.produce(new ConfigMappingBuildItem(
                OpenSearchClientsConfig.class,
                "quarkus.opensearch"));
    }

    @BuildStep
    void produceConfigMappingBean(BuildProducer<AdditionalBeanBuildItem> additionalBeans) {
        additionalBeans.produce(AdditionalBeanBuildItem.unremovableOf(OpenSearchClientsConfig.class));
    }

}
