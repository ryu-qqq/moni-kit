package com.monikit.starter;

import jakarta.annotation.PostConstruct;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.monikit.core.DataSourceProvider;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Spring Boot 환경에서 `DataSourceProvider`를 구현하는 클래스.
 * <p>
 * HikariCP를 지원하며, `DataSource`의 정보를 추출하여 `core` 모듈에서 사용 가능하도록 함.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
@Component
public class SpringDataSourceProvider implements DataSourceProvider {

    private static final Logger logger = LoggerFactory.getLogger(SpringDataSourceProvider.class);

    private final DataSource dataSource;

    public SpringDataSourceProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public String getDataSourceName() {
        try {
            HikariDataSource hikariDataSource = dataSource.unwrap(HikariDataSource.class);
            return hikariDataSource.getPoolName();
        } catch (Exception e) {
            logger.warn("Failed to retrieve HikariDataSource name, using default", e);
            return "defaultDataSource";
        }
    }

    /**
     * Spring Boot 실행 시 `DataSourceProvider`를 core 모듈에 자동 등록.
     */
    @PostConstruct
    public void init() {
        DataSourceProvider.setInstance(this);
        logger.info("SpringDataSourceProvider registered as DataSourceProvider instance.");
    }

}