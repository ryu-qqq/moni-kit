package com.monikit.starter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "monikit.logging")
public class MoniKitLoggingProperties {
    private boolean detailedLogging = false;

    public boolean isDetailedLogging() {
        return detailedLogging;
    }

    public void setDetailedLogging(boolean detailedLogging) {
        this.detailedLogging = detailedLogging;
    }
}