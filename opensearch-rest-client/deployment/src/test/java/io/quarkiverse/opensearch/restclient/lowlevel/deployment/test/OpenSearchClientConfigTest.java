package io.quarkiverse.opensearch.restclient.lowlevel.deployment.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.opensearch.client.RestClientBuilder;

import io.quarkiverse.opensearch.restclient.lowlevel.OpenSearchClientConfig;
import io.quarkiverse.opensearch.restclient.lowlevel.runtime.OpenSearchConfig;
import io.quarkiverse.opensearch.restclient.lowlevel.runtime.RestClientBuilderHelper;
import io.quarkus.test.QuarkusUnitTest;

public class OpenSearchClientConfigTest {
    @RegisterExtension
    static final QuarkusUnitTest TEST = new QuarkusUnitTest()
            .setArchiveProducer(
                    () -> ShrinkWrap.create(JavaArchive.class).addClasses(TestConfigurator.class, RestClientBuilderHelper.class)
                            .addAsResource(new StringAsset("quarkus.opensearch.hosts=opensearch:9200"),
                                    "application.properties"));

    @Inject
    OpenSearchConfig config;

    @Test
    public void testRestClientBuilderHelperWithElasticsearchClientConfig() {
        RestClientBuilderHelper.createRestClientBuilder(config).build();
        assertTrue(TestConfigurator.invoked);
    }

    @OpenSearchClientConfig
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
