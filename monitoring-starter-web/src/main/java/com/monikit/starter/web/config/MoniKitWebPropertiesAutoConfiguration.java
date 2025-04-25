package com.monikit.starter.web.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.monikit.starter.web.MoniKitWebProperties;

import jakarta.annotation.PostConstruct;

/**
 * MoniKit의 웹 설정을 자동 등록하는 AutoConfiguration 클래스.
 *
 * @author ryu
 * @since 1.1.2
 */
@AutoConfiguration
@EnableConfigurationProperties(MoniKitWebProperties.class)
public class MoniKitWebPropertiesAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(MoniKitWebPropertiesAutoConfiguration.class);
    private final MoniKitWebProperties props;

    public MoniKitWebPropertiesAutoConfiguration(MoniKitWebProperties props) {
        this.props = props;
    }

    @PostConstruct
    public void logConfiguration() {
        if(props.getExcludedPaths().isEmpty()){
            logger.warn("[MoniKit] No excludedPaths defined. All requests will be logged.");
        }else{
            logger.info("[MoniKit] Web Configuration Loaded: excludedPaths={}", props.getExcludedPaths());
        }
    }

}