package io.quarkiverse.opensearch;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;

import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;

public class SSLContextHelper {

    // create custom when key store was provided or ssl verification is disabled
    public static final SSLContext createSSLContext(final OpenSearchConfig config)
            throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, KeyManagementException {
        final SSLContextBuilder sslContextBuilder = config.keyStoreFile().isPresent() || !config.sslVerify()
                ? SSLContexts.custom()
                : SSLContextBuilder.create();

        if (config.keyStoreFile().isPresent()) {
            // load from keystore
            final File file = new File(config.keyStoreFile().get());
            if (config.keyStorePassword().isPresent()) {
                sslContextBuilder.loadTrustMaterial(file, config.keyStorePassword().get().toCharArray());
            } else {
                sslContextBuilder.loadTrustMaterial(file);
            }
        }
        if (!config.sslVerify()) {
            // load trust all strategy
            sslContextBuilder.loadTrustMaterial(new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    return true;
                }
            });
        }
        return sslContextBuilder.build();
    }
}
