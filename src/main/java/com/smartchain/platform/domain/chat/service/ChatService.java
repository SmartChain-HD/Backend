package com.smartchain.platform.domain.chat.service;

import com.smartchain.platform.domain.chat.client.AiChatApiClient;
import com.smartchain.platform.domain.user.entity.User;
import com.smartchain.platform.dto.chat.AdminInspectResponse;
import com.smartchain.platform.dto.chat.AdminSyncResponse;
import com.smartchain.platform.dto.chat.ChatMessage;
import com.smartchain.platform.dto.chat.ChatRequest;
import com.smartchain.platform.dto.chat.ChatResponse;
import com.smartchain.platform.global.error.CustomException;
import com.smartchain.platform.global.error.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * AI Chatbot 서비스
 */
@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);
    private static final Set<String> VALID_DOMAINS = Set.of("safety", "compliance", "esg", "all");

    private final AiChatApiClient aiChatApiClient;

    public ChatService(AiChatApiClient aiChatApiClient) {
        this.aiChatApiClient = aiChatApiClient;
    }

    /**
     * 채팅 처리
     * @param request 채팅 요청
     * @param user 현재 사용자
     * @return 채팅 응답
     */
    public ChatResponse chat(ChatRequest request, User user) {
        log.info("채팅 요청 - userId: {}, domain: {}", user.getUserId(), request.domain());

        // 도메인 유효성 검증
        validateDomain(request.domain());

        // sessionId가 없으면 생성
        String sessionId = request.sessionId();
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = UUID.randomUUID().toString();
            log.debug("새 세션 ID 생성: {}", sessionId);
        }

        // 요청 재구성 (sessionId 포함)
        ChatRequest enrichedRequest = new ChatRequest(
            request.message(),
            request.history() != null ? request.history() : List.of(),
            request.domain(),
            request.docName(),
            request.topK(),
            sessionId
        );

        return aiChatApiClient.chatSync(enrichedRequest);
    }

    /**
     * Admin 데이터 동기화
     * @param user 현재 사용자 (REVIEWER 권한 필요)
     * @return 동기화 응답
     */
    public AdminSyncResponse syncData(User user) {
        log.info("Admin 동기화 요청 - userId: {}", user.getUserId());

        validateReviewerRole(user);

        return aiChatApiClient.syncDataSync();
    }

    /**
     * Admin DB 현황 조회
     * @param user 현재 사용자 (REVIEWER 권한 필요)
     * @return DB 현황 응답
     */
    public AdminInspectResponse getDbStatus(User user) {
        log.info("Admin DB 현황 조회 - userId: {}", user.getUserId());

        validateReviewerRole(user);

        return aiChatApiClient.inspectDbSync();
    }

    /**
     * 도메인 유효성 검증
     */
    private void validateDomain(String domain) {
        if (domain != null && !VALID_DOMAINS.contains(domain.toLowerCase())) {
            log.warn("유효하지 않은 도메인: {}", domain);
            throw new CustomException(ErrorCode.AI_CHAT_INVALID_DOMAIN);
        }
    }

    /**
     * REVIEWER 역할 검증
     * 어느 도메인이든 REVIEWER 역할이 있으면 Admin API 접근 가능
     */
    private void validateReviewerRole(User user) {
        String userRoleCode = user.getRole() != null ? user.getRole().getCode() : "GUEST";

        if (!"REVIEWER".equals(userRoleCode)) {
            log.warn("Admin API 접근 권한 없음 - userId: {}, role: {}", user.getUserId(), userRoleCode);
            throw new CustomException(ErrorCode.PERMISSION_DENIED_ACTION);
        }
    }
}
