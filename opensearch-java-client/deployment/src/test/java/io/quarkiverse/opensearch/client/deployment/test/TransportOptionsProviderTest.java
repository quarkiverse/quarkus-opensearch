package io.quarkiverse.opensearch.client.deployment.test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.TransportOptions;

import io.quarkiverse.opensearch.OpenSearchClientName;
import io.quarkiverse.opensearch.client.runtime.OpenSearchRequestScopedClient;
import io.quarkiverse.opensearch.transport.OpenSearchTransportOptionsConfig;
import io.quarkiverse.opensearch.transport.spi.OpenSearchTransportOptionsProvider;
import io.quarkus.arc.Arc;
import io.quarkus.arc.ManagedContext;
import io.quarkus.test.QuarkusUnitTest;

/**
 * Tests for {@link OpenSearchTransportOptionsProvider} SPI and
 * {@link OpenSearchRequestScopedClient} integration.
 */
class TransportOptionsProviderTest {

    @RegisterExtension
    static final QuarkusUnitTest TEST = new QuarkusUnitTest()
            .setArchiveProducer(
                    () -> ShrinkWrap.create(JavaArchive.class)
                            .addClasses(
                                    TestAuthTokenProvider.class,
                                    TestTenantHeaderProvider.class,
                                    TestLowPriorityProvider.class)
                            .addAsResource(new StringAsset(
                                    "quarkus.opensearch.hosts=localhost:9200\n" +
                                            "quarkus.opensearch.devservices.enabled=false"),
                                    "application.properties"));

    @Inject
    OpenSearchRequestScopedClient requestScopedClient;

    @Inject
    OpenSearchClient defaultClient;

    @Test
    void testProviderIsInvoked() {
        ManagedContext requestContext = Arc.container().requestContext();
        requestContext.activate();
        try {
            // Reset invocation flags
            TestAuthTokenProvider.invoked = false;
            TestTenantHeaderProvider.invoked = false;

            // Getting the client should trigger provider invocation
            OpenSearchClient client = requestScopedClient.getClient();
            assertNotNull(client);

            // Providers should have been invoked during options resolution
            assertTrue(TestAuthTokenProvider.invoked, "Auth token provider should be invoked");
            assertTrue(TestTenantHeaderProvider.invoked, "Tenant header provider should be invoked");
        } finally {
            requestContext.terminate();
        }
    }

    @Test
    void testProviderPriorityOrder() {
        ManagedContext requestContext = Arc.container().requestContext();
        requestContext.activate();
        try {
            // Reset
            TestAuthTokenProvider.invokeOrder = 0;
            TestTenantHeaderProvider.invokeOrder = 0;
            TestLowPriorityProvider.invokeOrder = 0;
            invocationCounter = 0;

            requestScopedClient.getClient();

            // Higher priority (lower number) should be invoked first
            assertTrue(TestAuthTokenProvider.invokeOrder < TestTenantHeaderProvider.invokeOrder,
                    "Auth provider (priority 10) should run before tenant provider (priority 50)");
            assertTrue(TestTenantHeaderProvider.invokeOrder < TestLowPriorityProvider.invokeOrder,
                    "Tenant provider (priority 50) should run before low priority provider (priority 200)");
        } finally {
            requestContext.terminate();
        }
    }

    @Test
    void testClientNamePassedToProvider() {
        ManagedContext requestContext = Arc.container().requestContext();
        requestContext.activate();
        try {
            TestAuthTokenProvider.lastClientName = null;

            requestScopedClient.getClient();
            assertEquals(OpenSearchClientName.DEFAULT, TestAuthTokenProvider.lastClientName,
                    "Default client name should be passed to provider");
        } finally {
            requestContext.terminate();
        }
    }

    @Test
    void testProviderCanReturnEmpty() {
        ManagedContext requestContext = Arc.container().requestContext();
        requestContext.activate();
        try {
            // TestLowPriorityProvider returns empty - should not cause issues
            assertDoesNotThrow(() -> requestScopedClient.getClient());
        } finally {
            requestContext.terminate();
        }
    }

    // Shared counter for tracking invocation order
    private static int invocationCounter = 0;

    private static synchronized int nextInvocationOrder() {
        return ++invocationCounter;
    }

    /**
     * Test provider that simulates OIDC token injection.
     * Priority 10 (high priority for security).
     */
    @ApplicationScoped
    @OpenSearchTransportOptionsConfig
    public static class TestAuthTokenProvider implements OpenSearchTransportOptionsProvider {

        static boolean invoked = false;
        static int invokeOrder = 0;
        static String lastClientName = null;

        @Override
        public Optional<TransportOptions> getTransportOptions(String clientName) {
            invoked = true;
            invokeOrder = nextInvocationOrder();
            lastClientName = clientName;

            return Optional.of(TransportOptions.builder()
                    .addHeader("Authorization", "Bearer test-token-12345")
                    .build());
        }

        @Override
        public int priority() {
            return 10; // High priority for auth
        }
    }

    /**
     * Test provider that simulates tenant header injection.
     * Priority 50 (medium priority).
     */
    @ApplicationScoped
    @OpenSearchTransportOptionsConfig
    public static class TestTenantHeaderProvider implements OpenSearchTransportOptionsProvider {

        static boolean invoked = false;
        static int invokeOrder = 0;

        @Override
        public Optional<TransportOptions> getTransportOptions(String clientName) {
            invoked = true;
            invokeOrder = nextInvocationOrder();

            return Optional.of(TransportOptions.builder()
                    .addHeader("X-Tenant-Id", "test-tenant")
                    .build());
        }

        @Override
        public int priority() {
            return 50;
        }
    }

    /**
     * Test provider with low priority that returns empty.
     * Priority 200 (low priority).
     */
    @ApplicationScoped
    @OpenSearchTransportOptionsConfig
    public static class TestLowPriorityProvider implements OpenSearchTransportOptionsProvider {

        static boolean invoked = false;
        static int invokeOrder = 0;

        @Override
        public Optional<TransportOptions> getTransportOptions(String clientName) {
            invoked = true;
            invokeOrder = nextInvocationOrder();

            // Return empty - provider has nothing to add
            return Optional.empty();
        }

        @Override
        public int priority() {
            return 200; // Low priority
        }
    }
}
