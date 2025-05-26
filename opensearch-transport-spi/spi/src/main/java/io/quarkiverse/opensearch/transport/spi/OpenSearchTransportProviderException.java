package io.quarkiverse.opensearch.transport.spi;

public class OpenSearchTransportProviderException extends RuntimeException {
    public OpenSearchTransportProviderException(String message) {
        super(message);
    }

    public OpenSearchTransportProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}