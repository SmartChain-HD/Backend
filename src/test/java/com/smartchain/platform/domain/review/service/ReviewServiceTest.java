package com.smartchain.platform.domain.review.service;

import com.smartchain.platform.domain.ai.entity.AiAnalysisResult;
import com.smartchain.platform.domain.ai.repository.AiAnalysisResultRepository;
import com.smartchain.platform.domain.diagnostic.entity.Diagnostic;
import com.smartchain.platform.domain.review.entity.Review;
import com.smartchain.platform.domain.review.repository.ReviewRepository;
import com.smartchain.platform.domain.user.entity.Company;
import com.smartchain.platform.domain.user.entity.Domain;
import com.smartchain.platform.domain.user.entity.Industry;
import com.smartchain.platform.domain.user.entity.Role;
import com.smartchain.platform.domain.user.entity.User;
import com.smartchain.platform.domain.user.repository.CompanyRepository;
import com.smartchain.platform.domain.user.repository.DomainRepository;
import com.smartchain.platform.domain.user.repository.UserRepository;
import com.smartchain.platform.dto.ai.AiAnalysisResultDetailResponse;
import com.smartchain.platform.dto.review.dashboard.ReviewDashboardResponse;
import com.smartchain.platform.dto.review.decision.ReviewDecisionRequest;
import com.smartchain.platform.dto.review.decision.ReviewDecisionResponse;
import com.smartchain.platform.dto.review.detail.ReviewDetailResponse;
import com.smartchain.platform.dto.review.list.ReviewListResponse;
import com.smartchain.platform.global.enums.ReviewStatus;
import com.smartchain.platform.global.enums.RiskLevel;
import com.smartchain.platform.global.error.CustomException;
import com.smartchain.platform.global.error.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private DomainRepository domainRepository;

    @Mock
    private AiAnalysisResultRepository aiAnalysisResultRepository;

    @Mock
    private User reviewerUser;

    @Mock
    private User guestUser;

    @Mock
    private User reviewerWithoutDomain;

    @Mock
    private Company testCompany;

    @Mock
    private Industry testIndustry;

    @Mock
    private Role reviewerRole;

    @Mock
    private Role guestRole;

    @Mock
    private Diagnostic testDiagnostic;

    @Mock
    private Review testReview;

    @Mock
    private Domain envDomain;

    @Mock
    private Domain socDomain;

    @Mock
    private Domain esgDomain;

    @Mock
    private Domain safetyDomain;

    @BeforeEach
    void setUp() {
        lenient().when(reviewerRole.getCode()).thenReturn("REVIEWER");
        lenient().when(guestRole.getCode()).thenReturn("GUEST");

        lenient().when(envDomain.getCode()).thenReturn("ENV");
        lenient().when(envDomain.getName()).thenReturn("환경");
        lenient().when(envDomain.getDomainId()).thenReturn(1L);

        lenient().when(socDomain.getCode()).thenReturn("SOC");
        lenient().when(socDomain.getName()).thenReturn("사회");
        lenient().when(socDomain.getDomainId()).thenReturn(2L);

        lenient().when(esgDomain.getCode()).thenReturn("ESG");
        lenient().when(esgDomain.getName()).thenReturn("ESG 실사");
        lenient().when(esgDomain.getDomainId()).thenReturn(3L);

        lenient().when(safetyDomain.getCode()).thenReturn("SAFETY");
        lenient().when(safetyDomain.getName()).thenReturn("안전보건");
        lenient().when(safetyDomain.getDomainId()).thenReturn(4L);

        lenient().when(reviewerUser.getUserId()).thenReturn(1L);
        lenient().when(reviewerUser.getName()).thenReturn("심사자");
        lenient().when(reviewerUser.getEmail()).thenReturn("reviewer@test.com");
        lenient().when(reviewerUser.getRole()).thenReturn(reviewerRole);
        lenient().when(reviewerUser.getDomainsWithRole("REVIEWER")).thenReturn(List.of(envDomain));
        lenient().when(reviewerUser.hasRoleInDomain("ENV", "REVIEWER")).thenReturn(true);
        lenient().when(reviewerUser.hasRoleInDomain("SOC", "REVIEWER")).thenReturn(false);
        lenient().when(reviewerUser.hasRoleInDomain("ESG", "REVIEWER")).thenReturn(true);
        lenient().when(reviewerUser.hasRoleInDomain("SAFETY", "REVIEWER")).thenReturn(true);

        lenient().when(guestUser.getUserId()).thenReturn(2L);
        lenient().when(guestUser.getName()).thenReturn("게스트");
        lenient().when(guestUser.getRole()).thenReturn(guestRole);

        lenient().when(reviewerWithoutDomain.getUserId()).thenReturn(3L);
        lenient().when(reviewerWithoutDomain.getName()).thenReturn("도메인없는심사자");
        lenient().when(reviewerWithoutDomain.getRole()).thenReturn(reviewerRole);
        lenient().when(reviewerWithoutDomain.getDomainsWithRole("REVIEWER")).thenReturn(List.of());

        lenient().when(testIndustry.getName()).thenReturn("제조업");

        lenient().when(testCompany.getCompanyId()).thenReturn(10L);
        lenient().when(testCompany.getName()).thenReturn("(주)테스트회사");
        lenient().when(testCompany.getScale()).thenReturn("TIER1");
        lenient().when(testCompany.getIndustry()).thenReturn(testIndustry);

        lenient().when(testDiagnostic.getDiagnosticId()).thenReturn(100L);
        lenient().when(testDiagnostic.getDiagnosticCode()).thenReturn("DG-2026-00001");
        lenient().when(testDiagnostic.getTitle()).thenReturn("2026년 상반기 ESG 자가진단");
        lenient().when(testDiagnostic.getCompany()).thenReturn(testCompany);

        lenient().when(testReview.getReviewId()).thenReturn(1L);
        lenient().when(testReview.getDiagnostic()).thenReturn(testDiagnostic);
        lenient().when(testReview.getCompany()).thenReturn(testCompany);
        lenient().when(testReview.getStatus()).thenReturn(ReviewStatus.REVIEWING);
        lenient().when(testReview.getScore()).thenReturn(72);
        lenient().when(testReview.getRiskLevel()).thenReturn(RiskLevel.MEDIUM);
        lenient().when(testReview.getSubmittedAt()).thenReturn(LocalDateTime.now());
        lenient().when(testReview.getDomain()).thenReturn(envDomain);
    }

    @Nested
    @DisplayName("대시보드 조회 테스트")
    class GetDashboardTest {

        @Test
        @DisplayName("REVIEWER가 도메인 필터링된 대시보드 조회 성공")
        void getDashboard_AsReviewer_WithDomainFilter_Success() {
            // given
            List<Domain> domains = List.of(envDomain);
            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(reviewRepository.countDistinctCompaniesByDomainIn(domains)).willReturn(30L);
            given(reviewRepository.countByDomainInAndStatus(domains, ReviewStatus.REVIEWING)).willReturn(5L);
            given(reviewRepository.countByDomainInAndStatus(domains, ReviewStatus.APPROVED)).willReturn(20L);
            given(reviewRepository.countByDomainInAndStatus(domains, ReviewStatus.REVISION_REQUIRED)).willReturn(3L);
            given(reviewRepository.countByDomainInAndRiskLevel(domains, RiskLevel.HIGH)).willReturn(3L);
            given(reviewRepository.countByDomainInAndRiskLevel(domains, RiskLevel.MEDIUM)).willReturn(15L);
            given(reviewRepository.countByDomainInAndRiskLevel(domains, RiskLevel.LOW)).willReturn(10L);
            given(reviewRepository.findByDomainInOrderBySubmittedAtDesc(eq(domains), any())).willReturn(List.of(testReview));

            // when
            ReviewDashboardResponse response = reviewService.getDashboard(1L, null, null, null, null);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getOverview().getTotalCompanies()).isEqualTo(30);
            assertThat(response.getOverview().getInReviewCount()).isEqualTo(5);
            assertThat(response.getOverview().getCompletedCount()).isEqualTo(20);
            assertThat(response.getRiskDistribution().getHigh()).isEqualTo(3);
            assertThat(response.getRecentActivities()).hasSize(1);
        }

        @Test
        @DisplayName("REVIEWER가 아닌 사용자가 대시보드 조회 시 실패")
        void getDashboard_AsGuest_ThrowsException() {
            // given
            given(userRepository.findById(2L)).willReturn(Optional.of(guestUser));

            // when & then
            assertThatThrownBy(() -> reviewService.getDashboard(2L, null, null, null, null))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.PERMISSION_DENIED_ACTION);
                    });
        }

        @Test
        @DisplayName("REVIEWER 역할이지만 도메인 권한이 없으면 실패")
        void getDashboard_ReviewerWithoutDomain_ThrowsException() {
            // given
            given(userRepository.findById(3L)).willReturn(Optional.of(reviewerWithoutDomain));

            // when & then
            assertThatThrownBy(() -> reviewService.getDashboard(3L, null, null, null, null))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.PERMISSION_DENIED_ACTION);
                    });
        }

        @Test
        @DisplayName("domainCode 파라미터로 단일 도메인 대시보드 조회 성공")
        void getDashboard_WithDomainCodeParam_Success() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(domainRepository.findByCode("ENV")).willReturn(Optional.of(envDomain));
            given(reviewRepository.countDistinctCompaniesByDomain(envDomain)).willReturn(10L);
            given(reviewRepository.countByDomainAndStatus(envDomain, ReviewStatus.REVIEWING)).willReturn(3L);
            given(reviewRepository.countByDomainAndStatus(envDomain, ReviewStatus.APPROVED)).willReturn(5L);
            given(reviewRepository.countByDomainAndStatus(envDomain, ReviewStatus.REVISION_REQUIRED)).willReturn(2L);
            given(reviewRepository.countByDomainAndRiskLevel(envDomain, RiskLevel.HIGH)).willReturn(1L);
            given(reviewRepository.countByDomainAndRiskLevel(envDomain, RiskLevel.MEDIUM)).willReturn(4L);
            given(reviewRepository.countByDomainAndRiskLevel(envDomain, RiskLevel.LOW)).willReturn(5L);
            given(reviewRepository.findByDomainOrderBySubmittedAtDesc(eq(envDomain), any())).willReturn(List.of(testReview));

            // when
            ReviewDashboardResponse response = reviewService.getDashboard(1L, "ENV", null, null, null);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getOverview().getTotalCompanies()).isEqualTo(10);
            assertThat(response.getOverview().getInReviewCount()).isEqualTo(3);
            verify(reviewRepository).countDistinctCompaniesByDomain(envDomain);
            verify(reviewRepository, never()).countDistinctCompaniesByDomainIn(any());
        }
    }

    @Nested
    @DisplayName("심사 목록 조회 테스트")
    class GetReviewListTest {

        @Test
        @DisplayName("REVIEWER가 도메인 필터링된 심사 목록 조회 성공")
        void getReviewList_AsReviewer_WithDomainFilter_Success() {
            // given
            List<Domain> domains = List.of(envDomain);
            Page<Review> reviewPage = new PageImpl<>(List.of(testReview), PageRequest.of(0, 20), 1);

            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(reviewRepository.findByDomainInOrderByCreatedAtDesc(eq(domains), any())).willReturn(reviewPage);
            given(reviewRepository.countDistinctCompaniesByDomainIn(domains)).willReturn(30L);
            given(reviewRepository.countByDomainInAndStatus(domains, ReviewStatus.APPROVED)).willReturn(20L);
            given(reviewRepository.countByDomainInAndStatus(domains, ReviewStatus.REVIEWING)).willReturn(5L);
            given(reviewRepository.countByDomainInAndRiskLevel(domains, RiskLevel.HIGH)).willReturn(3L);
            given(reviewRepository.countByDomainInAndRiskLevel(domains, RiskLevel.MEDIUM)).willReturn(15L);
            given(reviewRepository.countByDomainInAndRiskLevel(domains, RiskLevel.LOW)).willReturn(10L);

            // when
            ReviewListResponse response = reviewService.getReviewList(1L, null, null, null, null, 0, 20);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).getReviewId()).isEqualTo(1L);
            verify(reviewRepository).findByDomainInOrderByCreatedAtDesc(eq(domains), any());
        }

        @Test
        @DisplayName("상태 필터 + 도메인 필터 조합 조회 성공")
        void getReviewList_WithStatusAndDomainFilter_Success() {
            // given
            List<Domain> domains = List.of(envDomain);
            Page<Review> reviewPage = new PageImpl<>(List.of(testReview), PageRequest.of(0, 20), 1);

            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(reviewRepository.findByDomainInAndStatusOrderByCreatedAtDesc(eq(domains), eq(ReviewStatus.REVIEWING), any())).willReturn(reviewPage);
            given(reviewRepository.countDistinctCompaniesByDomainIn(domains)).willReturn(30L);
            given(reviewRepository.countByDomainInAndStatus(eq(domains), any())).willReturn(10L);
            given(reviewRepository.countByDomainInAndRiskLevel(eq(domains), any())).willReturn(10L);

            // when
            ReviewListResponse response = reviewService.getReviewList(1L, null, "REVIEWING", null, null, 0, 20);

            // then
            assertThat(response).isNotNull();
            verify(reviewRepository).findByDomainInAndStatusOrderByCreatedAtDesc(eq(domains), eq(ReviewStatus.REVIEWING), any());
        }

        @Test
        @DisplayName("domainCode 쿼리 파라미터로 단일 도메인 필터링 성공")
        void getReviewList_WithDomainCodeParam_Success() {
            // given
            Page<Review> reviewPage = new PageImpl<>(List.of(testReview), PageRequest.of(0, 20), 1);

            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(domainRepository.findByCode("ENV")).willReturn(Optional.of(envDomain));
            given(reviewRepository.findByDomainOrderByCreatedAtDesc(eq(envDomain), any())).willReturn(reviewPage);
            given(reviewRepository.countDistinctCompaniesByDomain(envDomain)).willReturn(10L);
            given(reviewRepository.countByDomainAndStatus(envDomain, ReviewStatus.APPROVED)).willReturn(5L);
            given(reviewRepository.countByDomainAndStatus(envDomain, ReviewStatus.REVIEWING)).willReturn(3L);
            given(reviewRepository.countByDomainAndRiskLevel(envDomain, RiskLevel.HIGH)).willReturn(1L);
            given(reviewRepository.countByDomainAndRiskLevel(envDomain, RiskLevel.MEDIUM)).willReturn(4L);
            given(reviewRepository.countByDomainAndRiskLevel(envDomain, RiskLevel.LOW)).willReturn(5L);

            // when
            ReviewListResponse response = reviewService.getReviewList(1L, "ENV", null, null, null, 0, 20);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getSummary().getTotalCompanies()).isEqualTo(10);
            verify(reviewRepository).findByDomainOrderByCreatedAtDesc(eq(envDomain), any());
            verify(reviewRepository, never()).findByDomainInOrderByCreatedAtDesc(any(), any());
        }

        @Test
        @DisplayName("권한 없는 domainCode 파라미터로 조회 시 403 에러")
        void getReviewList_WithUnauthorizedDomainCode_ThrowsException() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(domainRepository.findByCode("SOC")).willReturn(Optional.of(socDomain));

            // when & then
            assertThatThrownBy(() -> reviewService.getReviewList(1L, "SOC", null, null, null, 0, 20))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.PERMISSION_DENIED_ACTION);
                    });
        }
    }

    @Nested
    @DisplayName("심사 상세 조회 - 도메인 권한 검증 테스트")
    class GetReviewDetailDomainTest {

        @Test
        @DisplayName("해당 도메인 REVIEWER 권한이 있으면 상세 조회 성공")
        void getReviewDetail_WithDomainPermission_Success() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(reviewRepository.findById(1L)).willReturn(Optional.of(testReview));

            // when
            ReviewDetailResponse response = reviewService.getReviewDetail(1L, 1L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getReviewId()).isEqualTo(1L);
            assertThat(response.getDiagnostic().getDiagnosticId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("다른 도메인의 심사 상세 조회 시 403 에러")
        void getReviewDetail_WithoutDomainPermission_ThrowsException() {
            // given
            Review socReview = mock(Review.class);
            when(socReview.getDomain()).thenReturn(socDomain);

            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(reviewRepository.findById(2L)).willReturn(Optional.of(socReview));

            // when & then
            assertThatThrownBy(() -> reviewService.getReviewDetail(1L, 2L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.PERMISSION_DENIED_ACTION);
                    });
        }

        @Test
        @DisplayName("도메인이 null인 심사 상세 조회 시 403 에러")
        void getReviewDetail_NullDomain_ThrowsException() {
            // given
            Review noDomainReview = mock(Review.class);
            when(noDomainReview.getDomain()).thenReturn(null);

            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(reviewRepository.findById(3L)).willReturn(Optional.of(noDomainReview));

            // when & then
            assertThatThrownBy(() -> reviewService.getReviewDetail(1L, 3L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.PERMISSION_DENIED_ACTION);
                    });
        }

        @Test
        @DisplayName("존재하지 않는 심사 조회 시 실패")
        void getReviewDetail_NotFound_ThrowsException() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(reviewRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reviewService.getReviewDetail(1L, 999L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.REVIEW_NOT_FOUND);
                    });
        }

        @Test
        @DisplayName("AI 분석 결과가 있는 경우 aiAnalysis 필드에 포함")
        void getReviewDetail_WithAiAnalysisResult_IncludesSlotResults() {
            // given
            String resultJson = """
                {
                    "slotResults": [
                        {"slotName": "compliance.contract.sample", "verdict": "PASS", "reasons": ["검토 완료"]}
                    ],
                    "clarifications": [
                        {"slotName": "compliance.fair.trade", "message": "추가 자료 필요"}
                    ]
                }
                """;

            AiAnalysisResult aiResult = AiAnalysisResult.builder()
                    .diagnostic(testDiagnostic)
                    .domainCode("COMPLIANCE")
                    .packageId("PKG_001")
                    .riskLevel("MEDIUM")
                    .verdict("WARN")
                    .whySummary("일부 항목 보완 필요")
                    .resultJson(resultJson)
                    .analyzedAt(LocalDateTime.now())
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(reviewRepository.findById(1L)).willReturn(Optional.of(testReview));
            given(aiAnalysisResultRepository.findTopByDiagnostic_DiagnosticIdOrderByAnalyzedAtDesc(100L))
                    .willReturn(Optional.of(aiResult));

            // when
            ReviewDetailResponse response = reviewService.getReviewDetail(1L, 1L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getDiagnostic().getAiAnalysis()).isNotNull();

            // AI 분석 결과가 AiAnalysisResultDetailResponse 타입으로 반환됨
            AiAnalysisResultDetailResponse aiAnalysis =
                    (AiAnalysisResultDetailResponse) response.getDiagnostic().getAiAnalysis();
            assertThat(aiAnalysis.verdict()).isEqualTo("WARN");
            assertThat(aiAnalysis.riskLevel()).isEqualTo("MEDIUM");
            assertThat(aiAnalysis.slotResults()).hasSize(1);
            assertThat(aiAnalysis.slotResults().get(0).slotName()).isEqualTo("compliance.contract.sample");
            assertThat(aiAnalysis.clarifications()).hasSize(1);
            assertThat(aiAnalysis.clarifications().get(0).message()).isEqualTo("추가 자료 필요");
        }

        @Test
        @DisplayName("AI 분석 결과가 없는 경우 aiAnalysis 필드는 null")
        void getReviewDetail_WithoutAiAnalysisResult_NullAiAnalysis() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(reviewRepository.findById(1L)).willReturn(Optional.of(testReview));
            given(aiAnalysisResultRepository.findTopByDiagnostic_DiagnosticIdOrderByAnalyzedAtDesc(100L))
                    .willReturn(Optional.empty());

            // when
            ReviewDetailResponse response = reviewService.getReviewDetail(1L, 1L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getDiagnostic().getAiAnalysis()).isNull();
        }
    }

    @Nested
    @DisplayName("심사 결과 입력 - 도메인 권한 검증 테스트")
    class ProcessReviewDomainTest {

        @Test
        @DisplayName("해당 도메인 REVIEWER 권한으로 심사 승인 성공")
        void processReview_WithDomainPermission_Approve_Success() {
            // given
            when(testReview.isReviewing()).thenReturn(true);
            when(testReview.getStatus()).thenReturn(ReviewStatus.APPROVED);
            when(testReview.getProcessedAt()).thenReturn(LocalDateTime.now());

            ReviewDecisionRequest request = ReviewDecisionRequest.builder()
                    .decision("APPROVED")
                    .comment("ESG 관리 체계가 양호합니다")
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(reviewRepository.findById(1L)).willReturn(Optional.of(testReview));

            // when
            ReviewDecisionResponse response = reviewService.processReview(1L, 1L, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo("APPROVED");
            assertThat(response.getMessage()).isEqualTo("심사가 승인되었습니다");
            verify(testReview).approve(eq(reviewerUser), eq("ESG 관리 체계가 양호합니다"), any(), any(), any());
            verify(testDiagnostic).complete();
        }

        @Test
        @DisplayName("다른 도메인 심사 결과 입력 시 403 에러")
        void processReview_WithoutDomainPermission_ThrowsException() {
            // given
            Review socReview = mock(Review.class);
            when(socReview.getDomain()).thenReturn(socDomain);

            ReviewDecisionRequest request = ReviewDecisionRequest.builder()
                    .decision("APPROVED")
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(reviewRepository.findById(2L)).willReturn(Optional.of(socReview));

            // when & then
            assertThatThrownBy(() -> reviewService.processReview(1L, 2L, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.PERMISSION_DENIED_ACTION);
                    });
        }

        @Test
        @DisplayName("보완 요청 성공")
        void processReview_RevisionRequired_Success() {
            // given
            when(testReview.isReviewing()).thenReturn(true);
            when(testReview.getStatus()).thenReturn(ReviewStatus.REVISION_REQUIRED);
            when(testReview.getProcessedAt()).thenReturn(LocalDateTime.now());

            ReviewDecisionRequest request = ReviewDecisionRequest.builder()
                    .decision("REVISION_REQUIRED")
                    .comment("추가 자료가 필요합니다")
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(reviewRepository.findById(1L)).willReturn(Optional.of(testReview));

            // when
            ReviewDecisionResponse response = reviewService.processReview(1L, 1L, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo("REVISION_REQUIRED");
            assertThat(response.getMessage()).isEqualTo("보완 요청이 완료되었습니다");
            verify(testReview).requestRevision(reviewerUser, "추가 자료가 필요합니다");
            verify(testDiagnostic).returnForRevision();
        }

        @Test
        @DisplayName("이미 승인된 심사 재처리 시 실패")
        void processReview_AlreadyApproved_ThrowsException() {
            // given - 이미 APPROVED 상태인 심사
            when(testReview.isReviewing()).thenReturn(false);
            when(testReview.isRevisionRequired()).thenReturn(false);

            ReviewDecisionRequest request = ReviewDecisionRequest.builder()
                    .decision("APPROVED")
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(reviewRepository.findById(1L)).willReturn(Optional.of(testReview));

            // when & then
            assertThatThrownBy(() -> reviewService.processReview(1L, 1L, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.ALREADY_PROCESSED_REVIEW);
                    });
        }

        @Test
        @DisplayName("REVISION_REQUIRED 상태에서 재승인 성공 (#155)")
        void processReview_RevisionRequired_ReApprove_Success() {
            // given - REVISION_REQUIRED 상태의 심사
            when(testReview.isReviewing()).thenReturn(false);
            when(testReview.isRevisionRequired()).thenReturn(true);
            when(testReview.getStatus()).thenReturn(ReviewStatus.APPROVED);
            when(testReview.getProcessedAt()).thenReturn(LocalDateTime.now());

            ReviewDecisionRequest request = ReviewDecisionRequest.builder()
                    .decision("APPROVED")
                    .comment("재검토 결과 승인합니다")
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(reviewRepository.findById(1L)).willReturn(Optional.of(testReview));

            // when
            ReviewDecisionResponse response = reviewService.processReview(1L, 1L, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo("APPROVED");
            assertThat(response.getMessage()).isEqualTo("심사가 승인되었습니다");

            // 기안 상태가 REVIEWING으로 되돌려진 후 완료 처리됨
            verify(testDiagnostic).markAsReviewing();
            verify(testReview).approve(eq(reviewerUser), eq("재검토 결과 승인합니다"), any(), any(), any());
            verify(testDiagnostic).complete();
        }

        @Test
        @DisplayName("REVISION_REQUIRED 상태에서 다시 보완요청 시 실패 (#155)")
        void processReview_RevisionRequired_DoubleRevision_ThrowsException() {
            // given - REVISION_REQUIRED 상태의 심사에서 또 보완요청 시도
            when(testReview.isReviewing()).thenReturn(false);
            when(testReview.isRevisionRequired()).thenReturn(true);

            ReviewDecisionRequest request = ReviewDecisionRequest.builder()
                    .decision("REVISION_REQUIRED")
                    .comment("추가 보완 필요")
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(reviewRepository.findById(1L)).willReturn(Optional.of(testReview));

            // when & then
            assertThatThrownBy(() -> reviewService.processReview(1L, 1L, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.ALREADY_PROCESSED_REVIEW);
                    });
        }

        @Test
        @DisplayName("유효하지 않은 결정으로 처리 시 실패")
        void processReview_InvalidDecision_ThrowsException() {
            // given
            when(testReview.isReviewing()).thenReturn(true);

            ReviewDecisionRequest request = ReviewDecisionRequest.builder()
                    .decision("INVALID")
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(reviewRepository.findById(1L)).willReturn(Optional.of(testReview));

            // when & then
            assertThatThrownBy(() -> reviewService.processReview(1L, 1L, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.INVALID_DECISION);
                    });
        }
    }

    @Nested
    @DisplayName("도메인별 카테고리 코멘트 검증 테스트")
    class DomainCategoryValidationTest {

        @Test
        @DisplayName("SAFETY 도메인에서 ESG 카테고리 코멘트 전송 시 RV004 에러")
        void processReview_SafetyDomain_WithEsgCategoryComments_ThrowsException() {
            // given
            Review safetyReview = mock(Review.class);
            lenient().when(safetyReview.getDomain()).thenReturn(safetyDomain);
            lenient().when(safetyReview.isReviewing()).thenReturn(true);
            lenient().when(safetyReview.getReviewId()).thenReturn(10L);

            ReviewDecisionRequest request = ReviewDecisionRequest.builder()
                    .decision("APPROVED")
                    .comment("안전보건 검토 완료")
                    .categoryComments(Map.of("E", "환경 의견", "S", "사회 의견"))
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(reviewRepository.findById(10L)).willReturn(Optional.of(safetyReview));

            // when & then
            assertThatThrownBy(() -> reviewService.processReview(1L, 10L, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.INVALID_CATEGORY_FOR_DOMAIN);
                    });
        }

        @Test
        @DisplayName("ESG 도메인에서 유효한 카테고리 코멘트로 승인 성공")
        void processReview_EsgDomain_WithValidCategoryComments_Success() {
            // given
            Review esgReview = mock(Review.class);
            Diagnostic esgDiagnostic = mock(Diagnostic.class);
            lenient().when(esgReview.getDomain()).thenReturn(esgDomain);
            lenient().when(esgReview.isReviewing()).thenReturn(true);
            lenient().when(esgReview.getReviewId()).thenReturn(20L);
            lenient().when(esgReview.getDiagnostic()).thenReturn(esgDiagnostic);
            lenient().when(esgReview.getStatus()).thenReturn(ReviewStatus.APPROVED);
            lenient().when(esgReview.getProcessedAt()).thenReturn(LocalDateTime.now());

            ReviewDecisionRequest request = ReviewDecisionRequest.builder()
                    .decision("APPROVED")
                    .comment("ESG 전체 양호")
                    .categoryComments(Map.of("E", "환경 우수", "S", "사회 양호", "G", "지배구조 개선 필요"))
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(reviewRepository.findById(20L)).willReturn(Optional.of(esgReview));

            // when
            ReviewDecisionResponse response = reviewService.processReview(1L, 20L, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo("APPROVED");
            verify(esgReview).approve(eq(reviewerUser), eq("ESG 전체 양호"),
                    eq("환경 우수"), eq("사회 양호"), eq("지배구조 개선 필요"));
            verify(esgDiagnostic).complete();
        }

        @Test
        @DisplayName("ESG 도메인에서 허용되지 않는 카테고리 키 전송 시 RV004 에러")
        void processReview_EsgDomain_WithInvalidCategoryKey_ThrowsException() {
            // given
            Review esgReview = mock(Review.class);
            lenient().when(esgReview.getDomain()).thenReturn(esgDomain);
            lenient().when(esgReview.isReviewing()).thenReturn(true);
            lenient().when(esgReview.getReviewId()).thenReturn(21L);

            ReviewDecisionRequest request = ReviewDecisionRequest.builder()
                    .decision("APPROVED")
                    .categoryComments(Map.of("E", "환경", "INVALID_KEY", "잘못된 키"))
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(reviewRepository.findById(21L)).willReturn(Optional.of(esgReview));

            // when & then
            assertThatThrownBy(() -> reviewService.processReview(1L, 21L, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.INVALID_CATEGORY_FOR_DOMAIN);
                    });
        }

        @Test
        @DisplayName("SAFETY 도메인에서 카테고리 코멘트 없이 승인 성공")
        void processReview_SafetyDomain_WithoutCategoryComments_Success() {
            // given
            Review safetyReview = mock(Review.class);
            Diagnostic safetyDiagnostic = mock(Diagnostic.class);
            lenient().when(safetyReview.getDomain()).thenReturn(safetyDomain);
            lenient().when(safetyReview.isReviewing()).thenReturn(true);
            lenient().when(safetyReview.getReviewId()).thenReturn(30L);
            lenient().when(safetyReview.getDiagnostic()).thenReturn(safetyDiagnostic);
            lenient().when(safetyReview.getStatus()).thenReturn(ReviewStatus.APPROVED);
            lenient().when(safetyReview.getProcessedAt()).thenReturn(LocalDateTime.now());

            ReviewDecisionRequest request = ReviewDecisionRequest.builder()
                    .decision("APPROVED")
                    .comment("안전보건 검토 완료")
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(reviewRepository.findById(30L)).willReturn(Optional.of(safetyReview));

            // when
            ReviewDecisionResponse response = reviewService.processReview(1L, 30L, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo("APPROVED");
            verify(safetyReview).approve(eq(reviewerUser), eq("안전보건 검토 완료"),
                    isNull(), isNull(), isNull());
        }

        @Test
        @DisplayName("ESG 도메인 상세 조회 시 allowedCategoryKeys로 [E, S, G] 반환")
        void getReviewDetail_EsgDomain_ReturnsAllowedCategoryKeys() {
            // given
            Review esgReview = mock(Review.class);
            Diagnostic esgDiagnostic = mock(Diagnostic.class);
            Company esgCompany = mock(Company.class);
            lenient().when(esgReview.getDomain()).thenReturn(esgDomain);
            lenient().when(esgReview.getReviewId()).thenReturn(40L);
            lenient().when(esgReview.getDiagnostic()).thenReturn(esgDiagnostic);
            lenient().when(esgReview.getCompany()).thenReturn(esgCompany);
            lenient().when(esgReview.getStatus()).thenReturn(ReviewStatus.REVIEWING);
            lenient().when(esgReview.getScore()).thenReturn(80);
            lenient().when(esgReview.getRiskLevel()).thenReturn(RiskLevel.LOW);
            lenient().when(esgReview.getSubmittedAt()).thenReturn(LocalDateTime.now());
            lenient().when(esgDiagnostic.getDiagnosticId()).thenReturn(200L);
            lenient().when(esgDiagnostic.getDiagnosticCode()).thenReturn("DG-2026-00002");
            lenient().when(esgDiagnostic.getTitle()).thenReturn("ESG 진단");
            lenient().when(esgCompany.getCompanyId()).thenReturn(20L);
            lenient().when(esgCompany.getName()).thenReturn("ESG회사");
            lenient().when(esgCompany.getIndustry()).thenReturn(null);

            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(reviewRepository.findById(40L)).willReturn(Optional.of(esgReview));

            // when
            ReviewDetailResponse response = reviewService.getReviewDetail(1L, 40L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getDomainCode()).isEqualTo("ESG");
            assertThat(response.getAllowedCategoryKeys()).containsExactly("E", "S", "G");
        }

        @Test
        @DisplayName("SAFETY 도메인 상세 조회 시 allowedCategoryKeys로 빈 리스트 반환")
        void getReviewDetail_SafetyDomain_ReturnsEmptyAllowedCategoryKeys() {
            // given
            Review safetyReview = mock(Review.class);
            Diagnostic safetyDiagnostic = mock(Diagnostic.class);
            Company safetyCompany = mock(Company.class);
            lenient().when(safetyReview.getDomain()).thenReturn(safetyDomain);
            lenient().when(safetyReview.getReviewId()).thenReturn(50L);
            lenient().when(safetyReview.getDiagnostic()).thenReturn(safetyDiagnostic);
            lenient().when(safetyReview.getCompany()).thenReturn(safetyCompany);
            lenient().when(safetyReview.getStatus()).thenReturn(ReviewStatus.REVIEWING);
            lenient().when(safetyReview.getScore()).thenReturn(60);
            lenient().when(safetyReview.getRiskLevel()).thenReturn(RiskLevel.MEDIUM);
            lenient().when(safetyReview.getSubmittedAt()).thenReturn(LocalDateTime.now());
            lenient().when(safetyDiagnostic.getDiagnosticId()).thenReturn(300L);
            lenient().when(safetyDiagnostic.getDiagnosticCode()).thenReturn("DG-2026-00003");
            lenient().when(safetyDiagnostic.getTitle()).thenReturn("안전보건 진단");
            lenient().when(safetyCompany.getCompanyId()).thenReturn(30L);
            lenient().when(safetyCompany.getName()).thenReturn("안전회사");
            lenient().when(safetyCompany.getIndustry()).thenReturn(null);

            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(reviewRepository.findById(50L)).willReturn(Optional.of(safetyReview));

            // when
            ReviewDetailResponse response = reviewService.getReviewDetail(1L, 50L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getDomainCode()).isEqualTo("SAFETY");
            assertThat(response.getAllowedCategoryKeys()).isEmpty();
        }
    }
}
