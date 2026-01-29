package com.smartchain.platform.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartchain.platform.domain.diagnostic.entity.Campaign;
import com.smartchain.platform.domain.diagnostic.entity.Diagnostic;
import com.smartchain.platform.domain.diagnostic.repository.CampaignRepository;
import com.smartchain.platform.domain.diagnostic.repository.DiagnosticRepository;
import com.smartchain.platform.domain.review.entity.Review;
import com.smartchain.platform.domain.review.repository.ReviewRepository;
import com.smartchain.platform.domain.user.entity.*;
import com.smartchain.platform.domain.user.repository.*;
import com.smartchain.platform.global.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ReviewController 상세 통합 테스트
 * Issue #52: API 통합 테스트 확장
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("ReviewController 통합 테스트")
class ReviewIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private IndustryRepository industryRepository;

    @Autowired
    private DomainRepository domainRepository;

    @Autowired
    private UserDomainRoleRepository userDomainRoleRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private DiagnosticRepository diagnosticRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    private User reviewerUser;
    private User drafterUser;
    private String reviewerToken;
    private Domain esgDomain;
    private Campaign campaign;
    private Company partnerCompany;
    private Role reviewerRole;

    @BeforeEach
    void setUp() {
        // 역할 설정
        Role drafterRole = roleRepository.save(new Role("기안자", "DRAFTER"));
        reviewerRole = roleRepository.save(new Role("수신자", "REVIEWER"));

        // 도메인 설정
        esgDomain = domainRepository.save(Domain.builder()
                .code("ESG")
                .name("ESG 실사")
                .description("ESG 실사 도메인")
                .isActive(true)
                .build());

        // 협력사 설정
        Industry industry = industryRepository.save(new Industry("제조업", "MANUFACTURING"));
        partnerCompany = companyRepository.save(Company.builder()
                .industry(industry)
                .name("(주)협력사")
                .scale("중소기업")
                .businessNumber("345-67-89012")
                .ceoName("협력대표")
                .address("경기도 성남시")
                .contactEmail("partner@test.com")
                .contactPhone("031-1234-5678")
                .build());

        // 원청 회사 설정
        Company ownerCompany = companyRepository.save(Company.builder()
                .industry(industry)
                .name("(주)원청회사")
                .scale("대기업")
                .businessNumber("456-78-90123")
                .ceoName("원청대표")
                .address("서울시 종로구")
                .contactEmail("owner@test.com")
                .contactPhone("02-9876-5432")
                .build());

        // 기안자 사용자 설정 (협력사)
        drafterUser = User.builder()
                .name("기안자")
                .email("drafter-review@test.com")
                .userPassword("password123")
                .company(partnerCompany)
                .role(drafterRole)
                .build();
        UserDomainRole drafterDomainRole = UserDomainRole.builder()
                .user(drafterUser)
                .domain(esgDomain)
                .role(drafterRole)
                .build();
        drafterUser.addDomainRole(drafterDomainRole);
        drafterUser = userRepository.save(drafterUser);

        // 수신자 사용자 설정 (원청)
        reviewerUser = User.builder()
                .name("수신자")
                .email("reviewer@test.com")
                .userPassword("password123")
                .company(ownerCompany)
                .role(reviewerRole)
                .build();
        UserDomainRole reviewerDomainRole = UserDomainRole.builder()
                .user(reviewerUser)
                .domain(esgDomain)
                .role(reviewerRole)
                .build();
        reviewerUser.addDomainRole(reviewerDomainRole);
        reviewerUser = userRepository.save(reviewerUser);

        // 캠페인 설정
        campaign = campaignRepository.save(Campaign.builder()
                .campaignCode("CAMP-REV-001")
                .ownerCompanyId(ownerCompany.getCompanyId())
                .domain(esgDomain)
                .title("심사 테스트 캠페인")
                .content("심사 테스트용 캠페인")
                .periodStartDate(LocalDate.of(2026, 1, 1))
                .periodEndDate(LocalDate.of(2026, 3, 31))
                .deadline(LocalDate.of(2026, 2, 28))
                .build());

        // JWT 토큰 생성
        reviewerToken = jwtTokenProvider.createAccessToken(reviewerUser.getUserId(), reviewerUser.getEmail(), "REVIEWER");
    }

    private Diagnostic createReviewingDiagnostic(String code) {
        Diagnostic diagnostic = diagnosticRepository.save(Diagnostic.builder()
                .diagnosticCode(code)
                .title("심사 대상 기안")
                .campaign(campaign)
                .company(partnerCompany)
                .domain(esgDomain)
                .drafterId(drafterUser.getUserId())
                .periodStartDate(LocalDate.of(2026, 1, 1))
                .periodEndDate(LocalDate.of(2026, 3, 31))
                .deadline(LocalDate.of(2026, 2, 28))
                .build());
        diagnostic.startReview();
        return diagnosticRepository.save(diagnostic);
    }

    private Review createReview(Diagnostic diagnostic) {
        return reviewRepository.save(Review.builder()
                .diagnostic(diagnostic)
                .company(partnerCompany)
                .assignedReviewer(reviewerUser)
                .domain(esgDomain)
                .score(75)
                .submittedAt(LocalDateTime.now())
                .build());
    }

    @Nested
    @DisplayName("대시보드 조회")
    class GetDashboard {

        @Test
        @DisplayName("대시보드 조회 성공")
        void getDashboard_Success() throws Exception {
            mockMvc.perform(get("/api/v1/reviews/dashboard")
                            .header("Authorization", "Bearer " + reviewerToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").exists());
        }

        @Test
        @DisplayName("도메인 코드로 필터링된 대시보드 조회")
        void getDashboard_FilterByDomainCode() throws Exception {
            mockMvc.perform(get("/api/v1/reviews/dashboard")
                            .header("Authorization", "Bearer " + reviewerToken)
                            .param("domainCode", "ESG")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("심사 목록 조회")
    class GetReviewList {

        @Test
        @DisplayName("심사 목록 조회 성공")
        void getReviewList_Success() throws Exception {
            mockMvc.perform(get("/api/v1/reviews")
                            .header("Authorization", "Bearer " + reviewerToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").exists());
        }

        @Test
        @DisplayName("도메인 코드로 필터링")
        void getReviewList_FilterByDomainCode() throws Exception {
            mockMvc.perform(get("/api/v1/reviews")
                            .header("Authorization", "Bearer " + reviewerToken)
                            .param("domainCode", "ESG")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("페이징 적용")
        void getReviewList_WithPaging() throws Exception {
            mockMvc.perform(get("/api/v1/reviews")
                            .header("Authorization", "Bearer " + reviewerToken)
                            .param("page", "0")
                            .param("size", "10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("심사 상세 조회")
    class GetReviewDetail {

        @Test
        @DisplayName("존재하지 않는 심사 조회 시 404 응답")
        void getReviewDetail_NotFound() throws Exception {
            mockMvc.perform(get("/api/v1/reviews/{reviewId}", 99999L)
                            .header("Authorization", "Bearer " + reviewerToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("심사 처리")
    class ProcessReview {

        @Test
        @DisplayName("심사 승인 성공")
        void processReview_Approve_Success() throws Exception {
            // Given
            Diagnostic diagnostic = createReviewingDiagnostic("DIAG-REV-APPROVE");
            Review review = createReview(diagnostic);

            String decisionRequest = """
                {
                    "decision": "APPROVED",
                    "score": 85,
                    "comment": "심사 승인합니다",
                    "categoryCommentE": "환경 분야 양호",
                    "categoryCommentS": "사회 분야 양호",
                    "categoryCommentG": "지배구조 분야 양호"
                }
                """;

            mockMvc.perform(patch("/api/v1/reviews/{reviewId}", review.getReviewId())
                            .header("Authorization", "Bearer " + reviewerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(decisionRequest))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}
