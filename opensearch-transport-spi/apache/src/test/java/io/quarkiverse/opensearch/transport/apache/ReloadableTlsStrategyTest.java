package io.quarkiverse.opensearch.transport.apache;

import static org.junit.jupiter.api.Assertions.*;

import javax.net.ssl.SSLContext;

import org.junit.jupiter.api.Test;

import io.quarkiverse.opensearch.ReloadableSSLContext;

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
    void testCreateSSLEngine() throws Exception {
        SSLContext sslContext = SSLContext.getDefault();
        ReloadableSSLContext reloadable = ReloadableSSLContext.nonReloadable(sslContext);
        ReloadableTlsStrategy strategy = new ReloadableTlsStrategy(reloadable, true);

        // Create SSL engine for a test endpoint
        var sslEngine = strategy.createSSLEngine(
                new TestNamedEndpoint("localhost", 9200),
                org.apache.hc.core5.reactor.ssl.SSLBufferMode.STATIC);

        assertNotNull(sslEngine);
        assertTrue(sslEngine.getUseClientMode(), "SSL engine should be in client mode");
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

    /**
     * Test implementation of NamedEndpoint.
     */
    private static class TestNamedEndpoint implements org.apache.hc.core5.net.NamedEndpoint {
        private final String hostName;
        private final int port;

        TestNamedEndpoint(String hostName, int port) {
            this.hostName = hostName;
            this.port = port;
        }

        @Override
        public String getHostName() {
            return hostName;
        }

        @Override
        public int getPort() {
            return port;
        }
    }
}
