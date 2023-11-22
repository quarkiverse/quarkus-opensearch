package io.quarkiverse.opensearch.deployment;

import io.quarkus.builder.item.MultiBuildItem;

public final class DevServicesOpenSearchBuildItem extends MultiBuildItem {
    private final String hostsConfigProperty;

    private final String version;

    public DevServicesOpenSearchBuildItem(String hostsConfigProperty) {
        this.hostsConfigProperty = hostsConfigProperty;
        this.version = null;
    }

    public DevServicesOpenSearchBuildItem(String configItemName, String version) {
        this.hostsConfigProperty = configItemName;
        this.version = version;
    }

    public String getHostsConfigProperty() {
        return hostsConfigProperty;
    }

    public String getVersion() {
        return version;
    }

}
