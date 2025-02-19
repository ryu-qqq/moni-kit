package com.monikit.starter.config;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * `JdbcTemplate`과 `NamedParameterJdbcTemplate`을 자동으로 등록하는 설정 클래스.
 *
 * `JdbcTemplate`을 사용해야 하는 경우에만 `LoggingDataSource`를 주입하여 래핑된 상태로 제공.
 *
 * @author ryu-qqq
 * @since 1.0
 */
@Configuration
public class JdbcTemplateConfig {

    private static final Logger logger = LoggerFactory.getLogger(JdbcTemplateConfig.class);

    /**
     * `JdbcTemplate`이 프로젝트에서 사용될 경우, `LoggingDataSource`가 적용된 상태로 제공.
     */
    @Bean
    @ConditionalOnClass(JdbcTemplate.class)
    public JdbcTemplate jdbcTemplate(DataSource loggingDataSource) {
        logger.info("Initializing JdbcTemplate with LoggingDataSource");
        return new JdbcTemplate(loggingDataSource);
    }

    /**
     * `NamedParameterJdbcTemplate`이 사용될 경우, `LoggingDataSource`를 적용하여 제공.
     */
    @Bean
    @ConditionalOnClass(NamedParameterJdbcTemplate.class)
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(DataSource loggingDataSource) {
        logger.info("Initializing NamedParameterJdbcTemplate with LoggingDataSource");
        return new NamedParameterJdbcTemplate(loggingDataSource);
    }
}