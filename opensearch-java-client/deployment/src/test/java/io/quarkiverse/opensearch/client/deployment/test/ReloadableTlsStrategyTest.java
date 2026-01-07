package io.quarkiverse.opensearch.client.deployment.test;

import static org.junit.jupiter.api.Assertions.*;

import javax.net.ssl.SSLContext;

import org.junit.jupiter.api.Test;

import io.quarkiverse.opensearch.ReloadableSSLContext;
import io.quarkiverse.opensearch.transport.apache.ReloadableTlsStrategy;

/**
 * Unit tests for {@link ReloadableTlsStrategy}.
 */
class ReloadableTlsStrategyTest {

    @Test
    void testCreateWithNonReloadableContext() throws Exception {
        SSLContext sslContext = SSLContext.getDefault();
        ReloadableSSLContext reloadable = ReloadableSSLContext.nonReloadable(sslContext);

        ReloadableTlsStrategy strategy = new ReloadableTlsStrategy(reloadable, true);

        assertNotNull(strategy);
        assertFalse(strategy.isReloadable(), "Strategy with non-reloadable context should not be reloadable");
    }

    @Test
    void testReloadOnNonReloadableReturnsFalse() throws Exception {
        SSLContext sslContext = SSLContext.getDefault();
        ReloadableSSLContext reloadable = ReloadableSSLContext.nonReloadable(sslContext);
        ReloadableTlsStrategy strategy = new ReloadableTlsStrategy(reloadable, true);

        assertFalse(strategy.reload(), "Reload on non-reloadable strategy should return false");
    }

    @Test
    void testGetReloadableSSLContext() throws Exception {
        SSLContext sslContext = SSLContext.getDefault();
        ReloadableSSLContext reloadable = ReloadableSSLContext.nonReloadable(sslContext);
        ReloadableTlsStrategy strategy = new ReloadableTlsStrategy(reloadable, true);

        assertSame(reloadable, strategy.getReloadableSSLContext());
    }

    @Test
    void testHostnameVerificationEnabled() throws Exception {
        SSLContext sslContext = SSLContext.getDefault();
        ReloadableSSLContext reloadable = ReloadableSSLContext.nonReloadable(sslContext);

        // Create with hostname verification enabled
        ReloadableTlsStrategy strategy = new ReloadableTlsStrategy(reloadable, true);
        assertNotNull(strategy);
    }

    @Test
    void testHostnameVerificationDisabled() throws Exception {
        SSLContext sslContext = SSLContext.getDefault();
        ReloadableSSLContext reloadable = ReloadableSSLContext.nonReloadable(sslContext);

        // Create with hostname verification disabled
        ReloadableTlsStrategy strategy = new ReloadableTlsStrategy(reloadable, false);
        assertNotNull(strategy);
    }

    @Test
    void testMultipleReloadCallsAreSafe() throws Exception {
        SSLContext sslContext = SSLContext.getDefault();
        ReloadableSSLContext reloadable = ReloadableSSLContext.nonReloadable(sslContext);
        ReloadableTlsStrategy strategy = new ReloadableTlsStrategy(reloadable, true);

        // Multiple reloads should be safe
        for (int i = 0; i < 10; i++) {
            assertFalse(strategy.reload());
        }
    }
}
