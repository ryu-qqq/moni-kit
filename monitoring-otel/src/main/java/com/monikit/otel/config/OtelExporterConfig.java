package com.monikit.otel.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * OpenTelemetry 설정.
 * <p>
 * - Traces: OTLP로 전송
 * - Logs: OTLP로 전송
 * - Metrics: OTLP로 전송
 * </p>
 *
 * @author ryu-qqq
 * @since 2.0.0
 */
@Configuration
public class OtelExporterConfig {

    // Resource Attributes (semconv 대신 직접 정의)
    private static final AttributeKey<String> SERVICE_NAME = AttributeKey.stringKey("service.name");
    private static final AttributeKey<String> SERVICE_VERSION = AttributeKey.stringKey("service.version");

    @Value("${spring.application.name:monikit-app}")
    private String serviceName;

    @Value("${monikit.otel.traces.endpoint:}")
    private String tracesEndpoint;

    @Value("${monikit.otel.logs.endpoint:}")
    private String logsEndpoint;

    @Value("${monikit.otel.metrics.endpoint:}")
    private String metricsEndpoint;

    @Bean
    public OpenTelemetry openTelemetry() {
        Resource resource = Resource.getDefault()
            .merge(Resource.create(
                Attributes.of(
                    SERVICE_NAME, serviceName,
                    SERVICE_VERSION, "2.0.0"
                )));

        // Trace Provider 설정
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(BatchSpanProcessor.builder(createTraceExporter()).build())
            .setResource(resource)
            .build();

        // Metrics Provider 설정
        SdkMeterProvider meterProvider = SdkMeterProvider.builder()
            .registerMetricReader(
                PeriodicMetricReader.builder(createMetricsExporter())
                    .setInterval(Duration.ofSeconds(30))
                    .build())
            .setResource(resource)
            .build();

        // Logs Provider 설정
        SdkLoggerProvider loggerProvider = SdkLoggerProvider.builder()
            .addLogRecordProcessor(
                BatchLogRecordProcessor.builder(createLogsExporter())
                    .build())
            .setResource(resource)
            .build();

        return OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .setMeterProvider(meterProvider)
            .setLoggerProvider(loggerProvider)
            .buildAndRegisterGlobal();
    }

    private OtlpGrpcSpanExporter createTraceExporter() {
        var builder = OtlpGrpcSpanExporter.builder();
        
        if (!tracesEndpoint.isEmpty()) {
            builder.setEndpoint(tracesEndpoint);
        } else {
            // 기본 엔드포인트
            builder.setEndpoint("http://localhost:4317");
        }
        
        return builder
            .setCompression("gzip")
            .setTimeout(Duration.ofSeconds(30))
            .build();
    }

    private OtlpGrpcLogRecordExporter createLogsExporter() {
        var builder = OtlpGrpcLogRecordExporter.builder();
        
        if (!logsEndpoint.isEmpty()) {
            builder.setEndpoint(logsEndpoint);
        } else {
            // 기본 엔드포인트
            builder.setEndpoint("http://localhost:4317");
        }
        
        return builder
            .setCompression("gzip")
            .setTimeout(Duration.ofSeconds(30))
            .build();
    }

    private OtlpGrpcMetricExporter createMetricsExporter() {
        var builder = OtlpGrpcMetricExporter.builder();
        
        if (!metricsEndpoint.isEmpty()) {
            builder.setEndpoint(metricsEndpoint);
        } else {
            // 기본 엔드포인트
            builder.setEndpoint("http://localhost:4317");
        }
        
        return builder
            .setCompression("gzip")
            .setTimeout(Duration.ofSeconds(30))
            .build();
    }
}
