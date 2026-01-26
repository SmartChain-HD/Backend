package com.smartchain.platform.domain.diagnostic.service;

import com.smartchain.platform.domain.approval.entity.Approval;
import com.smartchain.platform.domain.approval.repository.ApprovalRepository;
import com.smartchain.platform.domain.diagnostic.entity.Campaign;
import com.smartchain.platform.domain.diagnostic.entity.Diagnostic;
import com.smartchain.platform.domain.diagnostic.entity.DiagnosticHistory;
import com.smartchain.platform.domain.diagnostic.repository.CampaignRepository;
import com.smartchain.platform.domain.diagnostic.repository.DiagnosticHistoryRepository;
import com.smartchain.platform.domain.diagnostic.repository.DiagnosticRepository;
import com.smartchain.platform.domain.user.entity.Company;
import com.smartchain.platform.domain.user.entity.User;
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
import java.time.Year;
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

    private static final List<String> ALLOWED_ROLES = Arrays.asList("DRAFTER", "APPROVER");

    private static final Map<String, String> STATUS_LABEL_MAP = Map.of(
            "WRITING", "작성중",
            "SUBMITTED", "제출됨",
            "RETURNED", "반려됨",
            "APPROVED", "내부승인",
            "REVIEWING", "심사중",
            "COMPLETED", "완료"
    );

    public DiagnosticListResponse getDiagnosticList(Long userId, String statuses, LocalDate deadlineFrom,
                                                     LocalDate deadlineTo, int page, int size) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        validateAccessRole(currentUser);

        Company userCompany = currentUser.getCompany();
        if (userCompany == null) {
            throw new CustomException(ErrorCode.PERMISSION_DENIED_RESOURCE);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Diagnostic> diagnosticPage;

        if (statuses != null && !statuses.isEmpty()) {
            List<DiagnosticStatus> statusList = parseStatuses(statuses);
            diagnosticPage = diagnosticRepository.findByCompanyAndStatusInOrderByCreatedAtDesc(
                    userCompany, statusList, pageable);
        } else if (deadlineFrom != null && deadlineTo != null) {
            diagnosticPage = diagnosticRepository.findByCompanyAndDeadlineBetweenOrderByCreatedAtDesc(
                    userCompany, deadlineFrom, deadlineTo, pageable);
        } else {
            diagnosticPage = diagnosticRepository.findByCompanyOrderByCreatedAtDesc(userCompany, pageable);
        }

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

    public DiagnosticDetailResponse getDiagnosticDetail(Long userId, Long diagnosticId) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        validateAccessRole(currentUser);

        Diagnostic diagnostic = diagnosticRepository.findById(diagnosticId)
                .orElseThrow(() -> new CustomException(ErrorCode.DIAGNOSTIC_NOT_FOUND));

        validateCompanyAccess(currentUser, diagnostic);

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

        return DiagnosticDetailResponse.builder()
                .diagnosticId(diagnostic.getDiagnosticId())
                .diagnosticCode(diagnostic.getDiagnosticCode())
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

        String userRoleCode = currentUser.getRole() != null ? currentUser.getRole().getCode() : "GUEST";
        if (!"DRAFTER".equals(userRoleCode)) {
            throw new CustomException(ErrorCode.PERMISSION_DENIED_ACTION);
        }

        Company userCompany = currentUser.getCompany();
        if (userCompany == null) {
            throw new CustomException(ErrorCode.PERMISSION_DENIED_RESOURCE);
        }

        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new CustomException(ErrorCode.CAMPAIGN_NOT_FOUND));

        String diagnosticCode = generateDiagnosticCode();

        Diagnostic diagnostic = Diagnostic.builder()
                .diagnosticCode(diagnosticCode)
                .title(campaign.getTitle())
                .campaign(campaign)
                .company(userCompany)
                .drafterId(userId)
                .periodStartDate(campaign.getPeriodStartDate())
                .periodEndDate(campaign.getPeriodEndDate())
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

        String userRoleCode = currentUser.getRole() != null ? currentUser.getRole().getCode() : "GUEST";
        if (!"DRAFTER".equals(userRoleCode)) {
            throw new CustomException(ErrorCode.PERMISSION_DENIED_ACTION);
        }

        Diagnostic diagnostic = diagnosticRepository.findById(diagnosticId)
                .orElseThrow(() -> new CustomException(ErrorCode.DIAGNOSTIC_NOT_FOUND));

        if (!diagnostic.getDrafterId().equals(userId)) {
            throw new CustomException(ErrorCode.PERMISSION_DENIED_RESOURCE);
        }

        // BIZ_002: 이미 제출된 기안
        if (diagnostic.getStatus() == DiagnosticStatus.SUBMITTED) {
            throw new CustomException(ErrorCode.DIAGNOSTIC_ALREADY_SUBMITTED);
        }

        // BIZ_001: 상태 전이 불가
        if (!diagnostic.canSubmit()) {
            throw new CustomException(ErrorCode.DIAGNOSTIC_INVALID_STATE_TRANSITION);
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

        // Approval 레코드 자동 생성
        Approval approval = Approval.builder()
                .diagnostic(diagnostic)
                .requester(currentUser)
                .requestComment(request.getSubmitComment())
                .deadline(diagnostic.getDeadline())
                .build();
        Approval savedApproval = approvalRepository.save(approval);

        log.info("Diagnostic submitted: diagnosticId={}, submittedBy={}, approvalId={}",
                diagnosticId, userId, savedApproval.getApprovalId());

        return DiagnosticSubmitResponse.builder()
                .diagnosticId(diagnostic.getDiagnosticId())
                .previousStatus(previousStatus)
                .newStatus(DiagnosticStatus.SUBMITTED.name())
                .submittedAt(diagnostic.getSubmittedAt())
                .approvalId(savedApproval.getApprovalId())
                .message("기안이 제출되었습니다")
                .build();
    }

    public AiAnalysisResponse getAiAnalysis(Long userId, Long diagnosticId) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        validateAccessRole(currentUser);

        Diagnostic diagnostic = diagnosticRepository.findById(diagnosticId)
                .orElseThrow(() -> new CustomException(ErrorCode.DIAGNOSTIC_NOT_FOUND));

        validateCompanyAccess(currentUser, diagnostic);

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

        validateAccessRole(currentUser);

        Diagnostic diagnostic = diagnosticRepository.findById(diagnosticId)
                .orElseThrow(() -> new CustomException(ErrorCode.DIAGNOSTIC_NOT_FOUND));

        validateCompanyAccess(currentUser, diagnostic);

        List<DiagnosticHistory> histories = diagnosticHistoryRepository.findByDiagnosticOrderByCreatedAtDesc(diagnostic);

        List<DiagnosticHistoryItemDto> historyItems = histories.stream()
                .map(this::mapToHistoryItemDto)
                .toList();

        return DiagnosticHistoryResponse.builder()
                .diagnosticId(diagnosticId)
                .history(historyItems)
                .build();
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
        Long maxId = diagnosticRepository.findMaxDiagnosticId();
        long sequence = (maxId != null ? maxId : 0L) + 1;
        return String.format("DG-%d-%05d", year, sequence);
    }

    private DiagnosticListItemDto mapToListItemDto(Diagnostic diagnostic) {
        CampaignSimpleDto campaignDto = null;
        if (diagnostic.getCampaign() != null) {
            Campaign campaign = diagnostic.getCampaign();
            campaignDto = CampaignSimpleDto.builder()
                    .campaignId(campaign.getCampaignId())
                    .campaignCode(campaign.getCampaignCode())
                    .title(campaign.getTitle())
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

        return DiagnosticListItemDto.builder()
                .diagnosticId(diagnostic.getDiagnosticId())
                .diagnosticCode(diagnostic.getDiagnosticCode())
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
}
