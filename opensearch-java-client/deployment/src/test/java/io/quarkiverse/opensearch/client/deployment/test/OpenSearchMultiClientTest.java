package io.quarkiverse.opensearch.client.deployment.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.cluster.HealthResponse;

import io.quarkiverse.opensearch.OpenSearchClientName;
import io.quarkus.test.QuarkusUnitTest;

public class OpenSearchMultiClientTest {

    @RegisterExtension
    static final QuarkusUnitTest TEST = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addAsResource(new StringAsset(
                            // Dummy config triggers analytics client but lets Dev Services start it
                            "quarkus.opensearch.analytics.username=dummy\nquarkus.opensearch.reporting.aws.access-key-id=dummy\nquarkus.opensearch.username=dummy\nquarkus.opensearch.aws.access-key-id=dummy"),
                            "application.properties"));

    @Inject
    OpenSearchClient defaultClient;

    @Inject
    @OpenSearchClientName("analytics")
    OpenSearchClient analyticsClient;

    @Inject
    @OpenSearchClientName("reporting")
    OpenSearchClient reportingClient;

    @Test
    void defaultClient_shouldBeHealthy() throws Exception {
        HealthResponse response = defaultClient.cluster().health();
        String status = response.status().jsonValue();
        assertNotNull(status);
        assertTrue(
                status.equals("green") || status.equals("yellow"),
                "Expected default cluster status to be green or yellow, but was: " + status);
    }

    @Test
    void analyticsClient_shouldBeHealthy() throws Exception {
        HealthResponse response = analyticsClient.cluster().health();
        String status = response.status().jsonValue();
        assertNotNull(status);
        assertTrue(
                status.equals("green") || status.equals("yellow"),
                "Expected analytics cluster status to be green or yellow, but was: " + status);
    }

    @Test
    void reportingClient_shouldBeHealthy() throws Exception {
        HealthResponse response = reportingClient.cluster().health();
        String status = response.status().jsonValue();
        assertNotNull(status);
        assertTrue(
                status.equals("green") || status.equals("yellow"),
                "Expected analytics cluster status to be green or yellow, but was: " + status);
    }
}
