package com.monikit.starter.webflux;

import java.util.Set;

public class ExcludePathConstant {

    public static final Set<String> EXCLUDED_PATHS = Set.of(
        "/actuator/health",
        "/actuator/prometheus",
        "/actuator/metrics",
        "/actuator/info",
        "/metrics",
        "/health",
        "/"
    );

}
