package io.quarkiverse.opensearch.deployment;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.logging.Logger;

import io.quarkus.builder.BuildException;
import io.quarkus.deployment.IsDevServicesSupportedByLaunchMode;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.builditem.DockerStatusBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.deployment.dev.devservices.DevServicesConfig;
import io.quarkus.devservices.common.ContainerLocator;
import io.quarkus.runtime.configuration.ConfigUtils;

@BuildSteps(onlyIf = { IsDevServicesSupportedByLaunchMode.class, DevServicesConfig.Enabled.class })
public class DevServicesOpenSearchProcessor {
    private static final Logger log = Logger.getLogger(DevServicesOpenSearchProcessor.class);

    static final String DEV_SERVICE_LABEL = "quarkus-dev-service-opensearch";
    static final int OPENSEARCH_PORT = 9200;

    private static final ContainerLocator openSearchContainerLocator = new ContainerLocator(DEV_SERVICE_LABEL,
            OPENSEARCH_PORT);

    @BuildStep
    public void startOpenSearchDevService(
            DockerStatusBuildItem dockerStatusBuildItem,
            LaunchModeBuildItem launchMode,
            OpenSearchDevServicesBuildTimeConfig config,
            DevServicesConfig devServicesConfig,
            List<DevServicesOpenSearchBuildItem> devservicesOpenSearchBuildItems,
            BuildProducer<DevServicesResultBuildItem> devServicesResult) throws BuildException {

        if (devservicesOpenSearchBuildItems.isEmpty()) {
            return;
        }

        DevservicesOpenSearchBuildItemsConfiguration buildItemsConfig = new DevservicesOpenSearchBuildItemsConfiguration(
                devservicesOpenSearchBuildItems);
        Set<String> hostsProperties = buildItemsConfig.hostsConfigProperties;

        if (hostsProperties.stream().allMatch(ConfigUtils::isPropertyPresent)) {
            log.debug("Not starting OpenSearch Dev Services; all clients have 'hosts' configured.");
            return;
        }

        if (!config.enabled().orElse(true)) {
            log.debug("OpenSearch Dev Services disabled via config.");
            return;
        }

        if (!dockerStatusBuildItem.isContainerRuntimeAvailable()) {
            log.warnf("Docker isn't working, please configure the OpenSearch hosts property (%s).",
                    String.join(" and ", hostsProperties));
            return;
        }

        if (buildItemsConfig.version != null) {
            String containerTag = config.imageName().substring(config.imageName().indexOf(':') + 1);
            if (!containerTag.startsWith(buildItemsConfig.version)) {
                throw new BuildException("Dev services for OpenSearch version mismatch: container image "
                        + config.imageName() + " vs expected version " + buildItemsConfig.version,
                        Collections.emptyList());
            }
        }

        DevServicesResultBuildItem discovered = openSearchContainerLocator
                .locateContainer(config.serviceName(), config.shared(), launchMode.getLaunchMode())
                .map(addr -> DevServicesResultBuildItem.discovered()
                        .feature(OpenSearchDevServicesProcessor.FEATURE)
                        .containerId(addr.getId())
                        .config(hostsConfig(hostsProperties, addr.getUrl().replaceFirst("^http://", "")))
                        .build())
                .orElse(null);

        if (discovered != null) {
            devServicesResult.produce(discovered);
            return;
        }

        // The startable supplier only references OpenSearchContainerStarter, so the
        // testcontainers types are not needed on the deployment classpath unless Dev
        // Services actually start a container. See #557.
        devServicesResult.produce(DevServicesResultBuildItem.owned()
                .feature(OpenSearchDevServicesProcessor.FEATURE)
                .serviceName(config.serviceName())
                .serviceConfig(config)
                .startable(() -> OpenSearchContainerStarter.create(
                        config.imageName(),
                        DEV_SERVICE_LABEL,
                        launchMode.getLaunchMode(),
                        config.serviceName(),
                        config.port(),
                        devServicesConfig.timeout(),
                        config.javaOpts()))
                .configProvider(s -> hostsConfig(hostsProperties, s.getConnectionInfo()))
                .build());
    }

    private static Map<String, String> hostsConfig(Set<String> hostsProperties, String host) {
        Map<String, String> propertiesToSet = new HashMap<>();
        for (String property : hostsProperties) {
            propertiesToSet.put(property, host);
        }
        return propertiesToSet;
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
