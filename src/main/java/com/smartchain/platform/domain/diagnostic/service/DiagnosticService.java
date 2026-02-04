package com.smartchain.platform.domain.diagnostic.service;

import com.smartchain.platform.domain.approval.entity.Approval;
import com.smartchain.platform.domain.approval.repository.ApprovalRepository;
import com.smartchain.platform.domain.diagnostic.entity.Campaign;
import com.smartchain.platform.domain.diagnostic.entity.Diagnostic;
import com.smartchain.platform.domain.diagnostic.entity.DiagnosticHistory;
import com.smartchain.platform.domain.review.entity.Review;
import com.smartchain.platform.domain.review.repository.ReviewRepository;
import com.smartchain.platform.domain.diagnostic.repository.CampaignRepository;
import com.smartchain.platform.domain.diagnostic.repository.DiagnosticHistoryRepository;
import com.smartchain.platform.domain.diagnostic.repository.DiagnosticRepository;
import com.smartchain.platform.domain.user.entity.Company;
import com.smartchain.platform.domain.user.entity.Domain;
import com.smartchain.platform.domain.user.entity.User;
import com.smartchain.platform.domain.user.repository.DomainRepository;
import com.smartchain.platform.domain.user.repository.UserRepository;
import com.smartchain.platform.dto.diagnostic.ai.AiAnalysisResponse;
import com.smartchain.platform.dto.diagnostic.ai.CategoryScoresDto;
import com.smartchain.platform.dto.diagnostic.ai.HighlightDto;
import com.smartchain.platform.dto.diagnostic.ai.RecommendationDto;
import com.smartchain.platform.dto.diagnostic.common.*;
import com.smartchain.platform.dto.diagnostic.create.DiagnosticCreateRequest;
import com.smartchain.platform.dto.diagnostic.create.DiagnosticCreateResponse;
import com.smartchain.platform.dto.diagnostic.detail.CampaignDetailDto;
import com.smartchain.platform.dto.diagnostic.detail.CreatedByDto;
import com.smartchain.platform.dto.diagnostic.detail.DiagnosticDetailResponse;
import com.smartchain.platform.dto.diagnostic.history.DiagnosticHistoryItemDto;
import com.smartchain.platform.dto.diagnostic.history.DiagnosticHistoryResponse;
import com.smartchain.platform.dto.diagnostic.history.PerformedByDto;
import com.smartchain.platform.dto.diagnostic.list.DiagnosticListItemDto;
import com.smartchain.platform.dto.diagnostic.list.DiagnosticListResponse;
import com.smartchain.platform.dto.diagnostic.submit.DiagnosticSubmitRequest;
import com.smartchain.platform.dto.diagnostic.submit.DiagnosticSubmitResponse;
import com.smartchain.platform.global.enums.DiagnosticStatus;
import com.smartchain.platform.global.error.CustomException;
import com.smartchain.platform.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiagnosticService {

    private final DiagnosticRepository diagnosticRepository;
    private final DiagnosticHistoryRepository diagnosticHistoryRepository;
    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;
    private final ApprovalRepository approvalRepository;
    private final ReviewRepository reviewRepository;
    private final DomainRepository domainRepository;

    private static final List<String> ALLOWED_ROLES = Arrays.asList("DRAFTER", "APPROVER");

    private static final Map<String, String> STATUS_LABEL_MAP = Map.of(
            "WRITING", "작성중",
            "SUBMITTED", "제출됨",
            "RETURNED", "반려됨",
            "APPROVED", "내부승인",
            "REVIEWING", "심사중",
            "COMPLETED", "완료"
    );

    public DiagnosticListResponse getDiagnosticList(Long userId, String domainCode, String statuses,
                                                     String keyword, LocalDate deadlineFrom,
                                                     LocalDate deadlineTo, int page, int size) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Company userCompany = currentUser.getCompany();

        // 공통 필터 파라미터 준비
        List<DiagnosticStatus> statusList = (statuses != null && !statuses.isEmpty())
                ? parseStatuses(statuses) : Collections.emptyList();
        boolean hasStatuses = !statusList.isEmpty();
        String trimmedKeyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;

        // 도메인별 역할 분류
        List<Domain> reviewerDomains = new ArrayList<>();
        List<Domain> approverDomains = new ArrayList<>();
        List<Domain> drafterDomains = new ArrayList<>();

        for (var domainRole : currentUser.getDomainRoles()) {
            String roleCode = domainRole.getRole().getCode();
            Domain domain = domainRole.getDomain();
            switch (roleCode) {
                case "REVIEWER" -> reviewerDomains.add(domain);
                case "APPROVER" -> approverDomains.add(domain);
                case "DRAFTER" -> drafterDomains.add(domain);
            }
        }

        // 도메인 역할이 없으면 레거시 로직 사용
        boolean hasDomainRoles = !reviewerDomains.isEmpty() || !approverDomains.isEmpty() || !drafterDomains.isEmpty();
        if (!hasDomainRoles) {
            return getDiagnosticListLegacy(currentUser, userCompany, domainCode, hasStatuses, statusList,
                    trimmedKeyword, deadlineFrom, deadlineTo, page, size);
        }

        // domainCode 파라미터가 있으면 해당 도메인으로 필터링
        if (domainCode != null && !domainCode.isEmpty()) {
            reviewerDomains = reviewerDomains.stream().filter(d -> d.getCode().equals(domainCode)).toList();
            approverDomains = approverDomains.stream().filter(d -> d.getCode().equals(domainCode)).toList();
            drafterDomains = drafterDomains.stream().filter(d -> d.getCode().equals(domainCode)).toList();
        }

        boolean hasReviewer = !reviewerDomains.isEmpty();
        boolean hasApprover = !approverDomains.isEmpty();
        boolean hasDrafter = !drafterDomains.isEmpty();

        // 모든 도메인 리스트가 비어있으면 빈 결과 반환
        if (!hasReviewer && !hasApprover && !hasDrafter) {
            return buildEmptyResponse(page, size);
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<Diagnostic> diagnosticPage = diagnosticRepository.findByFilters(
                hasReviewer ? reviewerDomains : Collections.singletonList(null),
                hasApprover ? approverDomains : Collections.singletonList(null),
                hasDrafter ? drafterDomains : Collections.singletonList(null),
                hasReviewer, hasApprover, hasDrafter,
                userCompany, userId,
                hasStatuses, hasStatuses ? statusList : Collections.singletonList(DiagnosticStatus.WRITING),
                trimmedKeyword, deadlineFrom, deadlineTo,
                pageable);

        return buildListResponse(diagnosticPage);
    }

    /**
     * 레거시: 도메인 역할이 없는 사용자용 기안 목록 조회
     */
    private DiagnosticListResponse getDiagnosticListLegacy(User currentUser, Company userCompany,
                                                            String domainCode, boolean hasStatuses,
                                                            List<DiagnosticStatus> statusList, String keyword,
                                                            LocalDate deadlineFrom, LocalDate deadlineTo,
                                                            int page, int size) {
        validateAccessRole(currentUser);

        if (userCompany == null) {
            throw new CustomException(ErrorCode.PERMISSION_DENIED_RESOURCE);
        }

        Domain domain = null;
        boolean hasDomain = false;
        if (domainCode != null && !domainCode.isEmpty()) {
            domain = domainRepository.findByCode(domainCode)
                    .orElseThrow(() -> new CustomException(ErrorCode.DOMAIN_NOT_FOUND));
            hasDomain = true;
        }

        Pageable pageable = PageRequest.of(page, size);
        String userRoleCode = currentUser.getRole() != null ? currentUser.getRole().getCode() : "GUEST";

        Page<Diagnostic> diagnosticPage;
        List<DiagnosticStatus> queryStatuses = hasStatuses ? statusList : Collections.singletonList(DiagnosticStatus.WRITING);

        if ("DRAFTER".equals(userRoleCode)) {
            // DRAFTER는 본인 기안만 조회
            diagnosticPage = diagnosticRepository.findByDrafterIdAndFilters(
                    currentUser.getUserId(), hasDomain, domain,
                    hasStatuses, queryStatuses,
                    keyword, deadlineFrom, deadlineTo, pageable);
        } else {
            // APPROVER는 회사 전체 건 조회
            diagnosticPage = diagnosticRepository.findByCompanyAndFilters(
                    userCompany, hasDomain, domain,
                    hasStatuses, queryStatuses,
                    keyword, deadlineFrom, deadlineTo, pageable);
        }

        return buildListResponse(diagnosticPage);
    }

    public DiagnosticDetailResponse getDiagnosticDetail(Long userId, Long diagnosticId) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Diagnostic diagnostic = diagnosticRepository.findById(diagnosticId)
                .orElseThrow(() -> new CustomException(ErrorCode.DIAGNOSTIC_NOT_FOUND));

        // 도메인 기반 권한 검증
        validateDomainAccess(currentUser, diagnostic);

        User drafter = null;
        if (diagnostic.getDrafterId() != null) {
            drafter = userRepository.findById(diagnostic.getDrafterId()).orElse(null);
        }

        CampaignDetailDto campaignDto = null;
        if (diagnostic.getCampaign() != null) {
            Campaign campaign = diagnostic.getCampaign();
            campaignDto = CampaignDetailDto.builder()
                    .campaignId(campaign.getCampaignId())
                    .campaignCode(campaign.getCampaignCode())
                    .title(campaign.getTitle())
                    .disclosureStandards(Collections.emptyList())
                    .build();
        }

        CompanySimpleDto companyDto = null;
        if (diagnostic.getCompany() != null) {
            companyDto = CompanySimpleDto.builder()
                    .companyId(diagnostic.getCompany().getCompanyId())
                    .companyName(diagnostic.getCompany().getName())
                    .industryCode(null)
                    .build();
        }

        PeriodDto periodDto = PeriodDto.builder()
                .startDate(diagnostic.getPeriodStartDate())
                .endDate(diagnostic.getPeriodEndDate())
                .build();

        CreatedByDto createdByDto = null;
        if (drafter != null) {
            createdByDto = CreatedByDto.builder()
                    .userId(drafter.getUserId())
                    .name(drafter.getName())
                    .build();
        }

        DomainSimpleDto domainDto = null;
        if (diagnostic.getDomain() != null) {
            Domain domain = diagnostic.getDomain();
            domainDto = DomainSimpleDto.builder()
                    .domainId(domain.getDomainId())
                    .code(domain.getCode())
                    .name(domain.getName())
                    .build();
        }

        return DiagnosticDetailResponse.builder()
                .diagnosticId(diagnostic.getDiagnosticId())
                .diagnosticCode(diagnostic.getDiagnosticCode())
                .title(diagnostic.getTitle())
                .domain(domainDto)
                .campaign(campaignDto)
                .company(companyDto)
                .period(periodDto)
                .deadline(diagnostic.getDeadline())
                .status(diagnostic.getStatus().name())
                .statusLabel(STATUS_LABEL_MAP.getOrDefault(diagnostic.getStatus().name(), diagnostic.getStatus().name()))
                .qualitativeProgress(diagnostic.getQualitativeProgress())
                .quantitativeProgress(diagnostic.getQuantitativeProgress())
                .overallProgress(diagnostic.getOverallProgress())
                .createdBy(createdByDto)
                .createdAt(diagnostic.getCreatedAt())
                .updatedAt(diagnostic.getUpdatedAt())
                .submittedAt(diagnostic.getSubmittedAt())
                .build();
    }

    @Transactional
    public DiagnosticCreateResponse createDiagnostic(Long userId, DiagnosticCreateRequest request) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Company userCompany = currentUser.getCompany();
        if (userCompany == null) {
            throw new CustomException(ErrorCode.COMPANY_NOT_ASSIGNED);
        }

        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new CustomException(ErrorCode.CAMPAIGN_NOT_FOUND));

        // 캠페인 종료 여부 확인 (종료된 캠페인에는 진단 생성 불가)
        if (campaign.getPeriodEndDate().isBefore(LocalDate.now())) {
            throw new CustomException(ErrorCode.CAMPAIGN_CLOSED);
        }

        // 캠페인의 도메인 또는 요청의 도메인 코드로 도메인 조회
        Domain domain = campaign.getDomain();
        if (domain == null && request.getDomainCode() != null) {
            domain = domainRepository.findByCode(request.getDomainCode())
                    .orElseThrow(() -> new CustomException(ErrorCode.DOMAIN_NOT_FOUND));
        }

        // 도메인 기반 DRAFTER 권한 검증
        if (domain != null) {
            validateDrafterRole(currentUser, domain.getCode());
        } else {
            // 레거시: 도메인이 없으면 기본 역할로 검증
            String userRoleCode = currentUser.getRole() != null ? currentUser.getRole().getCode() : "GUEST";
            if (!"DRAFTER".equals(userRoleCode)) {
                throw new CustomException(ErrorCode.PERMISSION_DENIED_ACTION);
            }
        }

        String diagnosticCode = generateDiagnosticCode();

        // 기간: 요청에서 받은 값 우선, 없으면 캠페인 값 사용
        LocalDate periodStart = request.getPeriodStartDate() != null
                ? request.getPeriodStartDate()
                : campaign.getPeriodStartDate();
        LocalDate periodEnd = request.getPeriodEndDate() != null
                ? request.getPeriodEndDate()
                : campaign.getPeriodEndDate();

        Diagnostic diagnostic = Diagnostic.builder()
                .diagnosticCode(diagnosticCode)
                .title(request.getTitle())
                .campaign(campaign)
                .company(userCompany)
                .domain(domain)
                .drafterId(userId)
                .periodStartDate(periodStart)
                .periodEndDate(periodEnd)
                .deadline(campaign.getDeadline())
                .build();

        Diagnostic savedDiagnostic = diagnosticRepository.save(diagnostic);

        DiagnosticHistory history = DiagnosticHistory.builder()
                .diagnostic(savedDiagnostic)
                .actor(currentUser)
                .action("CREATED")
                .previousStatus(null)
                .newStatus(DiagnosticStatus.WRITING.name())
                .comment(null)
                .build();
        diagnosticHistoryRepository.save(history);

        log.info("Diagnostic created: diagnosticId={}, diagnosticCode={}, createdBy={}",
                savedDiagnostic.getDiagnosticId(), diagnosticCode, userId);

        return DiagnosticCreateResponse.builder()
                .diagnosticId(savedDiagnostic.getDiagnosticId())
                .diagnosticCode(savedDiagnostic.getDiagnosticCode())
                .status(savedDiagnostic.getStatus().name())
                .createdAt(savedDiagnostic.getCreatedAt())
                .build();
    }

    @Transactional
    public DiagnosticSubmitResponse submitDiagnostic(Long userId, Long diagnosticId, DiagnosticSubmitRequest request) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Diagnostic diagnostic = diagnosticRepository.findById(diagnosticId)
                .orElseThrow(() -> new CustomException(ErrorCode.DIAGNOSTIC_NOT_FOUND));

        // 본인 기안만 제출 가능 (소유자 검증 우선)
        if (!diagnostic.getDrafterId().equals(userId)) {
            throw new CustomException(ErrorCode.PERMISSION_DENIED_RESOURCE);
        }

        // 이미 제출된 기안: 멱등 처리 (재요청 시 기존 결과 반환)
        if (diagnostic.getStatus() == DiagnosticStatus.SUBMITTED) {
            log.info("Diagnostic already submitted (idempotent): diagnosticId={}, userId={}", diagnosticId, userId);
            return DiagnosticSubmitResponse.builder()
                    .diagnosticId(diagnostic.getDiagnosticId())
                    .previousStatus(DiagnosticStatus.SUBMITTED.name())
                    .newStatus(DiagnosticStatus.SUBMITTED.name())
                    .submittedAt(diagnostic.getSubmittedAt())
                    .message("기안이 이미 제출되었습니다")
                    .build();
        }

        // 상태 전이 불가
        if (!diagnostic.canSubmit()) {
            throw new CustomException(ErrorCode.DIAGNOSTIC_INVALID_STATE_TRANSITION);
        }

        // 도메인 기반 DRAFTER 권한 검증
        Domain domain = diagnostic.getDomain();
        if (domain != null) {
            validateDrafterRole(currentUser, domain.getCode());
        } else {
            String userRoleCode = currentUser.getRole() != null ? currentUser.getRole().getCode() : "GUEST";
            if (!"DRAFTER".equals(userRoleCode)) {
                throw new CustomException(ErrorCode.PERMISSION_DENIED_ACTION);
            }
        }

        String previousStatus = diagnostic.getStatus().name();
        diagnostic.submit();

        DiagnosticHistory history = DiagnosticHistory.builder()
                .diagnostic(diagnostic)
                .actor(currentUser)
                .action("SUBMITTED")
                .previousStatus(previousStatus)
                .newStatus(DiagnosticStatus.SUBMITTED.name())
                .comment(request.getSubmitComment())
                .build();
        diagnosticHistoryRepository.save(history);

        // 도메인별 워크플로우 분기
        String domainCode = domain != null ? domain.getCode() : "ESG";

        if ("ESG".equals(domainCode)) {
            // ESG: 결재자에게 결재 요청 (기안자 → 결재자 → 수신자)
            Approval approval = Approval.builder()
                    .diagnostic(diagnostic)
                    .requester(currentUser)
                    .requestComment(request.getSubmitComment())
                    .deadline(diagnostic.getDeadline())
                    .build();
            Approval savedApproval = approvalRepository.save(approval);

            log.info("Diagnostic submitted (ESG → Approval): diagnosticId={}, submittedBy={}, approvalId={}",
                    diagnosticId, userId, savedApproval.getApprovalId());

            return DiagnosticSubmitResponse.builder()
                    .diagnosticId(diagnostic.getDiagnosticId())
                    .previousStatus(previousStatus)
                    .newStatus(DiagnosticStatus.SUBMITTED.name())
                    .submittedAt(diagnostic.getSubmittedAt())
                    .approvalId(savedApproval.getApprovalId())
                    .message("기안이 제출되었습니다")
                    .build();
        } else {
            // SAFETY, COMPLIANCE: 결재 스킵, 수신자에게 직행 (기안자 → 수신자)
            diagnostic.approve();

            DiagnosticHistory approveHistory = DiagnosticHistory.builder()
                    .diagnostic(diagnostic)
                    .actor(currentUser)
                    .action("AUTO_APPROVED")
                    .previousStatus(DiagnosticStatus.SUBMITTED.name())
                    .newStatus(DiagnosticStatus.APPROVED.name())
                    .comment("결재 스킵 (도메인 정책: " + domainCode + ")")
                    .build();
            diagnosticHistoryRepository.save(approveHistory);

            Review review = Review.builder()
                    .diagnostic(diagnostic)
                    .company(diagnostic.getCompany())
                    .domain(domain)
                    .submittedAt(diagnostic.getSubmittedAt())
                    .build();
            Review savedReview = reviewRepository.save(review);

            log.info("Diagnostic submitted ({} → Review, approval skipped): diagnosticId={}, submittedBy={}, reviewId={}",
                    domainCode, diagnosticId, userId, savedReview.getReviewId());

            return DiagnosticSubmitResponse.builder()
                    .diagnosticId(diagnostic.getDiagnosticId())
                    .previousStatus(previousStatus)
                    .newStatus(DiagnosticStatus.APPROVED.name())
                    .submittedAt(diagnostic.getSubmittedAt())
                    .reviewId(savedReview.getReviewId())
                    .message("기안이 제출되었습니다 (심사 대기)")
                    .build();
        }
    }

    public AiAnalysisResponse getAiAnalysis(Long userId, Long diagnosticId) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Diagnostic diagnostic = diagnosticRepository.findById(diagnosticId)
                .orElseThrow(() -> new CustomException(ErrorCode.DIAGNOSTIC_NOT_FOUND));

        // 도메인 기반 권한 검증
        validateDomainAccess(currentUser, diagnostic);

        // 현재 AI 분석이 실제로 실행되지 않으므로 Mock 데이터 반환
        // 실제 AI 분석 결과는 별도의 AiAnalysis 엔티티에서 조회해야 함
        return AiAnalysisResponse.builder()
                .diagnosticId(diagnosticId)
                .analysisStatus("PENDING")
                .analyzedAt(null)
                .overallScore(0)
                .riskLevel(null)
                .categoryScores(null)
                .highlights(Collections.emptyList())
                .recommendations(Collections.emptyList())
                .build();
    }

    public DiagnosticHistoryResponse getDiagnosticHistory(Long userId, Long diagnosticId) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Diagnostic diagnostic = diagnosticRepository.findById(diagnosticId)
                .orElseThrow(() -> new CustomException(ErrorCode.DIAGNOSTIC_NOT_FOUND));

        // 도메인 기반 권한 검증
        validateDomainAccess(currentUser, diagnostic);

        List<DiagnosticHistory> histories = diagnosticHistoryRepository.findByDiagnosticOrderByCreatedAtDesc(diagnostic);

        List<DiagnosticHistoryItemDto> historyItems = histories.stream()
                .map(this::mapToHistoryItemDto)
                .toList();

        return DiagnosticHistoryResponse.builder()
                .diagnosticId(diagnosticId)
                .history(historyItems)
                .build();
    }

    /**
     * 기안 삭제
     * - WRITING 상태인 기안만 삭제 가능
     * - 본인이 생성한 기안만 삭제 가능
     */
    @Transactional
    public void deleteDiagnostic(Long userId, Long diagnosticId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Diagnostic diagnostic = diagnosticRepository.findById(diagnosticId)
                .orElseThrow(() -> new CustomException(ErrorCode.DIAGNOSTIC_NOT_FOUND));

        // 본인 기안인지 확인
        if (!diagnostic.getDrafterId().equals(userId)) {
            throw new CustomException(ErrorCode.DIAGNOSTIC_NOT_OWNER);
        }

        // WRITING 상태인지 확인
        if (diagnostic.getStatus() != DiagnosticStatus.WRITING) {
            throw new CustomException(ErrorCode.DIAGNOSTIC_DELETE_NOT_ALLOWED);
        }

        // 삭제 실행 (연관 파일은 Cascade로 삭제됨)
        diagnosticRepository.delete(diagnostic);

        log.info("Diagnostic deleted: diagnosticId={}, deletedBy={}", diagnosticId, userId);
    }

    /**
     * 도메인 기반 접근 권한 검증 (DRAFTER 또는 APPROVER)
     */
    private void validateDomainAccess(User user, Diagnostic diagnostic) {
        Domain domain = diagnostic.getDomain();
        if (domain == null) {
            // 도메인이 없는 기존 데이터는 기본 역할 및 회사/소유권 검증
            validateAccessRoleLegacy(user);
            validateCompanyAccess(user, diagnostic);
            return;
        }

        String domainCode = domain.getCode();

        // 해당 도메인에서 DRAFTER 또는 APPROVER 역할이 있는지 확인
        if (!user.hasAnyRoleInDomain(domainCode, "DRAFTER", "APPROVER", "REVIEWER")) {
            log.warn("User {} does not have access to domain {}", user.getUserId(), domainCode);
            throw new CustomException(ErrorCode.PERMISSION_DENIED_ACTION);
        }

        // 회사 및 소유권 검증
        validateCompanyAndOwnership(user, diagnostic, domainCode);
    }

    /**
     * 기안 생성/제출 시 DRAFTER 권한 검증
     */
    private void validateDrafterRole(User user, String domainCode) {
        if (!user.hasRoleInDomain(domainCode, "DRAFTER")) {
            log.warn("User {} does not have DRAFTER role in domain {}", user.getUserId(), domainCode);
            throw new CustomException(ErrorCode.PERMISSION_DENIED_ACTION);
        }
    }

    /**
     * 회사 및 소유권 검증
     */
    private void validateCompanyAndOwnership(User user, Diagnostic diagnostic, String domainCode) {
        Company userCompany = user.getCompany();

        // DRAFTER: 본인 기안만 접근
        if (user.hasRoleInDomain(domainCode, "DRAFTER")) {
            if (!diagnostic.getDrafterId().equals(user.getUserId())) {
                throw new CustomException(ErrorCode.PERMISSION_DENIED_RESOURCE);
            }
            return;
        }

        // APPROVER: 같은 회사 기안만 접근
        if (user.hasRoleInDomain(domainCode, "APPROVER")) {
            if (userCompany == null || !userCompany.getCompanyId().equals(diagnostic.getCompany().getCompanyId())) {
                throw new CustomException(ErrorCode.PERMISSION_DENIED_RESOURCE);
            }
            return;
        }

        // REVIEWER: 모든 기안 접근 가능 (원청)
        // REVIEWER는 제한 없음
    }

    /**
     * 레거시: 도메인 없는 데이터용 기본 역할 검증
     */
    private void validateAccessRoleLegacy(User user) {
        String userRoleCode = user.getRole() != null ? user.getRole().getCode() : "GUEST";
        if (!ALLOWED_ROLES.contains(userRoleCode)) {
            throw new CustomException(ErrorCode.PERMISSION_DENIED_ACTION);
        }
    }

    private void validateAccessRole(User user) {
        String userRoleCode = user.getRole() != null ? user.getRole().getCode() : "GUEST";
        if (!ALLOWED_ROLES.contains(userRoleCode)) {
            throw new CustomException(ErrorCode.PERMISSION_DENIED_ACTION);
        }
    }

    private void validateCompanyAccess(User user, Diagnostic diagnostic) {
        Company userCompany = user.getCompany();
        String userRoleCode = user.getRole() != null ? user.getRole().getCode() : "GUEST";

        if ("DRAFTER".equals(userRoleCode)) {
            if (!diagnostic.getDrafterId().equals(user.getUserId())) {
                throw new CustomException(ErrorCode.PERMISSION_DENIED_RESOURCE);
            }
        } else if ("APPROVER".equals(userRoleCode)) {
            if (userCompany == null || !userCompany.getCompanyId().equals(diagnostic.getCompany().getCompanyId())) {
                throw new CustomException(ErrorCode.PERMISSION_DENIED_RESOURCE);
            }
        }
    }

    private DiagnosticListResponse buildListResponse(Page<Diagnostic> diagnosticPage) {
        List<DiagnosticListItemDto> content = diagnosticPage.getContent().stream()
                .map(this::mapToListItemDto)
                .toList();

        PageDto pageDto = PageDto.builder()
                .number(diagnosticPage.getNumber())
                .size(diagnosticPage.getSize())
                .totalElements(diagnosticPage.getTotalElements())
                .totalPages(diagnosticPage.getTotalPages())
                .build();

        return DiagnosticListResponse.builder()
                .content(content)
                .page(pageDto)
                .build();
    }

    private DiagnosticListResponse buildEmptyResponse(int page, int size) {
        PageDto pageDto = PageDto.builder()
                .number(page)
                .size(size)
                .totalElements(0)
                .totalPages(0)
                .build();

        return DiagnosticListResponse.builder()
                .content(Collections.emptyList())
                .page(pageDto)
                .build();
    }

    private List<DiagnosticStatus> parseStatuses(String statuses) {
        return Arrays.stream(statuses.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .map(s -> {
                    try {
                        return DiagnosticStatus.valueOf(s);
                    } catch (IllegalArgumentException e) {
                        throw new CustomException(ErrorCode.INVALID_INPUT);
                    }
                })
                .toList();
    }

    private String generateDiagnosticCode() {
        int year = Year.now().getValue();
        long sequence = diagnosticRepository.getNextDiagnosticCodeSequence();
        return String.format("DG-%d-%05d", year, sequence);
    }

    private DiagnosticListItemDto mapToListItemDto(Diagnostic diagnostic) {
        CampaignSimpleDto campaignDto = null;
        if (diagnostic.getCampaign() != null) {
            Campaign campaign = diagnostic.getCampaign();
            // 기안 상태 기반으로 캠페인 상태 결정 (Issue #156)
            String campaignStatus = determineCampaignStatusByDiagnostic(diagnostic.getStatus());
            String campaignStatusLabel = getCampaignStatusLabel(campaignStatus);
            campaignDto = CampaignSimpleDto.builder()
                    .campaignId(campaign.getCampaignId())
                    .campaignCode(campaign.getCampaignCode())
                    .title(campaign.getTitle())
                    .status(campaignStatus)
                    .statusLabel(campaignStatusLabel)
                    .build();
        }

        PeriodDto periodDto = PeriodDto.builder()
                .startDate(diagnostic.getPeriodStartDate())
                .endDate(diagnostic.getPeriodEndDate())
                .build();

        ProgressDto progressDto = ProgressDto.builder()
                .qualitative(diagnostic.getQualitativeProgress())
                .quantitative(diagnostic.getQuantitativeProgress())
                .overall(diagnostic.getOverallProgress())
                .build();

        DomainSimpleDto domainDto = null;
        if (diagnostic.getDomain() != null) {
            Domain domain = diagnostic.getDomain();
            domainDto = DomainSimpleDto.builder()
                    .domainId(domain.getDomainId())
                    .code(domain.getCode())
                    .name(domain.getName())
                    .build();
        }

        return DiagnosticListItemDto.builder()
                .diagnosticId(diagnostic.getDiagnosticId())
                .diagnosticCode(diagnostic.getDiagnosticCode())
                .title(diagnostic.getTitle())
                .domain(domainDto)
                .campaign(campaignDto)
                .summary(diagnostic.getTitle())
                .period(periodDto)
                .deadline(diagnostic.getDeadline())
                .status(diagnostic.getStatus().name())
                .statusLabel(STATUS_LABEL_MAP.getOrDefault(diagnostic.getStatus().name(), diagnostic.getStatus().name()))
                .progress(progressDto)
                .createdAt(diagnostic.getCreatedAt())
                .updatedAt(diagnostic.getUpdatedAt())
                .build();
    }

    private DiagnosticHistoryItemDto mapToHistoryItemDto(DiagnosticHistory history) {
        PerformedByDto performedByDto = null;
        if (history.getActor() != null) {
            User actor = history.getActor();
            String roleCode = actor.getRole() != null ? actor.getRole().getCode() : null;
            performedByDto = PerformedByDto.builder()
                    .userId(actor.getUserId())
                    .name(actor.getName())
                    .role(roleCode)
                    .build();
        }

        return DiagnosticHistoryItemDto.builder()
                .historyId(history.getHistoryId())
                .action(history.getAction())
                .previousStatus(history.getPreviousStatus())
                .newStatus(history.getNewStatus())
                .performedBy(performedByDto)
                .comment(history.getComment())
                .timestamp(history.getCreatedAt())
                .build();
    }

    /**
     * 기안 상태 기반으로 캠페인 상태 결정 (Issue #156)
     * - 기안이 COMPLETED면 캠페인도 "COMPLETED" (완료)
     * - 그 외의 경우 "ACTIVE" (진행중)
     */
    private String determineCampaignStatusByDiagnostic(DiagnosticStatus diagnosticStatus) {
        if (diagnosticStatus == DiagnosticStatus.COMPLETED) {
            return "COMPLETED";
        }
        return "ACTIVE";
    }

    private String getCampaignStatusLabel(String campaignStatus) {
        return switch (campaignStatus) {
            case "COMPLETED" -> "완료";
            case "ACTIVE" -> "진행중";
            default -> "알 수 없음";
        };
    }
}
