package io.quarkiverse.opensearch.client;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;

/**
 * Annotate implementations of {@code org.opensearch.client.RestClientBuilder.HttpClientConfigCallback} to provide further
 * configuration of injected OpenSearch {@code RestClient} You may provide multiple implementations each annotated with
 * {@code OpenSearchTransportConfig} and configuration provided by each implementation will be applied in a randomly ordered
 * cascading manner
 */
@Qualifier
@Target({ FIELD, TYPE, PARAMETER })
@Retention(RUNTIME)
public @interface OpenSearchTransportConfig {

    class Literal extends AnnotationLiteral<OpenSearchTransportConfig> implements OpenSearchTransportConfig {

    }
}
