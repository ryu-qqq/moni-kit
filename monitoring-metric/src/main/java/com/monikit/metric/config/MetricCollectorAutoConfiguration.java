package com.monikit.metric.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.monikit.config.MoniKitMetricsProperties;
import com.monikit.core.hook.MetricCollector;
import com.monikit.metric.DatabaseQueryMetricCollector;
import com.monikit.metric.ExecutionDetailCountMetricsBinder;
import com.monikit.metric.ExecutionDetailDurationMetricsBinder;
import com.monikit.metric.ExecutionDetailMetricCollector;
import com.monikit.metric.ExecutionMetricRecorder;
import com.monikit.metric.HttpInboundResponseMetricCollector;
import com.monikit.metric.HttpOutboundResponseMetricCollector;
import com.monikit.metric.HttpResponseCountMetricsBinder;
import com.monikit.metric.HttpResponseDurationMetricsBinder;
import com.monikit.metric.HttpResponseMetricsRecorder;
import com.monikit.metric.QueryMetricsRecorder;
import com.monikit.metric.SqlQueryCountMetricsBinder;
import com.monikit.metric.SqlQueryDurationMetricsBinder;

/**
 * MoniKit 메트릭 수집기 자동 구성 클래스.
 * <p>
 * 다음과 같은 조건에 따라 메트릭 관련 Bean을 자동으로 등록한다:
 * </p>
 * <ul>
 *   <li>{@code monikit.metrics.queryMetricsEnabled=true} 일 때 SQL 쿼리 메트릭 수집기 등록</li>
 *   <li>{@code monikit.metrics.httpMetricsEnabled=true} 일 때 HTTP 응답 메트릭 수집기 등록</li>
 * </ul>
 *
 * <p>
 * 이 클래스는 다음과 같은 구성 요소를 제공한다:
 * </p>
 * <ul>
 *   <li>{@link QueryMetricsRecorder}: SQL 쿼리 실행 횟수 및 소요 시간 기록</li>
 *   <li>{@link HttpResponseMetricsRecorder}: HTTP 응답 횟수 및 응답 시간 기록</li>
 *   <li>{@link DatabaseQueryMetricCollector}, {@link HttpInboundResponseMetricCollector}, {@link HttpOutboundResponseMetricCollector}</li>
 * </ul>
 *
 * <p>
 * 조건에 따라 필요한 메트릭 구성 요소만 등록하여 불필요한 리소스 낭비를 방지하고,
 * 모듈 단위의 성능 최적화를 지원한다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.1.0
 */
@Configuration
public class MetricCollectorAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(MetricCollectorAutoConfiguration.class);

    @Bean
    @ConditionalOnClass(QueryMetricsRecorder.class)
    @ConditionalOnProperty(name = "monikit.metrics.queryMetricsEnabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(QueryMetricsRecorder.class)
    public QueryMetricsRecorder queryMetricsRecorder(SqlQueryCountMetricsBinder countMetricsBinder,
                                                     SqlQueryDurationMetricsBinder durationMetricsBinder) {
        logger.info("[MoniKit] Registered QueryMetricsRecorder");
        return new QueryMetricsRecorder(countMetricsBinder, durationMetricsBinder);
    }

    @Bean
    @ConditionalOnProperty(name = "monikit.metrics.queryMetricsEnabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnBean(QueryMetricsRecorder.class)
    public MetricCollector<?> databaseQueryMetricCollector(MoniKitMetricsProperties metricsProperties,
                                                           QueryMetricsRecorder queryMetricsRecorder) {
        logger.info("[MoniKit] Registered MetricCollector: DatabaseQueryMetricCollector");
        return new DatabaseQueryMetricCollector(metricsProperties, queryMetricsRecorder);
    }

    @Bean
    @ConditionalOnClass(HttpResponseMetricsRecorder.class)
    @ConditionalOnProperty(name = "monikit.metrics.httpMetricsEnabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(HttpResponseMetricsRecorder.class)
    public HttpResponseMetricsRecorder httpResponseMetricsRecorder(HttpResponseCountMetricsBinder countMetricsBinder,
                                                                   HttpResponseDurationMetricsBinder durationMetricsBinder) {
        logger.info("[MoniKit] Registered HttpResponseMetricsRecorder");
        return new HttpResponseMetricsRecorder(countMetricsBinder, durationMetricsBinder);
    }

    @Bean
    @ConditionalOnClass(ExecutionMetricRecorder.class)
    @ConditionalOnProperty(name = "monikit.metrics.metricsEnabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(ExecutionMetricRecorder.class)
    public ExecutionMetricRecorder executionMetricRecorder(ExecutionDetailCountMetricsBinder executionDetailCountMetricsBinder,
                                                           ExecutionDetailDurationMetricsBinder executionDetailDurationMetricsBinder) {
        logger.info("[MoniKit] Registered ExecutionMetricRecorder");
        return new ExecutionMetricRecorder(executionDetailCountMetricsBinder, executionDetailDurationMetricsBinder);
    }


    @Bean
    @ConditionalOnProperty(name = "monikit.metrics.httpMetricsEnabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnBean(HttpResponseMetricsRecorder.class)
    public MetricCollector<?> httpInboundResponseMetricCollector(MoniKitMetricsProperties metricsProperties,
                                                                 HttpResponseMetricsRecorder httpResponseMetricsRecorder) {
        logger.info("[MoniKit] Registered MetricCollector: HttpInboundResponseMetricCollector");
        return new HttpInboundResponseMetricCollector(metricsProperties, httpResponseMetricsRecorder);
    }

    @Bean
    @ConditionalOnProperty(name = "monikit.metrics.httpMetricsEnabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnBean(HttpResponseMetricsRecorder.class)
    public MetricCollector<?> httpOutboundResponseMetricCollector(MoniKitMetricsProperties metricsProperties,
                                                                  HttpResponseMetricsRecorder httpResponseMetricsRecorder) {
        logger.info("[MoniKit] Registered MetricCollector: HttpOutboundResponseMetricCollector");
        return new HttpOutboundResponseMetricCollector(metricsProperties, httpResponseMetricsRecorder);
    }


    @Bean
    @ConditionalOnProperty(name = "monikit.metrics.metricsEnabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnBean(ExecutionMetricRecorder.class)
    public MetricCollector<?> executionDetailMetricCollector(MoniKitMetricsProperties metricsProperties,
                                                                  ExecutionMetricRecorder executionMetricRecorder) {
        logger.info("[MoniKit] Registered MetricCollector: ExecutionDetailMetricCollector");
        return new ExecutionDetailMetricCollector(metricsProperties, executionMetricRecorder);
    }



}
