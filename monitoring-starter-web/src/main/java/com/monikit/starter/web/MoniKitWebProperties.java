package com.monikit.starter.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Web 환경에서 사용되는 MoniKit의 웹 설정을 바인딩하는 클래스입니다.
 * <p>
 * 설정 접두사는 <code>monikit.web</code>이며,
 * 주로 로깅 제외 경로와 관련된 설정을 포함합니다.
 * </p>
 *
 * <pre>
 * monikit:
 *   web:
 *     excluded-paths:
 *       - /health
 *       - /metrics
 * </pre>
 *
 * @author ryu-qqq
 * @since 1.1.2
 */
@ConfigurationProperties(prefix = "monikit.web")
public class MoniKitWebProperties {

    /**
     * 로깅 대상에서 제외할 경로 리스트입니다.
     */
    private List<String> excludedPaths = new ArrayList<>();

    public List<String> getExcludedPaths() {
        return excludedPaths;
    }

    public void setExcludedPaths(List<String> excludedPaths) {
        this.excludedPaths = excludedPaths;
    }

}

