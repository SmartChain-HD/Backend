package com.smartchain.platform.domain.campaign.service;

import com.smartchain.platform.domain.diagnostic.entity.Campaign;
import com.smartchain.platform.domain.diagnostic.entity.Diagnostic;
import com.smartchain.platform.domain.diagnostic.repository.CampaignRepository;
import com.smartchain.platform.domain.diagnostic.repository.DiagnosticRepository;
import com.smartchain.platform.dto.campaign.detail.CampaignDetailResponse;
import com.smartchain.platform.dto.campaign.detail.CampaignStatsDto;
import com.smartchain.platform.dto.campaign.list.CampaignItemDto;
import com.smartchain.platform.dto.campaign.list.CampaignListResponse;
import com.smartchain.platform.global.error.CustomException;
import com.smartchain.platform.global.error.ErrorCode;
import com.smartchain.platform.global.enums.DiagnosticStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final DiagnosticRepository diagnosticRepository;

    /**
     * 캠페인 목록 조회
     * @param activeOnly true이면 활성화된(isActive=true) 캠페인만 조회
     */
    public CampaignListResponse getCampaignList(Boolean activeOnly) {
        List<Campaign> campaigns;
        if (Boolean.TRUE.equals(activeOnly)) {
            campaigns = campaignRepository.findByIsActiveTrue();
        } else {
            campaigns = campaignRepository.findAll();
        }

        List<CampaignItemDto> items = campaigns.stream()
                .map(this::toCampaignItemDto)
                .toList();

        return CampaignListResponse.builder()
                .campaigns(items)
                .totalCount(items.size())
                .build();
    }

    /**
     * 캠페인 상세 조회
     */
    public CampaignDetailResponse getCampaignDetail(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new CustomException(ErrorCode.CAMPAIGN_NOT_FOUND));

        List<Diagnostic> diagnostics = diagnosticRepository.findByCampaign(campaign);

        // 통계 계산
        int totalTargets = diagnostics.size();
        int notStarted = (int) diagnostics.stream()
                .filter(d -> d.getStatus() == DiagnosticStatus.WRITING)
                .count();
        int inProgress = (int) diagnostics.stream()
                .filter(d -> d.getStatus() == DiagnosticStatus.SUBMITTED || d.getStatus() == DiagnosticStatus.REVIEWING)
                .count();
        int submitted = (int) diagnostics.stream()
                .filter(d -> d.getStatus() == DiagnosticStatus.APPROVED)
                .count();
        int approved = (int) diagnostics.stream()
                .filter(d -> d.getStatus() == DiagnosticStatus.COMPLETED)
                .count();
        int rejected = (int) diagnostics.stream()
                .filter(d -> d.getStatus() == DiagnosticStatus.RETURNED)
                .count();

        CampaignStatsDto stats = CampaignStatsDto.builder()
                .totalTargets(totalTargets)
                .notStarted(notStarted)
                .inProgress(inProgress)
                .submitted(submitted)
                .approved(approved)
                .rejected(rejected)
                .build();

        return CampaignDetailResponse.builder()
                .campaignId(campaign.getCampaignId())
                .campaignCode(campaign.getCampaignCode())
                .domainCode(campaign.getDomain() != null ? campaign.getDomain().getCode() : null)
                .title(campaign.getTitle())
                .description(campaign.getContent())
                .startDate(campaign.getPeriodStartDate())
                .endDate(campaign.getPeriodEndDate())
                .deadline(campaign.getDeadline())
                .status(determineCampaignStatus(campaign))
                .statusLabel(determineCampaignStatusLabel(campaign))
                .isActive(campaign.getIsActive())
                .stats(stats)
                .targetCompanies(List.of())
                .templates(List.of())
                .build();
    }

    private CampaignItemDto toCampaignItemDto(Campaign campaign) {
        List<Diagnostic> diagnostics = diagnosticRepository.findByCampaign(campaign);

        int targetCount = diagnostics.size();
        int submittedCount = (int) diagnostics.stream()
                .filter(d -> d.getStatus() != DiagnosticStatus.WRITING)
                .count();
        int progressRate = targetCount > 0 ? (submittedCount * 100) / targetCount : 0;

        return CampaignItemDto.builder()
                .campaignId(campaign.getCampaignId())
                .campaignCode(campaign.getCampaignCode())
                .domainCode(campaign.getDomain() != null ? campaign.getDomain().getCode() : null)
                .title(campaign.getTitle())
                .description(campaign.getContent())
                .startDate(campaign.getPeriodStartDate())
                .endDate(campaign.getPeriodEndDate())
                .deadline(campaign.getDeadline())
                .status(determineCampaignStatus(campaign))
                .statusLabel(determineCampaignStatusLabel(campaign))
                .isActive(campaign.getIsActive())
                .targetCompanyCount(targetCount)
                .submittedCount(submittedCount)
                .progressRate(progressRate)
                .build();
    }

    private String determineCampaignStatus(Campaign campaign) {
        LocalDate now = LocalDate.now();
        if (campaign.getPeriodStartDate().isAfter(now)) {
            return "DRAFT";
        } else if (campaign.getPeriodEndDate().isBefore(now)) {
            return "CLOSED";
        } else {
            return "ACTIVE";
        }
    }

    private String determineCampaignStatusLabel(Campaign campaign) {
        String status = determineCampaignStatus(campaign);
        return switch (status) {
            case "DRAFT" -> "준비중";
            case "ACTIVE" -> "진행중";
            case "CLOSED" -> "종료";
            default -> "알 수 없음";
        };
    }
}
