package com.smartchain.platform.domain.approval.service;

import com.smartchain.platform.domain.approval.entity.Approval;
import com.smartchain.platform.domain.approval.repository.ApprovalRepository;
import com.smartchain.platform.domain.user.entity.Company;
import com.smartchain.platform.domain.user.entity.User;
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
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApprovalService {

    private final ApprovalRepository approvalRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private static final Map<String, String> STATUS_LABEL_MAP = Map.of(
            "WAITING", "결재전",
            "APPROVED", "결재 완료",
            "REJECTED", "반려"
    );

    public ApprovalListResponse getApprovalList(Long userId, String status, int page, int size) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String userRoleCode = currentUser.getRole() != null ? currentUser.getRole().getCode() : "GUEST";
        if (!"APPROVER".equals(userRoleCode)) {
            throw new CustomException(ErrorCode.PERMISSION_DENIED_ACTION);
        }

        Company userCompany = currentUser.getCompany();
        if (userCompany == null) {
            throw new CustomException(ErrorCode.PERMISSION_DENIED_RESOURCE);
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

        String userRoleCode = currentUser.getRole() != null ? currentUser.getRole().getCode() : "GUEST";
        if (!"APPROVER".equals(userRoleCode)) {
            throw new CustomException(ErrorCode.PERMISSION_DENIED_ACTION);
        }

        Approval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPROVAL_NOT_FOUND));

        Company userCompany = currentUser.getCompany();
        if (userCompany == null || !userCompany.getCompanyId().equals(approval.getDiagnostic().getCompany().getCompanyId())) {
            throw new CustomException(ErrorCode.PERMISSION_DENIED_RESOURCE);
        }

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

        return ApprovalDetailResponse.builder()
                .approvalId(approval.getApprovalId())
                .diagnostic(diagnosticDto)
                .requester(requesterDto)
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

        String userRoleCode = currentUser.getRole() != null ? currentUser.getRole().getCode() : "GUEST";
        if (!"APPROVER".equals(userRoleCode)) {
            throw new CustomException(ErrorCode.PERMISSION_DENIED_ACTION);
        }

        Approval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPROVAL_NOT_FOUND));

        Company userCompany = currentUser.getCompany();
        if (userCompany == null || !userCompany.getCompanyId().equals(approval.getDiagnostic().getCompany().getCompanyId())) {
            throw new CustomException(ErrorCode.PERMISSION_DENIED_RESOURCE);
        }

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

        String userRoleCode = currentUser.getRole() != null ? currentUser.getRole().getCode() : "GUEST";
        if (!"APPROVER".equals(userRoleCode)) {
            throw new CustomException(ErrorCode.PERMISSION_DENIED_ACTION);
        }

        Approval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPROVAL_NOT_FOUND));

        Company userCompany = currentUser.getCompany();
        if (userCompany == null || !userCompany.getCompanyId().equals(approval.getDiagnostic().getCompany().getCompanyId())) {
            throw new CustomException(ErrorCode.PERMISSION_DENIED_RESOURCE);
        }

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

        return ApprovalListItemDto.builder()
                .approvalId(approval.getApprovalId())
                .diagnostic(diagnosticDto)
                .requester(requesterDto)
                .status(approval.getStatus().name())
                .statusLabel(STATUS_LABEL_MAP.getOrDefault(approval.getStatus().name(), approval.getStatus().name()))
                .requestedAt(approval.getCreatedAt())
                .requestedAtLabel(requestedAtLabel)
                .deadline(approval.getDeadline())
                .build();
    }
}