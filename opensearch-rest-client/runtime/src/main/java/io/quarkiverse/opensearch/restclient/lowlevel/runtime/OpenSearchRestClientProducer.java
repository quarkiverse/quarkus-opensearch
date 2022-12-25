package io.quarkiverse.opensearch.restclient.lowlevel.runtime;

import java.io.IOException;
import java.io.UncheckedIOException;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.sniff.Sniffer;

@ApplicationScoped
public class OpenSearchRestClientProducer {

    @Inject
    OpenSearchConfig config;

    private RestClient client;

    private Sniffer sniffer;

    @Produces
    @Singleton
    public RestClient restClient() {
        RestClientBuilder builder = RestClientBuilderHelper.createRestClientBuilder(config);

        this.client = builder.build();
        if (config.discovery().enabled()) {
            this.sniffer = RestClientBuilderHelper.createSniffer(client, config);
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
