package io.quarkiverse.opensearch.client.runtime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.opensearch.client.opensearch.OpenSearchAsyncClient;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.OpenSearchTransport;

@ApplicationScoped
public class OpenSearchClientProducer {

    @Inject
    @Default
    OpenSearchTransport transport;

    private OpenSearchClient client;

    private OpenSearchAsyncClient asyncClient;

    @Produces
    @Singleton
    public OpenSearchClient openSearchClient() {
        this.client = new OpenSearchClient(transport);
        return this.client;
    }

    @Produces
    @Singleton
    public OpenSearchAsyncClient openSearchAsyncClient() {
        this.asyncClient = new OpenSearchAsyncClient(transport);
        return this.asyncClient;
    }

}
