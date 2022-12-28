package io.quarkiverse.opensearch.restclient.lowlevel.deployment;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(prefix = "quarkiverse", name = "devservices.opensearch", phase = ConfigPhase.BUILD_TIME)
public class OpenSearchDevServicesBuildTimeConfig {

    /**
     * If Dev Services for OpenSearch has been explicitly enabled or disabled. Dev Services are generally enabled
     * by default, unless there is an existing configuration present. For OpenSearch, Dev Services starts a server unless
     * {@code quarkiverse.opensearch.hosts} is set.
     */
    @ConfigItem
    public Optional<Boolean> enabled = Optional.empty();

    /**
     * Optional fixed port the dev service will listen to.
     * <p>
     * If not defined, the port will be chosen randomly.
     */
    @ConfigItem
    public Optional<Integer> port;

    /**
     * The OpenSearch container image to use.
     * Defaults to the opensearch image provided by OpenSearch.
     */
    @ConfigItem(defaultValue = "docker.io/opensearchproject/opensearch:2.4.1")
    public String imageName;

    /**
     * The value for the OPENSEARCH_JAVA_OPTS env variable.
     * Defaults to setting the heap to 512MB min - 1GB max.
     */
    @ConfigItem(defaultValue = "-Xms512m -Xmx1g")
    public String javaOpts;

    /**
     * Indicates if the OpenSearch server managed by Quarkus Dev Services is shared.
     * When shared, Quarkus looks for running containers using label-based service discovery.
     * If a matching container is found, it is used, and so a second one is not started.
     * Otherwise, Dev Services for OpenSearch starts a new container.
     * <p>
     * The discovery uses the {@code quarkus-dev-service-opensearch} label.
     * The value is configured using the {@code service-name} property.
     * <p>
     * Container sharing is only used in dev mode.
     */
    @ConfigItem(defaultValue = "true")
    public boolean shared;

    /**
     * The value of the {@code quarkus-dev-service-opensearch} label attached to the started container.
     * This property is used when {@code shared} is set to {@code true}.
     * In this case, before starting a container, Dev Services for OpenSearch looks for a container with the
     * {@code quarkus-dev-service-opensearch} label
     * set to the configured value. If found, it will use this container instead of starting a new one. Otherwise it
     * starts a new container with the {@code quarkus-dev-service-opensearch} label set to the specified value.
     * <p>
     * This property is used when you need multiple shared OpenSearch servers.
     */
    @ConfigItem(defaultValue = "opensearch")
    public String serviceName;
}
