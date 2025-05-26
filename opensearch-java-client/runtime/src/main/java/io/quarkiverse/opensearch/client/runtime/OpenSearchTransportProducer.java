package io.quarkiverse.opensearch.client.runtime;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.Set;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import org.opensearch.client.transport.OpenSearchTransport;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkiverse.opensearch.OpenSearchConfig;

@ApplicationScoped
public class OpenSearchTransportProducer {

    private final Instance<ObjectMapper> objectMappers;

    private final OpenSearchConfig config;

    private Set<OpenSearchTransport> transports = new HashSet<>();

    public OpenSearchTransportProducer(final Instance<ObjectMapper> objectMappers,
            final OpenSearchConfig config) {
        this.objectMappers = objectMappers;
        this.config = config;
    }

    @Produces
    @Singleton
    public OpenSearchTransport openSearchTransport() throws Exception {
        return addTransport(OpenSearchTransportHelper.createTransport(config, objectMappers));
    }

    private OpenSearchTransport addTransport(final OpenSearchTransport transport) {
        transports.add(transport);
        return transport;
    }

    @PreDestroy
    void destroy() {
        for (OpenSearchTransport transport : transports) {
            try {
                transport.close();
            } catch (IOException ioe) {
                throw new UncheckedIOException(ioe);
            }
        }
    }

}
