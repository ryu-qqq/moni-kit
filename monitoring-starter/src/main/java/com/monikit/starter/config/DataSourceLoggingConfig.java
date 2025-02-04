package com.monikit.starter.config;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.monikit.core.QueryLoggingService;
import com.monikit.starter.LoggingDataSource;

/**
 * `LoggingDataSource`를 자동으로 감싸서 적용하는 설정 클래스.
 *
 * @author ryu-qqq
 * @since 1.0
 */
@Configuration
public class DataSourceLoggingConfig {

    /**
     * 사용자가 `DataSource`를 별도로 등록하지 않으면 `LoggingDataSource`를 자동 적용함.
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean
    public DataSource loggingDataSource(DataSource originalDataSource, QueryLoggingService queryLoggingService) {
        return new LoggingDataSource(originalDataSource, queryLoggingService);
    }


}