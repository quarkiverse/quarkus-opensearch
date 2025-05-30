package io.quarkiverse.opensearch;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.enterprise.util.Nonbinding;
import jakarta.inject.Qualifier;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD })
public @interface OpenSearchClientName {

    @Nonbinding
    String value() default OpenSearchClientName.DEFAULT;

    String DEFAULT = "<default>";

    final class Literal extends AnnotationLiteral<OpenSearchClientName> implements OpenSearchClientName {
        private final String value;

        public static Literal of(String value) {
            return new Literal(value);
        }

        private Literal(String value) {
            this.value = value;
        }

        @Override
        public String value() {
            return this.value;
        }
    }
}
