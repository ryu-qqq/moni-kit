package com.monikit.starter.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.monikit.core.LogEntryContextManager;
import com.monikit.starter.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 테스트 전용 모의 빈 설정
 */
@TestConfiguration
public class MockBeanConfig {

    @Bean
    @Primary
    public LogEntryContextManager logEntryContextManager() {
        return mock(LogEntryContextManager.class);
    }

    @Bean
    @Primary
    public MoniKitLoggingProperties mockMoniKitLoggingProperties() {
        MoniKitLoggingProperties properties = mock(MoniKitLoggingProperties.class);
        when(properties.isTraceEnabled()).thenReturn(true);
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

    @Bean
    public HttpInboundResponseMetricCollector httpInboundResponseMetricCollector() {
        return new HttpInboundResponseMetricCollector(moniKitMetricsProperties(), responseStatusCounter());
    }

    @Bean
    public DatabaseQueryMetricCollector databaseQueryMetricCollector() {
        return new DatabaseQueryMetricCollector(moniKitMetricsProperties(), queryMetricsRecorder());
    }

    @Bean
    public HttpOutboundResponseMetricCollector httpOutboundResponseMetricCollector() {
        return new HttpOutboundResponseMetricCollector(moniKitMetricsProperties(), responseStatusCounter());
    }

}
