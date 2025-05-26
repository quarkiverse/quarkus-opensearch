package io.quarkiverse.opensearch.client.runtime;

import java.util.ServiceLoader;

import jakarta.enterprise.inject.Instance;

import org.opensearch.client.transport.OpenSearchTransport;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkiverse.opensearch.OpenSearchConfig;
import io.quarkiverse.opensearch.transport.spi.OpenSearchTransportProvider;

public final class OpenSearchTransportHelper {

    private OpenSearchTransportHelper() {
        // private constructor
    }

    public static OpenSearchTransport createTransport(OpenSearchConfig config, Instance<ObjectMapper> objectMappers) {
        ObjectMapper mapper = objectMappers.stream()
                .findFirst()
                .orElse(new ObjectMapper().findAndRegisterModules());

        return ServiceLoader.load(OpenSearchTransportProvider.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .filter(p -> p.supports(config))
                .findFirst()
                .map(p -> p.create(config, mapper))
                .orElseThrow(() -> new IllegalStateException("No OpenSearchTransportProvider found for config"));
    }

}
