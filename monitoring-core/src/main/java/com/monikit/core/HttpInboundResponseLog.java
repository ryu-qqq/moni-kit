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
 * @since 1.0
 */
public class HttpInboundResponseLog extends AbstractLogEntry {
    private final String httpMethod;
    private final String requestUri;
    private final int statusCode;
    private final String headers;
    private final String responseBody;
    private final long executionTime;

    protected HttpInboundResponseLog(String traceId, String httpMethod, String requestUri, int statusCode,
                                     String headers, String responseBody, long executionTime, LogLevel logLevel) {
        super(traceId, logLevel);
        this.httpMethod = httpMethod;
        this.requestUri = requestUri;
        this.statusCode = statusCode;
        this.headers = headers;
        this.responseBody = responseBody;
        this.executionTime = executionTime;
    }

    @Override
    public LogType getLogType() {
        return LogType.INBOUND_RESPONSE;
    }

    @Override
    protected void addExtraFields(Map<String, Object> logMap) {
        logMap.put("httpMethod", httpMethod);
        logMap.put("requestUri", requestUri);
        logMap.put("statusCode", statusCode);
        logMap.put("headers", headers);
        logMap.put("responseBody", responseBody);
        logMap.put("executionTime", executionTime + "ms");
    }


    public static HttpInboundResponseLog create(String traceId, String httpMethod, String requestUri, int statusCode,
                                                String headers, String responseBody, long executionTime, LogLevel logLevel) {
        return new HttpInboundResponseLog(traceId, httpMethod, requestUri, statusCode, headers, responseBody, executionTime, logLevel);
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getHeaders() {
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
            && Objects.equals(httpMethod, that.httpMethod)
            && Objects.equals(requestUri, that.requestUri)
            && Objects.equals(headers, that.headers)
            && Objects.equals(responseBody, that.responseBody);
    }

    @Override
    public int hashCode() {
        return Objects.hash(httpMethod, requestUri, statusCode, headers, responseBody, executionTime);
    }

    @Override
    public String toString() {
        return "HttpInboundResponseLog{"
            +
            "httpMethod='"
            + httpMethod
            + '\''
            +
            ", requestUri='"
            + requestUri
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
