package io.quarkiverse.opensearch.transport;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;

/**
 * Qualifier annotation for {@link io.quarkiverse.opensearch.transport.spi.OpenSearchTransportOptionsProvider}
 * implementations.
 * <p>
 * Annotate your provider implementation with this qualifier to have it automatically
 * discovered and used for per-request transport options injection.
 * <p>
 * Example:
 *
 * <pre>
 * &#64;ApplicationScoped
 * &#64;OpenSearchTransportOptionsConfig
 * public class MyTransportOptionsProvider implements OpenSearchTransportOptionsProvider {
 *     // ...
 * }
 * </pre>
 *
 * @see io.quarkiverse.opensearch.transport.spi.OpenSearchTransportOptionsProvider
 */
@Qualifier
@Target({ FIELD, TYPE, PARAMETER })
@Retention(RUNTIME)
public @interface OpenSearchTransportOptionsConfig {

    /**
     * Literal implementation for programmatic lookup.
     */
    class Literal extends AnnotationLiteral<OpenSearchTransportOptionsConfig> implements OpenSearchTransportOptionsConfig {
        public static final Literal INSTANCE = new Literal();
    }
}
