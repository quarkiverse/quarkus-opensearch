package io.quarkiverse.opensearch.restclient.lowlevel;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;

/**
 * Annotate implementations of {@code org.opensearch.client.RestClientBuilder.HttpClientConfigCallback} to provide further
 * configuration of injected OpenSearch {@code RestClient} You may provide multiple implementations each annotated with
 * {@code OpenSearchClientConfig} and configuration provided by each implementation will be applied in a randomly ordered
 * cascading manner
 */
@Qualifier
@Target({ FIELD, TYPE, PARAMETER })
@Retention(RUNTIME)
public @interface OpenSearchClientConfig {

    class Literal extends AnnotationLiteral<OpenSearchClientConfig> implements OpenSearchClientConfig {

    }
}
