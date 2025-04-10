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
 * @since 1.1.0
 */
public class HttpOutboundRequestLog extends AbstractLogEntry implements HttpLogEntry {

    private final String uri;
    private final String method;
    private final Map<String, String> headers;
    private final String query;
    private final String body;

    protected HttpOutboundRequestLog(String traceId, LogLevel logLevel, String uri, String method,
                                  Map<String, String> headers,
                                  String query, String body) {
        super(traceId, logLevel);
        this.uri = uri;
        this.method = method;
        this.headers = headers;
        this.query = query;
        this.body = body;
    }

    @Override
    public LogType getLogType() {
        return LogType.OUTBOUND_REQUEST;
    }

    @Override
    protected void addExtraFields(Map<String, Object> logMap) {
        logMap.put("uri", uri);
        logMap.put("method", method);
        logMap.put("query", query);
        logMap.put("body", body);
        logMap.put("headers", headers);
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
        return -1;
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

    public static HttpOutboundRequestLog create(String traceId, LogLevel logLevel, String uri, String method,
                                                Map<String, String> headers,
                                                String query, String body) {
        return new HttpOutboundRequestLog(traceId, logLevel, uri, method, headers, query, body);
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
        return Objects.equals(uri, that.uri)
            && Objects.equals(method, that.method)
            && Objects.equals(headers, that.headers)
            && Objects.equals(query, that.query)
            && Objects.equals(body, that.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri, method, headers, query, body);
    }

    @Override
    public String toString() {
        return "HttpOutboundRequestLog{"
            +
            "uri='"
            + uri
            + '\''
            +
            ", method='"
            + method
            + '\''
            +
            ", headers="
            + headers
            +
            ", query='"
            + query
            + '\''
            +
            ", body='"
            + body
            + '\''
            +
            '}';
    }
}
