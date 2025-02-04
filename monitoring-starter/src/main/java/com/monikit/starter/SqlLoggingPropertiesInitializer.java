package com.monikit.starter;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.monikit.core.SqlLoggingPropertiesHolder;

/**
 * `SqlLoggingProperties` 값을 `core` 모듈에서 사용할 수 있도록 설정.
 *
 * @author ryu-qqq
 * @since 1.0
 */
@Component
public class SqlLoggingPropertiesInitializer {

    private final SqlLoggingProperties sqlLoggingProperties;

    public SqlLoggingPropertiesInitializer(SqlLoggingProperties sqlLoggingProperties) {
        this.sqlLoggingProperties = sqlLoggingProperties;
    }

    @PostConstruct
    public void init() {
        SqlLoggingPropertiesHolder.setSlowQueryThresholdMs(sqlLoggingProperties.getSlowQueryThresholdMs());
        SqlLoggingPropertiesHolder.setCriticalQueryThresholdMs(sqlLoggingProperties.getCriticalQueryThresholdMs());
    }
}