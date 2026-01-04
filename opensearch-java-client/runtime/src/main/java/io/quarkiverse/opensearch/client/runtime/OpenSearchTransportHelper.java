package io.quarkiverse.opensearch.client.runtime;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import jakarta.enterprise.inject.Instance;

import org.opensearch.client.transport.OpenSearchTransport;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkiverse.opensearch.OpenSearchClientConfig;
import io.quarkiverse.opensearch.transport.spi.OpenSearchTransportProvider;

public final class OpenSearchTransportHelper {

    private OpenSearchTransportHelper() {
        // private constructor
    }

    public static OpenSearchTransport createTransport(OpenSearchClientConfig config) {
        return createTransport(config, new ObjectMapper());
    }

    public static OpenSearchTransport createTransport(OpenSearchClientConfig config, ObjectMapper objectMapper) {
        List<OpenSearchTransportProvider> providers = ServiceLoader.load(OpenSearchTransportProvider.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .collect(Collectors.toList());

        return providers.stream()
                .filter(p -> p.supports(config))
                .findFirst()
                .map(p -> p.create(config, objectMapper))
                .orElseThrow(() -> createNoProviderException(config, providers));
    }

    private static IllegalStateException createNoProviderException(OpenSearchClientConfig config,
            List<OpenSearchTransportProvider> providers) {
        StringBuilder message = new StringBuilder("No OpenSearchTransportProvider found for config.\n");

        if (providers.isEmpty()) {
            message.append("\nNo transport providers are available on the classpath.\n");
            message.append("Add one of the following dependencies to your project:\n");
            message.append("  - quarkus-opensearch-transport-apache (recommended for most use cases)\n");
            message.append("  - quarkus-opensearch-transport-aws (for AWS OpenSearch Service)\n");
        } else {
            message.append("\nAvailable providers and why they didn't match:\n");
            for (OpenSearchTransportProvider provider : providers) {
                message.append("  - ").append(provider.name()).append(": ");
                if ("aws-sdk-v2".equals(provider.name())) {
                    if (config.awsService().isEmpty()) {
                        message.append("requires 'quarkus.opensearch.aws.service' to be set (es or aoss)\n");
                    } else {
                        message.append("unknown reason\n");
                    }
                } else if ("apache-http-client5".equals(provider.name())) {
                    if (config.awsService().isPresent()) {
                        message.append("not used when 'quarkus.opensearch.aws.service' is configured\n");
                    } else {
                        message.append("unknown reason\n");
                    }
                } else {
                    message.append("does not support current configuration\n");
                }
            }

            if (config.awsService().isEmpty()) {
                message.append("\nFor local development or non-AWS OpenSearch, add:\n");
                message.append("  <dependency>\n");
                message.append("    <groupId>io.quarkiverse.opensearch</groupId>\n");
                message.append("    <artifactId>quarkus-opensearch-transport-apache</artifactId>\n");
                message.append("  </dependency>\n");
            }
        }

        return new IllegalStateException(message.toString());
    }

    public static OpenSearchTransport createTransport(OpenSearchClientConfig config, Instance<ObjectMapper> objectMappers) {
        final ObjectMapper mapper = objectMappers.stream()
                .findFirst()
                .orElse(new ObjectMapper().findAndRegisterModules());
        return createTransport(config, mapper);
    }

}
