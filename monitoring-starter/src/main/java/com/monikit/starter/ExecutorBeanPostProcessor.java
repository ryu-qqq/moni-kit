package com.monikit.starter;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.monikit.core.ThreadContextPropagator;

/**
 * 모든 Executor 빈을 감싸서 ThreadContextPropagator를 자동 적용하는 PostProcessor.
 * <p>
 * - Spring 컨텍스트에서 등록되는 모든 `Executor` 및 `ExecutorService` 빈을 감싸서,
 *   실행 시 `ThreadContextPropagator.runWithContext()`를 자동 적용함.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.1
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class ExecutorBeanPostProcessor implements BeanPostProcessor {
    private static final Logger logger = LoggerFactory.getLogger(ExecutorBeanPostProcessor.class);


    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ExecutorService executorService) {
            logger.info("Wrapping ExecutorService Bean: {}", beanName);
            return wrapExecutorService(executorService);
        } else if (bean instanceof Executor executor) {
            logger.info("Wrapping Executor Bean: {}", beanName);
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

    private ExecutorService wrapExecutorService(ExecutorService executorService) {
        return new ExecutorService() {
            @Override
            public void execute(Runnable command) {
                executorService.execute(() -> {
                    try {
                        ThreadContextPropagator.runWithContextRunnable(command);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            @Override
            public <T> Future<T> submit(Callable<T> task) {
                return executorService.submit(() -> ThreadContextPropagator.runWithContextCallable(() -> task.call()));
            }

            @Override
            public <T> Future<T> submit(Runnable task, T result) {
                return executorService.submit(() -> {
                    try {
                        ThreadContextPropagator.runWithContextRunnable(task);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, result);
            }

            @Override
            public Future<?> submit(Runnable task) {
                return executorService.submit(() -> {
                    try {
                        ThreadContextPropagator.runWithContextRunnable(task);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            @Override
            public void shutdown() {
                executorService.shutdown();
            }

            @Override
            public List<Runnable> shutdownNow() {
                return executorService.shutdownNow();
            }

            @Override
            public boolean isShutdown() {
                return executorService.isShutdown();
            }

            @Override
            public boolean isTerminated() {
                return executorService.isTerminated();
            }

            @Override
            public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
                return executorService.awaitTermination(timeout, unit);
            }

            @Override
            public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
                throws InterruptedException {
                return executorService.invokeAll(wrapTasks(tasks));
            }

            @Override
            public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
                throws InterruptedException {
                return executorService.invokeAll(wrapTasks(tasks), timeout, unit);
            }

            @Override
            public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
                throws InterruptedException, ExecutionException {
                return executorService.invokeAny(wrapTasks(tasks));
            }

            @Override
            public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
                throws InterruptedException, ExecutionException, TimeoutException {
                return executorService.invokeAny(wrapTasks(tasks), timeout, unit);
            }

            private <T> Collection<Callable<T>> wrapTasks(Collection<? extends Callable<T>> tasks) {
                return tasks.stream()
                    .map(task -> (Callable<T>) () -> ThreadContextPropagator.runWithContextCallable(task::call))
                    .toList();
            }
        };
    }
}
