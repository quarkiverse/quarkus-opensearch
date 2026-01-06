package io.quarkiverse.opensearch.client.deployment.test;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.inject.Inject;

import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.opensearch.client.opensearch.OpenSearchClient;

import io.quarkiverse.opensearch.OpenSearchClientName;
import io.quarkiverse.opensearch.client.runtime.health.OpenSearchHealthCheck;
import io.quarkus.test.QuarkusUnitTest;

public class OpenSearchHealthCheckConfigTest {

    @RegisterExtension
    static final QuarkusUnitTest TEST = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addAsResource(new StringAsset(
                            "quarkus.opensearch.username=dummy\n" +
                                    "quarkus.opensearch.analytics.username=dummy\n" +
                                    "quarkus.opensearch.analytics.health-check.enabled=false"),
                            "application.properties"));

    @Inject
    OpenSearchClient defaultClient;

    @Inject
    @OpenSearchClientName("analytics")
    OpenSearchClient analyticsClient;

    @Inject
    @Readiness
    OpenSearchHealthCheck healthCheck;

    @Test
    void healthCheck_shouldExcludeDisabledClients() {
        // Trigger client initialization by accessing them
        assertNotNull(defaultClient);
        assertNotNull(analyticsClient);

        // Call health check
        HealthCheckResponse response = healthCheck.call();

        // Verify that health check is UP (both clients should be healthy)
        assertEquals(HealthCheckResponse.Status.UP, response.getStatus());

        // Verify that analytics client is NOT included in health check data
        // (since health.enabled=false for analytics)
        boolean hasAnalyticsStatus = response.getData()
                .map(data -> data.keySet().stream().anyMatch(key -> key.contains("analytics")))
                .orElse(false);
        assertFalse(hasAnalyticsStatus, "Analytics client should be excluded from health check");

        // Verify that default client IS included
        boolean hasDefaultStatus = response.getData()
                .map(data -> data.keySet().stream().anyMatch(key -> key.contains("<default>")))
                .orElse(false);
        assertTrue(hasDefaultStatus, "Default client should be included in health check");
    }
}
