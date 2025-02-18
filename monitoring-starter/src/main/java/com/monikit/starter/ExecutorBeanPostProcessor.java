package com.monikit.starter;

import java.util.concurrent.Executor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.monikit.core.ThreadContextPropagator;

/**
 * 모든 Executor 빈을 감싸서 ThreadContextPropagator를 자동 적용하는 PostProcessor.
 * <p>
 * - Spring 컨텍스트에서 등록되는 모든 `Executor` 빈을 감지하고,
 *   실행 시 `ThreadContextPropagator.runWithContext()`를 적용함.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class ExecutorBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof Executor executor) {
            return wrapExecutor(executor);
        }
        return bean;
    }

    private Executor wrapExecutor(Executor executor) {
        return task -> {
            try {
                ThreadContextPropagator.runWithContextCallable(() -> {
                    executor.execute(task);
                    return null;
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }



}