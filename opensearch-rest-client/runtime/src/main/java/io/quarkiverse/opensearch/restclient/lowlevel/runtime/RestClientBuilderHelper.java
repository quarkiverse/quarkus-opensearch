package io.quarkiverse.opensearch.restclient.lowlevel.runtime;

import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.conn.NoopIOSessionStrategy;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.jboss.logging.Logger;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.sniff.NodesSniffer;
import org.opensearch.client.sniff.OpenSearchNodesSniffer;
import org.opensearch.client.sniff.Sniffer;
import org.opensearch.client.sniff.SnifferBuilder;

import io.quarkiverse.opensearch.OpenSearchConfig;
import io.quarkiverse.opensearch.SSLContextHelper;
import io.quarkiverse.opensearch.restclient.OpenSearchClientConfig;
import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import io.quarkus.logging.Log;

public final class RestClientBuilderHelper {

    private static final Logger LOG = Logger.getLogger(RestClientBuilderHelper.class);

    private RestClientBuilderHelper() {
        // avoid instantiation
    }

    public static RestClientBuilder createRestClientBuilder(OpenSearchConfig config) {
        List<HttpHost> hosts = new ArrayList<>();
        if (config.hosts().isPresent()) {
            for (String host : config.hosts().get()) {
                String[] h = host.split(":");
                hosts.add(new HttpHost(h[0], Integer.valueOf(h[1]), config.protocol()));
            }
        }

        RestClientBuilder builder = RestClient.builder(hosts.toArray(new HttpHost[0]));

        builder.setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder
                .setConnectTimeout((int) config.connectionTimeout().toMillis())
                .setSocketTimeout((int) config.socketTimeout().toMillis())
                .setConnectionRequestTimeout(0));

        builder.setHttpClientConfigCallback(httpClientBuilder -> {
            if (config.username().isPresent()) {
                if (!"https".equalsIgnoreCase(config.protocol())) {
                    LOG.warn("Using Basic authentication in HTTP implies sending plain text passwords over the wire, " +
                            "use the HTTPS protocol instead.");
                }
                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(AuthScope.ANY,
                        new UsernamePasswordCredentials(config.username().get(), config.password().orElse(null)));
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }

            if (config.ioThreadCounts().isPresent()) {
                IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                        .setIoThreadCount(config.ioThreadCounts().get())
                        .build();
                httpClientBuilder.setDefaultIOReactorConfig(ioReactorConfig);
            }

            httpClientBuilder.setMaxConnTotal(config.maxConnections());
            httpClientBuilder.setMaxConnPerRoute(config.maxConnectionsPerRoute());

            if ("http".equalsIgnoreCase(config.protocol())) {
                // In this case disable the SSL capability as it might have an impact on
                // bootstrap time, for example consuming entropy for no reason
                httpClientBuilder.setSSLStrategy(NoopIOSessionStrategy.INSTANCE);
            }

            // setup SSLContext
            try {
                final SSLContext sslContext = SSLContextHelper.createSSLContext(config);
                httpClientBuilder.setSSLContext(sslContext);
                // setup ssl verification
                if (!config.sslVerifyHostname() || !config.sslVerify()) {
                    final HostnameVerifier hostnameVerifier = new NoopHostnameVerifier();
                    final SchemeIOSessionStrategy sessionStrategy = new SSLIOSessionStrategy(sslContext, hostnameVerifier);
                    httpClientBuilder.setSSLHostnameVerifier(hostnameVerifier);
                    httpClientBuilder.setSSLStrategy(sessionStrategy);
                }
            } catch (Exception e) {
                Log.error(e.getMessage(), e);
            }

            // Apply configuration from RestClientBuilder.HttpClientConfigCallback implementations annotated with OpenSearchClientConfig
            HttpAsyncClientBuilder result = httpClientBuilder;
            Iterable<InstanceHandle<RestClientBuilder.HttpClientConfigCallback>> handles = Arc.container()
                    .select(RestClientBuilder.HttpClientConfigCallback.class, new OpenSearchClientConfig.Literal())
                    .handles();
            for (InstanceHandle<RestClientBuilder.HttpClientConfigCallback> handle : handles) {
                result = handle.get().customizeHttpClient(result);
                handle.close();
            }
            return result;
        });

        return builder;
    }

    public static Sniffer createSniffer(RestClient client, OpenSearchConfig config) {
        SnifferBuilder builder = Sniffer.builder(client)
                .setSniffIntervalMillis((int) config.discovery().refreshInterval().toMillis());

        // https discovery support
        if ("https".equalsIgnoreCase(config.protocol())) {
            NodesSniffer hostsSniffer = new OpenSearchNodesSniffer(
                    client,
                    OpenSearchNodesSniffer.DEFAULT_SNIFF_REQUEST_TIMEOUT, // 1sec
                    OpenSearchNodesSniffer.Scheme.HTTPS);
            builder.setNodesSniffer(hostsSniffer);
        }

        return builder.build();
    }
}
