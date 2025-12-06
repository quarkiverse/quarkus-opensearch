package io.quarkiverse.opensearch.deployment;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import io.quarkus.deployment.annotations.BuildStep;

class OpenSearchDevServicesProcessor {

    static final String FEATURE = "opensearch-common";

    private static final String PREFIX = "quarkus.opensearch";

    @BuildStep
    DevServicesOpenSearchBuildItem devServices() {

        final Set<String> configuredClients = findAllClientNames().stream()
                .map(s -> String.format("quarkus.opensearch.%s.hosts", s))
                .collect(Collectors.toSet());
        configuredClients.add("quarkus.opensearch.hosts");
        return new DevServicesOpenSearchBuildItem(configuredClients);
    }

    /**
     * Finds all client names based on config keys 2 dots after the "quarkus.opensearch" prefix.
     */
    private Set<String> findAllClientNames() {
        Config config = ConfigProvider.getConfig();
        Set<String> clientNames = new HashSet<>();

        for (String propertyName : config.getPropertyNames()) {
            if (propertyName.startsWith(PREFIX) &&
                    !propertyName.startsWith("quarkus.opensearch.devservices") &&
                    !propertyName.startsWith("quarkus.opensearch.health") &&
                    !propertyName.startsWith("quarkus.opensearch.aws") &&
                    !propertyName.startsWith("quarkus.opensearch.discovery") &&
                    !propertyName.startsWith("quarkus.opensearch.ssl") &&
                    !propertyName.startsWith("quarkus.opensearch.tls")) {
                String stripped = propertyName.substring(PREFIX.length());
                int dotCount = stripped.length() - stripped.replace(".", "").length();

                if (dotCount > 1) {
                    int dotIndex = stripped.indexOf('.', 1);
                    if (dotIndex > 0) {
                        String clientName = stripped.substring(
                                1, dotIndex);
                        clientNames.add(clientName);
                    }
                }
            }
        }

        return clientNames;
    }
}
