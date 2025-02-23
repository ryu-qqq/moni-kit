package com.monikit.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SqlParameterHolder 테스트")
class SqlParameterHolderTest {

    @Nested
    @DisplayName("단일 스레드 환경에서의 동작 테스트")
    class SingleThreadTests {

        @Test
        @DisplayName("addParameter() 호출 시 파라미터가 올바르게 저장되어야 한다.")
        void shouldStoreAddedParameters() {
            // Given
            try (SqlParameterHolder holder = new SqlParameterHolder()) {
                // When
                holder.addParameter(42);
                holder.addParameter("test");
                holder.addParameter(3.14);

                // Then
                assertEquals("[42, test, 3.14]", holder.getCurrentParameters());
            }
        }

        @Test
        @DisplayName("close() 호출 시 저장된 파라미터가 정리되어야 한다.")
        void shouldClearParametersOnClose() {
            // Given
            SqlParameterHolder holder = new SqlParameterHolder();
            holder.addParameter("beforeClose");

            // When
            holder.close();

            // Then
            assertEquals("[]", holder.getCurrentParameters());
        }
    }

    @Nested
    @DisplayName("멀티스레드 환경에서의 동작 테스트")
    class MultiThreadTests {

        @Test
        @DisplayName("멀티스레드 환경에서도 각 스레드가 독립적으로 파라미터를 관리해야 한다.")
        void shouldMaintainThreadIsolation() throws InterruptedException {
            // Given
            SqlParameterHolder holder = new SqlParameterHolder();
            ExecutorService executor = Executors.newFixedThreadPool(3);
            CountDownLatch latch = new CountDownLatch(3);

            Runnable task1 = () -> {
                holder.addParameter("Thread-1");
                assertEquals("[Thread-1]", holder.getCurrentParameters());
                latch.countDown();
            };

            Runnable task2 = () -> {
                holder.addParameter("Thread-2");
                assertEquals("[Thread-2]", holder.getCurrentParameters());
                latch.countDown();
            };

            Runnable task3 = () -> {
                holder.addParameter(100);
                assertEquals("[100]", holder.getCurrentParameters());
                latch.countDown();
            };

            // When
            executor.execute(task1);
            executor.execute(task2);
            executor.execute(task3);

            latch.await();
            executor.shutdown();

            // Then (각 스레드의 데이터가 격리되었음을 확인)
            assertEquals("[]", holder.getCurrentParameters()); // 메인 스레드는 영향받지 않아야 함
        }
    }
}
