package io.quarkiverse.opensearch.transport.spi;

import org.opensearch.client.transport.OpenSearchTransport;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkiverse.opensearch.OpenSearchClientConfig;

public interface OpenSearchTransportProvider {
    boolean supports(OpenSearchClientConfig config);

    OpenSearchTransport create(OpenSearchClientConfig config, ObjectMapper mapper) throws OpenSearchTransportProviderException;

    String name();
}
