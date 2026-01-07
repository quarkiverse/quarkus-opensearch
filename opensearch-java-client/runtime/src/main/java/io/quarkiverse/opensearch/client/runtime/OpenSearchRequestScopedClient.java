package io.quarkiverse.opensearch.client.runtime;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.opensearch.client.opensearch.OpenSearchAsyncClient;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.TransportOptions;

import io.quarkiverse.opensearch.OpenSearchClientName;
import io.quarkiverse.opensearch.transport.OpenSearchTransportOptionsConfig;
import io.quarkiverse.opensearch.transport.spi.OpenSearchTransportOptionsProvider;

/**
 * Request-scoped wrapper for OpenSearch clients that applies per-request transport options.
 * <p>
 * This client collects transport options from all registered {@link OpenSearchTransportOptionsProvider}
 * implementations and merges them into the client for each request. This enables scenarios like:
 * <ul>
 * <li>OIDC/OAuth2 token propagation to OpenSearch</li>
 * <li>Multi-tenant request routing with tenant-specific headers</li>
 * <li>Request tracing and correlation ID propagation</li>
 * </ul>
 * <p>
 * Usage:
 *
 * <pre>
 * &#64;Inject
 * OpenSearchRequestScopedClient requestScopedClient;
 *
 * public void doSearch() {
 *     // Get client with per-request options applied
 *     OpenSearchClient client = requestScopedClient.getClient();
 *     // ... use client
 * }
 * </pre>
 *
 * For named clients:
 *
 * <pre>
 * OpenSearchClient tenantClient = requestScopedClient.getClient("tenant-a");
 * </pre>
 *
 * @see OpenSearchTransportOptionsProvider
 * @see OpenSearchTransportOptionsConfig
 */
@RequestScoped
public class OpenSearchRequestScopedClient {

    @Inject
    OpenSearchClientsProducer clientsProducer;

    @Inject
    @Any
    Instance<OpenSearchTransportOptionsProvider> optionsProviders;

    // Cached sorted providers for this request
    private List<OpenSearchTransportOptionsProvider> sortedProviders;

    /**
     * Get the default OpenSearch client with per-request transport options applied.
     *
     * @return the client with merged transport options
     */
    public OpenSearchClient getClient() {
        return getClient(OpenSearchClientName.DEFAULT);
    }

    /**
     * Get a named OpenSearch client with per-request transport options applied.
     *
     * @param clientName the client name, or {@link OpenSearchClientName#DEFAULT} for default
     * @return the client with merged transport options
     */
    public OpenSearchClient getClient(String clientName) {
        OpenSearchClient baseClient = clientsProducer.getClientByName(clientName);

        TransportOptions options = resolveTransportOptions(clientName);
        if (options != null) {
            return baseClient.withTransportOptions(options);
        }
        return baseClient;
    }

    /**
     * Get the default async OpenSearch client with per-request transport options applied.
     *
     * @return the async client with merged transport options
     */
    public OpenSearchAsyncClient getAsyncClient() {
        return getAsyncClient(OpenSearchClientName.DEFAULT);
    }

    /**
     * Get a named async OpenSearch client with per-request transport options applied.
     *
     * @param clientName the client name, or {@link OpenSearchClientName#DEFAULT} for default
     * @return the async client with merged transport options
     */
    public OpenSearchAsyncClient getAsyncClient(String clientName) {
        OpenSearchAsyncClient baseClient = clientsProducer.getAsyncClientByName(clientName);

        TransportOptions options = resolveTransportOptions(clientName);
        if (options != null) {
            return baseClient.withTransportOptions(options);
        }
        return baseClient;
    }

    /**
     * Resolve and merge transport options from all registered providers.
     *
     * @param clientName the client name
     * @return merged transport options, or null if no options provided
     */
    private TransportOptions resolveTransportOptions(String clientName) {
        List<OpenSearchTransportOptionsProvider> providers = getSortedProviders();
        if (providers.isEmpty()) {
            return null;
        }

        // Collect all provided options first
        List<TransportOptions> allOptions = new ArrayList<>();
        for (OpenSearchTransportOptionsProvider provider : providers) {
            provider.getTransportOptions(clientName).ifPresent(allOptions::add);
        }

        if (allOptions.isEmpty()) {
            return null;
        }

        // Use first options as base, merge remaining
        TransportOptions.Builder mergedBuilder = allOptions.get(0).toBuilder();
        for (int i = 1; i < allOptions.size(); i++) {
            TransportOptions opts = allOptions.get(i);
            for (var header : opts.headers()) {
                mergedBuilder.addHeader(header.getKey(), header.getValue());
            }
            for (var param : opts.queryParameters().entrySet()) {
                mergedBuilder.setParameter(param.getKey(), param.getValue());
            }
        }

        return mergedBuilder.build();
    }

    /**
     * Get providers sorted by priority (cached for this request).
     */
    private List<OpenSearchTransportOptionsProvider> getSortedProviders() {
        if (sortedProviders == null) {
            sortedProviders = new ArrayList<>();
            for (OpenSearchTransportOptionsProvider provider : optionsProviders
                    .select(OpenSearchTransportOptionsConfig.Literal.INSTANCE)) {
                sortedProviders.add(provider);
            }
            sortedProviders.sort(Comparator.comparingInt(OpenSearchTransportOptionsProvider::priority));
        }
        return sortedProviders;
    }
}
