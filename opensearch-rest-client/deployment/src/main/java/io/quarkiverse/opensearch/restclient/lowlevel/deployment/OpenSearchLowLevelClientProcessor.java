package io.quarkiverse.opensearch.restclient.lowlevel.deployment;

import org.jboss.jandex.DotName;

import io.quarkiverse.opensearch.restclient.lowlevel.OpenSearchClientConfig;
import io.quarkiverse.opensearch.restclient.lowlevel.runtime.OpenSearchRestClientProducer;
import io.quarkiverse.opensearch.restclient.lowlevel.runtime.health.OpenSearchHealthCheck;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanDefiningAnnotationBuildItem;
import io.quarkus.arc.processor.DotNames;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.smallrye.health.deployment.spi.HealthBuildItem;

class OpenSearchLowLevelClientProcessor {

    static final String FEATURE = "opensearch-rest-client";
    private static final DotName OPENSEARCH_CLIENT_CONFIG = DotName.createSimple(OpenSearchClientConfig.class.getName());

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    AdditionalBeanBuildItem build() {
        return AdditionalBeanBuildItem.unremovableOf(OpenSearchRestClientProducer.class);
    }

    @BuildStep
    void openSearchClientConfigSupport(BuildProducer<AdditionalBeanBuildItem> additionalBeans,
            BuildProducer<BeanDefiningAnnotationBuildItem> beanDefiningAnnotations) {
        // add the @OpenSearchClientConfig class otherwise it won't be registered as a qualifier
        additionalBeans.produce(AdditionalBeanBuildItem.builder().addBeanClass(OpenSearchClientConfig.class).build());

        beanDefiningAnnotations
                .produce(new BeanDefiningAnnotationBuildItem(OPENSEARCH_CLIENT_CONFIG, DotNames.APPLICATION_SCOPED, false));
    }

    @BuildStep
    HealthBuildItem addHealthCheck(OpenSearchBuildTimeConfig buildTimeConfig) {
        return new HealthBuildItem(OpenSearchHealthCheck.class.getName(),
                buildTimeConfig.healthEnabled);
    }

    @BuildStep
    DevservicesOpenSearchBuildItem devServices() {
        return new DevservicesOpenSearchBuildItem("quarkus.opensearch.hosts");
    }

}
