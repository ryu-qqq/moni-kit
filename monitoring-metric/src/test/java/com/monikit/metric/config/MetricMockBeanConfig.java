package com.monikit.metric.config;


import com.monikit.config.MoniKitLoggingProperties;
import com.monikit.config.MoniKitMetricsProperties;
import com.monikit.core.LogEntryContextManager;
import com.monikit.metric.DatabaseQueryMetricCollector;
import com.monikit.metric.HttpInboundResponseMetricCollector;
import com.monikit.metric.HttpOutboundResponseMetricCollector;
import com.monikit.metric.HttpResponseCountMetricsBinder;
import com.monikit.metric.HttpResponseDurationMetricsBinder;
import com.monikit.metric.HttpResponseMetricsRecorder;
import com.monikit.metric.QueryMetricsRecorder;
import com.monikit.metric.SqlQueryCountMetricsBinder;
import com.monikit.metric.SqlQueryDurationMetricsBinder;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * 테스트 전용 모의 빈 설정
 */
@TestConfiguration
public class MetricMockBeanConfig {

    @Bean
    @Primary
    public LogEntryContextManager logEntryContextManager() {
        return mock(LogEntryContextManager.class);
    }

    @Bean
    @Primary
    public MoniKitLoggingProperties mockMoniKitLoggingProperties() {
        MoniKitLoggingProperties properties = mock(MoniKitLoggingProperties.class);
        when(properties.isLogEnabled()).thenReturn(true);
        return properties;
    }

    @Bean
    public MeterRegistry meterRegistry() {
        return new SimpleMeterRegistry();
    }

    @Bean
    public MoniKitMetricsProperties moniKitMetricsProperties() {
        return new MoniKitMetricsProperties();
    }

    @Bean
    public HttpResponseCountMetricsBinder httpResponseCountMetricsBinder() {
        return new HttpResponseCountMetricsBinder();
    }

    @Bean
    public HttpResponseDurationMetricsBinder httpResponseDurationMetricsBinder() {
        return new HttpResponseDurationMetricsBinder();
    }

    @Bean
    public HttpResponseMetricsRecorder responseStatusCounter() {
        return new HttpResponseMetricsRecorder(httpResponseCountMetricsBinder(), httpResponseDurationMetricsBinder());
    }



    @Bean
    public SqlQueryCountMetricsBinder sqlQueryCountMetricsBinder() {
        return new SqlQueryCountMetricsBinder();
    }

    @Bean
    public SqlQueryDurationMetricsBinder sqlQueryDurationMetricsBinder() {
        return new SqlQueryDurationMetricsBinder();
    }

    @Bean
    public QueryMetricsRecorder queryMetricsRecorder(){
        return new QueryMetricsRecorder(sqlQueryCountMetricsBinder(), sqlQueryDurationMetricsBinder());
    }



}
