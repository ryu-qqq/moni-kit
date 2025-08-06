package com.monikit.otel.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import com.monikit.config.MoniKitLoggingProperties;
import com.monikit.core.TraceIdProvider;
import com.monikit.otel.aspect.OtelExecutionLoggingAspect;
import com.monikit.otel.trace.OtelTraceIdProvider;
import com.monikit.starter.DynamicMatcher;

/**
 * OpenTelemetry 기반 MoniKit 자동 설정.
 * <p>
 * 기존 모니터링 기능을 OpenTelemetry로 전환하는 설정을 제공합니다.
 * </p>
 *
 * @author ryu-qqq
 * @since 2.0.0
 */
@AutoConfiguration
@EnableConfigurationProperties(MoniKitLoggingProperties.class)
@ConditionalOnProperty(name = "monikit.otel.enabled", havingValue = "true", matchIfMissing = false)
@Import(OtelExporterConfig.class)
public class OtelAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(OtelAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public Tracer tracer(OpenTelemetry openTelemetry) {
        logger.info("[MoniKit-OTEL] Creating OpenTelemetry Tracer");
        return openTelemetry.getTracer("monikit", "2.0.0");
    }

    @Bean
    @ConditionalOnMissingBean
    public TraceIdProvider otelTraceIdProvider(Tracer tracer) {
        logger.info("[MoniKit-OTEL] Replacing DefaultTraceIdProvider with OtelTraceIdProvider");
        return new OtelTraceIdProvider(tracer);
    }

    @Bean
    @ConditionalOnMissingBean
    public DynamicMatcher dynamicMatcher(MoniKitLoggingProperties loggingProperties) {
        logger.info("[MoniKit-OTEL] Creating DynamicMatcher for OpenTelemetry");
        return new DynamicMatcher(
            loggingProperties.getDynamicMatching(),
            loggingProperties.getAllowedPackages()
        );
    }

    @Bean
    @ConditionalOnProperty(name = "monikit.otel.enabled", havingValue = "true", matchIfMissing = false)
    public OtelExecutionLoggingAspect otelExecutionLoggingAspect(
        Tracer tracer, 
        DynamicMatcher dynamicMatcher
    ) {
        logger.info("🚀 OpenTelemetry ExecutionLoggingAspect activated - pure OpenTelemetry mode");
        logger.info("📊 Metrics, tracing, and logs will be handled by OpenTelemetry standard");
        return new OtelExecutionLoggingAspect(tracer, dynamicMatcher);
    }
}
