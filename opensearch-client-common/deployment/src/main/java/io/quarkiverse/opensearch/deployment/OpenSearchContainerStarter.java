package io.quarkiverse.opensearch.deployment;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.opensearch.testcontainers.OpenSearchContainer;
import org.testcontainers.utility.DockerImageName;

import io.quarkus.deployment.builditem.DevServicesResultBuildItem;

/**
 * Lazily-loaded helper that creates and starts an {@link OpenSearchContainer}.
 *
 * <p>
 * This class is the <strong>only</strong> place in the deployment module
 * that references the OpenSearch testcontainers types. It is invoked from a
 * {@code Supplier} inside {@link DevServicesOpenSearchProcessor}'s build
 * step, so its bytecode is loaded only when Dev Services actually run
 * (dev/test mode) — not when the deployment classpath is scanned at build
 * time. Without this split, the JVM eagerly verifies the lambda's
 * synthetic method on processor-class load, requiring
 * {@code org.opensearch:opensearch-testcontainers} on the classpath even
 * for production builds where Dev Services aren't used.
 *
 * <p>
 * See <a href="https://github.com/quarkiverse/quarkus-opensearch/issues/557">issue #557</a>.
 */
final class OpenSearchContainerStarter {

    private OpenSearchContainerStarter() {
    }

    static DevServicesResultBuildItem.RunningDevService start(
            String imageName,
            String featureName,
            String containerLabel,
            Optional<String> serviceName,
            Optional<Integer> port,
            Optional<Duration> startupTimeout,
            String javaOpts,
            Set<String> hostsConfigProperties) {

        OpenSearchContainer container = new OpenSearchContainer(
                DockerImageName.parse(imageName)
                        .asCompatibleSubstituteFor("opensearchproject/opensearch"));

        serviceName.ifPresent(name -> container.withLabel(containerLabel, name));
        port.ifPresent(p -> container.setPortBindings(List.of(p + ":" + p)));
        startupTimeout.ifPresent(container::withStartupTimeout);
        container.addEnv("OPENSEARCH_JAVA_OPTS", javaOpts);

        container.start();

        Map<String, String> propertiesToSet = new HashMap<>();
        String strippedHost = container.getHttpHostAddress().replaceFirst("^http://", "");
        for (String property : hostsConfigProperties) {
            propertiesToSet.put(property, strippedHost);
        }

        return new DevServicesResultBuildItem.RunningDevService(
                featureName,
                container.getContainerId(),
                container::close,
                propertiesToSet);
    }
}
