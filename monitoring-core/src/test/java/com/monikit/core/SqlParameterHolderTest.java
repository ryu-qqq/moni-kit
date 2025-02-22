package com.monikit.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("SqlParameterHolder 개선 테스트")
class SqlParameterHolderTest {

    @Test
    @DisplayName("try-with-resources를 사용하면 자동으로 clear()가 호출되어야 한다")
    void shouldAutoClearUsingTryWithResources() {
        // Given
        try (SqlParameterHolder holder = new SqlParameterHolder()) {
            SqlParameterHolder.addParameter(123);
            SqlParameterHolder.addParameter("test");
            assertEquals("[123, test]",
                SqlParameterHolder.getCurrentParameters());
        }

        // Then (자동 clear 확인)
        assertEquals("[]",
            SqlParameterHolder.getCurrentParameters());
    }

    @Test
    @DisplayName("파라미터를 추가하면 SQL에 올바르게 치환되어야 한다")
    void shouldFormatSQLWithParameters() {
        // Given
        try (SqlParameterHolder holder = new SqlParameterHolder()) {
            SqlParameterHolder.addParameter(100);
            SqlParameterHolder.addParameter("hello");
            SqlParameterHolder.addParameter(null);

            // When
            String formattedSQL = SqlParameterHolder.getCurrentParameters();

            // Then
            assertEquals("[100, hello, null]", formattedSQL);
        }
    }

    @Test
    @DisplayName("멀티스레드 환경에서도 독립적인 값을 유지해야 한다")
    void shouldMaintainThreadLocalIsolation() throws InterruptedException {
        // Given
        try (SqlParameterHolder mainThreadHolder = new SqlParameterHolder()) {
            SqlParameterHolder.addParameter("main-thread");

            Thread thread = new Thread(() -> {
                try (SqlParameterHolder childThreadHolder = new SqlParameterHolder()) {
                    SqlParameterHolder.addParameter("child-thread");
                    assertEquals("SELECT * FROM logs WHERE id = 'child-thread'",
                        SqlParameterHolder.getCurrentParameters());
                }
            });

            thread.start();
            thread.join(); // 스레드가 끝날 때까지 대기

            // Then (메인 스레드의 값이 영향을 받지 않아야 함)
            assertEquals("[main-thread]",
                SqlParameterHolder.getCurrentParameters());
        }
    }
}