package com.monikit.core;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class SqlParameterHolderTest {

    @AfterEach
    void clearThreadLocal() {
        SqlParameterHolder.clear();  // 각 테스트 후 ThreadLocal 초기화
    }

    @Test
    @DisplayName("스레드 간 파라미터가 독립적으로 처리되는지 확인")
    void shouldAddParameterInDifferentThreadsWithoutInterference() throws InterruptedException, ExecutionException {
        Callable<String> threadTask1 = () -> {
            SqlParameterHolder.addParameter("param1");
            return SqlParameterHolder.getCurrentParameters();
        };

        Callable<String> threadTask2 = () -> {
            SqlParameterHolder.addParameter("param2");
            return SqlParameterHolder.getCurrentParameters();
        };

        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<String> result1 = executor.submit(threadTask1);
        Future<String> result2 = executor.submit(threadTask2);
        executor.shutdown();

        assertNotEquals(result1.get(), result2.get());
        assertEquals("[param1]", result1.get());
        assertEquals("[param2]", result2.get());
    }

    @Test
    @DisplayName("SQL 파라미터가 실행 후 클리어되는지 확인")
    void shouldClearParametersAfterExecution() {
        SqlParameterHolder.addParameter("param1");

        // 파라미터 추가 확인
        assertEquals("[param1]", SqlParameterHolder.getCurrentParameters());

        // 클리어 후 확인
        SqlParameterHolder.clear();
        assertEquals("[]", SqlParameterHolder.getCurrentParameters());
    }

    @Test
    @DisplayName("여러 개의 파라미터를 추가하고 제대로 처리되는지 확인")
    void shouldHandleMultipleParameters() {
        SqlParameterHolder.addParameter("param1");
        SqlParameterHolder.addParameter("param2");

        // 여러 파라미터가 제대로 반환되는지 확인
        assertEquals("[param1, param2]", SqlParameterHolder.getCurrentParameters());
    }
}