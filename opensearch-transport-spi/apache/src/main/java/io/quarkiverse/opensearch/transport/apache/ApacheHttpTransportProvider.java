package io.quarkiverse.opensearch.transport.apache;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.util.Timeout;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkiverse.opensearch.OpenSearchClientConfig;
import io.quarkiverse.opensearch.ReloadableSSLContext;
import io.quarkiverse.opensearch.SSLContextHelper;
import io.quarkiverse.opensearch.transport.OpenSearchTransportConfig;
import io.quarkiverse.opensearch.transport.spi.OpenSearchTransportProvider;
import io.quarkiverse.opensearch.transport.spi.OpenSearchTransportProviderException;
import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import io.quarkus.logging.Log;
import io.vertx.core.spi.VertxThreadFactory;

public class ApacheHttpTransportProvider implements OpenSearchTransportProvider {

    @Override
    public boolean supports(OpenSearchClientConfig config) {
        return !config.awsService().isPresent();
    }

    @Override
    public OpenSearchTransport create(OpenSearchClientConfig config, ObjectMapper objectMapper)
            throws OpenSearchTransportProviderException {
        List<HttpHost> list = new ArrayList<>();
        for (String s : config.hosts().orElse(List.of("127.0.0.1:9200"))) {
            String[] h = s.split(":");
            if (h.length != 2) {
                throw new OpenSearchTransportProviderException("Invalid host format (expected host:port): " + s);
            }
            try {
                list.add(new HttpHost(config.protocol(), h[0], Integer.parseInt(h[1])));
            } catch (NumberFormatException e) {
                throw new OpenSearchTransportProviderException("Invalid port in host definition: " + s, e);
            }
        }

        final HttpHost[] hosts = list.toArray(new HttpHost[0]);
        final ApacheHttpClient5TransportBuilder builder = ApacheHttpClient5TransportBuilder.builder(hosts);
        builder.setMapper(new JacksonJsonpMapper(objectMapper));

        try {
            final ReloadableSSLContext reloadableSSLContext = SSLContextHelper.createReloadableSSLContext(config);
            final ReloadableTlsStrategy reloadableTlsStrategy = ReloadableTlsStrategy.create(reloadableSSLContext, config);

            // Register the reloadable strategy for potential certificate reload operations
            if (Arc.container() != null && reloadableSSLContext.isReloadable()) {
                registerReloadableTlsStrategy(reloadableTlsStrategy);
            }

            builder.setHttpClientConfigCallback(httpAsyncClientBuilder -> {
                final TlsStrategy tlsStrategy = reloadableTlsStrategy;

                final ConnectionConfig connectionConfig = ConnectionConfig.custom()
                        .setConnectTimeout(Timeout.of(config.connectionTimeout()))
                        .setSocketTimeout(Timeout.of(config.socketTimeout()))
                        .build();

                final PoolingAsyncClientConnectionManager connectionManager = PoolingAsyncClientConnectionManagerBuilder
                        .create()
                        .setTlsStrategy(tlsStrategy)
                        .setMaxConnPerRoute(config.maxConnectionsPerRoute())
                        .setMaxConnTotal(config.maxConnections())
                        .setDefaultConnectionConfig(connectionConfig)
                        .build();

                String username = config.username().orElse(null);
                String password = config.password().orElse(null);
                if (username != null && password != null) {
                    if (!"https".equalsIgnoreCase(config.protocol())) {
                        Log.warn(
                                "Using Basic authentication in HTTP implies sending plain text passwords over the wire, use the HTTPS protocol instead.");
                    }
                    BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                    credentialsProvider.setCredentials(new AuthScope(null, -1),
                            new UsernamePasswordCredentials(username, password.toCharArray()));
                    httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                }

                HttpAsyncClientBuilder result = httpAsyncClientBuilder.setConnectionManager(connectionManager);

                if (VertxThreadFactory.INSTANCE != null) {
                    result.setThreadFactory(new ThreadFactory() {
                        @Override
                        public Thread newThread(Runnable r) {
                            return VertxThreadFactory.INSTANCE.newVertxThread(r,
                                    "HttpClient5OpenSearchTransport", true,
                                    config.threadTimeout().getSeconds(), TimeUnit.SECONDS);
                        }
                    });
                }

                if (Arc.container() != null) {
                    final Iterable<InstanceHandle<ApacheHttpClient5TransportBuilder.HttpClientConfigCallback>> httpClientConfigCallbackHandles = Arc
                            .container()
                            .select(ApacheHttpClient5TransportBuilder.HttpClientConfigCallback.class,
                                    new OpenSearchTransportConfig.Literal())
                            .handles();
                    for (InstanceHandle<ApacheHttpClient5TransportBuilder.HttpClientConfigCallback> handle : httpClientConfigCallbackHandles) {
                        result = handle.get().customizeHttpClient(result);
                        handle.close();
                    }
                }
                return result;
            });
            if (Arc.container() != null) {
                builder.setRequestConfigCallback(requestConfigBuilder -> {
                    RequestConfig.Builder result = requestConfigBuilder;
                    final Iterable<InstanceHandle<ApacheHttpClient5TransportBuilder.RequestConfigCallback>> requestConfigBuilderHandles = Arc
                            .container()
                            .select(ApacheHttpClient5TransportBuilder.RequestConfigCallback.class,
                                    new OpenSearchTransportConfig.Literal())
                            .handles();
                    for (InstanceHandle<ApacheHttpClient5TransportBuilder.RequestConfigCallback> handle : requestConfigBuilderHandles) {
                        result = handle.get().customizeRequestConfig(result);
                        handle.close();
                    }
                    return result;
                });
            }

            return builder.build();
        } catch (Exception e) {
            throw new OpenSearchTransportProviderException("Unable to create OpenSearchTransport", e);
        }
    }

    @Override
    public String name() {
        return "apache-http-client5";
    }

    // --- Certificate Reload Support ---

    private static final List<ReloadableTlsStrategy> reloadableStrategies = new ArrayList<>();

    /**
     * Register a reloadable TLS strategy for certificate reload operations.
     */
    private static synchronized void registerReloadableTlsStrategy(ReloadableTlsStrategy strategy) {
        reloadableStrategies.add(strategy);
    }

    /**
     * Reload certificates for all registered TLS strategies.
     * <p>
     * This method triggers a certificate reload for all OpenSearch connections that were
     * created with a reloadable TLS configuration. New connections will use the updated
     * certificates; existing connections are not affected.
     * <p>
     * Typically called in response to a certificate update event or on a scheduled basis.
     *
     * @return the number of strategies that were successfully reloaded
     */
    public static synchronized int reloadAllCertificates() {
        int reloaded = 0;
        for (ReloadableTlsStrategy strategy : reloadableStrategies) {
            if (strategy.reload()) {
                reloaded++;
                Log.info("Reloaded TLS certificates for OpenSearch connection");
            }
        }
        return reloaded;
    }

    /**
     * Get the list of registered reloadable TLS strategies.
     * <p>
     * This can be used to manually trigger reloads or check reload status.
     *
     * @return unmodifiable list of reloadable strategies
     */
    public static synchronized List<ReloadableTlsStrategy> getReloadableStrategies() {
        return List.copyOf(reloadableStrategies);
    }
}
