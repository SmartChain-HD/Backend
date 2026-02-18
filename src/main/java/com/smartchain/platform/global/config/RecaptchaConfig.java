package com.smartchain.platform.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "recaptcha")
public class RecaptchaConfig {

    private String secretKey;
    private double scoreThreshold = 0.1;
    private boolean enabled = true;
}
