package com.monikit.starter;

import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 모든 `DataSource` 빈을 감싸서 `LoggingDataSource`를 자동 적용하는 BeanPostProcessor.
 * <p>
 * - 사용자가 직접 등록한 `DataSource`도 자동으로 감싸짐.
 * -  monikit.datasource.logging.enabled=true 인 경우에만 로깅 실행
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class LoggingDataSourcePostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DataSource && !(bean instanceof LoggingDataSource)) {
            boolean loggingEnabled = Boolean.parseBoolean(System.getProperty("monikit.datasource.logging.enabled", "true"));
            if (loggingEnabled) {
                return new LoggingDataSource((DataSource) bean);
            }
        }
        return bean;
    }
}