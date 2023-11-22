package io.quarkiverse.opensearch.client.deployment.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;

import io.quarkiverse.opensearch.OpenSearchConfig;
import io.quarkiverse.opensearch.client.OpenSearchTransportConfig;
import io.quarkiverse.opensearch.client.runtime.OpenSearchTransportHelper;
import io.quarkus.test.QuarkusUnitTest;

class ApacheHttpClient5TransportConfigTest {
    @RegisterExtension
    static final QuarkusUnitTest TEST = new QuarkusUnitTest()
            .setArchiveProducer(
                    () -> ShrinkWrap.create(JavaArchive.class)
                            .addClasses(HttpClientTestConfigurator.class, RequestConfigTestConfigurator.class,
                                    OpenSearchTransportHelper.class)
                            .addAsResource(new StringAsset("quarkus.opensearch.hosts=opensearch:9200"),
                                    "application.properties"));

    @Inject
    OpenSearchConfig config;

    @Test
    void testOpenSearchTransportHelperWithOpenSearchClientConfig() throws Exception {
        OpenSearchTransportHelper.createApacheHttpClient5Transport(config);
        assertTrue(HttpClientTestConfigurator.invoked);
        assertTrue(RequestConfigTestConfigurator.invoked);
    }

    @OpenSearchTransportConfig
    @ApplicationScoped
    public static class HttpClientTestConfigurator implements ApacheHttpClient5TransportBuilder.HttpClientConfigCallback {

        private static boolean invoked = false;

        @Override
        public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder builder) {
            invoked = true;
            return builder;
        }
    }

    @OpenSearchTransportConfig
    @ApplicationScoped
    public static class RequestConfigTestConfigurator implements ApacheHttpClient5TransportBuilder.RequestConfigCallback {

        private static boolean invoked = false;

        @Override
        public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder builder) {
            invoked = true;
            return builder;
        }
    }
}
