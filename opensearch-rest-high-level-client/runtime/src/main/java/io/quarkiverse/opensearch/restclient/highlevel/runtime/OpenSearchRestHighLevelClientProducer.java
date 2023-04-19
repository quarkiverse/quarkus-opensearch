package io.quarkiverse.opensearch.restclient.highlevel.runtime;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;

@ApplicationScoped
public class OpenSearchRestHighLevelClientProducer {

    @Inject
    @Default
    RestClient restClient;

    private RestHighLevelClient client;

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
