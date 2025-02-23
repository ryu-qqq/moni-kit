package com.monikit.starter;

import feign.FeignException;
import jakarta.servlet.ServletException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.server.WebServerException;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.dao.TransientDataAccessException;

import com.monikit.core.ErrorCategory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

@DisplayName("ErrorCategoryClassifier 테스트")
class ErrorCategoryClassifierTest {

    @Nested
    @DisplayName("데이터베이스 예외 분류 테스트")
    class DatabaseExceptionTests {

        @Test
        @DisplayName("TransientDataAccessException 예외는 DATABASE_TRANSIENT_ERROR로 분류되어야 한다.")
        void shouldCategorizeAsDatabaseTransientError() {
            // Given
            TransientDataAccessException exception = new TransientDataAccessException("Temporary DB issue") {};

            // When
            ErrorCategory result = ErrorCategoryClassifier.categorize(exception);

            // Then
            assertEquals(ErrorCategory.DATABASE_TRANSIENT_ERROR, result);
        }

        @Test
        @DisplayName("NonTransientDataAccessException 예외는 DATABASE_PERMANENT_ERROR로 분류되어야 한다.")
        void shouldCategorizeAsDatabasePermanentError() {
            // Given
            NonTransientDataAccessException exception = new NonTransientDataAccessException("Permanent DB issue") {};

            // When
            ErrorCategory result = ErrorCategoryClassifier.categorize(exception);

            // Then
            assertEquals(ErrorCategory.DATABASE_PERMANENT_ERROR, result);
        }
    }

    @Nested
    @DisplayName("네트워크 예외 분류 테스트")
    class NetworkExceptionTests {

        @Test
        @DisplayName("SocketTimeoutException 예외는 INBOUND_NETWORK_ERROR로 분류되어야 한다.")
        void shouldCategorizeSocketTimeoutAsInboundNetworkError() {
            // Given
            SocketTimeoutException exception = new SocketTimeoutException("Timeout occurred");

            // When
            ErrorCategory result = ErrorCategoryClassifier.categorize(exception);

            // Then
            assertEquals(ErrorCategory.INBOUND_NETWORK_ERROR, result);
        }

        @Test
        @DisplayName("UnknownHostException 예외는 INBOUND_NETWORK_ERROR로 분류되어야 한다.")
        void shouldCategorizeUnknownHostAsInboundNetworkError() {
            // Given
            UnknownHostException exception = new UnknownHostException("Unknown host");

            // When
            ErrorCategory result = ErrorCategoryClassifier.categorize(exception);

            // Then
            assertEquals(ErrorCategory.INBOUND_NETWORK_ERROR, result);
        }

        @Test
        @DisplayName("ConnectException 예외는 INBOUND_NETWORK_ERROR로 분류되어야 한다.")
        void shouldCategorizeConnectExceptionAsInboundNetworkError() {
            // Given
            ConnectException exception = new ConnectException("Failed to connect");

            // When
            ErrorCategory result = ErrorCategoryClassifier.categorize(exception);

            // Then
            assertEquals(ErrorCategory.INBOUND_NETWORK_ERROR, result);
        }

        @Test
        @DisplayName("WebServerException 예외는 INBOUND_NETWORK_ERROR로 분류되어야 한다.")
        void shouldCategorizeWebServerExceptionAsInboundNetworkError() {
            // Given
            WebServerException exception = new WebServerException("Server error", new RuntimeException("Cause"));

            // When
            ErrorCategory result = ErrorCategoryClassifier.categorize(exception);

            // Then
            assertEquals(ErrorCategory.INBOUND_NETWORK_ERROR, result);
        }


        @Test
        @DisplayName("ServletException 예외는 INBOUND_NETWORK_ERROR로 분류되어야 한다.")
        void shouldCategorizeServletExceptionAsInboundNetworkError() {
            // Given
            ServletException exception = new ServletException("Servlet issue");

            // When
            ErrorCategory result = ErrorCategoryClassifier.categorize(exception);

            // Then
            assertEquals(ErrorCategory.INBOUND_NETWORK_ERROR, result);
        }

        @Test
        @DisplayName("FeignException 예외는 OUTBOUND_NETWORK_ERROR로 분류되어야 한다.")
        void shouldCategorizeFeignExceptionAsOutboundNetworkError() {
            // Given
            FeignException exception = FeignException.errorStatus("GET", feign.Response.builder()
                .status(500)
                .request(feign.Request.create(feign.Request.HttpMethod.GET, "http://example.com", Map.of(), null, null, null))
                .build());

            // When
            ErrorCategory result = ErrorCategoryClassifier.categorize(exception);

            // Then
            assertEquals(ErrorCategory.OUTBOUND_NETWORK_ERROR, result);
        }

    }

    @Nested
    @DisplayName("애플리케이션 예외 및 기타 예외 분류 테스트")
    class ApplicationExceptionTests {

        @Test
        @DisplayName("RuntimeException 예외는 APPLICATION_ERROR로 분류되어야 한다.")
        void shouldCategorizeRuntimeExceptionAsApplicationError() {
            // Given
            RuntimeException exception = new RuntimeException("Application error");

            // When
            ErrorCategory result = ErrorCategoryClassifier.categorize(exception);

            // Then
            assertEquals(ErrorCategory.APPLICATION_ERROR, result);
        }

        @Test
        @DisplayName("정의되지 않은 예외는 UNKNOWN_ERROR로 분류되어야 한다.")
        void shouldCategorizeUnknownExceptionAsUnknownError() {
            // Given
            Exception exception = new Exception("Unknown exception");

            // When
            ErrorCategory result = ErrorCategoryClassifier.categorize(exception);

            // Then
            assertEquals(ErrorCategory.UNKNOWN_ERROR, result);
        }
    }
}