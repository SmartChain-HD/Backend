package com.smartchain.platform.domain.diagnostic.repository;

import com.smartchain.platform.domain.diagnostic.entity.Campaign;
import com.smartchain.platform.domain.diagnostic.entity.Diagnostic;
import com.smartchain.platform.domain.user.entity.Company;
import com.smartchain.platform.domain.user.entity.Domain;
import com.smartchain.platform.domain.user.entity.Industry;
import com.smartchain.platform.domain.user.repository.CompanyRepository;
import com.smartchain.platform.domain.user.repository.DomainRepository;
import com.smartchain.platform.domain.user.repository.IndustryRepository;
import com.smartchain.platform.global.enums.CompanyStatus;
import com.smartchain.platform.global.enums.CompanyType;
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
class DiagnosticRepositoryTest {

    @Autowired
    private DiagnosticRepository diagnosticRepository;

    @Autowired
    private DomainRepository domainRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private IndustryRepository industryRepository;

    private Domain esgDomain;
    private Domain safetyDomain;
    private Campaign campaign;
    private Company company;

    @BeforeEach
    void setUp() {
        // Domain 생성
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

        // Industry 생성
        Industry industry = industryRepository.save(new Industry("제조업", "MANUFACTURING"));

        // Company 생성
        company = companyRepository.save(Company.builder()
                .industry(industry)
                .name("(주)테스트회사")
                .scale("중소기업")
                .sales(10000000000L)
                .asset(20000000000L)
                .businessNumber("123-45-67890")
                .companyType(CompanyType.TIER2)
                .ceoName("테스트")
                .address("서울시")
                .contactEmail("test@test.com")
                .contactPhone("02-1234-5678")
                .status(CompanyStatus.ACTIVE)
                .build());

        // Campaign 생성
        campaign = campaignRepository.save(Campaign.builder()
                .campaignCode("CAMP-TEST-001")
                .ownerCompanyId(company.getCompanyId())
                .title("테스트 캠페인")
                .content("테스트용 캠페인")
                .periodStartDate(LocalDate.of(2026, 1, 1))
                .periodEndDate(LocalDate.of(2026, 12, 31))
                .deadline(LocalDate.of(2026, 3, 31))
                .build());
    }

    @Test
    @DisplayName("Domain으로 Diagnostic 목록 조회 성공")
    void findByDomain_Success() {
        // given
        Diagnostic esgDiagnostic1 = diagnosticRepository.save(Diagnostic.builder()
                .diagnosticCode("DG-TEST-001")
                .title("ESG 진단 1")
                .campaign(campaign)
                .company(company)
                .domain(esgDomain)
                .drafterId(1L)
                .periodStartDate(LocalDate.of(2026, 1, 1))
                .periodEndDate(LocalDate.of(2026, 12, 31))
                .deadline(LocalDate.of(2026, 3, 31))
                .build());

        Diagnostic esgDiagnostic2 = diagnosticRepository.save(Diagnostic.builder()
                .diagnosticCode("DG-TEST-002")
                .title("ESG 진단 2")
                .campaign(campaign)
                .company(company)
                .domain(esgDomain)
                .drafterId(1L)
                .periodStartDate(LocalDate.of(2026, 1, 1))
                .periodEndDate(LocalDate.of(2026, 12, 31))
                .deadline(LocalDate.of(2026, 3, 31))
                .build());

        Diagnostic safetyDiagnostic = diagnosticRepository.save(Diagnostic.builder()
                .diagnosticCode("DG-TEST-003")
                .title("안전보건 진단")
                .campaign(campaign)
                .company(company)
                .domain(safetyDomain)
                .drafterId(1L)
                .periodStartDate(LocalDate.of(2026, 1, 1))
                .periodEndDate(LocalDate.of(2026, 12, 31))
                .deadline(LocalDate.of(2026, 3, 31))
                .build());

        // when
        List<Diagnostic> esgDiagnostics = diagnosticRepository.findByDomain(esgDomain);
        List<Diagnostic> safetyDiagnostics = diagnosticRepository.findByDomain(safetyDomain);

        // then
        assertThat(esgDiagnostics).hasSize(2);
        assertThat(esgDiagnostics).extracting(Diagnostic::getDomain)
                .allMatch(domain -> domain.getCode().equals("ESG"));

        assertThat(safetyDiagnostics).hasSize(1);
        assertThat(safetyDiagnostics.get(0).getTitle()).isEqualTo("안전보건 진단");
    }

    @Test
    @DisplayName("Domain으로 Diagnostic 페이징 조회 성공")
    void findByDomainOrderByCreatedAtDesc_Success() {
        // given
        for (int i = 1; i <= 5; i++) {
            diagnosticRepository.save(Diagnostic.builder()
                    .diagnosticCode("DG-TEST-00" + i)
                    .title("ESG 진단 " + i)
                    .campaign(campaign)
                    .company(company)
                    .domain(esgDomain)
                    .drafterId(1L)
                    .periodStartDate(LocalDate.of(2026, 1, 1))
                    .periodEndDate(LocalDate.of(2026, 12, 31))
                    .deadline(LocalDate.of(2026, 3, 31))
                    .build());
        }

        // when
        Page<Diagnostic> page = diagnosticRepository.findByDomainOrderByCreatedAtDesc(
                esgDomain, PageRequest.of(0, 3));

        // then
        assertThat(page.getContent()).hasSize(3);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("Diagnostic 엔티티에 Domain 필드가 정상적으로 저장되고 조회됨")
    void diagnosticDomainField_PersistenceTest() {
        // given
        Diagnostic diagnostic = diagnosticRepository.save(Diagnostic.builder()
                .diagnosticCode("DG-TEST-DOMAIN")
                .title("도메인 테스트 진단")
                .campaign(campaign)
                .company(company)
                .domain(esgDomain)
                .drafterId(1L)
                .periodStartDate(LocalDate.of(2026, 1, 1))
                .periodEndDate(LocalDate.of(2026, 12, 31))
                .deadline(LocalDate.of(2026, 3, 31))
                .build());

        // when
        Diagnostic found = diagnosticRepository.findById(diagnostic.getDiagnosticId()).orElseThrow();

        // then
        assertThat(found.getDomain()).isNotNull();
        assertThat(found.getDomain().getDomainId()).isEqualTo(esgDomain.getDomainId());
        assertThat(found.getDomain().getCode()).isEqualTo("ESG");
        assertThat(found.getDomain().getName()).isEqualTo("ESG 실사");
    }
}
