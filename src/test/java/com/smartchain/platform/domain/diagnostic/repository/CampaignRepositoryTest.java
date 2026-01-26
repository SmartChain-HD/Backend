package com.smartchain.platform.domain.diagnostic.repository;

import com.smartchain.platform.domain.diagnostic.entity.Campaign;
import com.smartchain.platform.domain.user.entity.Domain;
import com.smartchain.platform.domain.user.repository.DomainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CampaignRepositoryTest {

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private DomainRepository domainRepository;

    private Domain esgDomain;
    private Domain safetyDomain;

    @BeforeEach
    void setUp() {
        esgDomain = domainRepository.save(Domain.builder()
                .code("ESG")
                .name("ESG 실사")
                .description("ESG 공급망 실사")
                .isActive(true)
                .build());

        safetyDomain = domainRepository.save(Domain.builder()
                .code("SAFETY")
                .name("안전보건")
                .description("안전보건 관리")
                .isActive(true)
                .build());
    }

    @Test
    @DisplayName("Domain으로 Campaign 목록 조회 성공")
    void findByDomain_Success() {
        // given
        Campaign esgCampaign1 = campaignRepository.save(Campaign.builder()
                .campaignCode("CAMP-ESG-001")
                .ownerCompanyId(1L)
                .domain(esgDomain)
                .title("ESG 캠페인 1")
                .content("ESG 캠페인 내용")
                .periodStartDate(LocalDate.of(2026, 1, 1))
                .periodEndDate(LocalDate.of(2026, 12, 31))
                .deadline(LocalDate.of(2026, 3, 31))
                .build());

        Campaign esgCampaign2 = campaignRepository.save(Campaign.builder()
                .campaignCode("CAMP-ESG-002")
                .ownerCompanyId(1L)
                .domain(esgDomain)
                .title("ESG 캠페인 2")
                .content("ESG 캠페인 내용 2")
                .periodStartDate(LocalDate.of(2026, 1, 1))
                .periodEndDate(LocalDate.of(2026, 12, 31))
                .deadline(LocalDate.of(2026, 3, 31))
                .build());

        Campaign safetyCampaign = campaignRepository.save(Campaign.builder()
                .campaignCode("CAMP-SAFETY-001")
                .ownerCompanyId(1L)
                .domain(safetyDomain)
                .title("안전보건 캠페인")
                .content("안전보건 캠페인 내용")
                .periodStartDate(LocalDate.of(2026, 1, 1))
                .periodEndDate(LocalDate.of(2026, 12, 31))
                .deadline(LocalDate.of(2026, 3, 31))
                .build());

        // when
        List<Campaign> esgCampaigns = campaignRepository.findByDomain(esgDomain);
        List<Campaign> safetyCampaigns = campaignRepository.findByDomain(safetyDomain);

        // then
        assertThat(esgCampaigns).hasSize(2);
        assertThat(esgCampaigns).extracting(Campaign::getDomain)
                .allMatch(domain -> domain.getCode().equals("ESG"));

        assertThat(safetyCampaigns).hasSize(1);
        assertThat(safetyCampaigns.get(0).getTitle()).isEqualTo("안전보건 캠페인");
    }

    @Test
    @DisplayName("Domain으로 Campaign 페이징 조회 성공")
    void findByDomainOrderByCreatedAtDesc_Success() {
        // given
        for (int i = 1; i <= 5; i++) {
            campaignRepository.save(Campaign.builder()
                    .campaignCode("CAMP-ESG-00" + i)
                    .ownerCompanyId(1L)
                    .domain(esgDomain)
                    .title("ESG 캠페인 " + i)
                    .content("ESG 캠페인 내용 " + i)
                    .periodStartDate(LocalDate.of(2026, 1, 1))
                    .periodEndDate(LocalDate.of(2026, 12, 31))
                    .deadline(LocalDate.of(2026, 3, 31))
                    .build());
        }

        // when
        Page<Campaign> page = campaignRepository.findByDomainOrderByCreatedAtDesc(
                esgDomain, PageRequest.of(0, 3));

        // then
        assertThat(page.getContent()).hasSize(3);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("Campaign 엔티티에 Domain 필드가 정상적으로 저장되고 조회됨")
    void campaignDomainField_PersistenceTest() {
        // given
        Campaign campaign = campaignRepository.save(Campaign.builder()
                .campaignCode("CAMP-DOMAIN-TEST")
                .ownerCompanyId(1L)
                .domain(esgDomain)
                .title("도메인 테스트 캠페인")
                .content("도메인 테스트 내용")
                .periodStartDate(LocalDate.of(2026, 1, 1))
                .periodEndDate(LocalDate.of(2026, 12, 31))
                .deadline(LocalDate.of(2026, 3, 31))
                .build());

        // when
        Campaign found = campaignRepository.findById(campaign.getCampaignId()).orElseThrow();

        // then
        assertThat(found.getDomain()).isNotNull();
        assertThat(found.getDomain().getDomainId()).isEqualTo(esgDomain.getDomainId());
        assertThat(found.getDomain().getCode()).isEqualTo("ESG");
        assertThat(found.getDomain().getName()).isEqualTo("ESG 실사");
    }
}
