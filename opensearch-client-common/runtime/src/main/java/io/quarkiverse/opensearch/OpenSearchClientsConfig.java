package io.quarkiverse.opensearch;

import java.util.Map;

import io.quarkus.runtime.annotations.ConfigDocMapKey;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefaults;
import io.smallrye.config.WithParentName;
import io.smallrye.config.WithUnnamedKey;

@ConfigMapping(prefix = "quarkus.opensearch")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface OpenSearchClientsConfig {

    String DEFAULT_CLIENT_KEY = "<default>";

    /**
     * Additional named OpenSearch clients.
     */
    @ConfigDocMapKey("client-name")
    @WithParentName
    @WithUnnamedKey(DEFAULT_CLIENT_KEY)
    @WithDefaults
    Map<String, OpenSearchClientConfig> namedClients();

    default OpenSearchClientConfig defaultClient() {
        if (namedClients() == null || namedClients().isEmpty()) {
            return null;
        }
        return namedClients().get(DEFAULT_CLIENT_KEY);
    }

}
