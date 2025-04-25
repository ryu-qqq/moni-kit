package com.monikit.starter.batch.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.monikit.starter.batch.BatchJobCountMetricsBinder;
import com.monikit.starter.batch.BatchJobDurationMetricsBinder;
import com.monikit.starter.batch.StepCountMetricsBinder;
import com.monikit.starter.batch.StepDurationMetricsBinder;

@Configuration
public class BatchMeterBinderAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(BatchMeterBinderAutoConfiguration.class);


    @Bean
    @ConditionalOnMissingBean
    public BatchJobCountMetricsBinder batchJobCountMetricsBinder() {
        logger.info("[MoniKit] Registered MeterBinder: BatchJobCountMetricsBinder");
        return new BatchJobCountMetricsBinder();
    }


    @Bean
    @ConditionalOnMissingBean
    public BatchJobDurationMetricsBinder batchJobDurationMetricsBinder() {
        logger.info("[MoniKit] Registered MeterBinder: BatchJobDurationMetricsBinder");
        return new BatchJobDurationMetricsBinder();
    }


    @Bean
    @ConditionalOnMissingBean
    public StepCountMetricsBinder stepCountMetricsBinder() {
        logger.info("[MoniKit] Registered MeterBinder: StepCountMetricsBinder");
        return new StepCountMetricsBinder();
    }

    @Bean
    @ConditionalOnMissingBean
    public StepDurationMetricsBinder stepDurationMetricsBinder() {
        logger.info("[MoniKit] Registered MeterBinder: StepDurationMetricsBinder");
        return new StepDurationMetricsBinder();
    }

}
