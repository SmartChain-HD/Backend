package com.smartchain.platform.domain.diagnostic.controller;

import com.smartchain.platform.domain.diagnostic.service.DiagnosticService;
import com.smartchain.platform.dto.diagnostic.ai.AiAnalysisResponse;
import com.smartchain.platform.dto.diagnostic.create.DiagnosticCreateRequest;
import com.smartchain.platform.dto.diagnostic.create.DiagnosticCreateResponse;
import com.smartchain.platform.dto.diagnostic.detail.DiagnosticDetailResponse;
import com.smartchain.platform.dto.diagnostic.history.DiagnosticHistoryResponse;
import com.smartchain.platform.dto.diagnostic.list.DiagnosticListResponse;
import com.smartchain.platform.dto.diagnostic.submit.DiagnosticSubmitRequest;
import com.smartchain.platform.dto.diagnostic.submit.DiagnosticSubmitResponse;
import com.smartchain.platform.global.response.BaseResponse;
import com.smartchain.platform.global.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/diagnostics")
@RequiredArgsConstructor
@Tag(name = "Diagnostic", description = "기안 관리 API")
public class DiagnosticController {

    private final DiagnosticService diagnosticService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "기안 목록 조회", description = "기안 목록을 조회합니다. DRAFTER는 본인 기안만, APPROVER는 소속 회사 전체 기안을 조회합니다.")
    @GetMapping
    public ResponseEntity<BaseResponse<DiagnosticListResponse>> getDiagnosticList(
            HttpServletRequest request,
            @Parameter(description = "상태 필터 (쉼표로 구분, 예: WRITING,SUBMITTED)")
            @RequestParam(required = false) String statuses,
            @Parameter(description = "마감일 시작")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadlineFrom,
            @Parameter(description = "마감일 종료")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadlineTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = extractUserIdFromRequest(request);
        DiagnosticListResponse response = diagnosticService.getDiagnosticList(userId, statuses, deadlineFrom, deadlineTo, page, size);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "기안 상세 조회", description = "특정 기안의 상세 정보를 조회합니다.")
    @GetMapping("/{diagnosticId}")
    public ResponseEntity<BaseResponse<DiagnosticDetailResponse>> getDiagnosticDetail(
            HttpServletRequest request,
            @PathVariable Long diagnosticId) {
        Long userId = extractUserIdFromRequest(request);
        DiagnosticDetailResponse response = diagnosticService.getDiagnosticDetail(userId, diagnosticId);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "기안 생성", description = "새로운 기안을 생성합니다. DRAFTER 권한 필요.")
    @PostMapping
    public ResponseEntity<BaseResponse<DiagnosticCreateResponse>> createDiagnostic(
            HttpServletRequest request,
            @Valid @RequestBody DiagnosticCreateRequest createRequest) {
        Long userId = extractUserIdFromRequest(request);
        DiagnosticCreateResponse response = diagnosticService.createDiagnostic(userId, createRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.success("기안이 생성되었습니다", response));
    }

    @Operation(summary = "기안 제출", description = "기안을 결재자에게 제출합니다. DRAFTER 권한 필요.")
    @PostMapping("/{diagnosticId}/submit")
    public ResponseEntity<BaseResponse<DiagnosticSubmitResponse>> submitDiagnostic(
            HttpServletRequest request,
            @PathVariable Long diagnosticId,
            @Valid @RequestBody DiagnosticSubmitRequest submitRequest) {
        Long userId = extractUserIdFromRequest(request);
        DiagnosticSubmitResponse response = diagnosticService.submitDiagnostic(userId, diagnosticId, submitRequest);
        return ResponseEntity.ok(BaseResponse.success(response.getMessage(), response));
    }

    @Operation(summary = "AI 분석 결과 조회", description = "기안의 AI 분석 결과를 조회합니다. DRAFTER, APPROVER 권한 필요.")
    @GetMapping("/{diagnosticId}/ai-analysis")
    public ResponseEntity<BaseResponse<AiAnalysisResponse>> getAiAnalysis(
            HttpServletRequest request,
            @PathVariable Long diagnosticId) {
        Long userId = extractUserIdFromRequest(request);
        AiAnalysisResponse response = diagnosticService.getAiAnalysis(userId, diagnosticId);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "기안 이력 조회", description = "기안의 상태 변경 이력을 조회합니다. DRAFTER, APPROVER 권한 필요.")
    @GetMapping("/{diagnosticId}/history")
    public ResponseEntity<BaseResponse<DiagnosticHistoryResponse>> getDiagnosticHistory(
            HttpServletRequest request,
            @PathVariable Long diagnosticId) {
        Long userId = extractUserIdFromRequest(request);
        DiagnosticHistoryResponse response = diagnosticService.getDiagnosticHistory(userId, diagnosticId);
        return ResponseEntity.ok(BaseResponse.success(response));
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
