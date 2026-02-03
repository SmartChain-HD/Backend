package com.smartchain.platform.domain.approval.service;

import com.smartchain.platform.domain.approval.entity.Approval;
import com.smartchain.platform.domain.approval.repository.ApprovalRepository;
import com.smartchain.platform.domain.diagnostic.entity.Diagnostic;
import com.smartchain.platform.domain.user.entity.Company;
import com.smartchain.platform.domain.user.entity.Domain;
import com.smartchain.platform.domain.user.entity.User;
import com.smartchain.platform.domain.user.repository.DomainRepository;
import com.smartchain.platform.domain.user.repository.UserRepository;
import com.smartchain.platform.dto.approval.common.DiagnosticSimpleDto;
import com.smartchain.platform.dto.approval.common.PageDto;
import com.smartchain.platform.dto.approval.common.RequesterDto;
import com.smartchain.platform.dto.approval.decision.ApprovalDecisionRequest;
import com.smartchain.platform.dto.approval.decision.ApprovalDecisionResponse;
import com.smartchain.platform.dto.approval.detail.ApprovalDetailResponse;
import com.smartchain.platform.dto.approval.detail.DiagnosticDetailDto;
import com.smartchain.platform.dto.approval.detail.ProcessedByDto;
import com.smartchain.platform.dto.approval.detail.RequesterDetailDto;
import com.smartchain.platform.dto.approval.list.ApprovalListItemDto;
import com.smartchain.platform.dto.approval.list.ApprovalListResponse;
import com.smartchain.platform.dto.approval.list.ApprovalStatsDto;
import com.smartchain.platform.dto.approval.submit.SubmitToReviewerRequest;
import com.smartchain.platform.dto.approval.submit.SubmitToReviewerResponse;
import com.smartchain.platform.global.enums.ApprovalStatus;
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

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApprovalService {

    private final ApprovalRepository approvalRepository;
    private final UserRepository userRepository;
    private final DomainRepository domainRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private static final Map<String, String> STATUS_LABEL_MAP = Map.of(
            "WAITING", "결재전",
            "APPROVED", "결재 완료",
            "REJECTED", "반려"
    );

    public ApprovalListResponse getApprovalList(Long userId, String domainCode, String status, int page, int size) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Company userCompany = currentUser.getCompany();
        if (userCompany == null) {
            throw new CustomException(ErrorCode.PERMISSION_DENIED_RESOURCE);
        }

        // APPROVER 권한을 가진 도메인 목록 조회
        List<Domain> approverDomains = currentUser.getDomainsWithRole("APPROVER");

        // 도메인 역할이 없으면 레거시 로직 사용
        if (approverDomains.isEmpty()) {
            return getApprovalListLegacy(currentUser, userCompany, domainCode, status, page, size);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Approval> approvalPage;
        ApprovalStatsDto stats;

        // domainCode가 지정된 경우 단일 도메인 필터링
        if (domainCode != null && !domainCode.isEmpty()) {
            Domain filterDomain = domainRepository.findByCode(domainCode)
                    .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT));

            // 사용자가 해당 도메인에서 APPROVER 역할이 있는지 확인
            if (!currentUser.hasRoleInDomain(domainCode, "APPROVER")) {
                throw new CustomException(ErrorCode.PERMISSION_DENIED_ACTION);
            }

            if (status != null && !status.isEmpty()) {
                ApprovalStatus approvalStatus = parseStatus(status);
                approvalPage = approvalRepository.findByDomainAndCompanyAndStatusOrderByCreatedAtDesc(
                        filterDomain, userCompany, approvalStatus, pageable);
            } else {
                approvalPage = approvalRepository.findByDomainAndCompanyOrderByCreatedAtDesc(
                        filterDomain, userCompany, pageable);
            }

            stats = ApprovalStatsDto.builder()
                    .waiting(approvalRepository.countByDomainAndCompanyAndStatus(filterDomain, userCompany, ApprovalStatus.WAITING))
                    .approved(approvalRepository.countByDomainAndCompanyAndStatus(filterDomain, userCompany, ApprovalStatus.APPROVED))
                    .rejected(approvalRepository.countByDomainAndCompanyAndStatus(filterDomain, userCompany, ApprovalStatus.REJECTED))
                    .build();
        } else {
            // 전체 도메인 조회 (기존 로직)
            if (status != null && !status.isEmpty()) {
                ApprovalStatus approvalStatus = parseStatus(status);
                approvalPage = approvalRepository.findByDomainsAndCompanyAndStatusOrderByCreatedAtDesc(
                        approverDomains, userCompany, approvalStatus, pageable);
            } else {
                approvalPage = approvalRepository.findByDomainsAndCompanyOrderByCreatedAtDesc(
                        approverDomains, userCompany, pageable);
            }

            stats = ApprovalStatsDto.builder()
                    .waiting(approvalRepository.countByDomainsAndCompanyAndStatus(approverDomains, userCompany, ApprovalStatus.WAITING))
                    .approved(approvalRepository.countByDomainsAndCompanyAndStatus(approverDomains, userCompany, ApprovalStatus.APPROVED))
                    .rejected(approvalRepository.countByDomainsAndCompanyAndStatus(approverDomains, userCompany, ApprovalStatus.REJECTED))
                    .build();
        }

        List<ApprovalListItemDto> content = approvalPage.getContent().stream()
                .map(this::mapToListItemDto)
                .toList();

        PageDto pageDto = PageDto.builder()
                .number(approvalPage.getNumber())
                .size(approvalPage.getSize())
                .totalElements(approvalPage.getTotalElements())
                .totalPages(approvalPage.getTotalPages())
                .build();

        return ApprovalListResponse.builder()
                .stats(stats)
                .content(content)
                .page(pageDto)
                .build();
    }

    /**
     * 레거시: 도메인 역할이 없는 사용자용 결재 목록 조회
     * Note: domainCode 필터는 레거시 모드에서는 지원하지 않음 (도메인 정보가 없는 데이터)
     */
    private ApprovalListResponse getApprovalListLegacy(User currentUser, Company userCompany,
                                                        String domainCode, String status, int page, int size) {
        String userRoleCode = currentUser.getRole() != null ? currentUser.getRole().getCode() : "GUEST";
        if (!"APPROVER".equals(userRoleCode)) {
            throw new CustomException(ErrorCode.PERMISSION_DENIED_ACTION);
        }

        // 레거시 모드에서 domainCode 필터는 무시 (경고 로그만 출력)
        if (domainCode != null && !domainCode.isEmpty()) {
            log.warn("domainCode filter '{}' ignored in legacy mode for user {}", domainCode, currentUser.getUserId());
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Approval> approvalPage;

        if (status != null && !status.isEmpty()) {
            ApprovalStatus approvalStatus = parseStatus(status);
            approvalPage = approvalRepository.findByCompanyAndStatusOrderByCreatedAtDesc(userCompany, approvalStatus, pageable);
        } else {
            approvalPage = approvalRepository.findByCompanyOrderByCreatedAtDesc(userCompany, pageable);
        }

        ApprovalStatsDto stats = ApprovalStatsDto.builder()
                .waiting(approvalRepository.countByCompanyAndStatus(userCompany, ApprovalStatus.WAITING))
                .approved(approvalRepository.countByCompanyAndStatus(userCompany, ApprovalStatus.APPROVED))
                .rejected(approvalRepository.countByCompanyAndStatus(userCompany, ApprovalStatus.REJECTED))
                .build();

        List<ApprovalListItemDto> content = approvalPage.getContent().stream()
                .map(this::mapToListItemDto)
                .toList();

        PageDto pageDto = PageDto.builder()
                .number(approvalPage.getNumber())
                .size(approvalPage.getSize())
                .totalElements(approvalPage.getTotalElements())
                .totalPages(approvalPage.getTotalPages())
                .build();

        return ApprovalListResponse.builder()
                .stats(stats)
                .content(content)
                .page(pageDto)
                .build();
    }

    public ApprovalDetailResponse getApprovalDetail(Long userId, Long approvalId) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Approval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPROVAL_NOT_FOUND));

        // 도메인 기반 권한 검증
        validateDomainApproverAccess(currentUser, approval);

        DiagnosticDetailDto diagnosticDto = DiagnosticDetailDto.builder()
                .diagnosticId(approval.getDiagnostic().getDiagnosticId())
                .diagnosticCode(approval.getDiagnostic().getDiagnosticCode())
                .title(approval.getDiagnostic().getTitle())
                .qualitativeProgress(approval.getDiagnostic().getQualitativeProgress())
                .quantitativeProgress(approval.getDiagnostic().getQuantitativeProgress())
                .overallScore(approval.getDiagnostic().getOverallScore())
                .build();

        RequesterDetailDto requesterDto = RequesterDetailDto.builder()
                .userId(approval.getRequester().getUserId())
                .name(approval.getRequester().getName())
                .email(approval.getRequester().getEmail())
                .build();

        ProcessedByDto processedByDto = null;
        if (approval.getApprover() != null) {
            processedByDto = ProcessedByDto.builder()
                    .userId(approval.getApprover().getUserId())
                    .name(approval.getApprover().getName())
                    .build();
        }

        // 도메인 정보 추출
        Domain domain = approval.getDiagnostic().getDomain();
        String domainCode = domain != null ? domain.getCode() : null;
        String domainName = domain != null ? domain.getName() : null;

        return ApprovalDetailResponse.builder()
                .approvalId(approval.getApprovalId())
                .diagnostic(diagnosticDto)
                .requester(requesterDto)
                .domainCode(domainCode)
                .domainName(domainName)
                .status(approval.getStatus().name())
                .statusLabel(STATUS_LABEL_MAP.getOrDefault(approval.getStatus().name(), approval.getStatus().name()))
                .requestComment(approval.getRequestComment())
                .requestedAt(approval.getCreatedAt())
                .processedAt(approval.getProcessedAt())
                .processedBy(processedByDto)
                .approverComment(approval.getApproverComment())
                .build();
    }

    @Transactional
    public ApprovalDecisionResponse processApproval(Long userId, Long approvalId, ApprovalDecisionRequest request) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Approval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPROVAL_NOT_FOUND));

        // 도메인 기반 권한 검증
        validateDomainApproverAccess(currentUser, approval);

        if (!approval.isWaiting()) {
            throw new CustomException(ErrorCode.ALREADY_PROCESSED_APPROVAL);
        }

        String decision = request.getDecision().toUpperCase();
        String message;
        String diagnosticNewStatus;

        if ("APPROVED".equals(decision)) {
            approval.approve(currentUser, request.getComment());
            approval.getDiagnostic().markAsApproved();
            message = "결재가 완료되었습니다";
            diagnosticNewStatus = DiagnosticStatus.APPROVED.name();
            log.info("Approval approved: approvalId={}, approvedBy={}", approvalId, userId);
        } else if ("REJECTED".equals(decision)) {
            approval.reject(currentUser, request.getComment());
            approval.getDiagnostic().markAsReturned();
            message = "결재가 반려되었습니다";
            diagnosticNewStatus = DiagnosticStatus.RETURNED.name();
            log.info("Approval rejected: approvalId={}, rejectedBy={}, comment={}",
                    approvalId, userId, request.getComment());
        } else {
            throw new CustomException(ErrorCode.INVALID_DECISION);
        }

        ProcessedByDto processedByDto = ProcessedByDto.builder()
                .userId(currentUser.getUserId())
                .name(currentUser.getName())
                .build();

        return ApprovalDecisionResponse.builder()
                .approvalId(approval.getApprovalId())
                .status(approval.getStatus().name())
                .diagnosticNewStatus(diagnosticNewStatus)
                .processedAt(approval.getProcessedAt())
                .processedBy(processedByDto)
                .message(message)
                .build();
    }

    @Transactional
    public SubmitToReviewerResponse submitToReviewer(Long userId, Long approvalId, SubmitToReviewerRequest request) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Approval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPROVAL_NOT_FOUND));

        // 도메인 기반 권한 검증
        validateDomainApproverAccess(currentUser, approval);

        if (!approval.isApproved()) {
            throw new CustomException(ErrorCode.APPROVAL_NOT_APPROVED);
        }

        if (!approval.getDiagnostic().isApproved()) {
            throw new CustomException(ErrorCode.DIAGNOSTIC_NOT_APPROVED);
        }

        String previousStatus = approval.getDiagnostic().getStatus().name();

        approval.submitToReviewer();
        approval.getDiagnostic().markAsReviewing();

        log.info("Submitted to reviewer: approvalId={}, diagnosticId={}, submittedBy={}",
                approvalId, approval.getDiagnostic().getDiagnosticId(), userId);

        return SubmitToReviewerResponse.builder()
                .approvalId(approval.getApprovalId())
                .diagnosticId(approval.getDiagnostic().getDiagnosticId())
                .reviewId(null)  // Review entity not yet created - will be created by Review API
                .previousStatus(previousStatus)
                .newStatus(DiagnosticStatus.REVIEWING.name())
                .submittedAt(approval.getSubmittedToReviewerAt())
                .message("원청에 제출되었습니다")
                .build();
    }

    /**
     * 도메인 기반 APPROVER 접근 권한 검증
     */
    private void validateDomainApproverAccess(User user, Approval approval) {
        Diagnostic diagnostic = approval.getDiagnostic();
        Domain domain = diagnostic.getDomain();

        if (domain != null) {
            // 도메인 기반 권한 검증
            if (!user.hasRoleInDomain(domain.getCode(), "APPROVER")) {
                log.warn("User {} does not have APPROVER role in domain {}", user.getUserId(), domain.getCode());
                throw new CustomException(ErrorCode.PERMISSION_DENIED_ACTION);
            }

            // 같은 회사 검증
            Company userCompany = user.getCompany();
            if (userCompany == null || !userCompany.getCompanyId().equals(diagnostic.getCompany().getCompanyId())) {
                throw new CustomException(ErrorCode.PERMISSION_DENIED_RESOURCE);
            }
        } else {
            // 레거시: 도메인이 없는 경우 기본 역할로 검증
            String userRoleCode = user.getRole() != null ? user.getRole().getCode() : "GUEST";
            if (!"APPROVER".equals(userRoleCode)) {
                throw new CustomException(ErrorCode.PERMISSION_DENIED_ACTION);
            }

            Company userCompany = user.getCompany();
            if (userCompany == null || !userCompany.getCompanyId().equals(diagnostic.getCompany().getCompanyId())) {
                throw new CustomException(ErrorCode.PERMISSION_DENIED_RESOURCE);
            }
        }
    }

    private ApprovalStatus parseStatus(String status) {
        try {
            return ApprovalStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
    }

    private ApprovalListItemDto mapToListItemDto(Approval approval) {
        DiagnosticSimpleDto diagnosticDto = DiagnosticSimpleDto.builder()
                .diagnosticId(approval.getDiagnostic().getDiagnosticId())
                .diagnosticCode(approval.getDiagnostic().getDiagnosticCode())
                .title(approval.getDiagnostic().getTitle())
                .build();

        RequesterDto requesterDto = RequesterDto.builder()
                .userId(approval.getRequester().getUserId())
                .name(approval.getRequester().getName())
                .build();

        String requestedAtLabel = approval.getCreatedAt() != null
                ? approval.getCreatedAt().format(DATE_FORMATTER)
                : null;

        // 도메인 정보 추출
        Domain domain = approval.getDiagnostic().getDomain();
        String domainCode = domain != null ? domain.getCode() : null;
        String domainName = domain != null ? domain.getName() : null;

        return ApprovalListItemDto.builder()
                .approvalId(approval.getApprovalId())
                .diagnostic(diagnosticDto)
                .requester(requesterDto)
                .domainCode(domainCode)
                .domainName(domainName)
                .status(approval.getStatus().name())
                .statusLabel(STATUS_LABEL_MAP.getOrDefault(approval.getStatus().name(), approval.getStatus().name()))
                .requestedAt(approval.getCreatedAt())
                .requestedAtLabel(requestedAtLabel)
                .deadline(approval.getDeadline())
                .build();
    }
}