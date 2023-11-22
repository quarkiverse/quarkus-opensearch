package io.quarkiverse.opensearch.client.runtime;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

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

    private OpenSearchTransport transport;

    public OpenSearchTransportProducer(final Instance<ObjectMapper> objectMappers,
            final OpenSearchConfig config) {
        this.objectMappers = objectMappers;
        this.config = config;
    }

    @Produces
    @Singleton
    public OpenSearchTransport openSearchTransport() throws NoSuchAlgorithmException, KeyManagementException {
        final Optional<ObjectMapper> objectMapper = objectMappers.stream().findFirst();
        if (config.awsService().isPresent()) {
            return OpenSearchTransportHelper.createAwsSdk2Transport(config);
        }
        return OpenSearchTransportHelper.createApacheHttpClient5Transport(config);
    }

    @PreDestroy
    void destroy() {
        try {
            if (this.transport != null) {
                this.transport.close();
            }
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

}
