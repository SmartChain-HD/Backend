package com.smartchain.platform.domain.chat.client;

import com.smartchain.platform.domain.chat.config.AiChatConfig;
import com.smartchain.platform.dto.chat.AdminInspectResponse;
import com.smartchain.platform.dto.chat.AdminSyncResponse;
import com.smartchain.platform.dto.chat.ChatRequest;
import com.smartchain.platform.dto.chat.ChatResponse;
import com.smartchain.platform.global.error.CustomException;
import com.smartchain.platform.global.error.ErrorCode;
import io.netty.channel.ConnectTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;

/**
 * AI Chat API 클라이언트
 * FastAPI AI 서버와 통신
 */
@Component
public class AiChatApiClient {

    private static final Logger log = LoggerFactory.getLogger(AiChatApiClient.class);
    private static final String ADMIN_API_KEY_HEADER = "X-API-KEY";

    private final WebClient webClient;
    private final String adminApiKey;

    public AiChatApiClient(
        @Qualifier("aiChatWebClient") WebClient webClient,
        AiChatConfig config
    ) {
        this.webClient = webClient;
        this.adminApiKey = config.getAdminApiKey();
    }

    /**
     * 채팅 API 호출
     */
    public Mono<ChatResponse> chat(ChatRequest request) {
        log.info("AI Chat API 호출 - message: {}, domain: {}",
            truncateMessage(request.message()), request.domain());

        return webClient.post()
            .uri("/api/chat")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(ChatResponse.class)
            .onErrorMap(this::mapToCustomException)
            .doOnSuccess(response ->
                log.info("AI Chat API 성공 - confidence: {}", response.confidence()))
            .doOnError(error -> {
                if (!(error instanceof CustomException)) {
                    log.error("AI Chat API 실패", error);
                }
            });
    }

    /**
     * 채팅 API 동기 호출
     */
    public ChatResponse chatSync(ChatRequest request) {
        return chat(request).block();
    }

    /**
     * Admin 데이터 동기화 API 호출
     */
    public Mono<AdminSyncResponse> syncData() {
        log.info("AI Chat Admin Sync API 호출");

        return webClient.post()
            .uri("/api/admin/sync")
            .header(ADMIN_API_KEY_HEADER, adminApiKey)
            .contentType(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(AdminSyncResponse.class)
            .onErrorMap(this::mapToCustomException)
            .doOnSuccess(response ->
                log.info("AI Chat Admin Sync API 성공 - status: {}", response.status()))
            .doOnError(error -> {
                if (!(error instanceof CustomException)) {
                    log.error("AI Chat Admin Sync API 실패", error);
                }
            });
    }

    /**
     * Admin 데이터 동기화 API 동기 호출
     */
    public AdminSyncResponse syncDataSync() {
        return syncData().block();
    }

    /**
     * Admin DB 현황 조회 API 호출
     */
    public Mono<AdminInspectResponse> inspectDb() {
        log.info("AI Chat Admin Inspect API 호출");

        return webClient.get()
            .uri("/api/admin/inspect")
            .header(ADMIN_API_KEY_HEADER, adminApiKey)
            .retrieve()
            .bodyToMono(AdminInspectResponse.class)
            .onErrorMap(this::mapToCustomException)
            .doOnSuccess(response ->
                log.info("AI Chat Admin Inspect API 성공 - totalDocuments: {}", response.totalDocuments()))
            .doOnError(error -> {
                if (!(error instanceof CustomException)) {
                    log.error("AI Chat Admin Inspect API 실패", error);
                }
            });
    }

    /**
     * Admin DB 현황 조회 API 동기 호출
     */
    public AdminInspectResponse inspectDbSync() {
        return inspectDb().block();
    }

    /**
     * 에러를 CustomException으로 매핑
     */
    private Throwable mapToCustomException(Throwable throwable) {
        if (throwable instanceof CustomException) {
            return throwable;
        }

        // HTTP 에러 응답 처리
        if (throwable instanceof WebClientResponseException ex) {
            logErrorResponseBody(ex);

            if (ex.getStatusCode().is4xxClientError()) {
                log.warn("AI Chat 서비스 4xx 에러 - status: {}", ex.getStatusCode());
                return new CustomException(ErrorCode.AI_CHAT_SERVICE_ERROR);
            }
            if (ex.getStatusCode().is5xxServerError()) {
                log.error("AI Chat 서비스 5xx 에러 - status: {}", ex.getStatusCode());
                return new CustomException(ErrorCode.AI_CHAT_SERVICE_ERROR);
            }
        }

        // 타임아웃/네트워크 에러 처리
        if (isNetworkOrTimeoutError(throwable)) {
            log.error("AI Chat 서비스 연결 실패 - {}: {}",
                throwable.getClass().getSimpleName(), throwable.getMessage());
            return new CustomException(ErrorCode.AI_CHAT_TIMEOUT);
        }

        log.error("AI Chat 서비스 알 수 없는 에러 - {}: {}",
            throwable.getClass().getSimpleName(), throwable.getMessage());
        return new CustomException(ErrorCode.AI_CHAT_SERVICE_ERROR);
    }

    /**
     * 네트워크 또는 타임아웃 에러인지 확인
     */
    private boolean isNetworkOrTimeoutError(Throwable throwable) {
        if (throwable instanceof ConnectException ||
            throwable instanceof ConnectTimeoutException ||
            throwable instanceof SocketTimeoutException ||
            throwable instanceof TimeoutException) {
            return true;
        }
        if (throwable instanceof WebClientRequestException requestEx) {
            Throwable cause = requestEx.getCause();
            return cause instanceof ConnectException ||
                   cause instanceof ConnectTimeoutException ||
                   cause instanceof SocketTimeoutException ||
                   cause instanceof TimeoutException;
        }
        return false;
    }

    /**
     * AI 서비스 에러 응답 body 로깅
     */
    private void logErrorResponseBody(WebClientResponseException ex) {
        try {
            String responseBody = ex.getResponseBodyAsString();
            if (responseBody != null && !responseBody.isBlank()) {
                log.error("AI Chat 서비스 에러 응답 body: {}", responseBody);
            }
        } catch (Exception e) {
            log.debug("AI Chat 서비스 에러 응답 body 파싱 실패", e);
        }
    }

    /**
     * 로깅용 메시지 truncate (100자)
     */
    private String truncateMessage(String message) {
        if (message == null) return null;
        return message.length() > 100 ? message.substring(0, 100) + "..." : message;
    }
}
