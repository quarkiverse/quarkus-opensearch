package io.quarkiverse.opensearch.deployment;

import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;

import org.jboss.logging.Logger;
import org.opensearch.testcontainers.OpenSearchContainer;
import org.testcontainers.utility.DockerImageName;

import io.quarkus.builder.BuildException;
import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.builditem.CuratedApplicationShutdownBuildItem;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.builditem.DevServicesSharedNetworkBuildItem;
import io.quarkus.deployment.builditem.DockerStatusBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.deployment.console.ConsoleInstalledBuildItem;
import io.quarkus.deployment.console.StartupLogCompressor;
import io.quarkus.deployment.dev.devservices.DevServicesConfig;
import io.quarkus.deployment.logging.LoggingSetupBuildItem;
import io.quarkus.devservices.common.ContainerAddress;
import io.quarkus.devservices.common.ContainerLocator;
import io.quarkus.runtime.configuration.ConfigUtils;

@BuildSteps(onlyIfNot = IsNormal.class, onlyIf = DevServicesConfig.Enabled.class)
public class DevServicesOpenSearchProcessor {
    private static final Logger log = Logger.getLogger(DevServicesOpenSearchProcessor.class);

    static final String DEV_SERVICE_LABEL = "quarkus-dev-service-opensearch";
    static final int OPENSEARCH_PORT = 9200;

    private static final ContainerLocator openSearchContainerLocator = new ContainerLocator(DEV_SERVICE_LABEL, OPENSEARCH_PORT);

    static volatile DevServicesResultBuildItem.RunningDevService devService;
    static volatile OpenSearchDevServicesBuildTimeConfig cfg;
    static volatile boolean first = true;

    @BuildStep
    public DevServicesResultBuildItem startOpenSearchDevService(
            DockerStatusBuildItem dockerStatusBuildItem,
            LaunchModeBuildItem launchMode,
            OpenSearchDevServicesBuildTimeConfig configuration,
            List<DevServicesSharedNetworkBuildItem> devServicesSharedNetworkBuildItem,
            Optional<ConsoleInstalledBuildItem> consoleInstalledBuildItem,
            CuratedApplicationShutdownBuildItem closeBuildItem,
            LoggingSetupBuildItem loggingSetupBuildItem,
            DevServicesConfig devServicesConfig,
            List<DevServicesOpenSearchBuildItem> devservicesOpenSearchBuildItems) throws BuildException {

        if (devservicesOpenSearchBuildItems.isEmpty()) {
            return null;
        }

        DevservicesOpenSearchBuildItemsConfiguration buildItemsConfig = new DevservicesOpenSearchBuildItemsConfiguration(
                devservicesOpenSearchBuildItems);

        boolean allHostsExplicitlyConfigured = buildItemsConfig.hostsConfigProperties.stream()
                .allMatch(ConfigUtils::isPropertyPresent);

        if (allHostsExplicitlyConfigured) {
            log.debug("Not starting OpenSearch Dev Services; all clients have 'hosts' configured.");
            return null;
        }

        if (devService != null) {
            boolean shouldShutdownTheServer = !configuration.equals(cfg);
            if (!shouldShutdownTheServer) {
                return devService.toBuildItem();
            }
            shutdownOpenSearch();
            cfg = null;
        }

        StartupLogCompressor compressor = new StartupLogCompressor(
                (launchMode.isTest() ? "(test) " : "") + "OpenSearch Dev Services Starting:",
                consoleInstalledBuildItem, loggingSetupBuildItem);
        try {
            devService = startOpenSearch(dockerStatusBuildItem, configuration, buildItemsConfig, launchMode,
                    !devServicesSharedNetworkBuildItem.isEmpty(), devServicesConfig.timeout());
            if (devService == null) {
                compressor.closeAndDumpCaptured();
            } else {
                compressor.close();
            }
        } catch (Throwable t) {
            compressor.closeAndDumpCaptured();
            throw new RuntimeException(t);
        }

        if (devService == null) {
            return null;
        }

        if (first) {
            first = false;
            closeBuildItem.addCloseTask(() -> {
                if (devService != null) {
                    shutdownOpenSearch();
                }
                first = true;
                devService = null;
                cfg = null;
            }, true);
        }
        cfg = configuration;

        if (devService.isOwner()) {
            log.infof("Dev Services for OpenSearch started. Accessible via: %s",
                    String.join(", ", buildItemsConfig.hostsConfigProperties));
        }
        return devService.toBuildItem();
    }

