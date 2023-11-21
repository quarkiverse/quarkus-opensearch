package io.quarkiverse.opensearch.client.runtime.health;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.cluster.HealthResponse;

@Readiness
@ApplicationScoped
public class OpenSearchHealthCheck implements HealthCheck {

    private final OpenSearchClient client;

    public OpenSearchHealthCheck(final OpenSearchClient client) {
        this.client = client;
    }

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder builder = HealthCheckResponse.named("OpenSearch cluster health check").up();
        try {
            final HealthResponse response = client.cluster().health();
            final String status = response.status().toString();
            if ("red".equals(status)) {
                builder.down().withData("status", status);
            } else {
                builder.up().withData("status", status);
            }
        } catch (Exception e) {
            return builder.down().withData("reason", e.getMessage()).build();
        }
        return builder.build();
    }
}
