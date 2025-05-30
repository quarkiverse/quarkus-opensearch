package io.quarkiverse.opensearch.restclient.lowlevel.runtime;

import java.io.IOException;
import java.io.UncheckedIOException;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.sniff.Sniffer;

import io.quarkiverse.opensearch.OpenSearchClientsConfig;

@ApplicationScoped
public class OpenSearchRestClientProducer {

    private final OpenSearchClientsConfig config;

    private RestClient client;

    private Sniffer sniffer;

    public OpenSearchRestClientProducer(final OpenSearchClientsConfig config) {
        this.config = config;
    }

    @Produces
    @Singleton
    public RestClient restClient() {
        RestClientBuilder builder = RestClientBuilderHelper.createRestClientBuilder(config.defaultClient());

        this.client = builder.build();
        if (config.defaultClient().discovery().enabled()) {
            this.sniffer = RestClientBuilderHelper.createSniffer(client, config.defaultClient());
        }

        return this.client;
    }

    @PreDestroy
    void destroy() {
        try {
            if (this.sniffer != null) {
                this.sniffer.close();
            }
            if (this.client != null) {
                this.client.close();
            }
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }
}
