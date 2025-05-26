package io.quarkiverse.opensearch.client.deployment.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.opensearch.client.transport.aws.AwsSdk2TransportOptions;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkiverse.opensearch.OpenSearchConfig;
import io.quarkiverse.opensearch.client.runtime.OpenSearchTransportHelper;
import io.quarkiverse.opensearch.transport.OpenSearchTransportConfig;
import io.quarkiverse.opensearch.transport.aws.AwsSdk2TransportOptionsCallback;
import io.quarkus.test.QuarkusUnitTest;

class AwsSdk2TransportConfigTest {
    @RegisterExtension
    static final QuarkusUnitTest TEST = new QuarkusUnitTest()
            .setArchiveProducer(
                    () -> ShrinkWrap.create(JavaArchive.class)
                            .addClasses(AwsSdk2TransportOptionsTestConfigurator.class, OpenSearchTransportHelper.class)
                            .addAsResource(new StringAsset(
                                    "quarkus.opensearch.hosts=opensearch:9200\nquarkus.opensearch.protocol=https\nquarkus.opensearch.aws.service=es"),
                                    "application.properties"));

    @Inject
    OpenSearchConfig config;

    @Inject
    Instance<ObjectMapper> objectMappers;

    @Test
    void testOpenSearchTransportHelperWithOpenSearchClientConfig() throws Exception {
        OpenSearchTransportHelper.createTransport(config, objectMappers);
        assertTrue(AwsSdk2TransportOptionsTestConfigurator.invoked);
    }

    @ApplicationScoped
    @OpenSearchTransportConfig
    public static class AwsSdk2TransportOptionsTestConfigurator implements AwsSdk2TransportOptionsCallback {

        private static boolean invoked = false;

        @Override
        public AwsSdk2TransportOptions.Builder customize(AwsSdk2TransportOptions.Builder builder) {
            invoked = true;
            return builder;
        }
    }

}
