package com.monikit.metric;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HttpRequestMetricUtils 테스트")
class HttpRequestMetricUtilsTest {

    @Nested
    @DisplayName("normalizeUri() 메서드 테스트")
    class NormalizeUriTests {

        @ParameterizedTest
        @DisplayName("숫자를 {param}으로 변환해야 한다")
        @CsvSource({
            "/api/products/123, /api/products/{param}",
            "/user/98765/orders, /user/{param}/orders",
            "/v1/order/10001/detail, /v1/order/{param}/detail"
        })
        void shouldReplaceNumbersWithParam(String input, String expected) {
            // When
            String normalizedUri = HttpRequestMetricUtils.normalizeUri(input);

            // Then
            assertEquals(expected, normalizedUri);
        }


        @Test
        @DisplayName("정규화할 값이 없는 경우 원본 URI를 반환해야 한다")
        void shouldReturnOriginalUriIfNoNormalizationNeeded() {
            // Given
            String originalUri = "/api/products/all";

            // When
            String normalizedUri = HttpRequestMetricUtils.normalizeUri(originalUri);

            // Then
            assertEquals(originalUri, normalizedUri);
        }
    }
}