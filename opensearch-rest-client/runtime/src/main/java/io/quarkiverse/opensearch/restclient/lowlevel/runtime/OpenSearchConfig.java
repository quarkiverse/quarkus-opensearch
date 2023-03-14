package io.quarkiverse.opensearch.restclient.lowlevel.runtime;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;

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
    default String protocol() {
        return "http";
    };

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
    default Duration connectionTimeout() {
        return Duration.ofSeconds(15);
    }

    /**
     * The socket timeout.
     */
    default Duration socketTimeout() {
        return Duration.ofSeconds(30);
    }

    /**
     * The maximum number of connections to all the OpenSearch servers.
     */
    default int maxConnections() {
        return 20;
    }

    /**
     * The maximum number of connections per OpenSearch server.
     */
    default int maxConnectionsPerRoute() {
        return 10;
    }

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
    default DiscoveryConfig discovery() {
        return new DiscoveryConfig() {
        };
    }

    public static interface DiscoveryConfig {

        /**
         * Defines if automatic discovery is enabled.
         */
        default boolean enabled() {
            return false;
        }

        /**
         * Refresh interval of the node list.
         */
        default Duration refreshInterval() {
            return Duration.ofMinutes(5);
        }
    }
}
