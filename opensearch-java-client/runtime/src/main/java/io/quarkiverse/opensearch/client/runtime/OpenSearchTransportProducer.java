package io.quarkiverse.opensearch.client.runtime;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContextBuilder;
import org.opensearch.client.RestClient;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.aws.AwsSdk2Transport;
import org.opensearch.client.transport.aws.AwsSdk2TransportOptions;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;
import org.opensearch.client.transport.rest_client.RestClientTransport;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkiverse.opensearch.OpenSearchConfig;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;

@ApplicationScoped
public class OpenSearchTransportProducer {

    private final Instance<RestClient> restClient;

    private final Instance<ObjectMapper> objectMappers;

    private final OpenSearchConfig config;

    private OpenSearchTransport transport;

    public OpenSearchTransportProducer(final Instance<RestClient> restClient, final Instance<ObjectMapper> objectMappers,
            final OpenSearchConfig config) {
        this.restClient = restClient;
        this.objectMappers = objectMappers;
        this.config = config;
    }

    @Produces
    @Singleton
    public OpenSearchTransport openSearchTransport() throws Exception {
        final Optional<ObjectMapper> objectMapper = objectMappers.stream().findFirst();
        final JacksonJsonpMapper jacksonJsonpMapper = objectMapper.isPresent() ? new JacksonJsonpMapper(objectMapper.get())
                : new JacksonJsonpMapper();
        if (restClient.isResolvable()) {
            return new RestClientTransport(restClient.get(), jacksonJsonpMapper);
        }
        if (config.aws().isPresent()) {
            return createAwsSdk2Transport(config.aws().get());
        }
        return createApacheHttpClient5Transport();
    }

    private OpenSearchTransport createApacheHttpClient5Transport() throws Exception {
        final List<HttpHost> hosts = config.hosts()
                .orElse(List.of(InetSocketAddress.createUnresolved("127.0.0.1", 9200))).stream()
                .map(h -> new HttpHost(config.protocol(), h.getAddress(), h.getPort())).collect(Collectors.toList());
        final ApacheHttpClient5TransportBuilder builder = ApacheHttpClient5TransportBuilder
                .builder(hosts.toArray(new HttpHost[0]));
        final SSLContext sslContext = SSLContextBuilder.create().build();
        builder.setHttpClientConfigCallback(httpClientBuilder -> {
            final TlsStrategy tlsStrategy = ClientTlsStrategyBuilder.create()
                    .setSslContext(sslContext)
                    .build();

            final PoolingAsyncClientConnectionManager connectionManager = PoolingAsyncClientConnectionManagerBuilder
                    .create()
                    .setTlsStrategy(tlsStrategy)
                    .build();

            if (config.username().isPresent() && config.password().isPresent()) {
                final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(AuthScope.ANY,
                        new UsernamePasswordCredentials(config.username().get(), config.password().get()));
                httpClientBuilder
                        .setDefaultCredentialsProvider((CredentialsProvider) credentialsProvider);
            }
            return httpClientBuilder
                    .setConnectionManager(connectionManager);
        });
        return builder.build();
    }

    public AwsSdk2Transport createAwsSdk2Transport(final OpenSearchConfig.AWSConfig config) {
        final SdkAsyncHttpClient nettyHttpClient = NettyNioAsyncHttpClient.create();
        return new AwsSdk2Transport(
                nettyHttpClient,
                config.endpoint().orElse(""),
                config.service(),
                Region.of(config.region()),
                AwsSdk2TransportOptions.builder().build());
    }

    @PreDestroy
    void destroy() {
        try {
            if (this.transport != null) {
                this.transport.close();
            }
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

}
