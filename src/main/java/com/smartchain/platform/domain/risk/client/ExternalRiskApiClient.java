package com.smartchain.platform.domain.risk.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartchain.platform.domain.risk.config.ExternalRiskApiConfig;
import com.smartchain.platform.dto.risk.ExternalRiskDetectRequest;
import com.smartchain.platform.dto.risk.ExternalRiskDetectResponse;
import com.smartchain.platform.global.error.CustomException;
import com.smartchain.platform.global.error.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
public class ExternalRiskApiClient {

    private static final Logger log = LoggerFactory.getLogger(ExternalRiskApiClient.class);
    private static final String DETECT_PATH = "/risk/external/detect";
    private static final int ERROR_BODY_LOG_LIMIT = 300;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final int maxRetry;

    public ExternalRiskApiClient(
        @Qualifier("externalRiskApiWebClient") WebClient webClient,
        ObjectMapper objectMapper,
        ExternalRiskApiConfig config
    ) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
        this.baseUrl = config.getUrl();
        this.maxRetry = config.getMaxRetry();
    }

    public ExternalRiskDetectResponse detect(ExternalRiskDetectRequest request) {
        String detectUrl = buildDetectUrl();
        log.info("External risk detect API call - url: {}, vendors: {}", detectUrl, request.vendors());
        log.debug("External risk detect request payload: {}", toJsonSafely(request));

        return webClient.post()
            .uri(DETECT_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(ExternalRiskDetectResponse.class)
            .retryWhen(Retry.backoff(maxRetry, Duration.ofSeconds(1))
                .filter(this::isRetryableError)
                .onRetryExhaustedThrow((spec, signal) ->
                    new CustomException(ErrorCode.RISK_DETECT_FAILED)))
            .onErrorMap(error -> mapToCustomException(error, detectUrl))
            .doOnSuccess(response ->
                log.info("External risk detect API success - results: {}",
                    response.results() != null ? response.results().size() : 0))
            .doOnError(error -> {
                if (!(error instanceof CustomException)) {
                    log.error("External risk detect API unexpected failure", error);
                }
            })
            .block();
    }

    private boolean isRetryableError(Throwable throwable) {
        if (throwable instanceof WebClientResponseException ex) {
            return ex.getStatusCode().is5xxServerError();
        }
        return throwable instanceof WebClientRequestException;
    }

    private Throwable mapToCustomException(Throwable throwable, String detectUrl) {
        if (throwable instanceof CustomException) {
            return throwable;
        }

        if (throwable instanceof WebClientResponseException ex) {
            log.error("External risk detect API response error - url: {}, status: {}, body: {}",
                detectUrl, ex.getStatusCode(), truncate(ex.getResponseBodyAsString()));
        } else if (throwable instanceof WebClientRequestException ex) {
            log.error("External risk detect API request error - url: {}, message: {}",
                detectUrl, ex.getMessage());
        }

        return new CustomException(ErrorCode.RISK_DETECT_FAILED);
    }

    private String toJsonSafely(ExternalRiskDetectRequest request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            return "<json-serialize-failed>";
        }
    }

    private String buildDetectUrl() {
        String trimmedBaseUrl = baseUrl != null ? baseUrl.replaceAll("/+$", "") : "";
        return trimmedBaseUrl + DETECT_PATH;
    }

    private String truncate(String value) {
        if (value == null) {
            return "";
        }
        if (value.length() <= ERROR_BODY_LOG_LIMIT) {
            return value;
        }
        return value.substring(0, ERROR_BODY_LOG_LIMIT) + "...";
    }
}
