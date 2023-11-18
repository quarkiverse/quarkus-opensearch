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

}
