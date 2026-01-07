package io.quarkiverse.opensearch.client.runtime;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;

import org.opensearch.client.opensearch.OpenSearchAsyncClient;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.OpenSearchTransport;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkiverse.opensearch.OpenSearchClientConfig;
import io.quarkiverse.opensearch.OpenSearchClientName;
import io.quarkiverse.opensearch.OpenSearchClientsConfig;
import io.quarkus.runtime.annotations.RegisterForReflection;

@ApplicationScoped
@RegisterForReflection
public class OpenSearchClientsProducer {

    private static final String DEFAULT = OpenSearchClientName.DEFAULT;

    @Inject
    OpenSearchClientsConfig clientsConfig;

    @Inject
    Instance<ObjectMapper> objectMappers;

    private final Map<String, OpenSearchTransport> transports = new ConcurrentHashMap<>();
    private final Map<String, OpenSearchClient> clients = new ConcurrentHashMap<>();
    private final Map<String, OpenSearchAsyncClient> asyncClients = new ConcurrentHashMap<>();

    public OpenSearchClientsProducer() {
    }

    // ----- Transport -----

    @Produces
    @Default
    @OpenSearchClientName // Enables CDI match without explicit value
    public OpenSearchTransport transport(InjectionPoint ip) {
        String name = extractName(ip);
        return getOrCreateTransport(name);
    }

    // ----- Sync Client -----

    @Produces
    @Default
    @OpenSearchClientName
    public OpenSearchClient client(InjectionPoint ip) {
        String name = extractName(ip);
        return getOrCreateClient(name);
    }

    // ----- Async Client -----

    @Produces
    @Default
    @OpenSearchClientName
    public OpenSearchAsyncClient asyncClient(InjectionPoint ip) {
        String name = extractName(ip);
        return getOrCreateAsync(name);
    }

    // ----- Cleanup -----

    @PreDestroy
    void destroy() {
        asyncClients.clear();
        clients.clear();
        transports.values().forEach(transport -> {
            try {
                transport.close();
            } catch (IOException e) {
                throw new RuntimeException("Failed to close OpenSearchTransport", e);
            }
        });
        transports.clear();
    }

    // ----- Internal Resolution -----

    private OpenSearchTransport getOrCreateTransport(String name) {
        return transports.computeIfAbsent(name, n -> {
            OpenSearchClientConfig cfg = resolveConfig(n);
            return OpenSearchTransportHelper.createTransport(cfg, objectMappers);
        });
    }

    private OpenSearchClient getOrCreateClient(String name) {
        return clients.computeIfAbsent(name, n -> new OpenSearchClient(getOrCreateTransport(n)));
    }

    private OpenSearchAsyncClient getOrCreateAsync(String name) {
        return asyncClients.computeIfAbsent(name, n -> new OpenSearchAsyncClient(getOrCreateTransport(n)));
    }

    private OpenSearchClientConfig resolveConfig(String name) {
        if (DEFAULT.equals(name)) {
            return clientsConfig.defaultClient();
        }
        return Optional.ofNullable(clientsConfig.namedClients().get(name))
                .orElseThrow(() -> new IllegalStateException("No OpenSearch config found for client: " + name));
    }

    private String extractName(InjectionPoint ip) {
        return ip.getQualifiers().stream()
                .filter(q -> q.annotationType().equals(OpenSearchClientName.class))
                .map(q -> ((OpenSearchClientName) q).value())
                .findFirst()
                .orElse(DEFAULT);
    }

    public Map<String, OpenSearchClient> getAllClients() {
        return clients;
    }

    public Map<String, OpenSearchAsyncClient> getAllAsyncClients() {
        return asyncClients;
    }

    public OpenSearchClientConfig getClientConfig(String name) {
        return resolveConfig(name);
    }

    /**
     * Get or create a client by name. Useful for programmatic access.
     *
     * @param name the client name
     * @return the OpenSearch client
     */
    public OpenSearchClient getClientByName(String name) {
        return getOrCreateClient(name);
    }

    /**
     * Get or create an async client by name. Useful for programmatic access.
     *
     * @param name the client name
     * @return the OpenSearch async client
     */
    public OpenSearchAsyncClient getAsyncClientByName(String name) {
        return getOrCreateAsync(name);
    }
}
