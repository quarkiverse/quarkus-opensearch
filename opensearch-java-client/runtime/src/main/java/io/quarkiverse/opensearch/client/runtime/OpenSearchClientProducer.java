package io.quarkiverse.opensearch.client.runtime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

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
