package com.monikit.starter.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.monikit.core.MetricCollector;
import com.monikit.starter.DatabaseQueryMetricCollector;
import com.monikit.starter.HttpInboundResponseMetricCollector;
import com.monikit.starter.HttpOutboundResponseMetricCollector;
import com.monikit.starter.HttpResponseMetricsRecorder;

/**
 * MoniKit 메트릭 설정 클래스.
 * <p>
 * - HTTP 응답 관련 메트릭 (전체 응답 횟수, 경로별 응답 횟수, 상태 코드별 응답 횟수) 등록.
 * - SQL 쿼리 관련 메트릭 등록.
 * - 필요하지 않은 경우 메트릭을 생성하지 않도록 하여 성능 최적화.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0.1
 */
@Configuration
public class MetricCollectorAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(MetricCollectorAutoConfiguration.class);

    @Bean
    @ConditionalOnProperty(name = "monikit.metrics.query.enabled", havingValue = "true", matchIfMissing = true)
    public Counter sqlQueryCounter(MeterRegistry meterRegistry) {
        logger.info("Registered Metric Bean: sql_query_total (Counter) - Tracking total SQL queries");
        return Counter.builder("sql_query_total")
            .description("Total number of executed SQL queries")
            .register(meterRegistry);
    }

    @Bean
    @ConditionalOnProperty(name = "monikit.metrics.query.enabled", havingValue = "true", matchIfMissing = true)
    public Timer sqlQueryTimer(MeterRegistry meterRegistry) {
        logger.info("Registered Metric Bean: sql_query_duration (Timer) - Tracking SQL query execution time");
        return Timer.builder("sql_query_duration")
            .description("SQL query execution time")
            .register(meterRegistry);
    }



    @Bean
    @ConditionalOnProperty(name = "monikit.metrics.httpMetricsEnabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(HttpInboundResponseMetricCollector.class)
    public MetricCollector<?> httpInboundResponseMetricCollector(
        MoniKitMetricsProperties metricsProperties,
        HttpResponseMetricsRecorder httpResponseMetricsRecorder
    ) {
        logger.info("✅ Registered Metric Collector: HttpInboundResponseMetricCollector");
        return new HttpInboundResponseMetricCollector(metricsProperties, httpResponseMetricsRecorder);
    }

    @Bean
    @ConditionalOnProperty(name = "monikit.metrics.externalMallMetricsEnabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(HttpOutboundResponseMetricCollector.class)
    public MetricCollector<?> httpOutboundResponseMetricCollector(
        MoniKitMetricsProperties metricsProperties,
        HttpResponseMetricsRecorder httpResponseMetricsRecorder
    ) {
        logger.info("✅ Registered Metric Collector: HttpOutboundResponseMetricCollector");
        return new HttpOutboundResponseMetricCollector(metricsProperties, httpResponseMetricsRecorder);
    }

    @Bean
    @ConditionalOnProperty(name = "monikit.metrics.query.enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(DatabaseQueryMetricCollector.class)
    public MetricCollector<?> databaseQueryMetricCollector(
        @Qualifier("sqlQueryCounter") Counter sqlQueryCounter,
        @Qualifier("sqlQueryTimer") Timer sqlQueryTimer,
        MoniKitMetricsProperties metricsProperties
    ) {
        logger.info("✅ Registered Metric Collector: DatabaseQueryMetricCollector");
        return new DatabaseQueryMetricCollector(sqlQueryCounter, sqlQueryTimer, metricsProperties);
    }

}
