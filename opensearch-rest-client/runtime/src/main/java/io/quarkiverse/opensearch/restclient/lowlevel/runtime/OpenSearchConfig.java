package io.quarkiverse.opensearch.restclient.lowlevel.runtime;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.opensearch")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface OpenSearchConfig {

    /**
     * The list of hosts of the OpenSearch servers.
     */
    Optional<List<InetSocketAddress>> hosts();

    /**
     * The protocol to use when contacting OpenSearch servers.
     * Set to "https" to enable SSL/TLS.
     */
    @WithDefault("http")
    String protocol();

    /**
     * The username for basic HTTP authentication.
     */
    Optional<String> username();

    /**
     * The password for basic HTTP authentication.
     */
    Optional<String> password();

    /**
     * The connection timeout.
     */
    @WithDefault("15S")
    Duration connectionTimeout();

    /**
     * The socket timeout.
     */
    @WithDefault("30S")
    Duration socketTimeout();

    /**
     * The maximum number of connections to all the OpenSearch servers.
     */
    @WithDefault("20")
    int maxConnections();

    /**
     * The maximum number of connections per OpenSearch server.
     */
    @WithDefault("10")
    int maxConnectionsPerRoute();

    /**
     * The number of IO thread.
     * By default, this is the number of locally detected processors.
     * <p>
     * Thread counts higher than the number of processors should not be necessary because the I/O threads rely on non-blocking
     * operations, but you may want to use a thread count lower than the number of processors.
     */
    Optional<Integer> ioThreadCounts();

    /**
     * Configuration for the automatic discovery of new OpenSearch nodes.
     */
    DiscoveryConfig discovery();

    interface DiscoveryConfig {

        /**
         * Defines if automatic discovery is enabled.
         */
        @WithDefault("false")
        boolean enabled();

        /**
         * Refresh interval of the node list.
         */
        @WithDefault("5M")
        Duration refreshInterval();
    }
}
