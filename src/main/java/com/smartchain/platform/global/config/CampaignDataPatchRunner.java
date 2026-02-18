package com.smartchain.platform.global.config;

import com.smartchain.platform.domain.diagnostic.entity.Campaign;
import com.smartchain.platform.domain.diagnostic.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class CampaignDataPatchRunner implements CommandLineRunner {

    private static final String COMPLIANCE_CAMPAIGN_CODE = "CAMP-COMPL-2025-H2";
    private static final LocalDate TARGET_START_DATE = LocalDate.of(2025, 9, 30);
    private static final LocalDate TARGET_END_DATE = LocalDate.of(2026, 6, 30);
    private static final LocalDate TARGET_DEADLINE = LocalDate.of(2026, 8, 31);

    private final CampaignRepository campaignRepository;

    @Override
    @Transactional
    public void run(String... args) {
        campaignRepository.findByCampaignCode(COMPLIANCE_CAMPAIGN_CODE).ifPresent(campaign -> {
            boolean needsSchedulePatch =
                campaign.getPeriodEndDate() == null || campaign.getPeriodEndDate().isBefore(LocalDate.now());
            boolean needsTitlePatch = campaign.getTitle() == null
                || campaign.getTitle().contains("하도급 컴플라이언스");

            if (!needsSchedulePatch && !needsTitlePatch) {
                return;
            }

            if (needsSchedulePatch) {
                campaign.updateSchedule(TARGET_START_DATE, TARGET_END_DATE, TARGET_DEADLINE);
            }
            if (needsTitlePatch) {
                campaign.updateBasicInfo("2025년 하반기 컴플라이언스 점검", campaign.getContent());
            }
            if (!Boolean.TRUE.equals(campaign.getIsActive())) {
                campaign.activate();
            }

            Campaign saved = campaignRepository.save(campaign);
            log.info(
                "Patched compliance campaign: code={}, startDate={}, endDate={}, deadline={}, title={}",
                saved.getCampaignCode(),
                saved.getPeriodStartDate(),
                saved.getPeriodEndDate(),
                saved.getDeadline(),
                saved.getTitle()
            );
        });
    }
}
