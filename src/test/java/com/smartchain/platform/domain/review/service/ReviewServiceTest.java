package com.smartchain.platform.domain.review.service;

import com.smartchain.platform.domain.diagnostic.entity.Diagnostic;
import com.smartchain.platform.domain.review.entity.Review;
import com.smartchain.platform.domain.review.repository.ReviewRepository;
import com.smartchain.platform.domain.user.entity.Company;
import com.smartchain.platform.domain.user.entity.Industry;
import com.smartchain.platform.domain.user.entity.Role;
import com.smartchain.platform.domain.user.entity.User;
import com.smartchain.platform.domain.user.repository.CompanyRepository;
import com.smartchain.platform.domain.user.repository.UserRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
    private User reviewerUser;

    @Mock
    private User guestUser;

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

    @BeforeEach
    void setUp() {
        lenient().when(reviewerRole.getCode()).thenReturn("REVIEWER");
        lenient().when(guestRole.getCode()).thenReturn("GUEST");

        lenient().when(reviewerUser.getUserId()).thenReturn(1L);
        lenient().when(reviewerUser.getName()).thenReturn("심사자");
        lenient().when(reviewerUser.getEmail()).thenReturn("reviewer@test.com");
        lenient().when(reviewerUser.getRole()).thenReturn(reviewerRole);

        lenient().when(guestUser.getUserId()).thenReturn(2L);
        lenient().when(guestUser.getName()).thenReturn("게스트");
        lenient().when(guestUser.getRole()).thenReturn(guestRole);

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
    }

    @Nested
    @DisplayName("대시보드 조회 테스트")
    class GetDashboardTest {

        @Test
        @DisplayName("REVIEWER가 대시보드 조회 성공")
        void getDashboard_AsReviewer_Success() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(reviewRepository.countDistinctCompanies()).willReturn(50L);
            given(reviewRepository.countByStatus(ReviewStatus.REVIEWING)).willReturn(10L);
            given(reviewRepository.countByStatus(ReviewStatus.APPROVED)).willReturn(30L);
            given(reviewRepository.countByStatus(ReviewStatus.REVISION_REQUIRED)).willReturn(5L);
            given(reviewRepository.countByRiskLevel(RiskLevel.HIGH)).willReturn(5L);
            given(reviewRepository.countByRiskLevel(RiskLevel.MEDIUM)).willReturn(25L);
            given(reviewRepository.countByRiskLevel(RiskLevel.LOW)).willReturn(15L);
            given(reviewRepository.findTopNByOrderBySubmittedAtDesc(any())).willReturn(List.of(testReview));

            // when
            ReviewDashboardResponse response = reviewService.getDashboard(1L, null, null, null);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getOverview().getTotalCompanies()).isEqualTo(50);
            assertThat(response.getOverview().getInReviewCount()).isEqualTo(10);
            assertThat(response.getOverview().getCompletedCount()).isEqualTo(30);
            assertThat(response.getRiskDistribution().getHigh()).isEqualTo(5);
            assertThat(response.getRiskDistribution().getMedium()).isEqualTo(25);
            assertThat(response.getRiskDistribution().getLow()).isEqualTo(15);
            assertThat(response.getRecentActivities()).hasSize(1);
        }

        @Test
        @DisplayName("REVIEWER가 아닌 사용자가 대시보드 조회 시 실패")
        void getDashboard_AsGuest_ThrowsException() {
            // given
            given(userRepository.findById(2L)).willReturn(Optional.of(guestUser));

            // when & then
            assertThatThrownBy(() -> reviewService.getDashboard(2L, null, null, null))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.PERMISSION_DENIED_ACTION);
                    });
        }
    }

    @Nested
    @DisplayName("심사 목록 조회 테스트")
    class GetReviewListTest {

        @Test
        @DisplayName("REVIEWER가 심사 목록 조회 성공")
        void getReviewList_AsReviewer_Success() {
            // given
            Page<Review> reviewPage = new PageImpl<>(List.of(testReview), PageRequest.of(0, 20), 1);

            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(reviewRepository.findAllByOrderByCreatedAtDesc(any())).willReturn(reviewPage);
            given(reviewRepository.countDistinctCompanies()).willReturn(50L);
            given(reviewRepository.countByStatus(ReviewStatus.REVIEWING)).willReturn(10L);
            given(reviewRepository.countByStatus(ReviewStatus.APPROVED)).willReturn(30L);
            given(reviewRepository.countByRiskLevel(RiskLevel.HIGH)).willReturn(5L);
            given(reviewRepository.countByRiskLevel(RiskLevel.MEDIUM)).willReturn(25L);
            given(reviewRepository.countByRiskLevel(RiskLevel.LOW)).willReturn(15L);

            // when
            ReviewListResponse response = reviewService.getReviewList(1L, null, null, null, 0, 20);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).getReviewId()).isEqualTo(1L);
            assertThat(response.getContent().get(0).getScore()).isEqualTo(72);
            assertThat(response.getContent().get(0).getRiskLevel()).isEqualTo("MEDIUM");
            assertThat(response.getPage().getNumber()).isEqualTo(0);
            assertThat(response.getPage().getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("상태 필터로 심사 목록 조회 성공")
        void getReviewList_WithStatusFilter_Success() {
            // given
            Page<Review> reviewPage = new PageImpl<>(List.of(testReview), PageRequest.of(0, 20), 1);

            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(reviewRepository.findByStatusOrderByCreatedAtDesc(eq(ReviewStatus.REVIEWING), any())).willReturn(reviewPage);
            given(reviewRepository.countDistinctCompanies()).willReturn(50L);
            given(reviewRepository.countByStatus(any())).willReturn(10L);
            given(reviewRepository.countByRiskLevel(any())).willReturn(15L);

            // when
            ReviewListResponse response = reviewService.getReviewList(1L, "REVIEWING", null, null, 0, 20);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(1);
            verify(reviewRepository).findByStatusOrderByCreatedAtDesc(eq(ReviewStatus.REVIEWING), any());
        }
    }

    @Nested
    @DisplayName("심사 상세 조회 테스트")
    class GetReviewDetailTest {

        @Test
        @DisplayName("심사 상세 조회 성공")
        void getReviewDetail_Success() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(reviewRepository.findById(1L)).willReturn(Optional.of(testReview));

            // when
            ReviewDetailResponse response = reviewService.getReviewDetail(1L, 1L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getReviewId()).isEqualTo(1L);
            assertThat(response.getDiagnostic().getDiagnosticId()).isEqualTo(100L);
            assertThat(response.getDiagnostic().getDiagnosticCode()).isEqualTo("DG-2026-00001");
            assertThat(response.getCompany().getCompanyId()).isEqualTo(10L);
            assertThat(response.getScore()).isEqualTo(72);
            assertThat(response.getRiskLevel()).isEqualTo("MEDIUM");
            assertThat(response.getStatus()).isEqualTo("REVIEWING");
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
    }

    @Nested
    @DisplayName("심사 결과 입력 테스트")
    class ProcessReviewTest {

        @Test
        @DisplayName("심사 승인 성공")
        void processReview_Approve_Success() {
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
        @DisplayName("이미 처리된 심사 재처리 시 실패")
        void processReview_AlreadyProcessed_ThrowsException() {
            // given
            when(testReview.isReviewing()).thenReturn(false);

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
}
