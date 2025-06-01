package io.quarkiverse.opensearch.deployment;

import java.util.Collection;

import io.quarkus.builder.item.MultiBuildItem;

public final class DevServicesOpenSearchBuildItem extends MultiBuildItem {
    private final Collection<String> hostsConfigProperty;

    private final String version;

    public DevServicesOpenSearchBuildItem(Collection<String> hostsConfigProperty) {
        this.hostsConfigProperty = hostsConfigProperty;
        this.version = null;
    }

    public DevServicesOpenSearchBuildItem(Collection<String> configItemName, String version) {
        this.hostsConfigProperty = configItemName;
        this.version = version;
    }

    public Collection<String> getHostsConfigProperties() {
        return hostsConfigProperty;
    }

    public String getVersion() {
        return version;
    }

}
