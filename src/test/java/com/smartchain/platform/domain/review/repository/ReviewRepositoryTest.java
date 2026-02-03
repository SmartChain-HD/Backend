package com.smartchain.platform.domain.review.repository;

import com.smartchain.platform.domain.diagnostic.entity.Campaign;
import com.smartchain.platform.domain.diagnostic.entity.Diagnostic;
import com.smartchain.platform.domain.diagnostic.repository.CampaignRepository;
import com.smartchain.platform.domain.diagnostic.repository.DiagnosticRepository;
import com.smartchain.platform.domain.review.entity.Review;
import com.smartchain.platform.domain.user.entity.Company;
import com.smartchain.platform.domain.user.entity.Domain;
import com.smartchain.platform.domain.user.entity.Industry;
import com.smartchain.platform.domain.user.entity.Role;
import com.smartchain.platform.domain.user.entity.User;
import com.smartchain.platform.domain.user.repository.CompanyRepository;
import com.smartchain.platform.domain.user.repository.DomainRepository;
import com.smartchain.platform.domain.user.repository.IndustryRepository;
import com.smartchain.platform.domain.user.repository.RoleRepository;
import com.smartchain.platform.domain.user.repository.UserRepository;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private DomainRepository domainRepository;

    @Autowired
    private DiagnosticRepository diagnosticRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private IndustryRepository industryRepository;

    private Domain esgDomain;
    private Domain safetyDomain;
    private Company company;
    private User reviewer;
    private Diagnostic diagnostic;

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

        // Role 생성
        Role reviewerRole = roleRepository.save(new Role("심사자", "REVIEWER"));

        // User 생성
        reviewer = userRepository.save(User.builder()
                .name("심사자")
                .email("reviewer@test.com")
                .userPassword("password")
                .company(company)
                .role(reviewerRole)
                .build());

        // Campaign 생성
        Campaign campaign = campaignRepository.save(Campaign.builder()
                .campaignCode("CAMP-TEST-001")
                .ownerCompanyId(company.getCompanyId())
                .title("테스트 캠페인")
                .content("테스트용 캠페인")
                .periodStartDate(LocalDate.of(2026, 1, 1))
                .periodEndDate(LocalDate.of(2026, 12, 31))
                .deadline(LocalDate.of(2026, 3, 31))
                .build());

        // Diagnostic 생성
        diagnostic = diagnosticRepository.save(Diagnostic.builder()
                .diagnosticCode("DG-TEST-001")
                .title("테스트 진단")
                .campaign(campaign)
                .company(company)
                .drafterId(1L)
                .periodStartDate(LocalDate.of(2026, 1, 1))
                .periodEndDate(LocalDate.of(2026, 12, 31))
                .deadline(LocalDate.of(2026, 3, 31))
                .build());
    }

    @Test
    @DisplayName("Domain으로 Review 목록 조회 성공")
    void findByDomain_Success() {
        // given
        Review esgReview1 = reviewRepository.save(Review.builder()
                .diagnostic(diagnostic)
                .company(company)
                .assignedReviewer(reviewer)
                .domain(esgDomain)
                .score(80)
                .submittedAt(LocalDateTime.now())
                .build());

        Review esgReview2 = reviewRepository.save(Review.builder()
                .diagnostic(diagnostic)
                .company(company)
                .assignedReviewer(reviewer)
                .domain(esgDomain)
                .score(75)
                .submittedAt(LocalDateTime.now())
                .build());

        Review safetyReview = reviewRepository.save(Review.builder()
                .diagnostic(diagnostic)
                .company(company)
                .assignedReviewer(reviewer)
                .domain(safetyDomain)
                .score(85)
                .submittedAt(LocalDateTime.now())
                .build());

        // when
        List<Review> esgReviews = reviewRepository.findByDomain(esgDomain);
        List<Review> safetyReviews = reviewRepository.findByDomain(safetyDomain);

        // then
        assertThat(esgReviews).hasSize(2);
        assertThat(esgReviews).extracting(Review::getDomain)
                .allMatch(domain -> domain.getCode().equals("ESG"));

        assertThat(safetyReviews).hasSize(1);
        assertThat(safetyReviews.get(0).getScore()).isEqualTo(85);
    }

    @Test
    @DisplayName("담당 심사자와 Domain으로 Review 목록 조회 성공")
    void findByAssignedReviewerAndDomain_Success() {
        // given
        Review review1 = reviewRepository.save(Review.builder()
                .diagnostic(diagnostic)
                .company(company)
                .assignedReviewer(reviewer)
                .domain(esgDomain)
                .score(80)
                .submittedAt(LocalDateTime.now())
                .build());

        Review review2 = reviewRepository.save(Review.builder()
                .diagnostic(diagnostic)
                .company(company)
                .assignedReviewer(reviewer)
                .domain(esgDomain)
                .score(75)
                .submittedAt(LocalDateTime.now())
                .build());

        // when
        List<Review> reviews = reviewRepository.findByAssignedReviewerAndDomain(reviewer, esgDomain);

        // then
        assertThat(reviews).hasSize(2);
        assertThat(reviews).allMatch(r -> r.getAssignedReviewer().getUserId().equals(reviewer.getUserId()));
        assertThat(reviews).allMatch(r -> r.getDomain().getCode().equals("ESG"));
    }

    @Test
    @DisplayName("Domain으로 Review 페이징 조회 성공")
    void findByDomainOrderByCreatedAtDesc_Success() {
        // given
        for (int i = 1; i <= 5; i++) {
            reviewRepository.save(Review.builder()
                    .diagnostic(diagnostic)
                    .company(company)
                    .assignedReviewer(reviewer)
                    .domain(esgDomain)
                    .score(70 + i)
                    .submittedAt(LocalDateTime.now())
                    .build());
        }

        // when
        Page<Review> page = reviewRepository.findByDomainOrderByCreatedAtDesc(
                esgDomain, PageRequest.of(0, 3));

        // then
        assertThat(page.getContent()).hasSize(3);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("Review 엔티티에 Domain 필드가 정상적으로 저장되고 조회됨")
    void reviewDomainField_PersistenceTest() {
        // given
        Review review = reviewRepository.save(Review.builder()
                .diagnostic(diagnostic)
                .company(company)
                .assignedReviewer(reviewer)
                .domain(esgDomain)
                .score(85)
                .submittedAt(LocalDateTime.now())
                .build());

        // when
        Review found = reviewRepository.findById(review.getReviewId()).orElseThrow();

        // then
        assertThat(found.getDomain()).isNotNull();
        assertThat(found.getDomain().getDomainId()).isEqualTo(esgDomain.getDomainId());
        assertThat(found.getDomain().getCode()).isEqualTo("ESG");
        assertThat(found.getDomain().getName()).isEqualTo("ESG 실사");
    }
}
