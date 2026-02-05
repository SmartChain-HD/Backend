package com.smartchain.platform.domain.risk.client;

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

    private final WebClient webClient;
    private final int maxRetry;

    public ExternalRiskApiClient(
        @Qualifier("externalRiskApiWebClient") WebClient webClient,
        ExternalRiskApiConfig config
    ) {
        this.webClient = webClient;
        this.maxRetry = config.getMaxRetry();
    }

    public ExternalRiskDetectResponse detect(ExternalRiskDetectRequest request) {
        log.info("외부 위험 감지 API 호출 - vendors: {}", request.vendors());

        return webClient.post()
            .uri("/risk/external/detect")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(ExternalRiskDetectResponse.class)
            .retryWhen(Retry.backoff(maxRetry, Duration.ofSeconds(1))
                .filter(this::isRetryableError)
                .onRetryExhaustedThrow((spec, signal) ->
                    new CustomException(ErrorCode.RISK_DETECT_FAILED)))
            .onErrorMap(this::mapToCustomException)
            .doOnSuccess(response ->
                log.info("외부 위험 감지 API 성공 - results: {}",
                    response.results() != null ? response.results().size() : 0))
            .doOnError(error -> {
                if (!(error instanceof CustomException)) {
                    log.error("외부 위험 감지 API 실패", error);
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

    private Throwable mapToCustomException(Throwable throwable) {
        if (throwable instanceof CustomException) {
            return throwable;
        }

        if (throwable instanceof WebClientResponseException ex) {
            log.error("외부 위험 감지 API 에러 응답 - status: {}, body: {}",
                ex.getStatusCode(), ex.getResponseBodyAsString());
        }

        return new CustomException(ErrorCode.RISK_DETECT_FAILED);
    }
}
