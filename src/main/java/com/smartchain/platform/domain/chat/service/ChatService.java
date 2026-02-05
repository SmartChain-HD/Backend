package com.smartchain.platform.domain.chat.service;

import com.smartchain.platform.domain.chat.client.AiChatApiClient;
import com.smartchain.platform.domain.evidence.entity.EvidenceFile;
import com.smartchain.platform.domain.evidence.repository.EvidenceFileRepository;
import com.smartchain.platform.domain.file.storage.FileStorageService;
import com.smartchain.platform.domain.user.entity.User;
import com.smartchain.platform.dto.chat.AdminInspectResponse;
import com.smartchain.platform.dto.chat.AdminSyncResponse;
import com.smartchain.platform.dto.chat.ChatRequest;
import com.smartchain.platform.dto.chat.ChatResponse;
import com.smartchain.platform.global.error.CustomException;
import com.smartchain.platform.global.error.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.smartchain.platform.domain.user.entity.Domain;

import java.time.Duration;
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
    private static final Duration FILE_URL_EXPIRY = Duration.ofMinutes(30);

    private final AiChatApiClient aiChatApiClient;
    private final EvidenceFileRepository evidenceFileRepository;
    private final FileStorageService fileStorageService;

    @Value("${server.port:8080}")
    private int serverPort;

    public ChatService(
        AiChatApiClient aiChatApiClient,
        EvidenceFileRepository evidenceFileRepository,
        FileStorageService fileStorageService
    ) {
        this.aiChatApiClient = aiChatApiClient;
        this.evidenceFileRepository = evidenceFileRepository;
        this.fileStorageService = fileStorageService;
    }

    /**
     * 채팅 처리
     * @param request 채팅 요청
     * @param user 현재 사용자
     * @return 채팅 응답
     */
    public ChatResponse chat(ChatRequest request, User user) {
        log.info("채팅 요청 - userId: {}, domain: {}, fileId: {}",
            user.getUserId(), request.domain(), request.fileId());

        // 도메인 유효성 검증
        validateDomain(request.domain());

        // fileId → presigned URL 변환
        String fileUrl = resolveFileUrl(request.fileId(), user);

        // sessionId가 없으면 생성
        String sessionId = request.sessionId();
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = UUID.randomUUID().toString();
            log.debug("새 세션 ID 생성: {}", sessionId);
        }

        // 요청 재구성 (fileUrl, sessionId 포함)
        ChatRequest enrichedRequest = new ChatRequest(
            request.message(),
            null,
            fileUrl,
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
     * fileId로 EvidenceFile 조회 후 presigned URL 생성
     * @return presigned URL 또는 null (fileId가 없는 경우)
     */
    private String resolveFileUrl(Long fileId, User user) {
        if (fileId == null) {
            return null;
        }

        EvidenceFile evidenceFile = evidenceFileRepository.findById(fileId)
            .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));

        // 파일 소속 회사와 사용자 회사 일치 확인
        if (evidenceFile.getDiagnostic() != null
                && evidenceFile.getDiagnostic().getCompany() != null
                && user.getCompany() != null) {
            Long fileCompanyId = evidenceFile.getDiagnostic().getCompany().getCompanyId();
            Long userCompanyId = user.getCompany().getCompanyId();
            if (!fileCompanyId.equals(userCompanyId)) {
                log.warn("파일 접근 권한 없음 - userId: {}, fileId: {}, fileCompanyId: {}, userCompanyId: {}",
                    user.getUserId(), fileId, fileCompanyId, userCompanyId);
                throw new CustomException(ErrorCode.PERMISSION_DENIED_ACTION);
            }
        }

        String presignedUrl = fileStorageService.getPresignedUrl(evidenceFile.getFilePath(), FILE_URL_EXPIRY);

        // 로컬 환경: 상대 경로를 절대 URL로 변환 (Python이 다운로드 가능하도록)
        if (presignedUrl.startsWith("/")) {
            presignedUrl = "http://localhost:" + serverPort + presignedUrl;
        }

        log.debug("파일 URL 생성 - fileId: {}, fileName: {}", fileId, evidenceFile.getOriginalFileName());
        return presignedUrl;
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
        // 도메인 기반: 어떤 도메인이든 REVIEWER 역할이 있으면 허용
        List<Domain> reviewerDomains = user.getDomainsWithRole("REVIEWER");
        if (reviewerDomains != null && !reviewerDomains.isEmpty()) {
            return;
        }

        // 전역 역할 fallback (레거시)
        String userRoleCode = user.getRole() != null ? user.getRole().getCode() : "GUEST";
        if (!"REVIEWER".equals(userRoleCode)) {
            log.warn("Admin API 접근 권한 없음 - userId: {}, role: {}", user.getUserId(), userRoleCode);
            throw new CustomException(ErrorCode.PERMISSION_DENIED_ACTION);
        }
    }
}
