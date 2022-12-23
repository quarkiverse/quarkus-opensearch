package io.quarkiverse.opensearch.restclient.lowlevel.runtime;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(prefix = "quarkiverse", name = "opensearch", phase = ConfigPhase.BUILD_AND_RUN_TIME_FIXED)
public class OpenSearchConfig {

    /**
     * The list of hosts of the OpenSearch servers.
     */
    @ConfigItem(defaultValue = "localhost:9200")
    public List<InetSocketAddress> hosts;

    /**
     * The protocol to use when contacting OpenSearch servers.
     * Set to "https" to enable SSL/TLS.
     */
    @ConfigItem(defaultValue = "http")
    public String protocol;

    /**
     * The username for basic HTTP authentication.
     */
    @ConfigItem
    public Optional<String> username;

    /**
     * The password for basic HTTP authentication.
     */
    @ConfigItem
    public Optional<String> password;

    /**
     * The connection timeout.
     */
    @ConfigItem(defaultValue = "1S")
    public Duration connectionTimeout;

    /**
     * The socket timeout.
     */
    @ConfigItem(defaultValue = "30S")
    public Duration socketTimeout;

    /**
     * The maximum number of connections to all the OpenSearch servers.
     */
    @ConfigItem(defaultValue = "20")
    public int maxConnections;

    /**
     * The maximum number of connections per OpenSearch server.
     */
    @ConfigItem(defaultValue = "10")
    public int maxConnectionsPerRoute;

    /**
     * The number of IO thread.
     * By default, this is the number of locally detected processors.
     * <p>
     * Thread counts higher than the number of processors should not be necessary because the I/O threads rely on non-blocking
     * operations, but you may want to use a thread count lower than the number of processors.
     */
    @ConfigItem
    public Optional<Integer> ioThreadCounts;

    /**
     * Configuration for the automatic discovery of new OpenSearch nodes.
     */
    @ConfigItem
    public DiscoveryConfig discovery;

    @ConfigGroup
    public static class DiscoveryConfig {

        /**
         * Defines if automatic discovery is enabled.
         */
        @ConfigItem(defaultValue = "false")
        public boolean enabled;

        /**
         * Refresh interval of the node list.
         */
        @ConfigItem(defaultValue = "5M")
        public Duration refreshInterval;
    }
}
