package io.quarkiverse.opensearch.client.runtime.health;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;
import org.opensearch.client.opensearch.OpenSearchAsyncClient;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.cluster.HealthResponse;

import io.quarkiverse.opensearch.client.runtime.OpenSearchClientsProducer;

@Readiness
@ApplicationScoped
public class OpenSearchHealthCheck implements HealthCheck {

    @Inject
    OpenSearchClientsProducer transportProducer;

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder builder = HealthCheckResponse.named("OpenSearch cluster health check");
        boolean allHealthy = true;
        final Map<String, OpenSearchAsyncClient> asyncClients = transportProducer.getAllAsyncClients();
        for (Map.Entry<String, OpenSearchAsyncClient> entry : asyncClients.entrySet()) {
            String clientName = entry.getKey();
            OpenSearchAsyncClient client = entry.getValue();
            try {
                HealthResponse response = client.cluster().health().get(10, TimeUnit.SECONDS);
                String status = response.status().toString();
                builder.withData("status (" + clientName + ")", status);

                if ("red".equalsIgnoreCase(status)) {
                    allHealthy = false;
                }
            } catch (Exception e) {
                builder.withData("error (" + clientName + ")", e.getClass().getSimpleName() + ": " + e.getMessage());
                allHealthy = false;
            }
        }

        final Map<String, OpenSearchClient> clients = transportProducer.getAllClients();
        for (Map.Entry<String, OpenSearchClient> entry : clients.entrySet()) {
            String clientName = entry.getKey();
            OpenSearchClient client = entry.getValue();
            try {
                HealthResponse response = client.cluster().health();
                String status = response.status().toString();
                builder.withData("status (" + clientName + ")", status);

                if ("red".equalsIgnoreCase(status)) {
                    allHealthy = false;
                }
            } catch (Exception e) {
                builder.withData("error (" + clientName + ")", e.getClass().getSimpleName() + ": " + e.getMessage());
                allHealthy = false;
            }
        }

        return allHealthy ? builder.up().build() : builder.down().build();
    }
}
