package com.smartchain.platform.domain.ai.client;

import com.smartchain.platform.dto.ai.run.RunPreviewRequest;
import com.smartchain.platform.dto.ai.run.RunPreviewResponse;
import com.smartchain.platform.dto.ai.run.RunSubmitRequest;
import com.smartchain.platform.dto.ai.run.RunSubmitResponse;
import com.smartchain.platform.global.error.CustomException;
import com.smartchain.platform.global.error.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
 * AI Run API 클라이언트
 * 공통 /run/preview, /run/submit 엔드포인트 호출
 */
@Component
public class AiRunApiClient {

    private static final Logger log = LoggerFactory.getLogger(AiRunApiClient.class);

    private final WebClient webClient;
    private final int maxRetry;

    public AiRunApiClient(
        @Qualifier("aiRunApiWebClient") WebClient webClient,
        com.smartchain.platform.domain.ai.config.AiRunApiConfig config
    ) {
        this.webClient = webClient;
        this.maxRetry = config.getMaxRetry();
    }

    /**
     * Preview API 호출
     * 파일 추가 시 슬롯 추정 및 필수 항목 현황 반환
     */
    public Mono<RunPreviewResponse> preview(RunPreviewRequest request) {
        log.info("AI Run API preview 호출 - domain: {}, packageId: {}, files: {}",
            request.domain(), request.packageId(),
            request.addedFiles() != null ? request.addedFiles().size() : 0);

        return webClient.post()
            .uri("/run/preview")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(RunPreviewResponse.class)
            .retryWhen(Retry.backoff(maxRetry, Duration.ofSeconds(1))
                .filter(this::isRetryableError)
                .onRetryExhaustedThrow((spec, signal) ->
                    new CustomException(ErrorCode.AI_SERVICE_UNAVAILABLE)))
            .doOnSuccess(response ->
                log.info("AI Run API preview 성공 - packageId: {}", response.packageId()))
            .doOnError(error ->
                log.error("AI Run API preview 실패", error));
    }

    /**
     * Submit API 호출
     * 전체 파일 검증 및 최종 판정 결과 반환
     */
    public Mono<RunSubmitResponse> submit(RunSubmitRequest request) {
        log.info("AI Run API submit 호출 - domain: {}, packageId: {}, files: {}",
            request.domain(), request.packageId(),
            request.files() != null ? request.files().size() : 0);

        return webClient.post()
            .uri("/run/submit")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(RunSubmitResponse.class)
            .retryWhen(Retry.backoff(maxRetry, Duration.ofSeconds(2))
                .filter(this::isRetryableError)
                .onRetryExhaustedThrow((spec, signal) ->
                    new CustomException(ErrorCode.AI_SERVICE_UNAVAILABLE)))
            .doOnSuccess(response ->
                log.info("AI Run API submit 성공 - packageId: {}, verdict: {}",
                    response.packageId(), response.verdict()))
            .doOnError(error ->
                log.error("AI Run API submit 실패", error));
    }

    /**
     * Preview API 동기 호출
     */
    public RunPreviewResponse previewSync(RunPreviewRequest request) {
        return preview(request).block();
    }

    /**
     * Submit API 동기 호출
     */
    public RunSubmitResponse submitSync(RunSubmitRequest request) {
        return submit(request).block();
    }

    private boolean isRetryableError(Throwable throwable) {
        if (throwable instanceof WebClientResponseException ex) {
            // 5xx 에러만 재시도
            return ex.getStatusCode().is5xxServerError();
        }
        // 네트워크 에러도 재시도
        return throwable instanceof java.net.ConnectException;
    }
}
