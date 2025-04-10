package com.monikit.core;

import java.util.Map;
import java.util.Objects;

/**
 * 외부에서 내 서버로 들어오는 HTTP 요청을 기록하는 로그 클래스.
 * <p>
 * 요청 URI, HTTP 메서드, 쿼리 스트링, 헤더 정보, 요청 바디,
 * 클라이언트 IP 및 User-Agent 정보를 포함하여 API 요청 이력을 추적할 수 있다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.1.0
 */
public class HttpInboundRequestLog extends AbstractLogEntry implements HttpLogEntry {

    private final String uri;
    private final String method;
    private final Map<String, String> headers;
    private final String query;
    private final String body;
    private final String clientIp;
    private final String userAgent;

    public HttpInboundRequestLog(String traceId, LogLevel logLevel, String uri, String method, String query, String body,
                                 Map<String, String> headers, String clientIp, String userAgent) {
        super(traceId, logLevel);
        this.uri = uri;
        this.method = method;
        this.query = query;
        this.body = body;
        this.headers = headers;
        this.clientIp = clientIp;
        this.userAgent = userAgent;
    }

    @Override
    public LogType getLogType() {
        return LogType.INBOUND_REQUEST;
    }

    @Override
    protected void addExtraFields(Map<String, Object> logMap) {
        logMap.put("uri", uri);
        logMap.put("method", method);
        logMap.put("query", query);
        logMap.put("body", body);
        logMap.put("headers", headers);
        logMap.put("clientIp", clientIp);
        logMap.put("userAgent", userAgent);
    }

    public static HttpInboundRequestLog create(String traceId, LogLevel logLevel, String uri, String method, String query, String body,
                                               Map<String, String> headers, String clientIp, String userAgent) {
        return new HttpInboundRequestLog(traceId, logLevel, uri, method, query, body, headers, clientIp, userAgent);
    }

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public int getStatusCode() {
        return -1; // 요청은 상태 코드가 없음
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getQuery() {
        return query;
    }

    public String getBody() {
        return body;
    }

    public String getClientIp() {
        return clientIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HttpInboundRequestLog that = (HttpInboundRequestLog) o;
        return Objects.equals(uri, that.uri) &&
            Objects.equals(method, that.method) &&
            Objects.equals(query, that.query) &&
            Objects.equals(body, that.body) &&
            Objects.equals(headers, that.headers) &&
            Objects.equals(clientIp, that.clientIp) &&
            Objects.equals(userAgent, that.userAgent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri, method, query, body, headers, clientIp, userAgent);
    }

    @Override
    public String toString() {
        return "HttpInboundRequestLog{" +
            "uri='" + uri + '\'' +
            ", method='" + method + '\'' +
            ", query='" + query + '\'' +
            ", body='" + body + '\'' +
            ", headers=" + headers +
            ", clientIp='" + clientIp + '\'' +
            ", userAgent='" + userAgent + '\'' +
            '}';
    }
}
