package io.quarkiverse.opensearch.client;

import org.opensearch.client.transport.aws.AwsSdk2TransportOptions;

/**
 * Callback interface for customizing AwsSdk2TransportOptions
 */
public interface AwsSdk2TransportOptionsCallback {

    AwsSdk2TransportOptions.Builder customize(AwsSdk2TransportOptions.Builder builder);

}
