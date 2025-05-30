package io.quarkiverse.opensearch.restclient.lowlevel.deployment.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.opensearch.client.RestClientBuilder;

import io.quarkiverse.opensearch.OpenSearchClientConfig;
import io.quarkiverse.opensearch.OpenSearchClientsConfig;
import io.quarkiverse.opensearch.restclient.lowlevel.runtime.RestClientBuilderHelper;
import io.quarkus.test.QuarkusUnitTest;

class OpenSearchRestClientConfigTest {
    @RegisterExtension
    static final QuarkusUnitTest TEST = new QuarkusUnitTest()
            .setArchiveProducer(
                    () -> ShrinkWrap.create(JavaArchive.class).addClasses(TestConfigurator.class, RestClientBuilderHelper.class)
                            .addAsResource(new StringAsset("quarkus.opensearch.hosts=opensearch:9200"),
                                    "application.properties"));

    @Inject
    OpenSearchClientsConfig config;

    @Test
    void testRestClientBuilderHelperWithOpenSearchClientConfig() {
        RestClientBuilderHelper.createRestClientBuilder(config.defaultClient()).build();
        assertTrue(TestConfigurator.invoked);
    }

    @io.quarkiverse.opensearch.restclient.OpenSearchClientConfig
    @ApplicationScoped
    public static class TestConfigurator implements RestClientBuilder.HttpClientConfigCallback {

        private static boolean invoked = false;

        @Override
        public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder builder) {
            invoked = true;
            return builder;
        }
    }

}
