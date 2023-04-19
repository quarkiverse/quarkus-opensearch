package io.quarkiverse.opensearch.client.runtime;

import java.io.IOException;
import java.io.UncheckedIOException;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.opensearch.client.RestClient;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.rest_client.RestClientTransport;

import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
public class OpenSearchTransportProducer {

    @Inject
    @Default
    RestClient restClient;

    private OpenSearchTransport transport;

    @Inject
    private Instance<ObjectMapper> objectMappers;

    @Produces
    @Singleton
    public OpenSearchTransport openSearchTransport() {
        final ObjectMapper objectMapper = objectMappers.stream().findFirst()
                .orElse(new ObjectMapper().findAndRegisterModules());
        this.transport = new RestClientTransport(restClient,
                new JacksonJsonpMapper(objectMapper));
        return this.transport;
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
