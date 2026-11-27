package io.quarkiverse.opensearch.transport.apache.graal;

import java.util.function.BooleanSupplier;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

/**
 * Native-image substitutions for httpclient5's optional content decoders.
 *
 * <p>
 * Since 5.6, {@code ContentCompressionAsyncExec} registers zstd and brotli decoders when
 * {@code ZstdRuntime.available()} / {@code Brotli4jRuntime.available()} report the optional
 * {@code zstd-jni} / {@code brotli4j} libraries on the classpath. The check happens at runtime, so
 * native-image still parses the decoder classes and fails with unresolved types when those
 * libraries are absent. When they are absent we make the checks return a constant {@code false};
 * GraalVM inlines it before analysis and drops the unreachable decoder registration. When the
 * libraries are present nothing is substituted.
 */
final class HttpClient5Substitutions {

    private HttpClient5Substitutions() {
    }

    static final class ZstdAbsent implements BooleanSupplier {
        @Override
        public boolean getAsBoolean() {
            return !isPresent("com.github.luben.zstd.ZstdDecompressCtx");
        }
    }

    static final class BrotliAbsent implements BooleanSupplier {
        @Override
        public boolean getAsBoolean() {
            return !isPresent("com.aayushatharva.brotli4j.decoder.DecoderJNI");
        }
    }

    private static boolean isPresent(String className) {
        try {
            Class.forName(className, false, HttpClient5Substitutions.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException | LinkageError e) {
            return false;
        }
    }
}

@TargetClass(className = "org.apache.hc.client5.http.impl.ZstdRuntime", onlyWith = HttpClient5Substitutions.ZstdAbsent.class)
final class Target_org_apache_hc_client5_http_impl_ZstdRuntime {

    @Substitute
    public static boolean available() {
        return false;
    }
}

@TargetClass(className = "org.apache.hc.client5.http.impl.Brotli4jRuntime", onlyWith = HttpClient5Substitutions.BrotliAbsent.class)
final class Target_org_apache_hc_client5_http_impl_Brotli4jRuntime {

    @Substitute
    public static boolean available() {
        return false;
    }
}
