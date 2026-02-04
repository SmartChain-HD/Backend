package com.smartchain.platform.domain.review.controller;

import com.smartchain.platform.domain.job.service.ReportExportService;
import com.smartchain.platform.domain.review.service.ReviewService;
import com.smartchain.platform.dto.review.dashboard.ReviewDashboardResponse;
import com.smartchain.platform.dto.review.decision.ReviewDecisionRequest;
import com.smartchain.platform.dto.review.decision.ReviewDecisionResponse;
import com.smartchain.platform.dto.review.decision.RevisionDraftResponse;
import com.smartchain.platform.dto.review.detail.ReviewDetailResponse;
import com.smartchain.platform.dto.review.export.ExportRequest;
import com.smartchain.platform.dto.review.export.ExportResponse;
import com.smartchain.platform.dto.review.list.ReviewListResponse;
import com.smartchain.platform.dto.review.report.BulkReportRequest;
import com.smartchain.platform.dto.review.report.BulkReportResponse;
import com.smartchain.platform.dto.review.report.ReportPublishRequest;
import com.smartchain.platform.dto.review.report.ReportPublishResponse;
import com.smartchain.platform.global.response.BaseResponse;
import com.smartchain.platform.global.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Review", description = "심사 관리 API (REVIEWER 전용)")
public class ReviewController {

    private final ReviewService reviewService;
    private final ReportExportService reportExportService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "진단 현황 대시보드 조회", description = "수신자용 진단 현황 대시보드를 조회합니다. REVIEWER 권한이 필요합니다. domainCode 파라미터로 도메인별 통계를 조회할 수 있습니다.")
    @GetMapping("/dashboard")
    public ResponseEntity<BaseResponse<ReviewDashboardResponse>> getDashboard(
            HttpServletRequest request,
            @Parameter(description = "도메인 필터 (ESG, SAFETY, COMPLIANCE)")
            @RequestParam(required = false) String domainCode,
            @Parameter(description = "캠페인 ID 필터")
            @RequestParam(required = false) Long campaignId,
            @Parameter(description = "기간 시작 (yyyy-MM-dd)")
            @RequestParam(required = false) String fromDate,
            @Parameter(description = "기간 종료 (yyyy-MM-dd)")
            @RequestParam(required = false) String toDate) {
        Long userId = extractUserIdFromRequest(request);
        ReviewDashboardResponse response = reviewService.getDashboard(userId, domainCode, campaignId, fromDate, toDate);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "심사 대상 목록 조회", description = "심사 대상 목록을 조회합니다. REVIEWER 권한이 필요합니다. domainCode 파라미터로 도메인별 필터링이 가능합니다.")
    @GetMapping
    public ResponseEntity<BaseResponse<ReviewListResponse>> getReviewList(
            HttpServletRequest request,
            @Parameter(description = "도메인 필터 (ESG, SAFETY, COMPLIANCE)")
            @RequestParam(required = false) String domainCode,
            @Parameter(description = "심사 상태 필터 (REVIEWING, APPROVED, REVISION_REQUIRED)")
            @RequestParam(required = false) String status,
            @Parameter(description = "위험군 필터 (HIGH, MEDIUM, LOW)")
            @RequestParam(required = false) String riskLevel,
            @Parameter(description = "협력사 ID 필터")
            @RequestParam(required = false) Long companyId,
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "20") int size) {
        Long userId = extractUserIdFromRequest(request);
        ReviewListResponse response = reviewService.getReviewList(userId, domainCode, status, riskLevel, companyId, page, size);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "심사 상세 조회", description = "특정 심사의 상세 정보를 조회합니다. REVIEWER 권한이 필요합니다.")
    @GetMapping("/{reviewId}")
    public ResponseEntity<BaseResponse<ReviewDetailResponse>> getReviewDetail(
            HttpServletRequest request,
            @Parameter(description = "심사 ID")
            @PathVariable Long reviewId) {
        Long userId = extractUserIdFromRequest(request);
        ReviewDetailResponse response = reviewService.getReviewDetail(userId, reviewId);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "AI 보완요청 초안 조회", description = "AI가 생성한 보완요청 초안을 조회합니다. 보완요청 작성 시 자동 입력을 위해 사용합니다. REVIEWER 권한이 필요합니다.")
    @GetMapping("/{reviewId}/revision-draft")
    public ResponseEntity<BaseResponse<RevisionDraftResponse>> getRevisionDraft(
            HttpServletRequest request,
            @Parameter(description = "심사 ID")
            @PathVariable Long reviewId) {
        Long userId = extractUserIdFromRequest(request);
        RevisionDraftResponse response = reviewService.getRevisionDraft(userId, reviewId);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "심사 결과 입력 (승인/보완요청)", description = "심사 결과를 입력합니다. REVIEWING 상태에서만 처리 가능합니다. REVIEWER 권한이 필요합니다.")
    @PatchMapping("/{reviewId}")
    public ResponseEntity<BaseResponse<ReviewDecisionResponse>> processReview(
            HttpServletRequest request,
            @Parameter(description = "심사 ID")
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewDecisionRequest decisionRequest) {
        Long userId = extractUserIdFromRequest(request);
        ReviewDecisionResponse response = reviewService.processReview(userId, reviewId, decisionRequest);
        return ResponseEntity.ok(BaseResponse.success(response.getMessage(), response));
    }

    @Operation(summary = "보고서 생성 (비동기)", description = "특정 심사에 대한 보고서를 비동기로 생성합니다. REVIEWER 권한이 필요합니다.")
    @PostMapping("/{reviewId}/report")
    public ResponseEntity<BaseResponse<ReportPublishResponse>> createReport(
            HttpServletRequest request,
            @Parameter(description = "심사 ID")
            @PathVariable Long reviewId,
            @RequestBody ReportPublishRequest reportRequest) {
        Long userId = extractUserIdFromRequest(request);
        ReportPublishResponse response = reportExportService.createReport(userId, reviewId, reportRequest);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(BaseResponse.success(response.getMessage(), response));
    }

    @Operation(summary = "일괄 보고서 생성 (비동기)", description = "여러 심사에 대한 보고서를 일괄로 비동기 생성합니다. REVIEWER 권한이 필요합니다.")
    @PostMapping("/bulk-report")
    public ResponseEntity<BaseResponse<BulkReportResponse>> createBulkReport(
            HttpServletRequest request,
            @RequestBody BulkReportRequest bulkRequest) {
        Long userId = extractUserIdFromRequest(request);
        BulkReportResponse response = reportExportService.createBulkReport(userId, bulkRequest);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(BaseResponse.success(response.getMessage(), response));
    }

    @Operation(summary = "CSV/Excel 내보내기 (비동기)", description = "심사 데이터를 CSV 또는 Excel 형식으로 비동기 내보냅니다. REVIEWER 권한이 필요합니다.")
    @PostMapping("/export")
    public ResponseEntity<BaseResponse<ExportResponse>> exportReviews(
            HttpServletRequest request,
            @RequestBody ExportRequest exportRequest) {
        Long userId = extractUserIdFromRequest(request);
        ExportResponse response = reportExportService.exportReviews(userId, exportRequest);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(BaseResponse.success(response.getMessage(), response));
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
