package com.monikit.core;

import java.util.Map;
import java.util.Objects;

/**
 * 내 서버에서 외부 API로 요청을 보낼 때 실행된 HTTP 요청을 기록하는 로그 클래스.
 * <p>
 * 외부 API URL, HTTP 메서드, 요청 헤더, 요청 바디 등을 포함하여
 * API 연동 요청 이력을 추적할 수 있다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
public class HttpOutboundRequestLog extends AbstractLogEntry {
    private final String httpMethod;
    private final String targetUrl;
    private final String headers;
    private final String requestBody;
    private final long executionTime;

    protected HttpOutboundRequestLog(String traceId, String httpMethod, String targetUrl, String headers,
                                     String requestBody, long executionTime, LogLevel logLevel) {
        super(traceId, logLevel);
        this.httpMethod = httpMethod;
        this.targetUrl = targetUrl;
        this.headers = headers;
        this.requestBody = requestBody;
        this.executionTime = executionTime;
    }

    @Override
    public LogType getLogType() {
        return LogType.OUTBOUND_REQUEST;
    }

    @Override
    protected void addExtraFields(Map<String, Object> logMap) {
        logMap.put("httpMethod", httpMethod);
        logMap.put("targetUrl", targetUrl);
        logMap.put("headers", headers);
        logMap.put("requestBody", requestBody);
        logMap.put("executionTime", executionTime + "ms");
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public String getHeaders() {
        return headers;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public static HttpOutboundRequestLog create(String traceId, String httpMethod, String targetUrl, String headers,
                                                String requestBody, long executionTime, LogLevel logLevel) {
        return new HttpOutboundRequestLog(traceId, httpMethod, targetUrl, headers, requestBody, executionTime, logLevel);
    }

    @Override
    public boolean equals(Object object) {
        if (this
            == object) return true;
        if (object
            == null
            || getClass()
            != object.getClass()) return false;
        HttpOutboundRequestLog that = (HttpOutboundRequestLog) object;
        return executionTime
            == that.executionTime
            && Objects.equals(httpMethod, that.httpMethod)
            && Objects.equals(targetUrl, that.targetUrl)
            && Objects.equals(headers, that.headers)
            && Objects.equals(requestBody, that.requestBody);
    }

    @Override
    public int hashCode() {
        return Objects.hash(httpMethod, targetUrl, headers, requestBody, executionTime);
    }

    @Override
    public String toString() {
        return "HttpOutboundRequestLog{"
            +
            "httpMethod='"
            + httpMethod
            + '\''
            +
            ", targetUrl='"
            + targetUrl
            + '\''
            +
            ", headers='"
            + headers
            + '\''
            +
            ", requestBody='"
            + requestBody
            + '\''
            +
            ", executionTime="
            + executionTime
            +
            '}';
    }
}
