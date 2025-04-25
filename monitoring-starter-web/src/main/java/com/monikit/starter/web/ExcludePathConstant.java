package com.monikit.starter.web;

import java.util.List;

public class ExcludePathConstant {

    public static List<String> EXCLUDED_PATHS = List.of(
        "/actuator/health",
        "/actuator/prometheus",
        "/actuator/metrics",
        "/actuator/info",
        "/metrics",
        "/health",
        "/"
    );

}
