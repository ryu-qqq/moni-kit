package com.monikit.metric;

public class HttpRequestMetricUtils {

    private HttpRequestMetricUtils() {
        throw new UnsupportedOperationException("Utility class should not be instantiated.");
    }

    /**
     * 요청 URI를 정규화하여 특정 값(숫자)을 `{param}`으로 변환
     */
    public static String normalizeUri(String uri) {
        return uri.replaceAll("/\\d+", "/{param}"); // 숫자 변환만 수행
    }
}
