package com.smartchain.platform.domain.approval.controller;

import com.smartchain.platform.domain.approval.service.ApprovalService;
import com.smartchain.platform.dto.approval.decision.ApprovalDecisionRequest;
import com.smartchain.platform.dto.approval.decision.ApprovalDecisionResponse;
import com.smartchain.platform.dto.approval.detail.ApprovalDetailResponse;
import com.smartchain.platform.dto.approval.list.ApprovalListResponse;
import com.smartchain.platform.dto.approval.submit.SubmitToReviewerRequest;
import com.smartchain.platform.dto.approval.submit.SubmitToReviewerResponse;
import com.smartchain.platform.global.response.BaseResponse;
import com.smartchain.platform.global.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/approvals")
@RequiredArgsConstructor
@Tag(name = "Approval", description = "결재 관리 API")
public class ApprovalController {

    private final ApprovalService approvalService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "결재 대기 목록 조회", description = "결재 대기 목록을 조회합니다. APPROVER 권한이 필요합니다. domainCode 파라미터로 도메인별 필터링이 가능합니다.")
    @GetMapping
    public ResponseEntity<BaseResponse<ApprovalListResponse>> getApprovalList(
            HttpServletRequest request,
            @Parameter(description = "도메인 필터 (ESG, SAFETY, COMPLIANCE)")
            @RequestParam(required = false) String domainCode,
            @Parameter(description = "결재 상태 필터 (WAITING, APPROVED, REJECTED)")
            @RequestParam(required = false) String status,
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "10") int size) {
        Long userId = extractUserIdFromRequest(request);
        ApprovalListResponse response = approvalService.getApprovalList(userId, domainCode, status, page, size);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "결재 상세 조회", description = "특정 결재 요청의 상세 정보를 조회합니다.")
    @GetMapping("/{approvalId}")
    public ResponseEntity<BaseResponse<ApprovalDetailResponse>> getApprovalDetail(
            HttpServletRequest request,
            @Parameter(description = "결재 ID")
            @PathVariable Long approvalId) {
        Long userId = extractUserIdFromRequest(request);
        ApprovalDetailResponse response = approvalService.getApprovalDetail(userId, approvalId);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "결재 처리 (승인/반려)", description = "결재 요청을 승인하거나 반려합니다. WAITING 상태에서만 처리 가능합니다.")
    @PatchMapping("/{approvalId}")
    public ResponseEntity<BaseResponse<ApprovalDecisionResponse>> processApproval(
            HttpServletRequest request,
            @Parameter(description = "결재 ID")
            @PathVariable Long approvalId,
            @Valid @RequestBody ApprovalDecisionRequest decisionRequest) {
        Long userId = extractUserIdFromRequest(request);
        ApprovalDecisionResponse response = approvalService.processApproval(userId, approvalId, decisionRequest);
        return ResponseEntity.ok(BaseResponse.success(response.getMessage(), response));
    }

    @Operation(summary = "원청 제출", description = "승인된 결재를 원청(수신자)에게 제출합니다. 승인된 결재만 제출 가능합니다.")
    @PostMapping("/{approvalId}/submit-to-reviewer")
    public ResponseEntity<BaseResponse<SubmitToReviewerResponse>> submitToReviewer(
            HttpServletRequest request,
            @Parameter(description = "결재 ID")
            @PathVariable Long approvalId,
            @RequestBody(required = false) SubmitToReviewerRequest submitRequest) {
        Long userId = extractUserIdFromRequest(request);
        if (submitRequest == null) {
            submitRequest = new SubmitToReviewerRequest();
        }
        SubmitToReviewerResponse response = approvalService.submitToReviewer(userId, approvalId, submitRequest);
        return ResponseEntity.ok(BaseResponse.success(response.getMessage(), response));
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
