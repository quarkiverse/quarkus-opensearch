package io.quarkiverse.opensearch.restclient.highlevel.runtime;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;

@ApplicationScoped
public class OpenSearchRestHighLevelClientProducer {

    private final RestClient restClient;

    private RestHighLevelClient client;

    public OpenSearchRestHighLevelClientProducer(final RestClient restClient) {
        this.restClient = restClient;
    }

    @Produces
    @Singleton
    public RestHighLevelClient restHighLevelClient() {
        this.client = new QuarkusRestHighLevelClient(restClient, RestClient::close, Collections.emptyList());
        return this.client;
    }

    @PreDestroy
    void destroy() {
        try {
            if (this.client != null) {
                this.client.close();
            }
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }
}
