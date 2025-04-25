package com.monikit.starter.batch.config;

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
import com.monikit.starter.batch.BatchJobCountMetricsBinder;
import com.monikit.starter.batch.BatchJobDurationMetricsBinder;
import com.monikit.starter.batch.BatchJobMetricCollector;
import com.monikit.starter.batch.BatchJobMetricsRecorder;
import com.monikit.starter.batch.BatchStepMetricCollector;
import com.monikit.starter.batch.BatchStepMetricsRecorder;
import com.monikit.starter.batch.StepCountMetricsBinder;
import com.monikit.starter.batch.StepDurationMetricsBinder;

@Configuration
public class BatchCollectorAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(BatchCollectorAutoConfiguration.class);


    @Bean
    @ConditionalOnClass(BatchJobMetricsRecorder.class)
    @ConditionalOnProperty(name = "monikit.metrics.metricsEnabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(BatchJobMetricsRecorder.class)
    public BatchJobMetricsRecorder batchJobMetricsRecorder(BatchJobCountMetricsBinder batchJobCountMetricsBinder,
                                                           BatchJobDurationMetricsBinder batchJobDurationMetricsBinder) {
        logger.info("[MoniKit] Registered BatchJobMetricsRecorder");
        return new BatchJobMetricsRecorder(batchJobCountMetricsBinder, batchJobDurationMetricsBinder);
    }

    @Bean
    @ConditionalOnProperty(name = "monikit.metrics.metricsEnabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnBean(BatchJobMetricsRecorder.class)
    public MetricCollector<?> batchJobMetricCollector(MoniKitMetricsProperties metricsProperties,
                                                           BatchJobMetricsRecorder queryMetricsRecorder) {
        logger.info("[MoniKit] Registered MetricCollector: BatchJobMetricCollector");
        return new BatchJobMetricCollector(metricsProperties, queryMetricsRecorder);
    }


    @Bean
    @ConditionalOnClass(BatchStepMetricsRecorder.class)
    @ConditionalOnProperty(name = "monikit.metrics.metricsEnabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(BatchStepMetricsRecorder.class)
    public BatchStepMetricsRecorder batchStepMetricsRecorder(StepCountMetricsBinder stepCountMetricsBinder,
                                                             StepDurationMetricsBinder stepDurationMetricsBinder) {
        logger.info("[MoniKit] Registered BatchStepMetricsRecorder");
        return new BatchStepMetricsRecorder(stepCountMetricsBinder, stepDurationMetricsBinder);
    }


    @Bean
    @ConditionalOnProperty(name = "monikit.metrics.metricsEnabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnBean(BatchStepMetricsRecorder.class)
    public MetricCollector<?> batchStepMetricCollector(MoniKitMetricsProperties metricsProperties,
                                                       BatchStepMetricsRecorder batchStepMetricsRecorder) {
        logger.info("[MoniKit] Registered MetricCollector: BatchStepMetricCollector");
        return new BatchStepMetricCollector(metricsProperties, batchStepMetricsRecorder);
    }


}
