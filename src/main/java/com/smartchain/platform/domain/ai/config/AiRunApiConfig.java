package com.smartchain.platform.domain.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * AI Run API 설정
 */
@Configuration
@ConfigurationProperties(prefix = "ai.run-api")
public class AiRunApiConfig {

    private String url = "http://localhost:8000";
    private int timeoutSeconds = 180;
    private int maxRetry = 3;
    private int previewTimeoutSeconds = 30;
    private int previewMaxRetry = 1;

    @Bean
    public WebClient aiRunApiWebClient() {
        HttpClient httpClient = HttpClient.create()
            .responseTimeout(Duration.ofSeconds(timeoutSeconds));

        return WebClient.builder()
            .baseUrl(url)
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
    }

    // Getters and Setters
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public int getMaxRetry() {
        return maxRetry;
    }

    public void setMaxRetry(int maxRetry) {
        this.maxRetry = maxRetry;
    }

    public int getPreviewTimeoutSeconds() {
        return previewTimeoutSeconds;
    }

    public void setPreviewTimeoutSeconds(int previewTimeoutSeconds) {
        this.previewTimeoutSeconds = previewTimeoutSeconds;
    }

    public int getPreviewMaxRetry() {
        return previewMaxRetry;
    }

    public void setPreviewMaxRetry(int previewMaxRetry) {
        this.previewMaxRetry = previewMaxRetry;
    }
}
