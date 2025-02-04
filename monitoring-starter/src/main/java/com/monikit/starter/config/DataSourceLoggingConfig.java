package com.monikit.starter.config;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.monikit.starter.LoggingDataSource;

/**
 * `LoggingDataSource`를 자동으로 감싸서 적용하는 설정 클래스.
 *
 * @author ryu-qqq
 * @since 1.0
 */
@AutoConfiguration
public class DataSourceLoggingConfig {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceLoggingConfig.class);


    /**
     * 사용자가 `DataSource`를 별도로 등록하지 않으면 `LoggingDataSource`를 자동 적용함.
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean
    public DataSource loggingDataSource(DataSource originalDataSource) {
        logger.info("Using DataSource: {}", originalDataSource.getClass().getSimpleName());
        DataSource loggingDataSource = new LoggingDataSource(originalDataSource);
        logger.info("Wrapped DataSource with LoggingDataSource: {}", loggingDataSource.getClass().getSimpleName());

        return loggingDataSource;
    }


}