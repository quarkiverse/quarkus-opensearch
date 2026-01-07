package io.quarkiverse.opensearch;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;

import io.quarkus.tls.TlsConfigurationRegistry;

/**
 * Utility class for creating an {@link SSLContext} based on {@link OpenSearchClientConfig} settings.
 *
 * <p>
 * This helper supports three mechanisms for SSL context creation:
 * </p>
 * <ul>
 * <li>Integration with the Quarkus TLS Registry (if available), allowing reuse of configured TLS contexts</li>
 * <li>Custom trust store loading from a provided keystore file</li>
 * <li>Fallback to system default trust manager, or optionally disable SSL verification</li>
 * </ul>
 *
 * <p>
 * If TLS support is configured via {@code TlsConfigurationRegistry}, it will take precedence over manual options.
 * </p>
 *
 * <p>
 * For certificate reload support, use {@link #createReloadableSSLContext(OpenSearchClientConfig)} which
 * returns a {@link ReloadableSSLContext} that can refresh certificates without application restart.
 * </p>
 *
 * <p>
 * This class is used internally to configure secure connections to OpenSearch endpoints.
 * </p>
 *
 * @see ReloadableSSLContext
 */
public class SSLContextHelper {

    /**
     * Creates a {@link ReloadableSSLContext} that supports certificate reloading.
     * <p>
     * When using the Quarkus TLS Registry, the returned context can be reloaded to pick up
     * rotated certificates. For static configurations (keystore files), the context is
     * non-reloadable but wrapped for API consistency.
     *
     * @param config The OpenSearch client configuration
     * @return A reloadable SSLContext wrapper
     * @throws GeneralSecurityException If SSL setup fails
     * @throws IOException If keystore files cannot be read
     */
    public static ReloadableSSLContext createReloadableSSLContext(OpenSearchClientConfig config)
            throws GeneralSecurityException, IOException {
        // Try to use TLS configuration from the Quarkus TLS registry, if available
        final Instance<TlsConfigurationRegistry> certs = CDI.current().select(TlsConfigurationRegistry.class);
        if (certs.isResolvable()) {
            try {
                // Use named TLS configuration if defined
                if (config.tls().isPresent()) {
                    var cert = certs.get().get(config.tls().get().tlsConfigurationName());
                    if (cert.isPresent()) {
                        return ReloadableSSLContext.from(cert.get());
                    }
                    throw new RuntimeException(
                            "unable to find TLS configuration for " + config.tls().get().tlsConfigurationName());
                }
                // Use default TLS configuration if no name is specified
                else if (certs.get().getDefault().isPresent()) {
                    return ReloadableSSLContext.from(certs.get().getDefault().get());
                }
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // Fallback: create non-reloadable SSLContext
        return ReloadableSSLContext.nonReloadable(createSSLContextInternal(config));
    }

    /**
     * Creates an {@link SSLContext} instance based on the provided OpenSearch client configuration.
     *
     * @param config The OpenSearch client configuration, including SSL/TLS and keystore options.
     * @return A configured SSLContext ready to be used for HTTP clients.
     * @throws GeneralSecurityException If SSL setup or keystore loading fails.
     * @throws IOException If the keystore file cannot be read.
     */
    public static SSLContext createSSLContext(OpenSearchClientConfig config) throws GeneralSecurityException, IOException {
        // Try to use TLS configuration from the Quarkus TLS registry, if available
        final Instance<TlsConfigurationRegistry> certs = CDI.current().select(TlsConfigurationRegistry.class);
        if (certs.isResolvable()) {
            try {
                // Use named TLS configuration if defined
                if (config.tls().isPresent()) {
                    var cert = certs.get().get(config.tls().get().tlsConfigurationName());
                    if (cert.isPresent()) {
                        return cert.get().createSSLContext();
                    }
                    throw new RuntimeException(
                            "unable to find TLS configuration for " + config.tls().get().tlsConfigurationName());
                }
                // Use default TLS configuration if no name is specified
                else if (certs.get().getDefault().isPresent()) {
                    return certs.get().getDefault().get().createSSLContext();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // Fallback: create SSLContext manually
        return createSSLContextInternal(config);
    }

    /**
     * Internal method to create SSLContext without TLS Registry lookup.
     * Used by both createSSLContext and createReloadableSSLContext for fallback scenarios.
     */
    private static SSLContext createSSLContextInternal(OpenSearchClientConfig config)
            throws GeneralSecurityException, IOException {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        TrustManager[] trustManagers;

        // If SSL verification is disabled, use permissive TrustManager that accepts all certs
        if (!config.sslVerify()) {
            trustManagers = new TrustManager[] { new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    // No-op: trust all clients
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    // No-op: trust all servers
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            } };
        }
        // If a keystore is specified, load trust managers from it
        else if (config.keyStoreFile().isPresent()) {
            String keyStorePath = config.keyStoreFile().get();
            String keyStorePassword = config.keyStorePassword().orElse(null);

            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            try (FileInputStream fis = new FileInputStream(keyStorePath)) {
                trustStore.load(fis, keyStorePassword != null ? keyStorePassword.toCharArray() : null);
            }

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);
            trustManagers = tmf.getTrustManagers();
        }
        // Default fallback: use system trust store
        else {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init((KeyStore) null);
            trustManagers = tmf.getTrustManagers();
        }

        // Initialize and return SSLContext with resolved trust managers
        sslContext.init(null, trustManagers, new SecureRandom());
        return sslContext;
    }
}
