package io.quarkiverse.opensearch.client.deployment.test;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.opensearch.client.transport.TransportOptions;

import io.quarkiverse.opensearch.OpenSearchClientName;
import io.quarkiverse.opensearch.client.runtime.OpenSearchRequestScopedClient;
import io.quarkiverse.opensearch.transport.OpenSearchTransportOptionsConfig;
import io.quarkiverse.opensearch.transport.spi.OpenSearchTransportOptionsProvider;
import io.quarkus.arc.Arc;
import io.quarkus.arc.ClientProxy;
import io.quarkus.arc.ManagedContext;
import io.quarkus.test.QuarkusUnitTest;

/**
 * Reproduces the intermittent auth-header drop: a single {@link OpenSearchRequestScopedClient}
 * instance touched by more than one thread within a request (e.g. a create-PIT on the event
 * loop and a continuation on the OpenSearch I/O thread) races on the lazily-cached, non-volatile
 * {@code sortedProviders} field. A thread that observes the reference set but the list not yet
 * populated resolves an empty provider list, so {@code resolveTransportOptions} returns
 * {@code null}, the client is used without {@code withTransportOptions}, and the request goes out
 * with no {@code Authorization} header (→ 401 from OpenSearch).
 */
class ConcurrentAuthHeaderTest {

    static final String AUTH = "Bearer test-token";

    @RegisterExtension
    static final QuarkusUnitTest TEST = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClass(FixedAuthProvider.class)
                    .addAsResource(new StringAsset(
                            "quarkus.opensearch.hosts=localhost:9200\n" +
                                    "quarkus.opensearch.devservices.enabled=false\n"),
                            "application.properties"));

    @Inject
    OpenSearchRequestScopedClient requestScopedClient;

    /** A provider that always supplies an Authorization header (no request state needed). */
    @ApplicationScoped
    @OpenSearchTransportOptionsConfig
    public static class FixedAuthProvider implements OpenSearchTransportOptionsProvider {
        @Override
        public Optional<TransportOptions> getTransportOptions(String clientName) {
            return Optional.of(TransportOptions.builder().addHeader("Authorization", AUTH).build());
        }
    }

    @Test
    void authHeaderResolvedOnEveryConcurrentCall() throws Exception {
        final int threads = 8;
        final int iterations = 200;
        final Method resolve = OpenSearchRequestScopedClient.class
                .getDeclaredMethod("resolveTransportOptions", String.class);
        resolve.setAccessible(true);

        final Map<String, Integer> failures = new ConcurrentHashMap<>();
        final ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            for (int it = 0; it < iterations; it++) {
                // Fresh request context => fresh @RequestScoped instance with sortedProviders == null,
                // so every iteration races the first initialization.
                final ManagedContext ctx = Arc.container().requestContext();
                ctx.activate();
                final OpenSearchRequestScopedClient instance;
                try {
                    instance = ClientProxy.unwrap(requestScopedClient);
                } finally {
                    // keep context active while threads run (providers are @ApplicationScoped, but
                    // unwrap must have happened under an active context)
                }
                try {
                    final CyclicBarrier barrier = new CyclicBarrier(threads);
                    final List<String> results = new CopyOnWriteArrayList<>();
                    final Runnable task = () -> {
                        try {
                            barrier.await(5, TimeUnit.SECONDS);
                            Object opts = resolve.invoke(instance, OpenSearchClientName.DEFAULT);
                            if (opts == null) {
                                results.add("NULL_OPTIONS");
                            } else {
                                boolean hasAuth = ((TransportOptions) opts).headers().stream()
                                        .anyMatch(h -> "Authorization".equals(h.getKey()) && AUTH.equals(h.getValue()));
                                results.add(hasAuth ? "OK" : "NO_AUTH_HEADER");
                            }
                        } catch (Exception e) {
                            results.add("EX:" + e.getClass().getSimpleName());
                        }
                    };
                    final var futures = new java.util.ArrayList<java.util.concurrent.Future<?>>();
                    for (int i = 0; i < threads; i++) {
                        futures.add(pool.submit(task));
                    }
                    for (var f : futures) {
                        f.get(10, TimeUnit.SECONDS);
                    }
                    for (String r : results) {
                        if (!"OK".equals(r)) {
                            failures.merge(r, 1, Integer::sum);
                        }
                    }
                } finally {
                    ctx.terminate();
                }
            }
        } finally {
            pool.shutdownNow();
        }

        assertTrue(failures.isEmpty(),
                "auth header must be resolved on every concurrent call; failures=" + failures);
    }
}
