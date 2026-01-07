package io.quarkiverse.opensearch.client.deployment;

import org.jboss.jandex.DotName;

import io.quarkiverse.opensearch.client.runtime.OpenSearchClientsProducer;
import io.quarkiverse.opensearch.client.runtime.OpenSearchRequestScopedClient;
import io.quarkiverse.opensearch.client.runtime.health.OpenSearchHealthCheck;
import io.quarkiverse.opensearch.deployment.OpenSearchBuildTimeConfig;
import io.quarkiverse.opensearch.transport.OpenSearchTransportConfig;
import io.quarkiverse.opensearch.transport.OpenSearchTransportOptionsConfig;
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
    private static final DotName OPENSEARCH_TRANSPORT_OPTIONS_CONFIG = DotName
            .createSimple(OpenSearchTransportOptionsConfig.class.getName());

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep()
    AdditionalBeanBuildItem buildOpenSearchTransportProducer() {
        return AdditionalBeanBuildItem.unremovableOf(OpenSearchClientsProducer.class);
    }

    @BuildStep()
    AdditionalBeanBuildItem buildOpenSearchRequestScopedClient() {
        return AdditionalBeanBuildItem.unremovableOf(OpenSearchRequestScopedClient.class);
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
                buildTimeConfig.healthEnabled());
    }

    @BuildStep
    void registerTransportQualifierSupport(BuildProducer<AdditionalBeanBuildItem> additionalBeans,
            BuildProducer<BeanDefiningAnnotationBuildItem> definingAnnotations) {
        additionalBeans.produce(AdditionalBeanBuildItem.builder().addBeanClass(OpenSearchTransportConfig.class).build());
        definingAnnotations.produce(new BeanDefiningAnnotationBuildItem(
                OPENSEARCH_TRANSPORT_CONFIG, DotNames.APPLICATION_SCOPED, false));

        // Register OpenSearchTransportOptionsConfig qualifier for transport options providers
        additionalBeans
                .produce(AdditionalBeanBuildItem.builder().addBeanClass(OpenSearchTransportOptionsConfig.class).build());
        definingAnnotations.produce(new BeanDefiningAnnotationBuildItem(
                OPENSEARCH_TRANSPORT_OPTIONS_CONFIG, DotNames.APPLICATION_SCOPED, false));
    }

    /*
     * @BuildStep
     * void registerOpenSearchClientsConfig(BuildProducer<AdditionalBeanBuildItem> additionalBeans) {
     * additionalBeans.produce(
     * AdditionalBeanBuildItem.unremovableOf(OpenSearchClientsConfig.class)
     * );
     * }
     */
    /*
     * @BuildStep
     *
     * @Record(ExecutionTime.RUNTIME_INIT)
     * void configureNamedClients(OpenSearchClientsRecorder recorder,
     * OpenSearchClientsConfig config,
     * BuildProducer<SyntheticBeanBuildItem> syntheticBeans) {
     * for (Map.Entry<String, OpenSearchClientConfig> entry : config.namedClients().entrySet()) {
     * String name = entry.getKey();
     * if (OpenSearchClientsConfig.DEFAULT_CLIENT_KEY.equalsIgnoreCase(name)) {
     * continue;
     * }
     * OpenSearchClientConfig cfg = entry.getValue();
     *
     * AnnotationInstance qualifier = AnnotationInstance.create(
     * DotName.createSimple(OpenSearchClientName.class.getName()),
     * null,
     * List.of(AnnotationValue.createStringValue("value", name)));
     *
     * syntheticBeans.produce(SyntheticBeanBuildItem.configure(OpenSearchClientConfig.class)
     * .scope(ApplicationScoped.class)
     * .qualifiers(qualifier)
     * .setRuntimeInit()
     * .createWith(recorder.config(cfg))
     * .done());
     *
     * syntheticBeans.produce(SyntheticBeanBuildItem.configure(OpenSearchTransport.class)
     * .scope(ApplicationScoped.class)
     * .qualifiers(qualifier)
     * .setRuntimeInit()
     * .supplier(() -> recorder.transport(cfg))
     * .done());
     *
     * syntheticBeans.produce(SyntheticBeanBuildItem.configure(OpenSearchClient.class)
     * .scope(ApplicationScoped.class)
     * .qualifiers(qualifier)
     * .setRuntimeInit()
     * .supplier(() -> recorder.client(cfg))
     * .done());
     *
     * syntheticBeans.produce(SyntheticBeanBuildItem.configure(OpenSearchAsyncClient.class)
     * .scope(ApplicationScoped.class)
     * .qualifiers(qualifier)
     * .setRuntimeInit()
     * .supplier(() -> recorder.asyncClient(cfg))
     * .done());
     * }
     *
     * // Default client
     * OpenSearchClientConfig defaultCfg = config.defaultClient();
     *
     * syntheticBeans.produce(SyntheticBeanBuildItem.configure(OpenSearchClientConfig.class)
     * .scope(ApplicationScoped.class)
     * .defaultBean()
     * .setRuntimeInit()
     * .supplier(() -> recorder.config(defaultCfg))
     * .done());
     *
     * syntheticBeans.produce(SyntheticBeanBuildItem.configure(OpenSearchTransport.class)
     * .scope(ApplicationScoped.class)
     * .defaultBean()
     * .setRuntimeInit()
     * .supplier(() -> recorder.transport(defaultCfg))
     * .done());
     *
     * syntheticBeans.produce(SyntheticBeanBuildItem.configure(OpenSearchClient.class)
     * .scope(ApplicationScoped.class)
     * .defaultBean()
     * .setRuntimeInit()
     * .supplier(() -> recorder.client(defaultCfg))
     * .done());
     *
     * syntheticBeans.produce(SyntheticBeanBuildItem.configure(OpenSearchAsyncClient.class)
     * .scope(ApplicationScoped.class)
     * .defaultBean()
     * .setRuntimeInit()
     * .supplier(() -> recorder.asyncClient(defaultCfg))
     * .done());
     * }
     *
     */

    @BuildStep
    ReflectiveClassBuildItem addReflectiveClassBuildItem() {
        return ReflectiveClassBuildItem.builder(
                "org.apache.hc.client5.http.impl.auth.BasicScheme",
                "org.apache.hc.client5.http.auth.UsernamePasswordCredentials",
                "org.apache.hc.client5.http.auth.BasicUserPrincipal",
                "java.lang.String",
                "java.util.HashMap")
                .constructors()
                .fields()
                .methods()
                .serialization()
                .unsafeAllocated()
                .build();
    }

}
