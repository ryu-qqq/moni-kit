package com.monikit.core;

import java.util.Map;

/**
 * 외부에서 내 서버로 들어오는 HTTP 요청을 기록하는 로그 클래스.
 * <p>
 * 요청 URI, HTTP 메서드, 쿼리 스트링, 헤더 정보, 요청 바디,
 * 클라이언트 IP 및 User-Agent 정보를 포함하여 API 요청 이력을 추적할 수 있다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
public class HttpInboundRequestLog extends AbstractLogEntry {
    private final String httpMethod;
    private final String requestUri;
    private final String queryParams;
    private final String headers;
    private final String requestBody;
    private final String clientIp;
    private final String userAgent;

    protected HttpInboundRequestLog(String traceId, String httpMethod, String requestUri, String queryParams,
                                    String headers, String requestBody, String clientIp, String userAgent, LogLevel logLevel) {
        super(traceId, logLevel);
        this.httpMethod = httpMethod;
        this.requestUri = requestUri;
        this.queryParams = queryParams;
        this.headers = headers;
        this.requestBody = requestBody;
        this.clientIp = clientIp;
        this.userAgent = userAgent;
    }

    @Override
    public LogType getLogType() {
        return LogType.INBOUND_REQUEST;
    }

    @Override
    protected void addExtraFields(Map<String, Object> logMap) {
        logMap.put("httpMethod", httpMethod);
        logMap.put("requestUri", requestUri);
        logMap.put("queryParams", queryParams);
        logMap.put("headers", headers);
        logMap.put("requestBody", requestBody);
        logMap.put("clientIp", clientIp);
        logMap.put("userAgent", userAgent);
    }

    public static HttpInboundRequestLog create(String traceId, String httpMethod, String uri, String queryParams,
                                               String headers, String requestBody, String clientIp, String userAgent, LogLevel logLevel) {
        return new HttpInboundRequestLog(traceId, httpMethod, uri, queryParams, headers, requestBody, clientIp, userAgent, logLevel);
    }
}