package io.quarkiverse.opensearch;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigGroup
public interface OpenSearchClientConfig {

    /**
     * The list of hosts of the OpenSearch servers, when accessing AWS OpenSearch set to AWS endpoint name.
     *
     * Host Example: opensearch-01:9200,opensearch-02:9200
     * AWS Endpoint Example: search-domain-name-identifier.region.es.amazonaws.com
     */
    Optional<List<String>> hosts();

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
     * The connection timeout.
     */
    @WithDefault("30S")
    Duration threadTimeout();

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
     * AWS Region
     */
    @WithName("aws.region")
    @WithDefault("us-west-2")
    String awsRegion();

    /**
     * Set to "es" or "aoss" to use AWS OpenSearch Service.
     * es : Amazon OpenSearch Service
     * aoss : Amazon OpenSearch Serverless
     */
    @WithName("aws.service")
    Optional<String> awsService();

    /**
     * AWS Secret Access Key for setting up StaticCredentialsProvider
     */
    @WithName("aws.access-key-id")
    Optional<String> accessKeyId();

    /**
     * AWS Secret Access Key Secret for setting up StaticCredentialsProvider
     */
    @WithName("aws.access-key-secret")
    Optional<String> secretAccessKey();

    /**
     * TLS configuration.
     * Allows referencing a named TLS configuration defined elsewhere in the application,
     * such as via the Quarkus `quarkus.tls.*` configuration properties. This enables centralized
     * and reusable TLS settings, including trust stores, key stores, and verification behavior.
     */
    Optional<Tls> tls();

    interface Tls {
        /**
         * The name of the TLS configuration to use.
         * This refers to a configuration group defined under `quarkus.tls.*`. If not set,
         * no explicit TLS configuration will be applied beyond the default JDK behavior.
         */
        String tlsConfigurationName();
    }

    /**
     * Optional keyStoreFile to be used when connecting to cluster nodes
     */
    @WithName("ssl.key-store-file")
    @Deprecated(forRemoval = true, since = "2.0.0")
    Optional<String> keyStoreFile();

    /**
     * Optional password for accessing keyStoreFile
     */
    @WithName("ssl.key-store-password")
    @Deprecated(forRemoval = true, since = "2.0.0")
    Optional<String> keyStorePassword();

    /**
     * SSL Verify Hostname
     */
    @WithName("ssl.verify-hostname")
    @WithDefault("true")
    @Deprecated(forRemoval = true, since = "2.0.0")
    boolean sslVerifyHostname();

    /**
     * Verify SSL Certificates
     */
    @WithName("ssl.verify")
    @WithDefault("true")
    @Deprecated(forRemoval = true, since = "2.0.0")
    boolean sslVerify();

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

    /**
     * Health check configuration for this client.
     */
    HealthConfig health();

    interface HealthConfig {

        /**
         * Whether this client should be included in health checks.
         * When set to false, this client will be excluded from the OpenSearch health check endpoint.
         */
        @WithDefault("true")
        boolean enabled();
    }

}
