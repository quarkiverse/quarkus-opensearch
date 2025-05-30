package io.quarkiverse.opensearch.client.runtime;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.opensearch.client.opensearch.OpenSearchAsyncClient;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.OpenSearchTransport;

import io.quarkiverse.opensearch.OpenSearchClientConfig;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class OpenSearchClientsRecorder {

    private final Map<OpenSearchClientConfig, OpenSearchTransport> transportMap = new ConcurrentHashMap<>();

    private OpenSearchTransport getOrCreateTransport(OpenSearchClientConfig config) {
        OpenSearchTransport t = transportMap.get(config);
        if (Objects.isNull(t)) {
            t = OpenSearchTransportHelper.createTransport(config);
            transportMap.put(config, t);
        }
        return t;
    }

    public OpenSearchTransport transport(OpenSearchClientConfig config) {
        return getOrCreateTransport(config);
    }

    public OpenSearchClient client(OpenSearchClientConfig config) {
        return new OpenSearchClient(getOrCreateTransport(config));
    }

    public OpenSearchAsyncClient asyncClient(OpenSearchClientConfig config) {
        return new OpenSearchAsyncClient(getOrCreateTransport(config));
    }

    public OpenSearchClientConfig config(OpenSearchClientConfig config) {
        return config;
    }
}
