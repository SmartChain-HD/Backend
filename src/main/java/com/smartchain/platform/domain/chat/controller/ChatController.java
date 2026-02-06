package com.smartchain.platform.domain.chat.controller;

import com.smartchain.platform.domain.chat.service.ChatService;
import com.smartchain.platform.dto.chat.AdminInspectResponse;
import com.smartchain.platform.dto.chat.AdminSyncResponse;
import com.smartchain.platform.dto.chat.ChatRequest;
import com.smartchain.platform.dto.chat.ChatResponse;
import com.smartchain.platform.global.response.BaseResponse;
import com.smartchain.platform.global.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI Chatbot 컨트롤러
 */
@Tag(name = "AI Chat", description = "AI 챗봇 API")
@RestController
@RequestMapping("/api/v1")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ChatService chatService;
    private final JwtTokenProvider jwtTokenProvider;

    public ChatController(ChatService chatService, JwtTokenProvider jwtTokenProvider) {
        this.chatService = chatService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Operation(summary = "AI 채팅", description = "RAG 기반 AI 챗봇과 대화합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "채팅 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효하지 않은 도메인 등)"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "500", description = "AI 서비스 오류"),
        @ApiResponse(responseCode = "504", description = "AI 서비스 타임아웃")
    })
    @PostMapping("/chat")
    public ResponseEntity<BaseResponse<ChatResponse>> chat(
        @Valid @RequestBody ChatRequest request,
        HttpServletRequest httpRequest
    ) {
        Long userId = extractUserIdFromRequest(httpRequest);
        log.info("채팅 API 호출 - userId: {}", userId);

        ChatResponse response = chatService.chat(request, userId);

        return ResponseEntity.ok(BaseResponse.success("채팅 완료", response));
    }

    @Operation(summary = "Admin 데이터 동기화", description = "Vector DB에 PDF/소스코드를 동기화합니다. (REVIEWER 전용)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "동기화 요청 접수"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (REVIEWER만 접근 가능)")
    })
    @PostMapping("/admin/chat/sync")
    public ResponseEntity<BaseResponse<AdminSyncResponse>> syncData(
        HttpServletRequest httpRequest
    ) {
        Long userId = extractUserIdFromRequest(httpRequest);
        log.info("Admin 동기화 API 호출 - userId: {}", userId);

        AdminSyncResponse response = chatService.syncData(userId);

        return ResponseEntity.ok(BaseResponse.success("동기화 요청이 접수되었습니다", response));
    }

    @Operation(summary = "Admin DB 현황 조회", description = "Vector DB에 저장된 문서 현황을 조회합니다. (REVIEWER 전용)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (REVIEWER만 접근 가능)")
    })
    @GetMapping("/admin/chat/inspect")
    public ResponseEntity<BaseResponse<AdminInspectResponse>> inspectDb(
        HttpServletRequest httpRequest
    ) {
        Long userId = extractUserIdFromRequest(httpRequest);
        log.info("Admin DB 현황 조회 API 호출 - userId: {}", userId);

        AdminInspectResponse response = chatService.getDbStatus(userId);

        return ResponseEntity.ok(BaseResponse.success("DB 현황 조회 완료", response));
    }

    private Long extractUserIdFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            return jwtTokenProvider.getUserIdFromToken(token);
        }
        throw new IllegalArgumentException("Authorization header is missing or invalid");
    }
}
