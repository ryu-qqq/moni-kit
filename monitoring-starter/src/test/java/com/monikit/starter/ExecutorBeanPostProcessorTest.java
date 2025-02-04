package com.monikit.starter;

import java.util.concurrent.Executor;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ExecutorBeanPostProcessorTest {


    @Test
    void testThreadLocalPropagationInExecutor() throws InterruptedException {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(TestAsyncConfig.class, ExecutorBeanPostProcessor.class);
        context.refresh();

        Executor executor = context.getBean("testThreadPoolExecutor", Executor.class);
        assertNotNull(executor);
        assertInstanceOf(Executor.class, executor);

        executor.execute(() -> {
            String traceId = "test-trace-id";
            ThreadLocal<String> threadLocal = new ThreadLocal<>();
            threadLocal.set(traceId);

            assertEquals(traceId, threadLocal.get());
        });

        context.close();
    }

    /**
     * ✅ 테스트 전용 `AsyncConfig` (테스트 실행 시에만 등록됨)
     */
    @TestConfiguration
    @EnableAsync
    static class TestAsyncConfig {

        @Bean(name = "testThreadPoolExecutor")
        public ThreadPoolTaskExecutor testThreadPoolExecutor() {
            ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
            taskExecutor.setCorePoolSize(2);
            taskExecutor.setMaxPoolSize(4);
            taskExecutor.setQueueCapacity(10);
            taskExecutor.setThreadNamePrefix("TestAsyncThread-");
            taskExecutor.initialize();
            return taskExecutor;
        }
    }







}