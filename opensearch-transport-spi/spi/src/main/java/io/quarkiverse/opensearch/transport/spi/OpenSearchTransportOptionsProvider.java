package io.quarkiverse.opensearch.transport.spi;

import java.util.Optional;

import org.opensearch.client.transport.TransportOptions;

/**
 * SPI for providing per-request transport options (headers, parameters).
 * <p>
 * Implementations can inject security context, tenant information, OIDC tokens,
 * or any other per-request data into OpenSearch client requests.
 * <p>
 * Register implementations as CDI beans with the {@code @OpenSearchTransportOptionsConfig}
 * qualifier to have them automatically discovered and applied.
 * <p>
 * Example usage for OIDC token propagation:
 *
 * <pre>
 * &#64;ApplicationScoped
 * &#64;OpenSearchTransportOptionsConfig
 * public class OidcTokenTransportOptionsProvider implements OpenSearchTransportOptionsProvider {
 *
 *     &#64;Inject
 *     SecurityIdentity securityIdentity;
 *
 *     &#64;Override
 *     public Optional&lt;TransportOptions&gt; getTransportOptions(String clientName) {
 *         TokenCredential token = securityIdentity.getCredential(TokenCredential.class);
 *         if (token != null) {
 *             return Optional.of(TransportOptions.builder()
 *                     .addHeader("Authorization", "Bearer " + token.getToken())
 *                     .build());
 *         }
 *         return Optional.empty();
 *     }
 * }
 * </pre>
 *
 * @see io.quarkiverse.opensearch.transport.OpenSearchTransportOptionsConfig
 */
public interface OpenSearchTransportOptionsProvider {

    /**
     * Provide transport options for the current request context.
     * <p>
     * This method is called for each OpenSearch operation when using
     * the request-scoped client. Implementations should be efficient
     * as this is called frequently.
     *
     * @param clientName the named client being used, or {@code "<default>"} for the default client
     * @return optional transport options to apply, or empty to skip
     */
    Optional<TransportOptions> getTransportOptions(String clientName);

    /**
     * Priority for ordering multiple providers.
     * <p>
     * Lower values indicate higher priority. Providers are applied in priority order,
     * with later providers able to override headers set by earlier ones.
     * <p>
     * Default priority is 100. Security-related providers should use lower values
     * (e.g., 10) to ensure they run first.
     *
     * @return the priority value
     */
    default int priority() {
        return 100;
    }
}
