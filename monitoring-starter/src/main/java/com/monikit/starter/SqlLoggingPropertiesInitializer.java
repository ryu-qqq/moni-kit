package com.monikit.starter;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.monikit.core.SqlLoggingPropertiesHolder;
import com.monikit.starter.config.MoniKitLoggingProperties;

/**
 * `SqlLoggingProperties` 값을 `core` 모듈에서 사용할 수 있도록 설정.
 *
 * @author ryu-qqq
 * @since 1.0
 */
@Component
public class SqlLoggingPropertiesInitializer {

    private final MoniKitLoggingProperties sqlLoggingProperties;

    public SqlLoggingPropertiesInitializer(MoniKitLoggingProperties sqlLoggingProperties) {
        this.sqlLoggingProperties = sqlLoggingProperties;
    }

    @PostConstruct
    public void init() {
        SqlLoggingPropertiesHolder.setSlowQueryThresholdMs(sqlLoggingProperties.getSlowQueryThresholdMs());
        SqlLoggingPropertiesHolder.setCriticalQueryThresholdMs(sqlLoggingProperties.getCriticalQueryThresholdMs());
    }
}