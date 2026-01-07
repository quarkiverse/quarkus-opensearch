package io.quarkiverse.opensearch;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.SSLContext;

import io.quarkus.tls.TlsConfiguration;

/**
 * A wrapper for {@link SSLContext} that supports certificate reloading via the Quarkus TLS Registry.
 * <p>
 * This class provides thread-safe access to an SSLContext that can be reloaded when certificates
 * are rotated, without requiring application restart. This is particularly useful in Kubernetes
 * environments where certificates are managed by cert-manager or similar tools.
 * <p>
 * Usage:
 *
 * <pre>
 * ReloadableSSLContext reloadable = ReloadableSSLContext.from(tlsConfiguration);
 *
 * // Get current SSLContext
 * SSLContext ctx = reloadable.get();
 *
 * // Reload certificates (typically called by a scheduled task or file watcher)
 * reloadable.reload();
 * </pre>
 *
 * @see io.quarkus.tls.TlsConfiguration
 * @see io.quarkus.tls.TlsConfigurationRegistry
 */
public class ReloadableSSLContext {

    private final TlsConfiguration tlsConfiguration;
    private final AtomicReference<SSLContext> currentContext;
    private final boolean reloadable;

    private ReloadableSSLContext(TlsConfiguration tlsConfiguration, SSLContext initialContext, boolean reloadable) {
        this.tlsConfiguration = tlsConfiguration;
        this.currentContext = new AtomicReference<>(initialContext);
        this.reloadable = reloadable;
    }

    /**
     * Create a reloadable SSLContext from a Quarkus TLS configuration.
     *
     * @param tlsConfiguration the TLS configuration from the Quarkus TLS Registry
     * @return a reloadable SSLContext wrapper
     */
    public static ReloadableSSLContext from(TlsConfiguration tlsConfiguration) {
        try {
            SSLContext initialContext = tlsConfiguration.createSSLContext();
            return new ReloadableSSLContext(tlsConfiguration, initialContext, true);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SSLContext from TLS configuration", e);
        }
    }

    /**
     * Create a non-reloadable SSLContext wrapper from a static SSLContext.
     * <p>
     * Use this for SSLContexts created without the TLS Registry (e.g., manual keystore loading).
     *
     * @param staticContext the static SSLContext
     * @return a non-reloadable wrapper
     */
    public static ReloadableSSLContext nonReloadable(SSLContext staticContext) {
        return new ReloadableSSLContext(null, staticContext, false);
    }

    /**
     * Get the current SSLContext.
     * <p>
     * This method is thread-safe and returns the most recently loaded SSLContext.
     *
     * @return the current SSLContext
     */
    public SSLContext get() {
        return currentContext.get();
    }

    /**
     * Reload the SSLContext from the underlying TLS configuration.
     * <p>
     * This method triggers a reload of the TLS configuration (which re-reads certificates
     * from disk) and creates a new SSLContext. The new context is atomically swapped in,
     * so concurrent {@link #get()} calls remain thread-safe.
     * <p>
     * If this wrapper was created with a static SSLContext (non-reloadable), this method
     * does nothing.
     *
     * @return true if reload was successful, false if not reloadable or reload failed
     */
    public boolean reload() {
        if (!reloadable || tlsConfiguration == null) {
            return false;
        }

        try {
            // Trigger TLS configuration reload (re-reads certificates)
            tlsConfiguration.reload();
            // Create new SSLContext with reloaded certificates
            SSLContext newContext = tlsConfiguration.createSSLContext();
            currentContext.set(newContext);
            return true;
        } catch (Exception e) {
            // Log but don't throw - keep existing context working
            // Applications can check return value and handle accordingly
            return false;
        }
    }

    /**
     * Check if this SSLContext supports certificate reloading.
     *
     * @return true if backed by a TLS configuration that supports reload
     */
    public boolean isReloadable() {
        return reloadable;
    }

    /**
     * Get the underlying TLS configuration, if available.
     *
     * @return the TLS configuration, or empty if using a static SSLContext
     */
    public Optional<TlsConfiguration> getTlsConfiguration() {
        return Optional.ofNullable(tlsConfiguration);
    }
}
