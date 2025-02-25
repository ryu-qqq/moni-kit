package com.monikit.starter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.monikit.core.MetricCollector;
import com.monikit.starter.DatabaseQueryMetricCollector;
import com.monikit.starter.HttpInboundResponseMetricCollector;
import com.monikit.starter.HttpOutboundResponseMetricCollector;
import com.monikit.starter.HttpResponseCountMetricsBinder;
import com.monikit.starter.HttpResponseDurationMetricsBinder;
import com.monikit.starter.HttpResponseMetricsRecorder;
import com.monikit.starter.SqlQueryCountMetricsBinder;
import com.monikit.starter.SqlQueryDurationMetricsBinder;

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

    /**
     * SQL 쿼리 실행 메트릭을 수집하는 `DatabaseQueryMetricCollector` 빈 등록.
     */
    @Bean
    @ConditionalOnProperty(name = "monikit.metrics.queryMetricsEnabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    public MetricCollector<?> databaseQueryMetricCollector(
        MoniKitMetricsProperties metricsProperties,
        SqlQueryCountMetricsBinder countMetricsBinder,
        SqlQueryDurationMetricsBinder durationMetricsBinder
    ) {
        logger.info("Registered MetricCollector: DatabaseQueryMetricCollector");
        return new DatabaseQueryMetricCollector(metricsProperties, countMetricsBinder, durationMetricsBinder);
    }

    /**
     * HTTP Inbound 응답 메트릭을 수집하는 `HttpInboundResponseMetricCollector` 빈 등록.
     */
    @Bean
    @ConditionalOnProperty(name = "monikit.metrics.httpMetricsEnabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    public MetricCollector<?> httpInboundResponseMetricCollector(
        MoniKitMetricsProperties metricsProperties,
        HttpResponseMetricsRecorder httpResponseMetricsRecorder
    ) {
        logger.info("Registered MetricCollector: HttpInboundResponseMetricCollector");
        return new HttpInboundResponseMetricCollector(metricsProperties, httpResponseMetricsRecorder);
    }

    /**
     * HTTP Outbound 응답 메트릭을 수집하는 `HttpOutboundResponseMetricCollector` 빈 등록.
     */
    @Bean
    @ConditionalOnProperty(name = "monikit.metrics.httpMetricsEnabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    public MetricCollector<?> httpOutboundResponseMetricCollector(
        MoniKitMetricsProperties metricsProperties,
        HttpResponseMetricsRecorder httpResponseMetricsRecorder
    ) {
        logger.info("Registered MetricCollector: HttpOutboundResponseMetricCollector");
        return new HttpOutboundResponseMetricCollector(metricsProperties, httpResponseMetricsRecorder);
    }


    @Bean
    @ConditionalOnProperty(name = "monikit.metrics.httpMetricsEnabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    public HttpResponseMetricsRecorder httpResponseMetricsRecorder(HttpResponseCountMetricsBinder countMetricsBinder,
                                                                   HttpResponseDurationMetricsBinder durationMetricsBinder){
        logger.info("Registered HttpResponseMetricsRecorder: HttpResponseMetricsRecorder");
        return new HttpResponseMetricsRecorder(countMetricsBinder, durationMetricsBinder);
    }

}
