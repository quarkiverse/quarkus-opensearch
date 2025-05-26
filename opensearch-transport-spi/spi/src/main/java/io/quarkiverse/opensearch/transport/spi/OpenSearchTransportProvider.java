package io.quarkiverse.opensearch.transport.spi;

import org.opensearch.client.transport.OpenSearchTransport;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkiverse.opensearch.OpenSearchConfig;

public interface OpenSearchTransportProvider {
    boolean supports(OpenSearchConfig config);

    OpenSearchTransport create(OpenSearchConfig config, ObjectMapper mapper) throws OpenSearchTransportProviderException;

    String name();
}
