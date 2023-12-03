package io.quarkiverse.opensearch.client.runtime;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;

import jakarta.enterprise.inject.Instance;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.util.Timeout;
import org.jboss.logging.Logger;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.transport.aws.AwsSdk2Transport;
import org.opensearch.client.transport.aws.AwsSdk2TransportOptions;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5Transport;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkiverse.opensearch.OpenSearchConfig;
import io.quarkiverse.opensearch.client.AwsSdk2TransportOptionsCallback;
import io.quarkiverse.opensearch.client.OpenSearchTransportConfig;
import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;

public final class OpenSearchTransportHelper {

    private OpenSearchTransportHelper() {
        // private constructor
    }

    private static final Logger LOG = Logger.getLogger(OpenSearchTransportHelper.class);

    public static ApacheHttpClient5Transport createApacheHttpClient5Transport(final OpenSearchConfig config,
            final Instance<ObjectMapper> objectMappers)
            throws NoSuchAlgorithmException, KeyManagementException {
        List<HttpHost> list = new ArrayList<>();
        for (String s : config.hosts()
                .orElse(List.of("127.0.0.1:9200"))) {
            String[] h = s.split(":");
            HttpHost apply = new HttpHost(config.protocol(), h[0], Integer.valueOf(h[1]));
            list.add(apply);
        }

        final HttpHost[] hosts = list.toArray(new HttpHost[0]);
        final ApacheHttpClient5TransportBuilder builder = ApacheHttpClient5TransportBuilder
                .builder(hosts);

        // use existing ObjectMapper or create new ObjectMapper and register all modules
        final ObjectMapper objectMapper = objectMappers.stream().findFirst()
                .orElse(new ObjectMapper().findAndRegisterModules());
        builder.setMapper(new JacksonJsonpMapper(objectMapper));

        final SSLContext sslContext = SSLContextBuilder.create().build();

        builder.setHttpClientConfigCallback(httpAsyncClientBuilder -> {

            final TlsStrategy tlsStrategy = ClientTlsStrategyBuilder.create()
                    .setSslContext(sslContext)
                    .build();
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

            if (config.username().isPresent() && config.password().isPresent()) {
                if (!"https".equalsIgnoreCase(config.protocol())) {
                    LOG.warn("Using Basic authentication in HTTP implies sending plain text passwords over the wire, " +
                            "use the HTTPS protocol instead.");
                }
                BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(new AuthScope(null, -1),
                        new UsernamePasswordCredentials(config.username().get(), config.password().get().toCharArray()));
                httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }

            // Apply configuration from ApacheHttpClient5TransportBuilder.HttpClientConfigCallback implementations annotated with OpenSearchTransportConfig
            HttpAsyncClientBuilder result = httpAsyncClientBuilder.setConnectionManager(connectionManager);
            final Iterable<InstanceHandle<ApacheHttpClient5TransportBuilder.HttpClientConfigCallback>> httpClientConfigCallbackHandles = Arc
                    .container()
                    .select(ApacheHttpClient5TransportBuilder.HttpClientConfigCallback.class,
                            new OpenSearchTransportConfig.Literal())
                    .handles();
            for (InstanceHandle<ApacheHttpClient5TransportBuilder.HttpClientConfigCallback> handle : httpClientConfigCallbackHandles) {
                result = handle.get().customizeHttpClient(result);
                handle.close();
            }
            return result;
        });

        // Apply configuration from ApacheHttpClient5TransportBuilder.RequestConfigCallback implementations annotated with OpenSearchTransportConfig
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
        return builder.build();
    }

    public static AwsSdk2Transport createAwsSdk2Transport(final OpenSearchConfig config,
            final Instance<ObjectMapper> objectMappers) {
        final SdkAsyncHttpClient nettyHttpClient = NettyNioAsyncHttpClient.create();
        AwsSdk2TransportOptions.Builder options = AwsSdk2TransportOptions.builder();

        // use existing ObjectMapper or create new ObjectMapper and register all modules
        final ObjectMapper objectMapper = objectMappers.stream().findFirst()
                .orElse(new ObjectMapper().findAndRegisterModules());
        options.setMapper(new JacksonJsonpMapper(objectMapper));

        if (config.accessKeyId().isPresent() && config.secretAccessKey().isPresent()) {
            options.setCredentials(AwsCredentialsProviderChain.of(
                    StaticCredentialsProvider
                            .create(AwsBasicCredentials.create(config.accessKeyId().get(), config.secretAccessKey().get())),
                    DefaultCredentialsProvider.create()));
        } else {
            options.setCredentials(DefaultCredentialsProvider.create());
        }

        // Apply configuration from AwsSdk2TransportOptionsCallback implementations
        final Iterable<InstanceHandle<AwsSdk2TransportOptionsCallback>> handles = Arc.container()
                .select(AwsSdk2TransportOptionsCallback.class, new OpenSearchTransportConfig.Literal())
                .handles();
        for (InstanceHandle<AwsSdk2TransportOptionsCallback> handle : handles) {
            options = handle.get().customize(options);
            handle.close();
        }
        return new AwsSdk2Transport(
                nettyHttpClient,
                config.hosts().orElse(List.of("")).get(0),
                config.awsService().get(),
                Region.of(config.awsRegion()),
                options.build());
    }

}
