package io.quarkiverse.opensearch.transport.apache;

import java.net.SocketAddress;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.net.NamedEndpoint;
import org.apache.hc.core5.reactor.ssl.TransportSecurityLayer;
import org.apache.hc.core5.util.Timeout;

import io.quarkiverse.opensearch.OpenSearchClientConfig;
import io.quarkiverse.opensearch.ReloadableSSLContext;

/**
 * A TLS strategy that supports certificate reloading without requiring transport recreation.
 * <p>
 * This strategy delegates to the underlying {@link ReloadableSSLContext} which can be reloaded
 * when certificates are rotated. Each new SSL connection will use the current SSLContext from
 * the reloadable wrapper.
 * <p>
 * Note: Existing connections are not affected by certificate reloads. Only new connections
 * will use the updated certificates. For immediate effect, applications should close idle
 * connections after a reload.
 *
 * @see ReloadableSSLContext
 */
public class ReloadableTlsStrategy implements TlsStrategy {

    private final ReloadableSSLContext reloadableSSLContext;
    private final boolean verifyHostname;
    private volatile TlsStrategy delegate;

    /**
     * Create a reloadable TLS strategy.
     *
     * @param reloadableSSLContext the reloadable SSL context wrapper
     * @param verifyHostname whether to verify hostnames in certificates
     */
    public ReloadableTlsStrategy(ReloadableSSLContext reloadableSSLContext, boolean verifyHostname) {
        this.reloadableSSLContext = reloadableSSLContext;
        this.verifyHostname = verifyHostname;
        this.delegate = buildDelegate(reloadableSSLContext.get());
    }

    /**
     * Create a reloadable TLS strategy from configuration.
     *
     * @param reloadableSSLContext the reloadable SSL context wrapper
     * @param config the OpenSearch client configuration
     * @return a new ReloadableTlsStrategy
     */
    @SuppressWarnings("deprecation")
    public static ReloadableTlsStrategy create(ReloadableSSLContext reloadableSSLContext, OpenSearchClientConfig config) {
        boolean verifyHostname = config.sslVerifyHostname() && config.sslVerify();
        return new ReloadableTlsStrategy(reloadableSSLContext, verifyHostname);
    }

    /**
     * Reload the TLS configuration.
     * <p>
     * This triggers a reload of the underlying certificates and recreates the delegate
     * TLS strategy. New connections will use the updated certificates.
     *
     * @return true if reload was successful
     */
    public boolean reload() {
        if (reloadableSSLContext.reload()) {
            this.delegate = buildDelegate(reloadableSSLContext.get());
            return true;
        }
        return false;
    }

    /**
     * Check if this strategy supports certificate reloading.
     *
     * @return true if backed by a reloadable SSL context
     */
    public boolean isReloadable() {
        return reloadableSSLContext.isReloadable();
    }

    /**
     * Get the underlying reloadable SSL context.
     *
     * @return the reloadable SSL context
     */
    public ReloadableSSLContext getReloadableSSLContext() {
        return reloadableSSLContext;
    }

    private TlsStrategy buildDelegate(SSLContext sslContext) {
        ClientTlsStrategyBuilder builder = ClientTlsStrategyBuilder.create()
                .setSslContext(sslContext);
        if (!verifyHostname) {
            builder.setHostnameVerifier(NoopHostnameVerifier.INSTANCE);
        }
        return builder.build();
    }

    // Delegate all TlsStrategy methods to the current delegate

    @SuppressWarnings("deprecation")
    @Override
    public boolean upgrade(
            TransportSecurityLayer tlsSession,
            HttpHost host,
            SocketAddress localAddress,
            SocketAddress remoteAddress,
            Object attachment,
            Timeout handshakeTimeout) {
        return delegate.upgrade(tlsSession, host, localAddress, remoteAddress, attachment, handshakeTimeout);
    }

    @Override
    public void upgrade(
            TransportSecurityLayer tlsSession,
            NamedEndpoint endpoint,
            Object attachment,
            Timeout handshakeTimeout,
            FutureCallback<TransportSecurityLayer> callback) {
        delegate.upgrade(tlsSession, endpoint, attachment, handshakeTimeout, callback);
    }
}
