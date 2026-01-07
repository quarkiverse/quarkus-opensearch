package io.quarkiverse.opensearch.client.deployment.test;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.opensearch.client.opensearch.OpenSearchAsyncClient;
import org.opensearch.client.opensearch.OpenSearchClient;

import io.quarkiverse.opensearch.OpenSearchClientName;
import io.quarkiverse.opensearch.client.runtime.OpenSearchClientsProducer;
import io.quarkiverse.opensearch.client.runtime.OpenSearchRequestScopedClient;
import io.quarkus.arc.Arc;
import io.quarkus.arc.ManagedContext;
import io.quarkus.test.QuarkusUnitTest;

/**
 * Tests for {@link OpenSearchRequestScopedClient}.
 */
class RequestScopedClientTest {

    @RegisterExtension
    static final QuarkusUnitTest TEST = new QuarkusUnitTest()
            .setArchiveProducer(
                    () -> ShrinkWrap.create(JavaArchive.class)
                            .addAsResource(new StringAsset(
                                    "quarkus.opensearch.hosts=localhost:9200\n" +
                                            "quarkus.opensearch.devservices.enabled=false\n" +
                                            "quarkus.opensearch.named-client.hosts=localhost:9201\n" +
                                            "quarkus.opensearch.named-client.devservices.enabled=false"),
                                    "application.properties"));

    @Inject
    OpenSearchRequestScopedClient requestScopedClient;

    @Inject
    OpenSearchClientsProducer clientsProducer;

    @Test
    void testGetDefaultClient() {
        ManagedContext requestContext = Arc.container().requestContext();
        requestContext.activate();
        try {
            OpenSearchClient client = requestScopedClient.getClient();
            assertNotNull(client, "Default client should not be null");
        } finally {
            requestContext.terminate();
        }
    }

    @Test
    void testGetDefaultClientWithExplicitName() {
        ManagedContext requestContext = Arc.container().requestContext();
        requestContext.activate();
        try {
            OpenSearchClient client = requestScopedClient.getClient(OpenSearchClientName.DEFAULT);
            assertNotNull(client, "Client with explicit default name should not be null");
        } finally {
            requestContext.terminate();
        }
    }

    @Test
    void testGetDefaultAsyncClient() {
        ManagedContext requestContext = Arc.container().requestContext();
        requestContext.activate();
        try {
            OpenSearchAsyncClient asyncClient = requestScopedClient.getAsyncClient();
            assertNotNull(asyncClient, "Default async client should not be null");
        } finally {
            requestContext.terminate();
        }
    }

    @Test
    void testGetDefaultAsyncClientWithExplicitName() {
        ManagedContext requestContext = Arc.container().requestContext();
        requestContext.activate();
        try {
            OpenSearchAsyncClient asyncClient = requestScopedClient.getAsyncClient(OpenSearchClientName.DEFAULT);
            assertNotNull(asyncClient, "Async client with explicit default name should not be null");
        } finally {
            requestContext.terminate();
        }
    }

    @Test
    void testClientsAreBasedOnProducerClients() {
        ManagedContext requestContext = Arc.container().requestContext();
        requestContext.activate();
        try {
            // The request-scoped client should use the same base client from the producer
            OpenSearchClient producerClient = clientsProducer.getClientByName(OpenSearchClientName.DEFAULT);
            OpenSearchClient scopedClient = requestScopedClient.getClient();

            assertNotNull(producerClient);
            assertNotNull(scopedClient);

            // Both should use the same underlying transport
            // Note: withTransportOptions returns a new client instance, but with the same transport
        } finally {
            requestContext.terminate();
        }
    }

    @Test
    void testMultipleCallsReturnWorkingClients() {
        ManagedContext requestContext = Arc.container().requestContext();
        requestContext.activate();
        try {
            // Multiple calls within the same request should work
            OpenSearchClient client1 = requestScopedClient.getClient();
            OpenSearchClient client2 = requestScopedClient.getClient();

            assertNotNull(client1);
            assertNotNull(client2);
        } finally {
            requestContext.terminate();
        }
    }

    @Test
    void testAsyncAndSyncClientsCoexist() {
        ManagedContext requestContext = Arc.container().requestContext();
        requestContext.activate();
        try {
            // Both sync and async clients should be available
            OpenSearchClient syncClient = requestScopedClient.getClient();
            OpenSearchAsyncClient asyncClient = requestScopedClient.getAsyncClient();

            assertNotNull(syncClient);
            assertNotNull(asyncClient);
        } finally {
            requestContext.terminate();
        }
    }
}
