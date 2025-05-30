package io.quarkiverse.opensearch.client.runtime;

import java.util.ServiceLoader;

import jakarta.enterprise.inject.Instance;

import org.opensearch.client.transport.OpenSearchTransport;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkiverse.opensearch.OpenSearchClientConfig;
import io.quarkiverse.opensearch.transport.spi.OpenSearchTransportProvider;

public final class OpenSearchTransportHelper {

    private OpenSearchTransportHelper() {
        // private constructor
    }

    public static OpenSearchTransport createTransport(OpenSearchClientConfig config) {
        return createTransport(config, new ObjectMapper());
    }

    public static OpenSearchTransport createTransport(OpenSearchClientConfig config, ObjectMapper objectMapper) {
        return ServiceLoader.load(OpenSearchTransportProvider.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .filter(p -> p.supports(config))
                .findFirst()
                .map(p -> p.create(config, objectMapper))
                .orElseThrow(() -> new IllegalStateException("No OpenSearchTransportProvider found for config"));
    }

    public static OpenSearchTransport createTransport(OpenSearchClientConfig config, Instance<ObjectMapper> objectMappers) {
        final ObjectMapper mapper = objectMappers.stream()
                .findFirst()
                .orElse(new ObjectMapper().findAndRegisterModules());
        return createTransport(config, mapper);
    }

}
