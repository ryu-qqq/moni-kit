package com.monikit.metric.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.monikit.metric.HttpResponseCountMetricsBinder;
import com.monikit.metric.HttpResponseDurationMetricsBinder;
import com.monikit.metric.SqlQueryCountMetricsBinder;
import com.monikit.metric.SqlQueryDurationMetricsBinder;


/**
 * Micrometer 기반의 MeterBinder 구성 클래스.
 * <p>
 * MoniKit이 수집한 메트릭 데이터를 Micrometer를 통해 외부로 노출하기 위한 MeterBinder들을 등록한다.
 * </p>
 * <ul>
 *   <li>{@link SqlQueryCountMetricsBinder}: SQL 실행 횟수 메트릭 바인딩</li>
 *   <li>{@link SqlQueryDurationMetricsBinder}: SQL 실행 시간 메트릭 바인딩</li>
 *   <li>{@link HttpResponseCountMetricsBinder}: HTTP 응답 횟수 메트릭 바인딩</li>
 *   <li>{@link HttpResponseDurationMetricsBinder}: HTTP 응답 시간 메트릭 바인딩</li>
 * </ul>
 *
 * <p>
 * 모든 Bean은 {@code @ConditionalOnMissingBean} 조건을 통해
 * 사용자 정의 빈이 있을 경우 자동 구성하지 않도록 구성되어 있으며,
 * Micrometer를 사용하는 시스템에서 Prometheus, Datadog 등 외부 모니터링 시스템과의 연동을 가능하게 한다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.1.0
 */

@Configuration
public class MoniKitMeterBinderAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(MoniKitMeterBinderAutoConfiguration.class);

    /**
     * SQL 실행 횟수 측정을 위한 `SqlQueryCountMetricsBinder` 빈 등록.
     */
    @Bean
    @ConditionalOnMissingBean
    public SqlQueryCountMetricsBinder sqlQueryCountMetricsBinder() {
        logger.info("Registered MeterBinder: SqlQueryCountMetricsBinder");
        return new SqlQueryCountMetricsBinder();
    }

    /**
     * SQL 실행 시간을 측정하는 `SqlQueryDurationMetricsBinder` 빈 등록.
     */
    @Bean
    @ConditionalOnMissingBean
    public SqlQueryDurationMetricsBinder sqlQueryDurationMetricsBinder() {
        logger.info("Registered MeterBinder: SqlQueryDurationMetricsBinder");
        return new SqlQueryDurationMetricsBinder();
    }

    /**
     * HTTP 응답 횟수를 측정하는 `HttpResponseCountMetricsBinder` 빈 등록.
     */
    @Bean
    @ConditionalOnMissingBean
    public HttpResponseCountMetricsBinder httpResponseCountMetricsBinder() {
        logger.info("Registered MeterBinder: HttpResponseCountMetricsBinder");
        return new HttpResponseCountMetricsBinder();
    }

    /**
     * HTTP 응답 시간을 측정하는 `HttpResponseDurationMetricsBinder` 빈 등록.
     */
    @Bean
    @ConditionalOnMissingBean
    public HttpResponseDurationMetricsBinder httpResponseDurationMetricsBinder() {
        logger.info("Registered MeterBinder: HttpResponseDurationMetricsBinder");
        return new HttpResponseDurationMetricsBinder();
    }

}
