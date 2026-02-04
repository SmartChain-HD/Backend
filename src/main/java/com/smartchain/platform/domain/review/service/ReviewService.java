package com.smartchain.platform.domain.review.service;

import com.smartchain.platform.domain.review.entity.Review;
import com.smartchain.platform.domain.review.repository.ReviewRepository;
import com.smartchain.platform.domain.user.entity.Company;
import com.smartchain.platform.domain.user.entity.Domain;
import com.smartchain.platform.domain.user.entity.User;
import com.smartchain.platform.domain.user.repository.CompanyRepository;
import com.smartchain.platform.domain.user.repository.DomainRepository;
import com.smartchain.platform.domain.user.repository.UserRepository;
import com.smartchain.platform.dto.review.common.*;
import com.smartchain.platform.dto.review.dashboard.*;
import com.smartchain.platform.dto.review.decision.ReviewDecisionRequest;
import com.smartchain.platform.dto.review.decision.ReviewDecisionResponse;
import com.smartchain.platform.dto.review.detail.*;
import com.smartchain.platform.dto.review.list.*;
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
    private final DomainRepository domainRepository;

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
     * - domainCode가 지정된 경우 해당 도메인만 통계에 포함
     * - domainCode가 없으면 사용자가 REVIEWER 권한을 가진 모든 도메인의 심사 통계
     */
    public ReviewDashboardResponse getDashboard(Long userId, String domainCode, Long campaignId, String fromDate, String toDate) {
        User currentUser = validateReviewerRole(userId);
        List<Domain> permittedDomains = getPermittedReviewerDomains(currentUser);

        long totalCompanies;
        long inReviewCount;
        long completedCount;
        long revisionCount;
        int highRisk;
        int mediumRisk;
        int lowRisk;
        List<Review> recentReviews;

        // domainCode가 지정된 경우 단일 도메인 필터링
        if (domainCode != null && !domainCode.isEmpty()) {
            Domain filterDomain = domainRepository.findByCode(domainCode)
                    .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT));

            // 사용자가 해당 도메인에서 REVIEWER 역할이 있는지 확인
            if (!currentUser.hasRoleInDomain(domainCode, "REVIEWER")) {
                throw new CustomException(ErrorCode.PERMISSION_DENIED_ACTION);
            }

            totalCompanies = reviewRepository.countDistinctCompaniesByDomain(filterDomain);
            inReviewCount = reviewRepository.countByDomainAndStatus(filterDomain, ReviewStatus.REVIEWING);
            completedCount = reviewRepository.countByDomainAndStatus(filterDomain, ReviewStatus.APPROVED);
            revisionCount = reviewRepository.countByDomainAndStatus(filterDomain, ReviewStatus.REVISION_REQUIRED);
            highRisk = (int) reviewRepository.countByDomainAndRiskLevel(filterDomain, RiskLevel.HIGH);
            mediumRisk = (int) reviewRepository.countByDomainAndRiskLevel(filterDomain, RiskLevel.MEDIUM);
            lowRisk = (int) reviewRepository.countByDomainAndRiskLevel(filterDomain, RiskLevel.LOW);
            recentReviews = reviewRepository.findByDomainOrderBySubmittedAtDesc(filterDomain, PageRequest.of(0, 5));
        } else {
            // 전체 도메인 통계 (기존 로직)
            totalCompanies = reviewRepository.countDistinctCompaniesByDomainIn(permittedDomains);
            inReviewCount = reviewRepository.countByDomainInAndStatus(permittedDomains, ReviewStatus.REVIEWING);
            completedCount = reviewRepository.countByDomainInAndStatus(permittedDomains, ReviewStatus.APPROVED);
            revisionCount = reviewRepository.countByDomainInAndStatus(permittedDomains, ReviewStatus.REVISION_REQUIRED);
            highRisk = (int) reviewRepository.countByDomainInAndRiskLevel(permittedDomains, RiskLevel.HIGH);
            mediumRisk = (int) reviewRepository.countByDomainInAndRiskLevel(permittedDomains, RiskLevel.MEDIUM);
            lowRisk = (int) reviewRepository.countByDomainInAndRiskLevel(permittedDomains, RiskLevel.LOW);
            recentReviews = reviewRepository.findByDomainInOrderBySubmittedAtDesc(permittedDomains, PageRequest.of(0, 5));
        }

        long submittedCount = inReviewCount + completedCount + revisionCount;

        OverviewDto overview = OverviewDto.builder()
                .totalCompanies((int) totalCompanies)
                .submittedCount((int) submittedCount)
                .inReviewCount((int) inReviewCount)
                .completedCount((int) completedCount)
                .notStartedCount(0)
                .build();

        RiskDistributionDto riskDistribution = RiskDistributionDto.builder()
                .high(highRisk)
                .medium(mediumRisk)
                .low(lowRisk)
                .build();

        // Category Averages (현재 단순 고정값, 추후 실제 계산 로직 추가)
        CategoryAveragesDto categoryAverages = CategoryAveragesDto.builder()
                .E(70.0)
                .S(72.0)
                .G(68.0)
                .build();

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
     * - domainCode가 지정된 경우 해당 도메인만 조회
     * - domainCode가 없으면 사용자가 REVIEWER 권한을 가진 모든 도메인의 심사 조회
     */
    public ReviewListResponse getReviewList(Long userId, String domainCode, String status, String riskLevel, Long companyId, int page, int size) {
        User currentUser = validateReviewerRole(userId);
        List<Domain> permittedDomains = getPermittedReviewerDomains(currentUser);
        Pageable pageable = PageRequest.of(page, size);

        Page<Review> reviewPage;
        ReviewSummaryDto summary;

        // domainCode가 지정된 경우 단일 도메인 필터링
        if (domainCode != null && !domainCode.isEmpty()) {
            Domain filterDomain = domainRepository.findByCode(domainCode)
                    .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT));

            // 사용자가 해당 도메인에서 REVIEWER 역할이 있는지 확인
            if (!currentUser.hasRoleInDomain(domainCode, "REVIEWER")) {
                throw new CustomException(ErrorCode.PERMISSION_DENIED_ACTION);
            }

            reviewPage = findReviewsWithSingleDomainFilters(filterDomain, status, riskLevel, companyId, pageable);

            summary = ReviewSummaryDto.builder()
                    .totalCompanies((int) reviewRepository.countDistinctCompaniesByDomain(filterDomain))
                    .completedCount((int) reviewRepository.countByDomainAndStatus(filterDomain, ReviewStatus.APPROVED))
                    .inProgressCount((int) reviewRepository.countByDomainAndStatus(filterDomain, ReviewStatus.REVIEWING))
                    .pendingCount(0)
                    .highRiskCount((int) reviewRepository.countByDomainAndRiskLevel(filterDomain, RiskLevel.HIGH))
                    .mediumRiskCount((int) reviewRepository.countByDomainAndRiskLevel(filterDomain, RiskLevel.MEDIUM))
                    .lowRiskCount((int) reviewRepository.countByDomainAndRiskLevel(filterDomain, RiskLevel.LOW))
                    .build();
        } else {
            // 전체 도메인 조회 (기존 로직)
            reviewPage = findReviewsWithDomainFilters(permittedDomains, status, riskLevel, companyId, pageable);

            summary = ReviewSummaryDto.builder()
                    .totalCompanies((int) reviewRepository.countDistinctCompaniesByDomainIn(permittedDomains))
                    .completedCount((int) reviewRepository.countByDomainInAndStatus(permittedDomains, ReviewStatus.APPROVED))
                    .inProgressCount((int) reviewRepository.countByDomainInAndStatus(permittedDomains, ReviewStatus.REVIEWING))
                    .pendingCount(0)
                    .highRiskCount((int) reviewRepository.countByDomainInAndRiskLevel(permittedDomains, RiskLevel.HIGH))
                    .mediumRiskCount((int) reviewRepository.countByDomainInAndRiskLevel(permittedDomains, RiskLevel.MEDIUM))
                    .lowRiskCount((int) reviewRepository.countByDomainInAndRiskLevel(permittedDomains, RiskLevel.LOW))
                    .build();
        }

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
     * - 해당 심사의 도메인에 REVIEWER 권한이 있는지 검증
     */
    public ReviewDetailResponse getReviewDetail(Long userId, Long reviewId) {
        User currentUser = validateReviewerRole(userId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

        validateDomainReviewerAccess(currentUser, review);

        // Diagnostic Info
        DiagnosticDetailInfoDto diagnosticDto = DiagnosticDetailInfoDto.builder()
                .diagnosticId(review.getDiagnostic().getDiagnosticId())
                .diagnosticCode(review.getDiagnostic().getDiagnosticCode())
                .title(review.getDiagnostic().getTitle())
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

        // 도메인 정보 추출
        Domain domain = review.getDomain();
        String domainCode = domain != null ? domain.getCode() : null;
        String domainName = domain != null ? domain.getName() : null;

        return ReviewDetailResponse.builder()
                .reviewId(review.getReviewId())
                .reviewIdLabel("심사 ID: REVIEW-" + String.format("%04d", review.getReviewId()))
                .diagnostic(diagnosticDto)
                .company(companyDto)
                .domainCode(domainCode)
                .domainName(domainName)
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
     * - 해당 심사의 도메인에 REVIEWER 권한이 있는지 검증
     */
    @Transactional
    public ReviewDecisionResponse processReview(Long userId, Long reviewId, ReviewDecisionRequest request) {
        User currentUser = validateReviewerRole(userId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

        validateDomainReviewerAccess(currentUser, review);

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

    private List<Domain> getPermittedReviewerDomains(User user) {
        List<Domain> domains = user.getDomainsWithRole("REVIEWER");
        if (domains.isEmpty()) {
            throw new CustomException(ErrorCode.PERMISSION_DENIED_ACTION);
        }
        return domains;
    }

    private void validateDomainReviewerAccess(User user, Review review) {
        if (review.getDomain() == null) {
            throw new CustomException(ErrorCode.PERMISSION_DENIED_ACTION);
        }
        String domainCode = review.getDomain().getCode();
        if (!user.hasRoleInDomain(domainCode, "REVIEWER")) {
            throw new CustomException(ErrorCode.PERMISSION_DENIED_ACTION);
        }
    }

    private Page<Review> findReviewsWithDomainFilters(List<Domain> domains, String status, String riskLevel, Long companyId, Pageable pageable) {
        ReviewStatus reviewStatus = parseReviewStatus(status);
        RiskLevel riskLevelEnum = parseRiskLevel(riskLevel);
        Company company = companyId != null ? companyRepository.findById(companyId).orElse(null) : null;

        if (reviewStatus != null && riskLevelEnum != null && company != null) {
            return reviewRepository.findByDomainInAndStatusAndRiskLevelAndCompanyOrderByCreatedAtDesc(
                    domains, reviewStatus, riskLevelEnum, company, pageable);
        } else if (reviewStatus != null && riskLevelEnum != null) {
            return reviewRepository.findByDomainInAndStatusAndRiskLevelOrderByCreatedAtDesc(
                    domains, reviewStatus, riskLevelEnum, pageable);
        } else if (reviewStatus != null && company != null) {
            return reviewRepository.findByDomainInAndStatusAndCompanyOrderByCreatedAtDesc(
                    domains, reviewStatus, company, pageable);
        } else if (riskLevelEnum != null && company != null) {
            return reviewRepository.findByDomainInAndRiskLevelAndCompanyOrderByCreatedAtDesc(
                    domains, riskLevelEnum, company, pageable);
        } else if (reviewStatus != null) {
            return reviewRepository.findByDomainInAndStatusOrderByCreatedAtDesc(domains, reviewStatus, pageable);
        } else if (riskLevelEnum != null) {
            return reviewRepository.findByDomainInAndRiskLevelOrderByCreatedAtDesc(domains, riskLevelEnum, pageable);
        } else if (company != null) {
            return reviewRepository.findByDomainInAndCompanyOrderByCreatedAtDesc(domains, company, pageable);
        } else {
            return reviewRepository.findByDomainInOrderByCreatedAtDesc(domains, pageable);
        }
    }

    /**
     * 단일 도메인 필터링 쿼리 (domainCode 파라미터 지정시 사용)
     */
    private Page<Review> findReviewsWithSingleDomainFilters(Domain domain, String status, String riskLevel, Long companyId, Pageable pageable) {
        ReviewStatus reviewStatus = parseReviewStatus(status);
        RiskLevel riskLevelEnum = parseRiskLevel(riskLevel);
        Company company = companyId != null ? companyRepository.findById(companyId).orElse(null) : null;

        if (reviewStatus != null && riskLevelEnum != null && company != null) {
            return reviewRepository.findByDomainAndStatusAndRiskLevelAndCompanyOrderByCreatedAtDesc(
                    domain, reviewStatus, riskLevelEnum, company, pageable);
        } else if (reviewStatus != null && riskLevelEnum != null) {
            return reviewRepository.findByDomainAndStatusAndRiskLevelOrderByCreatedAtDesc(
                    domain, reviewStatus, riskLevelEnum, pageable);
        } else if (reviewStatus != null && company != null) {
            return reviewRepository.findByDomainAndStatusAndCompanyOrderByCreatedAtDesc(
                    domain, reviewStatus, company, pageable);
        } else if (riskLevelEnum != null && company != null) {
            return reviewRepository.findByDomainAndRiskLevelAndCompanyOrderByCreatedAtDesc(
                    domain, riskLevelEnum, company, pageable);
        } else if (reviewStatus != null) {
            return reviewRepository.findByDomainAndStatusOrderByCreatedAtDesc(domain, reviewStatus, pageable);
        } else if (riskLevelEnum != null) {
            return reviewRepository.findByDomainAndRiskLevelOrderByCreatedAtDesc(domain, riskLevelEnum, pageable);
        } else if (company != null) {
            return reviewRepository.findByDomainAndCompanyOrderByCreatedAtDesc(domain, company, pageable);
        } else {
            return reviewRepository.findByDomainOrderByCreatedAtDesc(domain, pageable);
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
                .title(review.getDiagnostic().getTitle())
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

        // 도메인 정보 추출
        Domain domain = review.getDomain();
        String domainCode = domain != null ? domain.getCode() : null;
        String domainName = domain != null ? domain.getName() : null;

        return ReviewListItemDto.builder()
                .reviewId(review.getReviewId())
                .reviewIdLabel("Review" + String.format("%04d", review.getReviewId()))
                .diagnostic(diagnosticDto)
                .company(companyDto)
                .domainCode(domainCode)
                .domainName(domainName)
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
