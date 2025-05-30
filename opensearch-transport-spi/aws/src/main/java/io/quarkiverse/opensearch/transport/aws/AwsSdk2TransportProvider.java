package io.quarkiverse.opensearch.transport.aws;

import java.util.List;

import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.aws.AwsSdk2Transport;
import org.opensearch.client.transport.aws.AwsSdk2TransportOptions;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkiverse.opensearch.OpenSearchClientConfig;
import io.quarkiverse.opensearch.transport.OpenSearchTransportConfig;
import io.quarkiverse.opensearch.transport.spi.OpenSearchTransportProvider;
import io.quarkiverse.opensearch.transport.spi.OpenSearchTransportProviderException;
import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;

public class AwsSdk2TransportProvider implements OpenSearchTransportProvider {

    @Override
    public boolean supports(OpenSearchClientConfig config) {
        return config.awsService().isPresent();
    }

    @Override
    public OpenSearchTransport create(OpenSearchClientConfig config, ObjectMapper objectMapper)
            throws OpenSearchTransportProviderException {

        try {
            final SdkAsyncHttpClient nettyHttpClient = NettyNioAsyncHttpClient.create();

            AwsSdk2TransportOptions.Builder options = AwsSdk2TransportOptions.builder()
                    .setMapper(new JacksonJsonpMapper(objectMapper));

            // Set credentials
            if (config.accessKeyId().isPresent() && config.secretAccessKey().isPresent()) {
                AwsCredentials staticCreds = AwsBasicCredentials.create(
                        config.accessKeyId().get(), config.secretAccessKey().get());
                options.setCredentials(AwsCredentialsProviderChain.of(
                        StaticCredentialsProvider.create(staticCreds),
                        DefaultCredentialsProvider.create()));
            } else {
                options.setCredentials(DefaultCredentialsProvider.create());
            }

            // Apply transport option callbacks via Quarkus CDI
            Iterable<InstanceHandle<AwsSdk2TransportOptionsCallback>> handles = Arc.container()
                    .select(AwsSdk2TransportOptionsCallback.class, new OpenSearchTransportConfig.Literal())
                    .handles();

            for (InstanceHandle<AwsSdk2TransportOptionsCallback> handle : handles) {
                options = handle.get().customize(options);
                handle.close();
            }

            // Fallback/default host logic
            List<String> hosts = config.hosts().orElse(List.of(""));
            if (hosts.isEmpty() || hosts.get(0).isBlank()) {
                throw new OpenSearchTransportProviderException("No valid OpenSearch host configured for AWS transport.");
            }

            return new AwsSdk2Transport(
                    nettyHttpClient,
                    hosts.get(0),
                    config.awsService().orElseThrow(
                            () -> new OpenSearchTransportProviderException("AWS service must be defined in OpenSearchConfig")),
                    Region.of(config.awsRegion()),
                    options.build());

        } catch (Exception e) {
            throw new OpenSearchTransportProviderException("Failed to create AWS SDK v2 OpenSearch transport", e);
        }
    }

    @Override
    public String name() {
        return "aws-sdk-v2";
    }
}
