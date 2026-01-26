package com.smartchain.platform.domain.review.service;

import com.smartchain.platform.domain.review.entity.Review;
import com.smartchain.platform.domain.review.repository.ReviewRepository;
import com.smartchain.platform.domain.user.entity.Company;
import com.smartchain.platform.domain.user.entity.User;
import com.smartchain.platform.domain.user.repository.CompanyRepository;
import com.smartchain.platform.domain.user.repository.UserRepository;
import com.smartchain.platform.dto.review.common.*;
import com.smartchain.platform.dto.review.dashboard.*;
import com.smartchain.platform.dto.review.decision.ReviewDecisionRequest;
import com.smartchain.platform.dto.review.decision.ReviewDecisionResponse;
import com.smartchain.platform.dto.review.detail.*;
import com.smartchain.platform.dto.review.list.*;
import com.smartchain.platform.global.enums.DiagnosticStatus;
import com.smartchain.platform.global.enums.ReviewStatus;
import com.smartchain.platform.global.enums.RiskLevel;
import com.smartchain.platform.global.error.CustomException;
import com.smartchain.platform.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    private static final Map<String, String> RISK_LEVEL_LABEL_MAP = Map.of(
            "HIGH", "고위험군",
            "MEDIUM", "중위험군",
            "LOW", "저위험군"
    );

    private static final Map<String, String> RISK_COLOR_MAP = Map.of(
            "HIGH", "red",
            "MEDIUM", "yellow",
            "LOW", "green"
    );

    private static final Map<String, String> STATUS_LABEL_MAP = Map.of(
            "REVIEWING", "검수중",
            "APPROVED", "승인",
            "REVISION_REQUIRED", "보완요청"
    );

    /**
     * 진단 현황 대시보드 조회
     */
    public ReviewDashboardResponse getDashboard(Long userId, Long campaignId, String fromDate, String toDate) {
        User currentUser = validateReviewerRole(userId);

        // Overview 통계
        long totalCompanies = reviewRepository.countDistinctCompanies();
        long inReviewCount = reviewRepository.countByStatus(ReviewStatus.REVIEWING);
        long completedCount = reviewRepository.countByStatus(ReviewStatus.APPROVED);
        long revisionCount = reviewRepository.countByStatus(ReviewStatus.REVISION_REQUIRED);
        long submittedCount = inReviewCount + completedCount + revisionCount;

        OverviewDto overview = OverviewDto.builder()
                .totalCompanies((int) totalCompanies)
                .submittedCount((int) submittedCount)
                .inReviewCount((int) inReviewCount)
                .completedCount((int) completedCount)
                .notStartedCount(0) // 계산 로직 필요시 추가
                .build();

        // Risk Distribution
        RiskDistributionDto riskDistribution = RiskDistributionDto.builder()
                .high((int) reviewRepository.countByRiskLevel(RiskLevel.HIGH))
                .medium((int) reviewRepository.countByRiskLevel(RiskLevel.MEDIUM))
                .low((int) reviewRepository.countByRiskLevel(RiskLevel.LOW))
                .build();

        // Category Averages (현재 단순 고정값, 추후 실제 계산 로직 추가)
        CategoryAveragesDto categoryAverages = CategoryAveragesDto.builder()
                .E(70.0)
                .S(72.0)
                .G(68.0)
                .build();

        // Recent Activities
        List<Review> recentReviews = reviewRepository.findTopNByOrderBySubmittedAtDesc(PageRequest.of(0, 5));
        List<RecentActivityDto> recentActivities = recentReviews.stream()
                .map(review -> RecentActivityDto.builder()
                        .type(review.getStatus().name())
                        .companyName(review.getCompany().getName())
                        .diagnosticCode(review.getDiagnostic().getDiagnosticCode())
                        .timestamp(review.getSubmittedAt())
                        .build())
                .toList();

        return ReviewDashboardResponse.builder()
                .overview(overview)
                .riskDistribution(riskDistribution)
                .categoryAverages(categoryAverages)
                .recentActivities(recentActivities)
                .build();
    }

    /**
     * 심사 대상 목록 조회
     */
    public ReviewListResponse getReviewList(Long userId, String status, String riskLevel, Long companyId, int page, int size) {
        User currentUser = validateReviewerRole(userId);
        Pageable pageable = PageRequest.of(page, size);

        Page<Review> reviewPage = findReviewsWithFilters(status, riskLevel, companyId, pageable);

        // Summary 생성
        ReviewSummaryDto summary = ReviewSummaryDto.builder()
                .totalCompanies((int) reviewRepository.countDistinctCompanies())
                .completedCount((int) reviewRepository.countByStatus(ReviewStatus.APPROVED))
                .inProgressCount((int) reviewRepository.countByStatus(ReviewStatus.REVIEWING))
                .pendingCount(0)
                .highRiskCount((int) reviewRepository.countByRiskLevel(RiskLevel.HIGH))
                .mediumRiskCount((int) reviewRepository.countByRiskLevel(RiskLevel.MEDIUM))
                .lowRiskCount((int) reviewRepository.countByRiskLevel(RiskLevel.LOW))
                .build();

        // Content 변환
        List<ReviewListItemDto> content = reviewPage.getContent().stream()
                .map(this::mapToListItemDto)
                .toList();

        // Page 정보
        PageDto pageDto = PageDto.builder()
                .number(reviewPage.getNumber())
                .size(reviewPage.getSize())
                .totalElements(reviewPage.getTotalElements())
                .totalPages(reviewPage.getTotalPages())
                .build();

        return ReviewListResponse.builder()
                .summary(summary)
                .content(content)
                .page(pageDto)
                .build();
    }

    /**
     * 심사 상세 조회
     */
    public ReviewDetailResponse getReviewDetail(Long userId, Long reviewId) {
        User currentUser = validateReviewerRole(userId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

        // Diagnostic Info
        DiagnosticDetailInfoDto diagnosticDto = DiagnosticDetailInfoDto.builder()
                .diagnosticId(review.getDiagnostic().getDiagnosticId())
                .diagnosticCode(review.getDiagnostic().getDiagnosticCode())
                .qualitativeData(null)
                .quantitativeData(null)
                .aiAnalysis(null)
                .build();

        // Company Info
        String industryName = review.getCompany().getIndustry() != null
                ? review.getCompany().getIndustry().getName()
                : null;
        CompanyDetailInfoDto companyDto = CompanyDetailInfoDto.builder()
                .companyId(review.getCompany().getCompanyId())
                .companyName(review.getCompany().getName())
                .industry(industryName)
                .contactPerson(null)
                .contactEmail(null)
                .build();

        String riskLevelStr = review.getRiskLevel() != null ? review.getRiskLevel().name() : null;

        return ReviewDetailResponse.builder()
                .reviewId(review.getReviewId())
                .reviewIdLabel("심사 ID: REVIEW-" + String.format("%04d", review.getReviewId()))
                .diagnostic(diagnosticDto)
                .company(companyDto)
                .score(review.getScore() != null ? review.getScore() : 0)
                .riskLevel(riskLevelStr)
                .riskLevelLabel(riskLevelStr != null ? RISK_LEVEL_LABEL_MAP.get(riskLevelStr) : null)
                .riskColorClass(riskLevelStr != null ? RISK_COLOR_MAP.get(riskLevelStr) : null)
                .status(review.getStatus().name())
                .submittedAt(review.getSubmittedAt())
                .files(new ArrayList<>())
                .tabs(new ArrayList<>())
                .statusCard(null)
                .availableActions(new ArrayList<>())
                .reviewHistory(new ArrayList<>())
                .build();
    }

    /**
     * 심사 결과 입력 (승인/보완요청)
     */
    @Transactional
    public ReviewDecisionResponse processReview(Long userId, Long reviewId, ReviewDecisionRequest request) {
        User currentUser = validateReviewerRole(userId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

        if (!review.isReviewing()) {
            throw new CustomException(ErrorCode.ALREADY_PROCESSED_REVIEW);
        }

        String decision = request.getDecision().toUpperCase();
        String message;
        String nextStep;

        if ("APPROVED".equals(decision)) {
            String commentE = request.getCategoryComments() != null ? request.getCategoryComments().get("E") : null;
            String commentS = request.getCategoryComments() != null ? request.getCategoryComments().get("S") : null;
            String commentG = request.getCategoryComments() != null ? request.getCategoryComments().get("G") : null;

            review.approve(currentUser, request.getComment(), commentE, commentS, commentG);
            review.getDiagnostic().complete();
            message = "심사가 승인되었습니다";
            nextStep = "보고서 발행이 가능합니다";
            log.info("Review approved: reviewId={}, approvedBy={}", reviewId, userId);
        } else if ("REVISION_REQUIRED".equals(decision)) {
            review.requestRevision(currentUser, request.getComment());
            review.getDiagnostic().returnForRevision();
            message = "보완 요청이 완료되었습니다";
            nextStep = "협력사에게 보완 요청이 전달됩니다";
            log.info("Review revision required: reviewId={}, requestedBy={}, comment={}",
                    reviewId, userId, request.getComment());
        } else {
            throw new CustomException(ErrorCode.INVALID_DECISION);
        }

        ProcessedByDto processedByDto = ProcessedByDto.builder()
                .userId(currentUser.getUserId())
                .name(currentUser.getName())
                .build();

        return ReviewDecisionResponse.builder()
                .reviewId(review.getReviewId())
                .status(review.getStatus().name())
                .statusLabel(STATUS_LABEL_MAP.get(review.getStatus().name()))
                .processedAt(review.getProcessedAt())
                .processedBy(processedByDto)
                .message(message)
                .nextStep(nextStep)
                .build();
    }

    // ========== Private Helper Methods ==========

    private User validateReviewerRole(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String roleCode = user.getRole() != null ? user.getRole().getCode() : "GUEST";
        if (!"REVIEWER".equals(roleCode)) {
            throw new CustomException(ErrorCode.PERMISSION_DENIED_ACTION);
        }

        return user;
    }

    private Page<Review> findReviewsWithFilters(String status, String riskLevel, Long companyId, Pageable pageable) {
        ReviewStatus reviewStatus = parseReviewStatus(status);
        RiskLevel riskLevelEnum = parseRiskLevel(riskLevel);
        Company company = companyId != null ? companyRepository.findById(companyId).orElse(null) : null;

        // 필터 조합에 따른 쿼리 선택
        if (reviewStatus != null && riskLevelEnum != null && company != null) {
            return reviewRepository.findByStatusAndRiskLevelAndCompanyOrderByCreatedAtDesc(
                    reviewStatus, riskLevelEnum, company, pageable);
        } else if (reviewStatus != null && riskLevelEnum != null) {
            return reviewRepository.findByStatusAndRiskLevelOrderByCreatedAtDesc(
                    reviewStatus, riskLevelEnum, pageable);
        } else if (reviewStatus != null && company != null) {
            return reviewRepository.findByStatusAndCompanyOrderByCreatedAtDesc(
                    reviewStatus, company, pageable);
        } else if (riskLevelEnum != null && company != null) {
            return reviewRepository.findByRiskLevelAndCompanyOrderByCreatedAtDesc(
                    riskLevelEnum, company, pageable);
        } else if (reviewStatus != null) {
            return reviewRepository.findByStatusOrderByCreatedAtDesc(reviewStatus, pageable);
        } else if (riskLevelEnum != null) {
            return reviewRepository.findByRiskLevelOrderByCreatedAtDesc(riskLevelEnum, pageable);
        } else if (company != null) {
            return reviewRepository.findByCompanyOrderByCreatedAtDesc(company, pageable);
        } else {
            return reviewRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
    }

    private ReviewStatus parseReviewStatus(String status) {
        if (status == null || status.isEmpty()) {
            return null;
        }
        try {
            return ReviewStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private RiskLevel parseRiskLevel(String riskLevel) {
        if (riskLevel == null || riskLevel.isEmpty()) {
            return null;
        }
        try {
            return RiskLevel.valueOf(riskLevel.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private ReviewListItemDto mapToListItemDto(Review review) {
        DiagnosticSimpleDto diagnosticDto = DiagnosticSimpleDto.builder()
                .diagnosticId(review.getDiagnostic().getDiagnosticId())
                .diagnosticCode(review.getDiagnostic().getDiagnosticCode())
                .build();

        CompanySimpleDto companyDto = CompanySimpleDto.builder()
                .companyId(review.getCompany().getCompanyId())
                .companyName(review.getCompany().getName())
                .companyType(review.getCompany().getScale())
                .build();

        String riskLevelStr = review.getRiskLevel() != null ? review.getRiskLevel().name() : null;

        AssignedToDto assignedToDto = null;
        if (review.getAssignedReviewer() != null) {
            assignedToDto = AssignedToDto.builder()
                    .userId(review.getAssignedReviewer().getUserId())
                    .name(review.getAssignedReviewer().getName())
                    .build();
        }

        ReviewFileLinksDto fileLinks = ReviewFileLinksDto.builder()
                .hasDiagnosticPdf(false)
                .hasDataPackage(false)
                .hasModificationLog(false)
                .hasAiReport(false)
                .build();

        return ReviewListItemDto.builder()
                .reviewId(review.getReviewId())
                .reviewIdLabel("Review" + String.format("%04d", review.getReviewId()))
                .diagnostic(diagnosticDto)
                .company(companyDto)
                .score(review.getScore() != null ? review.getScore() : 0)
                .riskLevel(riskLevelStr)
                .riskLevelLabel(riskLevelStr != null ? RISK_LEVEL_LABEL_MAP.get(riskLevelStr) : null)
                .riskColorClass(riskLevelStr != null ? RISK_COLOR_MAP.get(riskLevelStr) : null)
                .status(review.getStatus().name())
                .statusLabel(STATUS_LABEL_MAP.get(review.getStatus().name()))
                .submittedAt(review.getSubmittedAt())
                .files(fileLinks)
                .assignedTo(assignedToDto)
                .build();
    }
}
