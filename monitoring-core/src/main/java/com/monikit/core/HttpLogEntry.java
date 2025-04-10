package com.monikit.core;

import java.util.Map;

/**
 * HTTP 로그 공통 속성을 정의하는 인터페이스.
 * <p>
 * Inbound/Outbound, Request/Response 상관없이 공통으로 가지는 URI, Method, Header, Status 등
 * 공통 항목을 다룰 수 있도록 추상화한 구조입니다.
 * - 이 인터페이스를 활용하면 Http 로그들에 대한 필터링, 분류, 처리 전략을 공통화할 수 있습니다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.1.0
 */
public interface HttpLogEntry extends LogEntry {

    /**
     * 요청 또는 응답 대상 URI
     */
    String getUri();

    /**
     * HTTP Method (예: GET, POST)
     */
    String getMethod();

    /**
     * 응답인 경우 상태 코드, 요청인 경우 -1 반환
     */
    int getStatusCode();

    /**
     * 요청 또는 응답의 헤더 맵
     */
    Map<String, String> getHeaders();


}

