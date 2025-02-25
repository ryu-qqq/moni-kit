package com.monikit.starter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.monikit.starter.HttpResponseCountMetricsBinder;
import com.monikit.starter.HttpResponseDurationMetricsBinder;
import com.monikit.starter.SqlQueryCountMetricsBinder;
import com.monikit.starter.SqlQueryDurationMetricsBinder;

import io.micrometer.core.instrument.binder.MeterBinder;

@Configuration
public class MoniKitMeterBinderAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(MoniKitMeterBinderAutoConfiguration.class);

    /**
     * SQL 실행 횟수 측정을 위한 `SqlQueryCountMetricsBinder` 빈 등록.
     */
    @Bean
    @ConditionalOnMissingBean
    public MeterBinder sqlQueryCountMetricsBinder() {
        logger.info("Registered MeterBinder: SqlQueryCountMetricsBinder");
        return new SqlQueryCountMetricsBinder();
    }

    /**
     * SQL 실행 시간을 측정하는 `SqlQueryDurationMetricsBinder` 빈 등록.
     */
    @Bean
    @ConditionalOnMissingBean
    public MeterBinder sqlQueryDurationMetricsBinder() {
        logger.info("Registered MeterBinder: SqlQueryDurationMetricsBinder");
        return new SqlQueryDurationMetricsBinder();
    }

    /**
     * HTTP 응답 횟수를 측정하는 `HttpResponseCountMetricsBinder` 빈 등록.
     */
    @Bean
    @ConditionalOnMissingBean
    public MeterBinder httpResponseCountMetricsBinder() {
        logger.info("Registered MeterBinder: HttpResponseCountMetricsBinder");
        return new HttpResponseCountMetricsBinder();
    }

    /**
     * HTTP 응답 시간을 측정하는 `HttpResponseDurationMetricsBinder` 빈 등록.
     */
    @Bean
    @ConditionalOnMissingBean
    public MeterBinder httpResponseDurationMetricsBinder() {
        logger.info("Registered MeterBinder: HttpResponseDurationMetricsBinder");
        return new HttpResponseDurationMetricsBinder();
    }

}
