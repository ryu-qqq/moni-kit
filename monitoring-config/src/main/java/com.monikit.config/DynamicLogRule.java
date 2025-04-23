package com.monikit.config;

/**
 * 클래스명 및 메서드명 정규식 기반 로깅 대상 규칙.
 * <p>
 * - classNamePattern: 클래스 이름 정규식 (예: ^Repository.*)
 * - methodNamePattern: 메서드 이름 정규식 (예: .*Register)
 * - when: SpEL 표현식으로 동적 조건 제어 (예: "#executionTime > 100")
 * - thresholdMillis: ExecutionLog 기록 시 포함될 기준 임계값 (ms)
 * - tag: 로그 그룹 구분용 태그 (예: "external-api", "order")
 * </p>
 *
 * @author ryu-qqq
 * @since 1.1.2
 */
public class DynamicLogRule {

    /**
     * 클래스 이름 정규식. 기본값은 모든 클래스 허용.
     */
    private String classNamePattern = ".*";

    /**
     * 메서드 이름 정규식. 기본값은 모든 메서드 허용.
     */
    private String methodNamePattern = ".*";

    /**
     * SpEL 조건식 (예: "#executionTime > 100")
     */
    private String when = "";

    private long thresholdMillis = 0;

    /**
     * 로그를 분류하기 위한 사용자 정의 태그.
     * 예: "external-api", "product-batch", "order-service"
     */
    private String tag = "";


    public String getClassNamePattern() {
        return classNamePattern;
    }

    public void setClassNamePattern(String classNamePattern) {
        this.classNamePattern = classNamePattern;
    }

    public String getMethodNamePattern() {
        return methodNamePattern;
    }

    public void setMethodNamePattern(String methodNamePattern) {
        this.methodNamePattern = methodNamePattern;
    }

    public String getWhen() {
        return when;
    }

    public void setWhen(String when) {
        this.when = when;
    }

    public long getThresholdMillis() {
        return thresholdMillis;
    }

    public void setThresholdMillis(long thresholdMillis) {
        this.thresholdMillis = thresholdMillis;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

}
