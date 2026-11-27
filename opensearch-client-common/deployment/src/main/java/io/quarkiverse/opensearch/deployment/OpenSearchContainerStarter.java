package io.quarkiverse.opensearch.deployment;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.opensearch.testcontainers.OpenSearchContainer;
import org.testcontainers.utility.DockerImageName;

import io.quarkus.deployment.builditem.Startable;
import io.quarkus.devservices.common.ConfigureUtil;
import io.quarkus.runtime.LaunchMode;

/**
 * Lazily-loaded helper that creates an {@link OpenSearchContainer} for Dev Services.
 *
 * <p>
 * This class is the <strong>only</strong> place in the deployment module that references
 * the OpenSearch testcontainers types. It is invoked from a {@code Supplier} inside
 * {@link DevServicesOpenSearchProcessor}'s build step, so its bytecode is loaded only
 * when Dev Services actually run (dev/test mode) — not when the deployment classpath is
 * scanned at build time. Without this split, the JVM eagerly verifies the lambda's
 * synthetic method on processor-class load, requiring
 * {@code org.opensearch:opensearch-testcontainers} on the classpath even for production
 * builds where Dev Services aren't used.
 *
 * <p>
 * See <a href="https://github.com/quarkiverse/quarkus-opensearch/issues/557">issue #557</a>.
 */
final class OpenSearchContainerStarter {

    private OpenSearchContainerStarter() {
    }

    /**
     * Creates (but does not start) the container. Quarkus' Dev Services lifecycle starts,
     * reuses and stops it.
     */
    static Startable create(
            String imageName,
            String containerLabel,
            LaunchMode launchMode,
            String serviceName,
            Optional<Integer> port,
            Optional<Duration> startupTimeout,
            String javaOpts) {

        QuarkusOpenSearchContainer container = new QuarkusOpenSearchContainer(
                DockerImageName.parse(imageName)
                        .asCompatibleSubstituteFor("opensearchproject/opensearch"));

        ConfigureUtil.configureSharedServiceLabel(container, launchMode, containerLabel, serviceName);
        port.ifPresent(p -> container.setPortBindings(List.of(p + ":" + DevServicesOpenSearchProcessor.OPENSEARCH_PORT)));
        startupTimeout.ifPresent(container::withStartupTimeout);
        container.addEnv("OPENSEARCH_JAVA_OPTS", javaOpts);
        return container;
    }

    static final class QuarkusOpenSearchContainer extends OpenSearchContainer<QuarkusOpenSearchContainer>
            implements Startable {

        QuarkusOpenSearchContainer(DockerImageName imageName) {
            super(imageName);
        }

        @Override
        public String getConnectionInfo() {
            return getHttpHostAddress().replaceFirst("^http://", "");
        }

        @Override
        public void close() {
            super.close();
        }
    }
}
