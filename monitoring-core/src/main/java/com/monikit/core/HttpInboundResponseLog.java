package com.monikit.core;

import java.util.Map;
import java.util.Objects;

/**
 * 내 서버가 클라이언트에게 반환하는 HTTP 응답을 기록하는 로그 클래스.
 * <p>
 * 응답 상태 코드, 요청 URI, HTTP 메서드, 헤더 정보, 실행 시간을 포함하여 API 성능 모니터링 및 분석에 활용된다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.1.0
 */
public class HttpInboundResponseLog extends AbstractLogEntry implements HttpLogEntry {

    private final String method;
    private final String uri;
    private final int statusCode;
    private final Map<String, String> headers;
    private final String responseBody;
    private final long executionTime;

    public HttpInboundResponseLog(String traceId, LogLevel logLevel, String method, String uri, int statusCode,
                                  Map<String, String> headers, String responseBody, long executionTime) {
        super(traceId, logLevel);
        this.method = method;
        this.uri = uri;
        this.statusCode = statusCode;
        this.headers = headers;
        this.responseBody = responseBody;
        this.executionTime = executionTime;
    }

    public static HttpInboundResponseLog create(String traceId, LogLevel logLevel, String method, String uri, int statusCode,
                                                Map<String, String> headers, String responseBody, long executionTime) {
        return new HttpInboundResponseLog(traceId, logLevel, method, uri, statusCode, headers, responseBody, executionTime);
    }

    @Override
    public LogType getLogType() {
        return LogType.INBOUND_RESPONSE;
    }

    @Override
    protected void addExtraFields(Map<String, Object> logMap) {
        logMap.put("method", method);
        logMap.put("uri", uri);
        logMap.put("statusCode", statusCode);
        logMap.put("headers", headers);
        logMap.put("responseBody", responseBody);
        logMap.put("executionTime", executionTime + "ms");
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    @Override
    public boolean equals(Object object) {
        if (this
            == object) return true;
        if (object
            == null
            || getClass()
            != object.getClass()) return false;
        HttpInboundResponseLog that = (HttpInboundResponseLog) object;
        return statusCode
            == that.statusCode
            && executionTime
            == that.executionTime
            && Objects.equals(method, that.method)
            && Objects.equals(uri, that.uri)
            && Objects.equals(headers, that.headers)
            && Objects.equals(responseBody, that.responseBody);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, uri, statusCode, headers, responseBody, executionTime);
    }

    @Override
    public String toString() {
        return "HttpInboundResponseLog{"
            +
            "method='"
            + method
            + '\''
            +
            ", uri='"
            + uri
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