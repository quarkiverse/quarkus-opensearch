package io.quarkiverse.opensearch.client.deployment;

import org.jboss.jandex.DotName;

import io.quarkiverse.opensearch.client.OpenSearchTransportConfig;
import io.quarkiverse.opensearch.client.runtime.OpenSearchClientProducer;
import io.quarkiverse.opensearch.client.runtime.OpenSearchTransportProducer;
import io.quarkiverse.opensearch.client.runtime.health.OpenSearchHealthCheck;
import io.quarkiverse.opensearch.deployment.OpenSearchBuildTimeConfig;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanDefiningAnnotationBuildItem;
import io.quarkus.arc.processor.DotNames;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.smallrye.health.deployment.spi.HealthBuildItem;

class OpenSearchClientProcessor {

    private static final String FEATURE = "opensearch-java-client";

    private static final DotName OPENSEARCH_TRANSPORT_CONFIG = DotName.createSimple(OpenSearchTransportConfig.class.getName());

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

    @BuildStep
    void openSearchTransportConfigSupport(BuildProducer<AdditionalBeanBuildItem> additionalBeans,
            BuildProducer<BeanDefiningAnnotationBuildItem> beanDefiningAnnotations) {
        // add the @OpenSearchTransportConfig class otherwise it won't be registered as a qualifier
        additionalBeans.produce(AdditionalBeanBuildItem.builder().addBeanClass(OpenSearchTransportConfig.class).build());

        beanDefiningAnnotations
                .produce(new BeanDefiningAnnotationBuildItem(OPENSEARCH_TRANSPORT_CONFIG, DotNames.APPLICATION_SCOPED, false));
    }

    @BuildStep
    HealthBuildItem addHealthCheck(OpenSearchBuildTimeConfig buildTimeConfig) {
        return new HealthBuildItem(OpenSearchHealthCheck.class.getName(),
                buildTimeConfig.healthEnabled);
    }

    @BuildStep
    ReflectiveClassBuildItem addReflectiveClassBuildItem() {
        return ReflectiveClassBuildItem.builder(
                "org.apache.hc.client5.http.impl.auth.BasicScheme",
                "org.apache.hc.client5.http.auth.UsernamePasswordCredentials")
                .constructors()
                .fields()
                .methods()
                .serialization()
                .unsafeAllocated()
                .build();
    }

}
