package io.quarkiverse.opensearch.deployment;

import io.quarkiverse.opensearch.OpenSearchClientsConfig;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;

/**
 * {@link OpenSearchClientsConfig} is a {@code @ConfigRoot} mapping and therefore registered by Quarkus
 * automatically; we only make sure the bean is never removed.
 */
@BuildSteps
public class OpenSearchClientsConfigProcessor {

    @BuildStep
    void produceConfigMappingBean(BuildProducer<AdditionalBeanBuildItem> additionalBeans) {
        additionalBeans.produce(AdditionalBeanBuildItem.unremovableOf(OpenSearchClientsConfig.class));
    }

}
