package io.quarkiverse.opensearch;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.X509Certificate;

import javax.net.ssl.*;

public class SSLContextHelper {

    public static SSLContext createSSLContext(OpenSearchConfig config)
            throws GeneralSecurityException, IOException {

        SSLContext sslContext = SSLContext.getInstance("TLS");
        TrustManager[] trustManagers;

        // If hostname verification is disabled, use a permissive TrustManager
        if (!config.sslVerify()) {
            trustManagers = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            };
        } else if (config.keyStoreFile().isPresent()) {
            // Load custom trust store
            String keyStorePath = config.keyStoreFile().get();
            String keyStorePassword = config.keyStorePassword().orElse(null);

            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            try (FileInputStream fis = new FileInputStream(keyStorePath)) {
                trustStore.load(fis, keyStorePassword != null ? keyStorePassword.toCharArray() : null);
            }

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);
            trustManagers = tmf.getTrustManagers();
        } else {
            // Use system default trust store
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init((KeyStore) null);
            trustManagers = tmf.getTrustManagers();
        }

        sslContext.init(null, trustManagers, new SecureRandom());
        return sslContext;
    }
}