    private void shutdownOpenSearch() {
        if (devService != null) {
            try {
                devService.close();
            } catch (Throwable e) {
                log.error("Failed to stop the OpenSearch server", e);
            } finally {
                devService = null;
            }
        }
    }

    private DevServicesResultBuildItem.RunningDevService startOpenSearch(
            DockerStatusBuildItem dockerStatusBuildItem,
            OpenSearchDevServicesBuildTimeConfig config,
            DevservicesOpenSearchBuildItemsConfiguration buildItemConfig,
            LaunchModeBuildItem launchMode,
            boolean useSharedNetwork,
            Optional<Duration> timeout) throws BuildException {

        if (!config.enabled().orElse(true)) {
            log.debug("OpenSearch Dev Services disabled via config.");
            return null;
        }

        if (!dockerStatusBuildItem.isContainerRuntimeAvailable()) {
            log.warnf("Docker isn't working, please configure the OpenSearch hosts property (%s).",
                    displayProperties(buildItemConfig.hostsConfigProperties));
            return null;
        }

        if (buildItemConfig.version != null) {
            String containerTag = config.imageName().substring(config.imageName().indexOf(':') + 1);
            if (!containerTag.startsWith(buildItemConfig.version)) {
                throw new BuildException("Dev services for OpenSearch version mismatch: container image "
                        + config.imageName() + " vs expected version " + buildItemConfig.version, Collections.emptyList());
            }
        }

        Optional<ContainerAddress> maybeContainerAddress = openSearchContainerLocator.locateContainer(
                config.serviceName(), config.shared(), launchMode.getLaunchMode());

        Supplier<DevServicesResultBuildItem.RunningDevService> startContainer = () -> {
            OpenSearchContainer container = new OpenSearchContainer(
                    DockerImageName.parse(config.imageName())
                            .asCompatibleSubstituteFor("opensearchproject/opensearch"));

            if (config.serviceName() != null) {
                container.withLabel(DEV_SERVICE_LABEL, config.serviceName());
            }

            config.port().ifPresent(port -> container.setPortBindings(List.of(port + ":" + port)));
            timeout.ifPresent(container::withStartupTimeout);
            container.addEnv("OPENSEARCH_JAVA_OPTS", config.javaOpts());

            container.start();
            return new DevServicesResultBuildItem.RunningDevService(
                    OpenSearchDevServicesProcessor.FEATURE,
                    container.getContainerId(),
                    container::close,
                    buildPropertiesMap(buildItemConfig, container.getHttpHostAddress()));
        };

        return maybeContainerAddress
                .map(addr -> new DevServicesResultBuildItem.RunningDevService(
                        OpenSearchDevServicesProcessor.FEATURE,
                        addr.getId(),
                        null,
                        buildPropertiesMap(buildItemConfig, addr.getUrl())))
                .orElseGet(startContainer);
    }

    private Map<String, String> buildPropertiesMap(DevservicesOpenSearchBuildItemsConfiguration buildItemConfig,
            String httpHosts) {
        Map<String, String> propertiesToSet = new HashMap<>();
        String strippedHost = httpHosts.replaceFirst("^http://", "");
        for (String property : buildItemConfig.hostsConfigProperties) {
            propertiesToSet.put(property, strippedHost);
        }
        return propertiesToSet;
    }

    private String displayProperties(Set<String> hostsConfigProperties) {
        return String.join(" and ", hostsConfigProperties);
    }

    private static class DevservicesOpenSearchBuildItemsConfiguration {
        private final Set<String> hostsConfigProperties;
        private final String version;

        private DevservicesOpenSearchBuildItemsConfiguration(List<DevServicesOpenSearchBuildItem> buildItems)
                throws BuildException {
            hostsConfigProperties = new HashSet<>(buildItems.size());
            String detectedVersion = null;

            for (DevServicesOpenSearchBuildItem item : buildItems) {
                if (detectedVersion == null) {
                    detectedVersion = item.getVersion();
                } else if (!detectedVersion.equals(item.getVersion())) {
                    throw new BuildException("Multiple extensions requested OpenSearch Dev Services with different versions.",
                            Collections.emptyList());
                }
                hostsConfigProperties.addAll(item.getHostsConfigProperties());
            }
            this.version = detectedVersion;
        }
    }
}
