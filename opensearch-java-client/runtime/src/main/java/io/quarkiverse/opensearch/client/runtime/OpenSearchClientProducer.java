package io.quarkiverse.opensearch.client.runtime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import org.opensearch.client.opensearch.OpenSearchAsyncClient;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.OpenSearchTransport;

@ApplicationScoped
public class OpenSearchClientProducer {

    private final OpenSearchTransport transport;

    public OpenSearchClientProducer(final OpenSearchTransport transport) {
        this.transport = transport;
    }

    @Produces
    @Singleton
    public OpenSearchClient openSearchClient() {
        return new OpenSearchClient(transport);
    }

    @Produces
    @Singleton
    public OpenSearchAsyncClient openSearchAsyncClient() {
        return new OpenSearchAsyncClient(transport);
    }

}
