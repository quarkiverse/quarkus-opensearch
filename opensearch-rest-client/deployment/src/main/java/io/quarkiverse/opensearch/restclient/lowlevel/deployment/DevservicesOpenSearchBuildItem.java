package io.quarkiverse.opensearch.restclient.lowlevel.deployment;

import io.quarkus.builder.item.MultiBuildItem;

public final class DevservicesOpenSearchBuildItem extends MultiBuildItem {
    private final String hostsConfigProperty;

    private final String version;

    public DevservicesOpenSearchBuildItem(String hostsConfigProperty) {
        this.hostsConfigProperty = hostsConfigProperty;
        this.version = null;
    }

    public DevservicesOpenSearchBuildItem(String configItemName, String version) {
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
