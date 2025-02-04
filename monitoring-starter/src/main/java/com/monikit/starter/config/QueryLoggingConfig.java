package com.monikit.starter.config;

import com.monikit.core.QueryLoggingService;
import com.monikit.core.MetricCollector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * QueryLoggingService를 빈으로 등록하는 설정 클래스.
 * <p>
 * - `MetricCollector`를 주입받아 `QueryLoggingService`를 생성하고 관리
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
@Configuration
public class QueryLoggingConfig {

    @Bean
    public QueryLoggingService queryLoggingService(MetricCollector metricCollector) {
        return new QueryLoggingService(metricCollector);
    }

}
