package io.quarkiverse.opensearch.restclient.highlevel.deployment.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.opensearch.client.RestHighLevelClient;

import io.quarkus.test.QuarkusUnitTest;

class OpenSearchRestHighLevelClientInjectionTest {
    @RegisterExtension
    static final QuarkusUnitTest TEST = new QuarkusUnitTest()
            .setArchiveProducer(
                    () -> ShrinkWrap.create(JavaArchive.class));

    @Inject
    RestHighLevelClient highLevelClient;

    @Test
    void testRestHighLevelClient() {
        assertNotNull(highLevelClient.getLowLevelClient());
    }

}
