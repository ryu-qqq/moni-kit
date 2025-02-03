package com.monikit.core;

import java.util.Map;
import java.util.Objects;

/**
 * 외부 API에서 받은 응답을 기록하는 로그 클래스.
 * <p>
 * 응답 상태 코드, 응답 헤더, 응답 바디, 실행 시간을 포함하여 API 연동 응답을 분석할 수 있다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
public class HttpOutboundResponseLog extends AbstractLogEntry {
    private final String targetUrl;
    private final int statusCode;
    private final String headers;
    private final String responseBody;
    private final long executionTime;

    protected HttpOutboundResponseLog(String traceId, String targetUrl, int statusCode, String headers,
                                      String responseBody, long executionTime, LogLevel logLevel) {
        super(traceId, logLevel);
        this.targetUrl = targetUrl;
        this.statusCode = statusCode;
        this.headers = headers;
        this.responseBody = responseBody;
        this.executionTime = executionTime;
    }

    @Override
    public LogType getLogType() {
        return LogType.OUTBOUND_RESPONSE;
    }

    @Override
    protected void addExtraFields(Map<String, Object> logMap) {
        logMap.put("targetUrl", targetUrl);
        logMap.put("statusCode", statusCode);
        logMap.put("headers", headers);
        logMap.put("responseBody", responseBody);
        logMap.put("executionTime", executionTime + "ms");
    }

    public static HttpOutboundResponseLog create(String traceId, String targetUrl, int statusCode, String headers,
                                                 String responseBody, long executionTime, LogLevel logLevel) {
        return new HttpOutboundResponseLog(traceId, targetUrl, statusCode, headers, responseBody, executionTime, logLevel);
    }

    @Override
    public boolean equals(Object object) {
        if (this
            == object) return true;
        if (object
            == null
            || getClass()
            != object.getClass()) return false;
        HttpOutboundResponseLog that = (HttpOutboundResponseLog) object;
        return statusCode
            == that.statusCode
            && executionTime
            == that.executionTime
            && Objects.equals(targetUrl, that.targetUrl)
            && Objects.equals(headers, that.headers)
            && Objects.equals(responseBody, that.responseBody);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetUrl, statusCode, headers, responseBody, executionTime);
    }

    @Override
    public String toString() {
        return "HttpOutboundResponseLog{"
            +
            "targetUrl='"
            + targetUrl
            + '\''
            +
            ", statusCode="
            + statusCode
            +
            ", headers='"
            + headers
            + '\''
            +
            ", responseBody='"
            + responseBody
            + '\''
            +
            ", executionTime="
            + executionTime
            +
            '}';
    }
}
