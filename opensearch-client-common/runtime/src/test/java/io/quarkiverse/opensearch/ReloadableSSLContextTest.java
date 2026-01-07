package io.quarkiverse.opensearch;

import static org.junit.jupiter.api.Assertions.*;

import javax.net.ssl.SSLContext;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ReloadableSSLContext}.
 */
class ReloadableSSLContextTest {

    @Test
    void testNonReloadableContext() throws Exception {
        // Create a static SSLContext
        SSLContext staticContext = SSLContext.getDefault();

        // Wrap it as non-reloadable
        ReloadableSSLContext wrapper = ReloadableSSLContext.nonReloadable(staticContext);

        // Should return the same context
        assertSame(staticContext, wrapper.get());

        // Should not be reloadable
        assertFalse(wrapper.isReloadable());

        // Reload should return false (no-op)
        assertFalse(wrapper.reload());

        // Should still return the same context after reload attempt
        assertSame(staticContext, wrapper.get());

        // TLS configuration should be empty
        assertTrue(wrapper.getTlsConfiguration().isEmpty());
    }

    @Test
    void testGetReturnsCurrentContext() throws Exception {
        SSLContext context = SSLContext.getDefault();
        ReloadableSSLContext wrapper = ReloadableSSLContext.nonReloadable(context);

        // Multiple calls should return the same context
        assertSame(wrapper.get(), wrapper.get());
    }

    @Test
    void testNonReloadableIsThreadSafe() throws Exception {
        SSLContext context = SSLContext.getDefault();
        ReloadableSSLContext wrapper = ReloadableSSLContext.nonReloadable(context);

        // Simulate concurrent access
        Thread[] threads = new Thread[10];
        SSLContext[] results = new SSLContext[10];

        for (int i = 0; i < threads.length; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                results[index] = wrapper.get();
            });
        }

        for (Thread t : threads) {
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }

        // All threads should get the same context
        for (SSLContext result : results) {
            assertSame(context, result);
        }
    }

    @Test
    void testReloadOnNonReloadableIsSafe() throws Exception {
        SSLContext context = SSLContext.getDefault();
        ReloadableSSLContext wrapper = ReloadableSSLContext.nonReloadable(context);

        // Concurrent reloads on non-reloadable should be safe
        Thread[] threads = new Thread[5];
        boolean[] results = new boolean[5];

        for (int i = 0; i < threads.length; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                results[index] = wrapper.reload();
            });
        }

        for (Thread t : threads) {
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }

        // All reloads should return false
        for (boolean result : results) {
            assertFalse(result);
        }

        // Context should still be valid
        assertSame(context, wrapper.get());
    }
}
