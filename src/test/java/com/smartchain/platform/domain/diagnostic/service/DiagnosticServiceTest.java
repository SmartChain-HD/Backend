package com.smartchain.platform.domain.diagnostic.service;

import com.smartchain.platform.domain.approval.entity.Approval;
import com.smartchain.platform.domain.approval.repository.ApprovalRepository;
import com.smartchain.platform.domain.diagnostic.entity.Campaign;
import com.smartchain.platform.domain.diagnostic.entity.Diagnostic;
import com.smartchain.platform.domain.diagnostic.entity.DiagnosticHistory;
import com.smartchain.platform.domain.diagnostic.repository.CampaignRepository;
import com.smartchain.platform.domain.diagnostic.repository.DiagnosticHistoryRepository;
import com.smartchain.platform.domain.diagnostic.repository.DiagnosticRepository;
import com.smartchain.platform.domain.diagnostic.repository.ResultQualRepository;
import com.smartchain.platform.domain.diagnostic.repository.ResultQuantRepository;
import com.smartchain.platform.domain.ai.repository.AiAnalysisResultRepository;
import com.smartchain.platform.domain.evidence.repository.EvidenceFileRepository;
import com.smartchain.platform.domain.review.entity.Review;
import com.smartchain.platform.domain.review.repository.ReviewRepository;
import com.smartchain.platform.domain.user.entity.Company;
import com.smartchain.platform.domain.user.entity.Domain;
import com.smartchain.platform.domain.user.entity.Role;
import com.smartchain.platform.domain.user.entity.User;
import com.smartchain.platform.domain.user.entity.UserDomainRole;
import com.smartchain.platform.domain.user.repository.DomainRepository;
import com.smartchain.platform.domain.user.repository.UserRepository;
import com.smartchain.platform.dto.diagnostic.ai.AiAnalysisResponse;
import com.smartchain.platform.dto.diagnostic.create.DiagnosticCreateRequest;
import com.smartchain.platform.dto.diagnostic.create.DiagnosticCreateResponse;
import com.smartchain.platform.dto.diagnostic.detail.DiagnosticDetailResponse;
import com.smartchain.platform.dto.diagnostic.history.DiagnosticHistoryResponse;
import com.smartchain.platform.dto.diagnostic.list.DiagnosticListResponse;
import com.smartchain.platform.dto.diagnostic.submit.DiagnosticSubmitRequest;
import com.smartchain.platform.dto.diagnostic.submit.DiagnosticSubmitResponse;
import com.smartchain.platform.global.enums.DiagnosticStatus;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiagnosticServiceTest {

    @InjectMocks
    private DiagnosticService diagnosticService;

    @Mock
    private DiagnosticRepository diagnosticRepository;

    @Mock
    private DiagnosticHistoryRepository diagnosticHistoryRepository;

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApprovalRepository approvalRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private DomainRepository domainRepository;

    @Mock
    private ResultQualRepository resultQualRepository;

    @Mock
    private ResultQuantRepository resultQuantRepository;

    @Mock
    private AiAnalysisResultRepository aiAnalysisResultRepository;

    @Mock
    private EvidenceFileRepository evidenceFileRepository;

    @Mock
    private User drafterUser;

    @Mock
    private User approverUser;

    @Mock
    private Company testCompany;

    @Mock
    private Role drafterRole;

    @Mock
    private Role approverRole;

    @Mock
    private Role guestRole;

    @Mock
    private Campaign testCampaign;

    @BeforeEach
    void setUp() {
        lenient().when(drafterRole.getCode()).thenReturn("DRAFTER");
        lenient().when(approverRole.getCode()).thenReturn("APPROVER");
        lenient().when(guestRole.getCode()).thenReturn("GUEST");

        lenient().when(drafterUser.getUserId()).thenReturn(1L);
        lenient().when(drafterUser.getName()).thenReturn("기안자");
        lenient().when(drafterUser.getRole()).thenReturn(drafterRole);
        lenient().when(drafterUser.getCompany()).thenReturn(testCompany);
        lenient().when(drafterUser.getDomainRoles()).thenReturn(new ArrayList<>());

        lenient().when(approverUser.getUserId()).thenReturn(2L);
        lenient().when(approverUser.getName()).thenReturn("결재자");
        lenient().when(approverUser.getRole()).thenReturn(approverRole);
        lenient().when(approverUser.getCompany()).thenReturn(testCompany);
        lenient().when(approverUser.getDomainRoles()).thenReturn(new ArrayList<>());

        lenient().when(testCompany.getCompanyId()).thenReturn(10L);
        lenient().when(testCompany.getName()).thenReturn("(주)테스트회사");

        lenient().when(testCampaign.getCampaignId()).thenReturn(100L);
        lenient().when(testCampaign.getCampaignCode()).thenReturn("CP-2026-00001");
        lenient().when(testCampaign.getTitle()).thenReturn("2026년 ESG 자가진단");
        lenient().when(testCampaign.getPeriodStartDate()).thenReturn(LocalDate.of(2026, 1, 1));
        lenient().when(testCampaign.getPeriodEndDate()).thenReturn(LocalDate.of(2026, 12, 31));
        lenient().when(testCampaign.getDeadline()).thenReturn(LocalDate.of(2026, 3, 31));
    }

    @Nested
    @DisplayName("기안 목록 조회 테스트")
    class GetDiagnosticListTest {

        @Test
        @DisplayName("DRAFTER가 자신의 기안 목록 조회 성공")
        void getDiagnosticList_AsDrafter_Success() {
            // given
            Diagnostic diagnostic = mock(Diagnostic.class);
            lenient().when(diagnostic.getDiagnosticId()).thenReturn(1L);
            lenient().when(diagnostic.getDiagnosticCode()).thenReturn("DG-2026-00001");
            lenient().when(diagnostic.getTitle()).thenReturn("2026년 ESG 자가진단");
            lenient().when(diagnostic.getCampaign()).thenReturn(testCampaign);
            lenient().when(diagnostic.getCompany()).thenReturn(testCompany);
            lenient().when(diagnostic.getStatus()).thenReturn(DiagnosticStatus.WRITING);
            lenient().when(diagnostic.getPeriodStartDate()).thenReturn(LocalDate.of(2026, 1, 1));
            lenient().when(diagnostic.getPeriodEndDate()).thenReturn(LocalDate.of(2026, 12, 31));
            lenient().when(diagnostic.getDeadline()).thenReturn(LocalDate.of(2026, 3, 31));
            lenient().when(diagnostic.getQualitativeProgress()).thenReturn(0);
            lenient().when(diagnostic.getQuantitativeProgress()).thenReturn(0);
            lenient().when(diagnostic.getOverallProgress()).thenReturn(0);
            lenient().when(diagnostic.getCreatedAt()).thenReturn(LocalDateTime.now());

            Page<Diagnostic> diagnosticPage = new PageImpl<>(List.of(diagnostic), PageRequest.of(0, 10), 1);

            given(userRepository.findById(1L)).willReturn(Optional.of(drafterUser));
            given(diagnosticRepository.findByDrafterIdAndFilters(eq(1L), eq(false), any(), eq(false), any(), any(), any(), any(), any()))
                    .willReturn(diagnosticPage);

            // when
            DiagnosticListResponse response = diagnosticService.getDiagnosticList(1L, null, null, null, null, null, 0, 10);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).getDiagnosticId()).isEqualTo(1L);
            assertThat(response.getContent().get(0).getStatus()).isEqualTo("WRITING");
            assertThat(response.getPage().getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("APPROVER가 회사 전체 기안 목록 조회 성공")
        void getDiagnosticList_AsApprover_Success() {
            // given
            Diagnostic diagnostic = mock(Diagnostic.class);
            lenient().when(diagnostic.getDiagnosticId()).thenReturn(1L);
            lenient().when(diagnostic.getDiagnosticCode()).thenReturn("DG-2026-00001");
            lenient().when(diagnostic.getTitle()).thenReturn("2026년 ESG 자가진단");
            lenient().when(diagnostic.getCampaign()).thenReturn(testCampaign);
            lenient().when(diagnostic.getCompany()).thenReturn(testCompany);
            lenient().when(diagnostic.getStatus()).thenReturn(DiagnosticStatus.SUBMITTED);
            lenient().when(diagnostic.getPeriodStartDate()).thenReturn(LocalDate.of(2026, 1, 1));
            lenient().when(diagnostic.getPeriodEndDate()).thenReturn(LocalDate.of(2026, 12, 31));
            lenient().when(diagnostic.getDeadline()).thenReturn(LocalDate.of(2026, 3, 31));
            lenient().when(diagnostic.getQualitativeProgress()).thenReturn(100);
            lenient().when(diagnostic.getQuantitativeProgress()).thenReturn(100);
            lenient().when(diagnostic.getOverallProgress()).thenReturn(100);
            lenient().when(diagnostic.getCreatedAt()).thenReturn(LocalDateTime.now());

            Page<Diagnostic> diagnosticPage = new PageImpl<>(List.of(diagnostic), PageRequest.of(0, 10), 1);

            given(userRepository.findById(2L)).willReturn(Optional.of(approverUser));
            given(diagnosticRepository.findByCompanyAndFilters(eq(testCompany), eq(false), any(), eq(false), any(), any(), any(), any(), any()))
                    .willReturn(diagnosticPage);

            // when
            DiagnosticListResponse response = diagnosticService.getDiagnosticList(2L, null, null, null, null, null, 0, 10);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).getStatus()).isEqualTo("SUBMITTED");
        }

        @Test
        @DisplayName("권한 없는 사용자가 기안 목록 조회 시 실패")
        void getDiagnosticList_AsGuest_ThrowsException() {
            // given
            User guestUser = mock(User.class);
            when(guestUser.getRole()).thenReturn(guestRole);

            given(userRepository.findById(99L)).willReturn(Optional.of(guestUser));

            // when & then
            assertThatThrownBy(() -> diagnosticService.getDiagnosticList(99L, null, null, null, null, null, 0, 10))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.PERMISSION_DENIED_ACTION);
                    });
        }
    }

    @Nested
    @DisplayName("기안 상세 조회 테스트")
    class GetDiagnosticDetailTest {

        @Test
        @DisplayName("DRAFTER가 자신의 기안 상세 조회 성공")
        void getDiagnosticDetail_AsDrafter_Success() {
            // given
            Diagnostic diagnostic = mock(Diagnostic.class);
            when(diagnostic.getDiagnosticId()).thenReturn(1L);
            when(diagnostic.getDiagnosticCode()).thenReturn("DG-2026-00001");
            when(diagnostic.getCampaign()).thenReturn(testCampaign);
            when(diagnostic.getCompany()).thenReturn(testCompany);
            when(diagnostic.getDrafterId()).thenReturn(1L);
            when(diagnostic.getStatus()).thenReturn(DiagnosticStatus.WRITING);
            when(diagnostic.getPeriodStartDate()).thenReturn(LocalDate.of(2026, 1, 1));
            when(diagnostic.getPeriodEndDate()).thenReturn(LocalDate.of(2026, 12, 31));
            when(diagnostic.getDeadline()).thenReturn(LocalDate.of(2026, 3, 31));
            when(diagnostic.getQualitativeProgress()).thenReturn(50);
            when(diagnostic.getQuantitativeProgress()).thenReturn(30);
            when(diagnostic.getOverallProgress()).thenReturn(40);
            when(diagnostic.getCreatedAt()).thenReturn(LocalDateTime.now());

            given(userRepository.findById(1L)).willReturn(Optional.of(drafterUser));
            given(diagnosticRepository.findById(1L)).willReturn(Optional.of(diagnostic));

            // when
            DiagnosticDetailResponse response = diagnosticService.getDiagnosticDetail(1L, 1L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getDiagnosticId()).isEqualTo(1L);
            assertThat(response.getDiagnosticCode()).isEqualTo("DG-2026-00001");
            assertThat(response.getStatus()).isEqualTo("WRITING");
            assertThat(response.getStatusLabel()).isEqualTo("작성중");
            assertThat(response.getQualitativeProgress()).isEqualTo(50);
        }

        @Test
        @DisplayName("존재하지 않는 기안 조회 시 실패")
        void getDiagnosticDetail_NotFound_ThrowsException() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(drafterUser));
            given(diagnosticRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> diagnosticService.getDiagnosticDetail(1L, 999L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.DIAGNOSTIC_NOT_FOUND);
                    });
        }

        @Test
        @DisplayName("다른 사용자의 기안 조회 시 실패 (DRAFTER)")
        void getDiagnosticDetail_OtherUserDiagnostic_ThrowsException() {
            // given
            Diagnostic diagnostic = mock(Diagnostic.class);
            when(diagnostic.getDrafterId()).thenReturn(999L); // 다른 사용자의 기안

            given(userRepository.findById(1L)).willReturn(Optional.of(drafterUser));
            given(diagnosticRepository.findById(1L)).willReturn(Optional.of(diagnostic));

            // when & then
            assertThatThrownBy(() -> diagnosticService.getDiagnosticDetail(1L, 1L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.PERMISSION_DENIED_RESOURCE);
                    });
        }

        @Test
        @DisplayName("APPROVER가 같은 회사 기안 상세 조회 성공")
        void getDiagnosticDetail_AsApprover_SameCompany_Success() {
            // given
            Domain esgDomain = mock(Domain.class);
            when(esgDomain.getDomainId()).thenReturn(1L);
            when(esgDomain.getCode()).thenReturn("ESG");
            when(esgDomain.getName()).thenReturn("ESG 실사");

            Diagnostic diagnostic = mock(Diagnostic.class);
            when(diagnostic.getDiagnosticId()).thenReturn(1L);
            when(diagnostic.getDiagnosticCode()).thenReturn("DG-2026-00001");
            when(diagnostic.getCampaign()).thenReturn(testCampaign);
            when(diagnostic.getCompany()).thenReturn(testCompany);
            when(diagnostic.getDomain()).thenReturn(esgDomain);
            when(diagnostic.getDrafterId()).thenReturn(1L); // 다른 사용자가 작성
            when(diagnostic.getStatus()).thenReturn(DiagnosticStatus.SUBMITTED);
            when(diagnostic.getPeriodStartDate()).thenReturn(LocalDate.of(2026, 1, 1));
            when(diagnostic.getPeriodEndDate()).thenReturn(LocalDate.of(2026, 12, 31));
            when(diagnostic.getDeadline()).thenReturn(LocalDate.of(2026, 3, 31));
            when(diagnostic.getQualitativeProgress()).thenReturn(100);
            when(diagnostic.getQuantitativeProgress()).thenReturn(100);
            when(diagnostic.getOverallProgress()).thenReturn(100);
            when(diagnostic.getCreatedAt()).thenReturn(LocalDateTime.now());

            when(approverUser.hasAnyRoleInDomain("ESG", "DRAFTER", "APPROVER", "REVIEWER")).thenReturn(true);
            when(approverUser.hasRoleInDomain("ESG", "DRAFTER")).thenReturn(false);
            when(approverUser.hasRoleInDomain("ESG", "APPROVER")).thenReturn(true);

            given(userRepository.findById(2L)).willReturn(Optional.of(approverUser));
            given(diagnosticRepository.findById(1L)).willReturn(Optional.of(diagnostic));

            // when
            DiagnosticDetailResponse response = diagnosticService.getDiagnosticDetail(2L, 1L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getDiagnosticId()).isEqualTo(1L);
            assertThat(response.getStatus()).isEqualTo("SUBMITTED");
        }

        @Test
        @DisplayName("APPROVER가 다른 회사 기안 조회 시 403")
        void getDiagnosticDetail_AsApprover_DifferentCompany_ThrowsException() {
            // given
            Domain esgDomain = mock(Domain.class);
            when(esgDomain.getCode()).thenReturn("ESG");

            Company otherCompany = mock(Company.class);
            when(otherCompany.getCompanyId()).thenReturn(999L);

            Diagnostic diagnostic = mock(Diagnostic.class);
            when(diagnostic.getDomain()).thenReturn(esgDomain);
            when(diagnostic.getCompany()).thenReturn(otherCompany);

            when(approverUser.hasAnyRoleInDomain("ESG", "DRAFTER", "APPROVER", "REVIEWER")).thenReturn(true);
            when(approverUser.hasRoleInDomain("ESG", "DRAFTER")).thenReturn(false);
            when(approverUser.hasRoleInDomain("ESG", "APPROVER")).thenReturn(true);

            given(userRepository.findById(2L)).willReturn(Optional.of(approverUser));
            given(diagnosticRepository.findById(1L)).willReturn(Optional.of(diagnostic));

            // when & then
            assertThatThrownBy(() -> diagnosticService.getDiagnosticDetail(2L, 1L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.PERMISSION_DENIED_RESOURCE);
                    });
        }

        @Test
        @DisplayName("REVIEWER가 도메인 내 기안 상세 조회 성공")
        void getDiagnosticDetail_AsReviewer_Success() {
            // given
            Domain esgDomain = mock(Domain.class);
            when(esgDomain.getDomainId()).thenReturn(1L);
            when(esgDomain.getCode()).thenReturn("ESG");
            when(esgDomain.getName()).thenReturn("ESG 실사");

            Company otherCompany = mock(Company.class);
            when(otherCompany.getCompanyId()).thenReturn(999L);
            when(otherCompany.getName()).thenReturn("다른회사");

            User reviewerUser = mock(User.class);
            lenient().when(reviewerUser.getUserId()).thenReturn(3L);
            lenient().when(reviewerUser.getName()).thenReturn("수신자");
            lenient().when(reviewerUser.getDomainRoles()).thenReturn(new ArrayList<>());

            Diagnostic diagnostic = mock(Diagnostic.class);
            when(diagnostic.getDiagnosticId()).thenReturn(1L);
            when(diagnostic.getDiagnosticCode()).thenReturn("DG-2026-00001");
            when(diagnostic.getCampaign()).thenReturn(testCampaign);
            when(diagnostic.getCompany()).thenReturn(otherCompany);
            when(diagnostic.getDomain()).thenReturn(esgDomain);
            when(diagnostic.getDrafterId()).thenReturn(1L);
            when(diagnostic.getStatus()).thenReturn(DiagnosticStatus.REVIEWING);
            when(diagnostic.getPeriodStartDate()).thenReturn(LocalDate.of(2026, 1, 1));
            when(diagnostic.getPeriodEndDate()).thenReturn(LocalDate.of(2026, 12, 31));
            when(diagnostic.getDeadline()).thenReturn(LocalDate.of(2026, 3, 31));
            when(diagnostic.getQualitativeProgress()).thenReturn(100);
            when(diagnostic.getQuantitativeProgress()).thenReturn(100);
            when(diagnostic.getOverallProgress()).thenReturn(100);
            when(diagnostic.getCreatedAt()).thenReturn(LocalDateTime.now());

            when(reviewerUser.hasAnyRoleInDomain("ESG", "DRAFTER", "APPROVER", "REVIEWER")).thenReturn(true);
            when(reviewerUser.hasRoleInDomain("ESG", "DRAFTER")).thenReturn(false);
            when(reviewerUser.hasRoleInDomain("ESG", "APPROVER")).thenReturn(false);

            given(userRepository.findById(3L)).willReturn(Optional.of(reviewerUser));
            given(diagnosticRepository.findById(1L)).willReturn(Optional.of(diagnostic));

            // when
            DiagnosticDetailResponse response = diagnosticService.getDiagnosticDetail(3L, 1L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getDiagnosticId()).isEqualTo(1L);
            assertThat(response.getStatus()).isEqualTo("REVIEWING");
        }

        @Test
        @DisplayName("도메인 권한 없는 사용자가 상세 조회 시 403")
        void getDiagnosticDetail_NoDomainRole_ThrowsException() {
            // given
            Domain esgDomain = mock(Domain.class);
            when(esgDomain.getCode()).thenReturn("ESG");

            Diagnostic diagnostic = mock(Diagnostic.class);
            when(diagnostic.getDomain()).thenReturn(esgDomain);

            User noRoleUser = mock(User.class);
            when(noRoleUser.getUserId()).thenReturn(99L);
            when(noRoleUser.hasAnyRoleInDomain("ESG", "DRAFTER", "APPROVER", "REVIEWER")).thenReturn(false);

            given(userRepository.findById(99L)).willReturn(Optional.of(noRoleUser));
            given(diagnosticRepository.findById(1L)).willReturn(Optional.of(diagnostic));

            // when & then
            assertThatThrownBy(() -> diagnosticService.getDiagnosticDetail(99L, 1L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.PERMISSION_DENIED_ACTION);
                    });
        }
    }

    @Nested
    @DisplayName("기안 생성 테스트")
    class CreateDiagnosticTest {

        @Test
        @DisplayName("DRAFTER가 기안 생성 성공")
        void createDiagnostic_Success() {
            // given
            DiagnosticCreateRequest request = DiagnosticCreateRequest.builder()
                    .campaignId(100L)
                    .build();

            Diagnostic savedDiagnostic = mock(Diagnostic.class);
            when(savedDiagnostic.getDiagnosticId()).thenReturn(1L);
            when(savedDiagnostic.getDiagnosticCode()).thenReturn("DG-2026-00001");
            when(savedDiagnostic.getStatus()).thenReturn(DiagnosticStatus.WRITING);
            when(savedDiagnostic.getCreatedAt()).thenReturn(LocalDateTime.now());

            given(userRepository.findById(1L)).willReturn(Optional.of(drafterUser));
            given(campaignRepository.findById(100L)).willReturn(Optional.of(testCampaign));
            given(diagnosticRepository.getNextDiagnosticCodeSequence()).willReturn(1L);
            given(diagnosticRepository.save(any(Diagnostic.class))).willReturn(savedDiagnostic);
            given(diagnosticHistoryRepository.save(any(DiagnosticHistory.class))).willReturn(null);

            // when
            DiagnosticCreateResponse response = diagnosticService.createDiagnostic(1L, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getDiagnosticId()).isEqualTo(1L);
            assertThat(response.getDiagnosticCode()).isEqualTo("DG-2026-00001");
            assertThat(response.getStatus()).isEqualTo("WRITING");

            verify(diagnosticRepository).save(any(Diagnostic.class));
            verify(diagnosticHistoryRepository).save(any(DiagnosticHistory.class));
        }

        @Test
        @DisplayName("DRAFTER 권한이 없는 사용자가 기안 생성 시 실패")
        void createDiagnostic_NotDrafter_ThrowsException() {
            // given
            DiagnosticCreateRequest request = DiagnosticCreateRequest.builder()
                    .campaignId(100L)
                    .build();

            given(userRepository.findById(2L)).willReturn(Optional.of(approverUser));
            given(campaignRepository.findById(100L)).willReturn(Optional.of(testCampaign));

            // when & then
            assertThatThrownBy(() -> diagnosticService.createDiagnostic(2L, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.PERMISSION_DENIED_ACTION);
                    });
        }

        @Test
        @DisplayName("존재하지 않는 캠페인으로 기안 생성 시 실패")
        void createDiagnostic_CampaignNotFound_ThrowsException() {
            // given
            DiagnosticCreateRequest request = DiagnosticCreateRequest.builder()
                    .campaignId(999L)
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(drafterUser));
            given(campaignRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> diagnosticService.createDiagnostic(1L, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.CAMPAIGN_NOT_FOUND);
                    });
        }
    }

    @Nested
    @DisplayName("기안 제출 테스트")
    class SubmitDiagnosticTest {

        @Test
        @DisplayName("DRAFTER가 기안 제출 성공 및 Approval 자동 생성")
        void submitDiagnostic_Success_CreatesApproval() {
            // given
            DiagnosticSubmitRequest request = DiagnosticSubmitRequest.builder()
                    .submitComment("검토 부탁드립니다.")
                    .build();

            Diagnostic diagnostic = mock(Diagnostic.class);
            when(diagnostic.getDiagnosticId()).thenReturn(1L);
            when(diagnostic.getDrafterId()).thenReturn(1L);
            when(diagnostic.canSubmit()).thenReturn(true);
            // getStatus()는 BIZ_002 체크(1회)와 previousStatus 조회(1회)에서 호출됨
            when(diagnostic.getStatus()).thenReturn(DiagnosticStatus.WRITING);
            when(diagnostic.getSubmittedAt()).thenReturn(LocalDateTime.now());
            when(diagnostic.getDeadline()).thenReturn(LocalDate.of(2026, 3, 31));

            Approval savedApproval = mock(Approval.class);
            when(savedApproval.getApprovalId()).thenReturn(100L);

            given(userRepository.findById(1L)).willReturn(Optional.of(drafterUser));
            given(diagnosticRepository.findById(1L)).willReturn(Optional.of(diagnostic));
            given(diagnosticHistoryRepository.save(any(DiagnosticHistory.class))).willReturn(null);
            given(approvalRepository.save(any(Approval.class))).willReturn(savedApproval);

            // when
            DiagnosticSubmitResponse response = diagnosticService.submitDiagnostic(1L, 1L, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getDiagnosticId()).isEqualTo(1L);
            assertThat(response.getPreviousStatus()).isEqualTo("WRITING");
            assertThat(response.getNewStatus()).isEqualTo("SUBMITTED");
            assertThat(response.getApprovalId()).isEqualTo(100L);
            assertThat(response.getMessage()).isEqualTo("기안이 제출되었습니다");

            verify(diagnostic).submit();
            verify(diagnosticHistoryRepository).save(any(DiagnosticHistory.class));
            verify(approvalRepository).save(any(Approval.class));
        }

        @Test
        @DisplayName("이미 제출된 기안 재제출 시 멱등 처리 (성공 응답 반환)")
        void submitDiagnostic_AlreadySubmitted_IdempotentSuccess() {
            // given
            DiagnosticSubmitRequest request = DiagnosticSubmitRequest.builder()
                    .submitComment("제출합니다.")
                    .build();

            Diagnostic diagnostic = mock(Diagnostic.class);
            when(diagnostic.getDrafterId()).thenReturn(1L);
            when(diagnostic.getStatus()).thenReturn(DiagnosticStatus.SUBMITTED);
            when(diagnostic.getDiagnosticId()).thenReturn(1L);
            when(diagnostic.getSubmittedAt()).thenReturn(null);

            given(userRepository.findById(1L)).willReturn(Optional.of(drafterUser));
            given(diagnosticRepository.findById(1L)).willReturn(Optional.of(diagnostic));

            // when
            DiagnosticSubmitResponse response = diagnosticService.submitDiagnostic(1L, 1L, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getDiagnosticId()).isEqualTo(1L);
            assertThat(response.getNewStatus()).isEqualTo("SUBMITTED");
            assertThat(response.getMessage()).isEqualTo("기안이 이미 제출되었습니다");
        }

        @Test
        @DisplayName("제출할 수 없는 상태의 기안 제출 시 실패 (BIZ_001)")
        void submitDiagnostic_InvalidStateTransition_ThrowsException() {
            // given
            DiagnosticSubmitRequest request = DiagnosticSubmitRequest.builder()
                    .submitComment("제출합니다.")
                    .build();

            Diagnostic diagnostic = mock(Diagnostic.class);
            when(diagnostic.getDrafterId()).thenReturn(1L);
            when(diagnostic.getStatus()).thenReturn(DiagnosticStatus.APPROVED); // not SUBMITTED
            when(diagnostic.canSubmit()).thenReturn(false);

            given(userRepository.findById(1L)).willReturn(Optional.of(drafterUser));
            given(diagnosticRepository.findById(1L)).willReturn(Optional.of(diagnostic));

            // when & then
            assertThatThrownBy(() -> diagnosticService.submitDiagnostic(1L, 1L, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.DIAGNOSTIC_INVALID_STATE_TRANSITION);
                    });
        }

        @Test
        @DisplayName("SAFETY 도메인 제출 시 결재 스킵 → Review 생성")
        void submitDiagnostic_Safety_SkipsApproval_CreatesReview() {
            // given
            Domain safetyDomain = mock(Domain.class);
            when(safetyDomain.getCode()).thenReturn("SAFETY");

            DiagnosticSubmitRequest request = DiagnosticSubmitRequest.builder()
                    .submitComment("안전보건 제출합니다.")
                    .build();

            Diagnostic diagnostic = mock(Diagnostic.class);
            when(diagnostic.getDiagnosticId()).thenReturn(1L);
            when(diagnostic.getDrafterId()).thenReturn(1L);
            when(diagnostic.canSubmit()).thenReturn(true);
            when(diagnostic.getStatus()).thenReturn(DiagnosticStatus.WRITING);
            when(diagnostic.getSubmittedAt()).thenReturn(LocalDateTime.now());
            when(diagnostic.getDomain()).thenReturn(safetyDomain);
            when(diagnostic.getCompany()).thenReturn(testCompany);

            when(drafterUser.hasRoleInDomain("SAFETY", "DRAFTER")).thenReturn(true);

            Review savedReview = mock(Review.class);
            when(savedReview.getReviewId()).thenReturn(200L);

            given(userRepository.findById(1L)).willReturn(Optional.of(drafterUser));
            given(diagnosticRepository.findById(1L)).willReturn(Optional.of(diagnostic));
            given(diagnosticHistoryRepository.save(any(DiagnosticHistory.class))).willReturn(null);
            given(reviewRepository.save(any(Review.class))).willReturn(savedReview);

            // when
            DiagnosticSubmitResponse response = diagnosticService.submitDiagnostic(1L, 1L, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getNewStatus()).isEqualTo("APPROVED");
            assertThat(response.getReviewId()).isEqualTo(200L);
            assertThat(response.getApprovalId()).isNull();

            verify(diagnostic).submit();
            verify(diagnostic).approve();
            verify(reviewRepository).save(any(Review.class));
            verify(approvalRepository, never()).save(any(Approval.class));
            // 히스토리 2건: SUBMITTED + AUTO_APPROVED
            verify(diagnosticHistoryRepository, times(2)).save(any(DiagnosticHistory.class));
        }

        @Test
        @DisplayName("COMPLIANCE 도메인 제출 시 결재 스킵 → Review 생성")
        void submitDiagnostic_Compliance_SkipsApproval_CreatesReview() {
            // given
            Domain complianceDomain = mock(Domain.class);
            when(complianceDomain.getCode()).thenReturn("COMPLIANCE");

            DiagnosticSubmitRequest request = DiagnosticSubmitRequest.builder()
                    .submitComment("컴플라이언스 제출합니다.")
                    .build();

            Diagnostic diagnostic = mock(Diagnostic.class);
            when(diagnostic.getDiagnosticId()).thenReturn(1L);
            when(diagnostic.getDrafterId()).thenReturn(1L);
            when(diagnostic.canSubmit()).thenReturn(true);
            when(diagnostic.getStatus()).thenReturn(DiagnosticStatus.WRITING);
            when(diagnostic.getSubmittedAt()).thenReturn(LocalDateTime.now());
            when(diagnostic.getDomain()).thenReturn(complianceDomain);
            when(diagnostic.getCompany()).thenReturn(testCompany);

            when(drafterUser.hasRoleInDomain("COMPLIANCE", "DRAFTER")).thenReturn(true);

            Review savedReview = mock(Review.class);
            when(savedReview.getReviewId()).thenReturn(300L);

            given(userRepository.findById(1L)).willReturn(Optional.of(drafterUser));
            given(diagnosticRepository.findById(1L)).willReturn(Optional.of(diagnostic));
            given(diagnosticHistoryRepository.save(any(DiagnosticHistory.class))).willReturn(null);
            given(reviewRepository.save(any(Review.class))).willReturn(savedReview);

            // when
            DiagnosticSubmitResponse response = diagnosticService.submitDiagnostic(1L, 1L, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getNewStatus()).isEqualTo("APPROVED");
            assertThat(response.getReviewId()).isEqualTo(300L);
            assertThat(response.getApprovalId()).isNull();

            verify(diagnostic).approve();
            verify(reviewRepository).save(any(Review.class));
            verify(approvalRepository, never()).save(any(Approval.class));
        }

        @Test
        @DisplayName("ESG 도메인 제출 시 Approval 생성 (기존 동작 유지)")
        void submitDiagnostic_Esg_CreatesApproval() {
            // given
            Domain esgDomain = mock(Domain.class);
            when(esgDomain.getCode()).thenReturn("ESG");

            DiagnosticSubmitRequest request = DiagnosticSubmitRequest.builder()
                    .submitComment("ESG 제출합니다.")
                    .build();

            Diagnostic diagnostic = mock(Diagnostic.class);
            when(diagnostic.getDiagnosticId()).thenReturn(1L);
            when(diagnostic.getDrafterId()).thenReturn(1L);
            when(diagnostic.canSubmit()).thenReturn(true);
            when(diagnostic.getStatus()).thenReturn(DiagnosticStatus.WRITING);
            when(diagnostic.getSubmittedAt()).thenReturn(LocalDateTime.now());
            when(diagnostic.getDeadline()).thenReturn(LocalDate.of(2026, 3, 31));
            when(diagnostic.getDomain()).thenReturn(esgDomain);

            when(drafterUser.hasRoleInDomain("ESG", "DRAFTER")).thenReturn(true);

            Approval savedApproval = mock(Approval.class);
            when(savedApproval.getApprovalId()).thenReturn(100L);

            given(userRepository.findById(1L)).willReturn(Optional.of(drafterUser));
            given(diagnosticRepository.findById(1L)).willReturn(Optional.of(diagnostic));
            given(diagnosticHistoryRepository.save(any(DiagnosticHistory.class))).willReturn(null);
            given(approvalRepository.save(any(Approval.class))).willReturn(savedApproval);

            // when
            DiagnosticSubmitResponse response = diagnosticService.submitDiagnostic(1L, 1L, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getNewStatus()).isEqualTo("SUBMITTED");
            assertThat(response.getApprovalId()).isEqualTo(100L);
            assertThat(response.getReviewId()).isNull();

            verify(approvalRepository).save(any(Approval.class));
            verify(reviewRepository, never()).save(any(Review.class));
        }

        @Test
        @DisplayName("다른 사용자의 기안 제출 시 실패")
        void submitDiagnostic_NotOwner_ThrowsException() {
            // given
            DiagnosticSubmitRequest request = DiagnosticSubmitRequest.builder()
                    .submitComment("제출합니다.")
                    .build();

            Diagnostic diagnostic = mock(Diagnostic.class);
            when(diagnostic.getDrafterId()).thenReturn(999L); // 다른 사용자의 기안

            given(userRepository.findById(1L)).willReturn(Optional.of(drafterUser));
            given(diagnosticRepository.findById(1L)).willReturn(Optional.of(diagnostic));

            // when & then
            assertThatThrownBy(() -> diagnosticService.submitDiagnostic(1L, 1L, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.PERMISSION_DENIED_RESOURCE);
                    });
        }
    }

    @Nested
    @DisplayName("AI 분석 결과 조회 테스트")
    class GetAiAnalysisTest {

        @Test
        @DisplayName("DRAFTER가 자신의 기안 AI 분석 결과 조회 성공")
        void getAiAnalysis_AsDrafter_Success() {
            // given
            Diagnostic diagnostic = mock(Diagnostic.class);
            when(diagnostic.getDrafterId()).thenReturn(1L);

            given(userRepository.findById(1L)).willReturn(Optional.of(drafterUser));
            given(diagnosticRepository.findById(1L)).willReturn(Optional.of(diagnostic));

            // when
            AiAnalysisResponse response = diagnosticService.getAiAnalysis(1L, 1L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getDiagnosticId()).isEqualTo(1L);
            assertThat(response.getAnalysisStatus()).isEqualTo("PENDING");
        }

        @Test
        @DisplayName("APPROVER가 회사 기안 AI 분석 결과 조회 성공")
        void getAiAnalysis_AsApprover_Success() {
            // given
            Diagnostic diagnostic = mock(Diagnostic.class);
            when(diagnostic.getCompany()).thenReturn(testCompany);

            given(userRepository.findById(2L)).willReturn(Optional.of(approverUser));
            given(diagnosticRepository.findById(1L)).willReturn(Optional.of(diagnostic));

            // when
            AiAnalysisResponse response = diagnosticService.getAiAnalysis(2L, 1L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getDiagnosticId()).isEqualTo(1L);
            assertThat(response.getAnalysisStatus()).isEqualTo("PENDING");
        }

        @Test
        @DisplayName("존재하지 않는 기안 AI 분석 결과 조회 시 실패")
        void getAiAnalysis_DiagnosticNotFound_ThrowsException() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(drafterUser));
            given(diagnosticRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> diagnosticService.getAiAnalysis(1L, 999L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.DIAGNOSTIC_NOT_FOUND);
                    });
        }
    }

    @Nested
    @DisplayName("기안 이력 조회 테스트")
    class GetDiagnosticHistoryTest {

        @Test
        @DisplayName("기안 이력 조회 성공")
        void getDiagnosticHistory_Success() {
            // given
            Diagnostic diagnostic = mock(Diagnostic.class);
            when(diagnostic.getDrafterId()).thenReturn(1L);

            DiagnosticHistory history = mock(DiagnosticHistory.class);
            when(history.getHistoryId()).thenReturn(1L);
            when(history.getAction()).thenReturn("CREATED");
            when(history.getPreviousStatus()).thenReturn(null);
            when(history.getNewStatus()).thenReturn("WRITING");
            when(history.getActor()).thenReturn(drafterUser);
            when(history.getComment()).thenReturn(null);
            when(history.getCreatedAt()).thenReturn(LocalDateTime.now());

            given(userRepository.findById(1L)).willReturn(Optional.of(drafterUser));
            given(diagnosticRepository.findById(1L)).willReturn(Optional.of(diagnostic));
            given(diagnosticHistoryRepository.findByDiagnosticOrderByCreatedAtDesc(diagnostic))
                    .willReturn(List.of(history));

            // when
            DiagnosticHistoryResponse response = diagnosticService.getDiagnosticHistory(1L, 1L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getDiagnosticId()).isEqualTo(1L);
            assertThat(response.getHistory()).hasSize(1);
            assertThat(response.getHistory().get(0).getAction()).isEqualTo("CREATED");
            assertThat(response.getHistory().get(0).getNewStatus()).isEqualTo("WRITING");
        }
    }

    @Nested
    @DisplayName("도메인 파라미터 관련 테스트")
    class DomainParameterTest {

        @Test
        @DisplayName("domainCode 필터로 기안 목록 조회 성공 (레거시)")
        void getDiagnosticList_WithDomainCodeFilter_Legacy_Success() {
            // given
            Domain envDomain = mock(Domain.class);
            when(envDomain.getDomainId()).thenReturn(1L);
            when(envDomain.getCode()).thenReturn("ENV");
            when(envDomain.getName()).thenReturn("환경");

            Diagnostic diagnostic = mock(Diagnostic.class);
            lenient().when(diagnostic.getDiagnosticId()).thenReturn(1L);
            lenient().when(diagnostic.getDiagnosticCode()).thenReturn("DG-2026-00001");
            lenient().when(diagnostic.getTitle()).thenReturn("2026년 ESG 자가진단");
            lenient().when(diagnostic.getCampaign()).thenReturn(testCampaign);
            lenient().when(diagnostic.getCompany()).thenReturn(testCompany);
            lenient().when(diagnostic.getDomain()).thenReturn(envDomain);
            lenient().when(diagnostic.getStatus()).thenReturn(DiagnosticStatus.WRITING);
            lenient().when(diagnostic.getPeriodStartDate()).thenReturn(LocalDate.of(2026, 1, 1));
            lenient().when(diagnostic.getPeriodEndDate()).thenReturn(LocalDate.of(2026, 12, 31));
            lenient().when(diagnostic.getDeadline()).thenReturn(LocalDate.of(2026, 3, 31));
            lenient().when(diagnostic.getQualitativeProgress()).thenReturn(0);
            lenient().when(diagnostic.getQuantitativeProgress()).thenReturn(0);
            lenient().when(diagnostic.getOverallProgress()).thenReturn(0);
            lenient().when(diagnostic.getCreatedAt()).thenReturn(LocalDateTime.now());

            Page<Diagnostic> diagnosticPage = new PageImpl<>(List.of(diagnostic), PageRequest.of(0, 10), 1);

            given(userRepository.findById(1L)).willReturn(Optional.of(drafterUser));
            given(domainRepository.findByCode("ENV")).willReturn(Optional.of(envDomain));
            given(diagnosticRepository.findByDrafterIdAndFilters(eq(1L), eq(true), eq(envDomain), eq(false), any(), any(), any(), any(), any()))
                    .willReturn(diagnosticPage);

            // when
            DiagnosticListResponse response = diagnosticService.getDiagnosticList(1L, "ENV", null, null, null, null, 0, 10);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).getDomain()).isNotNull();
            assertThat(response.getContent().get(0).getDomain().getCode()).isEqualTo("ENV");
            assertThat(response.getContent().get(0).getDomain().getName()).isEqualTo("환경");
        }

        @Test
        @DisplayName("상세 조회 시 도메인 정보가 응답에 포함됨")
        void getDiagnosticDetail_IncludesDomainInfo() {
            // given
            Domain envDomain = mock(Domain.class);
            when(envDomain.getDomainId()).thenReturn(1L);
            when(envDomain.getCode()).thenReturn("ENV");
            when(envDomain.getName()).thenReturn("환경");

            Diagnostic diagnostic = mock(Diagnostic.class);
            when(diagnostic.getDiagnosticId()).thenReturn(1L);
            when(diagnostic.getDiagnosticCode()).thenReturn("DG-2026-00001");
            when(diagnostic.getCampaign()).thenReturn(testCampaign);
            when(diagnostic.getCompany()).thenReturn(testCompany);
            when(diagnostic.getDomain()).thenReturn(envDomain);
            when(diagnostic.getDrafterId()).thenReturn(1L);
            when(diagnostic.getStatus()).thenReturn(DiagnosticStatus.WRITING);
            when(diagnostic.getPeriodStartDate()).thenReturn(LocalDate.of(2026, 1, 1));
            when(diagnostic.getPeriodEndDate()).thenReturn(LocalDate.of(2026, 12, 31));
            when(diagnostic.getDeadline()).thenReturn(LocalDate.of(2026, 3, 31));
            when(diagnostic.getQualitativeProgress()).thenReturn(50);
            when(diagnostic.getQuantitativeProgress()).thenReturn(30);
            when(diagnostic.getOverallProgress()).thenReturn(40);
            when(diagnostic.getCreatedAt()).thenReturn(LocalDateTime.now());

            // 도메인 기반 접근 검증을 통과하도록 설정
            when(drafterUser.hasAnyRoleInDomain("ENV", "DRAFTER", "APPROVER", "REVIEWER")).thenReturn(true);
            when(drafterUser.hasRoleInDomain("ENV", "DRAFTER")).thenReturn(true);

            given(userRepository.findById(1L)).willReturn(Optional.of(drafterUser));
            given(diagnosticRepository.findById(1L)).willReturn(Optional.of(diagnostic));

            // when
            DiagnosticDetailResponse response = diagnosticService.getDiagnosticDetail(1L, 1L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getDomain()).isNotNull();
            assertThat(response.getDomain().getDomainId()).isEqualTo(1L);
            assertThat(response.getDomain().getCode()).isEqualTo("ENV");
            assertThat(response.getDomain().getName()).isEqualTo("환경");
        }

        @Test
        @DisplayName("keyword 필터로 기안 목록 검색 성공 (레거시)")
        void getDiagnosticList_WithKeywordFilter_Legacy_Success() {
            // given
            Diagnostic diagnostic = mock(Diagnostic.class);
            lenient().when(diagnostic.getDiagnosticId()).thenReturn(1L);
            lenient().when(diagnostic.getDiagnosticCode()).thenReturn("DG-2026-00001");
            lenient().when(diagnostic.getTitle()).thenReturn("2026년 ESG 자가진단");
            lenient().when(diagnostic.getCampaign()).thenReturn(testCampaign);
            lenient().when(diagnostic.getCompany()).thenReturn(testCompany);
            lenient().when(diagnostic.getStatus()).thenReturn(DiagnosticStatus.WRITING);
            lenient().when(diagnostic.getPeriodStartDate()).thenReturn(LocalDate.of(2026, 1, 1));
            lenient().when(diagnostic.getPeriodEndDate()).thenReturn(LocalDate.of(2026, 12, 31));
            lenient().when(diagnostic.getDeadline()).thenReturn(LocalDate.of(2026, 3, 31));
            lenient().when(diagnostic.getQualitativeProgress()).thenReturn(0);
            lenient().when(diagnostic.getQuantitativeProgress()).thenReturn(0);
            lenient().when(diagnostic.getOverallProgress()).thenReturn(0);
            lenient().when(diagnostic.getCreatedAt()).thenReturn(LocalDateTime.now());

            Page<Diagnostic> diagnosticPage = new PageImpl<>(List.of(diagnostic), PageRequest.of(0, 10), 1);

            given(userRepository.findById(1L)).willReturn(Optional.of(drafterUser));
            given(diagnosticRepository.findByDrafterIdAndFilters(
                    eq(1L), eq(false), any(), eq(false), any(),
                    eq("ESG"), any(), any(), any()))
                    .willReturn(diagnosticPage);

            // when
            DiagnosticListResponse response = diagnosticService.getDiagnosticList(1L, null, null, "ESG", null, null, 0, 10);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("status 필터로 기안 목록 조회 성공 (레거시)")
        void getDiagnosticList_WithStatusFilter_Legacy_Success() {
            // given
            Diagnostic diagnostic = mock(Diagnostic.class);
            lenient().when(diagnostic.getDiagnosticId()).thenReturn(1L);
            lenient().when(diagnostic.getDiagnosticCode()).thenReturn("DG-2026-00001");
            lenient().when(diagnostic.getTitle()).thenReturn("2026년 ESG 자가진단");
            lenient().when(diagnostic.getCampaign()).thenReturn(testCampaign);
            lenient().when(diagnostic.getCompany()).thenReturn(testCompany);
            lenient().when(diagnostic.getStatus()).thenReturn(DiagnosticStatus.WRITING);
            lenient().when(diagnostic.getPeriodStartDate()).thenReturn(LocalDate.of(2026, 1, 1));
            lenient().when(diagnostic.getPeriodEndDate()).thenReturn(LocalDate.of(2026, 12, 31));
            lenient().when(diagnostic.getDeadline()).thenReturn(LocalDate.of(2026, 3, 31));
            lenient().when(diagnostic.getQualitativeProgress()).thenReturn(0);
            lenient().when(diagnostic.getQuantitativeProgress()).thenReturn(0);
            lenient().when(diagnostic.getOverallProgress()).thenReturn(0);
            lenient().when(diagnostic.getCreatedAt()).thenReturn(LocalDateTime.now());

            Page<Diagnostic> diagnosticPage = new PageImpl<>(List.of(diagnostic), PageRequest.of(0, 10), 1);

            given(userRepository.findById(1L)).willReturn(Optional.of(drafterUser));
            given(diagnosticRepository.findByDrafterIdAndFilters(
                    eq(1L), eq(false), any(), eq(true), any(),
                    any(), any(), any(), any()))
                    .willReturn(diagnosticPage);

            // when
            DiagnosticListResponse response = diagnosticService.getDiagnosticList(1L, null, "WRITING", null, null, null, 0, 10);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).getStatus()).isEqualTo("WRITING");
        }

        @Test
        @DisplayName("도메인 역할 DRAFTER는 본인 기안만 반환")
        void getDiagnosticList_DomainDrafter_ReturnsOwnOnly() {
            // given
            Domain esgDomain = mock(Domain.class);
            lenient().when(esgDomain.getDomainId()).thenReturn(1L);
            lenient().when(esgDomain.getCode()).thenReturn("ESG");
            lenient().when(esgDomain.getName()).thenReturn("ESG 실사");

            Role dRole = mock(Role.class);
            lenient().when(dRole.getCode()).thenReturn("DRAFTER");
            UserDomainRole udr = mock(UserDomainRole.class);
            lenient().when(udr.getDomain()).thenReturn(esgDomain);
            lenient().when(udr.getRole()).thenReturn(dRole);

            User domainDrafter = mock(User.class);
            lenient().when(domainDrafter.getUserId()).thenReturn(50L);
            lenient().when(domainDrafter.getCompany()).thenReturn(testCompany);
            lenient().when(domainDrafter.getDomainRoles()).thenReturn(List.of(udr));

            Diagnostic diagnostic = mock(Diagnostic.class);
            lenient().when(diagnostic.getDiagnosticId()).thenReturn(1L);
            lenient().when(diagnostic.getDiagnosticCode()).thenReturn("DG-2026-00001");
            lenient().when(diagnostic.getTitle()).thenReturn("테스트");
            lenient().when(diagnostic.getCampaign()).thenReturn(testCampaign);
            lenient().when(diagnostic.getCompany()).thenReturn(testCompany);
            lenient().when(diagnostic.getDomain()).thenReturn(esgDomain);
            lenient().when(diagnostic.getStatus()).thenReturn(DiagnosticStatus.WRITING);
            lenient().when(diagnostic.getPeriodStartDate()).thenReturn(LocalDate.of(2026, 1, 1));
            lenient().when(diagnostic.getPeriodEndDate()).thenReturn(LocalDate.of(2026, 12, 31));
            lenient().when(diagnostic.getDeadline()).thenReturn(LocalDate.of(2026, 3, 31));
            lenient().when(diagnostic.getQualitativeProgress()).thenReturn(0);
            lenient().when(diagnostic.getQuantitativeProgress()).thenReturn(0);
            lenient().when(diagnostic.getOverallProgress()).thenReturn(0);
            lenient().when(diagnostic.getCreatedAt()).thenReturn(LocalDateTime.now());

            Page<Diagnostic> page = new PageImpl<>(List.of(diagnostic), PageRequest.of(0, 10), 1);

            given(userRepository.findById(50L)).willReturn(Optional.of(domainDrafter));
            // findByFilters 호출 시 hasDrafter=true, drafterId=50L 전달 검증
            given(diagnosticRepository.findByFilters(
                    any(), any(), any(),
                    eq(false), eq(false), eq(true),
                    eq(testCompany), eq(50L),
                    eq(false), any(), any(), any(), any(), any()))
                    .willReturn(page);

            // when
            DiagnosticListResponse response = diagnosticService.getDiagnosticList(50L, null, null, null, null, null, 0, 10);

            // then
            assertThat(response.getContent()).hasSize(1);
            // DRAFTER 도메인만 활성화되었는지 검증
            verify(diagnosticRepository).findByFilters(
                    any(), any(), any(),
                    eq(false), eq(false), eq(true),
                    eq(testCompany), eq(50L),
                    eq(false), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("도메인 역할 REVIEWER는 해당 도메인 전체 건 반환")
        void getDiagnosticList_DomainReviewer_ReturnsAll() {
            // given
            Domain esgDomain = mock(Domain.class);
            lenient().when(esgDomain.getCode()).thenReturn("ESG");

            Role rRole = mock(Role.class);
            lenient().when(rRole.getCode()).thenReturn("REVIEWER");
            UserDomainRole udr = mock(UserDomainRole.class);
            lenient().when(udr.getDomain()).thenReturn(esgDomain);
            lenient().when(udr.getRole()).thenReturn(rRole);

            User domainReviewer = mock(User.class);
            lenient().when(domainReviewer.getUserId()).thenReturn(60L);
            lenient().when(domainReviewer.getCompany()).thenReturn(null);
            lenient().when(domainReviewer.getDomainRoles()).thenReturn(List.of(udr));

            Page<Diagnostic> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

            given(userRepository.findById(60L)).willReturn(Optional.of(domainReviewer));
            given(diagnosticRepository.findByFilters(
                    any(), any(), any(),
                    eq(true), eq(false), eq(false),
                    any(), eq(60L),
                    eq(false), any(), any(), any(), any(), any()))
                    .willReturn(emptyPage);

            // when
            DiagnosticListResponse response = diagnosticService.getDiagnosticList(60L, null, null, null, null, null, 0, 10);

            // then
            assertThat(response).isNotNull();
            verify(diagnosticRepository).findByFilters(
                    any(), any(), any(),
                    eq(true), eq(false), eq(false),
                    any(), eq(60L),
                    eq(false), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("권한 없는 도메인 접근 시 빈 목록 반환")
        void getDiagnosticList_NoDomainAccess_ReturnsEmpty() {
            // given: ESG DRAFTER만 가진 사용자가 SAFETY 도메인 조회
            Domain esgDomain = mock(Domain.class);
            lenient().when(esgDomain.getCode()).thenReturn("ESG");

            Role dRole = mock(Role.class);
            lenient().when(dRole.getCode()).thenReturn("DRAFTER");
            UserDomainRole udr = mock(UserDomainRole.class);
            lenient().when(udr.getDomain()).thenReturn(esgDomain);
            lenient().when(udr.getRole()).thenReturn(dRole);

            User user = mock(User.class);
            lenient().when(user.getUserId()).thenReturn(70L);
            lenient().when(user.getCompany()).thenReturn(testCompany);
            lenient().when(user.getDomainRoles()).thenReturn(List.of(udr));

            given(userRepository.findById(70L)).willReturn(Optional.of(user));

            // when — SAFETY 필터지만 ESG DRAFTER만 보유
            DiagnosticListResponse response = diagnosticService.getDiagnosticList(70L, "SAFETY", null, null, null, null, 0, 10);

            // then — 빈 결과, 쿼리 미실행
            assertThat(response.getContent()).isEmpty();
            assertThat(response.getPage().getTotalElements()).isEqualTo(0);
            verifyNoInteractions(diagnosticRepository);
        }

        @Test
        @DisplayName("레거시 DRAFTER는 본인 기안만 조회 (회사 전체X)")
        void getDiagnosticList_LegacyDrafter_ReturnsOwnOnly() {
            // given
            Page<Diagnostic> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

            given(userRepository.findById(1L)).willReturn(Optional.of(drafterUser));
            given(diagnosticRepository.findByDrafterIdAndFilters(
                    eq(1L), eq(false), any(), eq(false), any(), any(), any(), any(), any()))
                    .willReturn(emptyPage);

            // when
            DiagnosticListResponse response = diagnosticService.getDiagnosticList(1L, null, null, null, null, null, 0, 10);

            // then
            assertThat(response).isNotNull();
            verify(diagnosticRepository).findByDrafterIdAndFilters(
                    eq(1L), eq(false), any(), eq(false), any(), any(), any(), any(), any());
            verify(diagnosticRepository, never()).findByCompanyAndFilters(any(), anyBoolean(), any(), anyBoolean(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("존재하지 않는 domainCode 필터 시 DOMAIN_NOT_FOUND")
        void getDiagnosticList_InvalidDomainCode_ThrowsException() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(drafterUser));
            given(domainRepository.findByCode("INVALID")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> diagnosticService.getDiagnosticList(1L, "INVALID", null, null, null, null, 0, 10))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.DOMAIN_NOT_FOUND);
                    });
        }
    }

    @Nested
    @DisplayName("기안 삭제 테스트")
    class DeleteDiagnosticTest {

        @Test
        @DisplayName("WRITING 상태의 본인 기안 삭제 성공")
        void deleteDiagnostic_WritingStatus_Success() {
            // given
            Diagnostic diagnostic = mock(Diagnostic.class);
            lenient().when(diagnostic.getDiagnosticId()).thenReturn(100L);
            when(diagnostic.getDrafterId()).thenReturn(1L);
            when(diagnostic.getStatus()).thenReturn(DiagnosticStatus.WRITING);

            given(userRepository.findById(1L)).willReturn(Optional.of(drafterUser));
            given(diagnosticRepository.findById(100L)).willReturn(Optional.of(diagnostic));

            // when
            diagnosticService.deleteDiagnostic(1L, 100L);

            // then
            verify(diagnosticRepository).delete(diagnostic);
        }

        @Test
        @DisplayName("본인 기안이 아닌 경우 DIAGNOSTIC_NOT_OWNER 에러")
        void deleteDiagnostic_NotOwner_ThrowsException() {
            // given
            Diagnostic diagnostic = mock(Diagnostic.class);
            when(diagnostic.getDrafterId()).thenReturn(999L); // 다른 사용자

            given(userRepository.findById(1L)).willReturn(Optional.of(drafterUser));
            given(diagnosticRepository.findById(100L)).willReturn(Optional.of(diagnostic));

            // when & then
            assertThatThrownBy(() -> diagnosticService.deleteDiagnostic(1L, 100L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.DIAGNOSTIC_NOT_OWNER);
                    });

            verify(diagnosticRepository, never()).delete(any());
        }

        @Test
        @DisplayName("SUBMITTED 상태 기안 삭제 시 DIAGNOSTIC_DELETE_NOT_ALLOWED 에러")
        void deleteDiagnostic_SubmittedStatus_ThrowsException() {
            // given
            Diagnostic diagnostic = mock(Diagnostic.class);
            when(diagnostic.getDrafterId()).thenReturn(1L);
            when(diagnostic.getStatus()).thenReturn(DiagnosticStatus.SUBMITTED);

            given(userRepository.findById(1L)).willReturn(Optional.of(drafterUser));
            given(diagnosticRepository.findById(100L)).willReturn(Optional.of(diagnostic));

            // when & then
            assertThatThrownBy(() -> diagnosticService.deleteDiagnostic(1L, 100L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.DIAGNOSTIC_DELETE_NOT_ALLOWED);
                    });

            verify(diagnosticRepository, never()).delete(any());
        }

        @Test
        @DisplayName("존재하지 않는 기안 삭제 시 DIAGNOSTIC_NOT_FOUND 에러")
        void deleteDiagnostic_NotFound_ThrowsException() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(drafterUser));
            given(diagnosticRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> diagnosticService.deleteDiagnostic(1L, 999L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.DIAGNOSTIC_NOT_FOUND);
                    });
        }

        @Test
        @DisplayName("APPROVED 상태 기안 삭제 시 DIAGNOSTIC_DELETE_NOT_ALLOWED 에러")
        void deleteDiagnostic_ApprovedStatus_ThrowsException() {
            // given
            Diagnostic diagnostic = mock(Diagnostic.class);
            when(diagnostic.getDrafterId()).thenReturn(1L);
            when(diagnostic.getStatus()).thenReturn(DiagnosticStatus.APPROVED);

            given(userRepository.findById(1L)).willReturn(Optional.of(drafterUser));
            given(diagnosticRepository.findById(100L)).willReturn(Optional.of(diagnostic));

            // when & then
            assertThatThrownBy(() -> diagnosticService.deleteDiagnostic(1L, 100L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.DIAGNOSTIC_DELETE_NOT_ALLOWED);
                    });
        }
    }

    @Nested
    @DisplayName("캠페인 상태 일관성 테스트 (Issue #156)")
    class CampaignStatusConsistencyTest {

        @Test
        @DisplayName("기안이 REVIEWING 상태면 캠페인도 '진행중' 표시")
        void getDiagnosticList_ReviewingStatus_CampaignShowsActive() {
            // given
            Diagnostic diagnostic = mock(Diagnostic.class);
            lenient().when(diagnostic.getDiagnosticId()).thenReturn(1L);
            lenient().when(diagnostic.getDiagnosticCode()).thenReturn("DG-2026-00001");
            lenient().when(diagnostic.getTitle()).thenReturn("2026년 ESG 자가진단");
            lenient().when(diagnostic.getCampaign()).thenReturn(testCampaign);
            lenient().when(diagnostic.getCompany()).thenReturn(testCompany);
            lenient().when(diagnostic.getStatus()).thenReturn(DiagnosticStatus.REVIEWING);  // 심사중
            lenient().when(diagnostic.getPeriodStartDate()).thenReturn(LocalDate.of(2026, 1, 1));
            lenient().when(diagnostic.getPeriodEndDate()).thenReturn(LocalDate.of(2026, 12, 31));
            lenient().when(diagnostic.getDeadline()).thenReturn(LocalDate.of(2026, 3, 31));
            lenient().when(diagnostic.getQualitativeProgress()).thenReturn(100);
            lenient().when(diagnostic.getQuantitativeProgress()).thenReturn(100);
            lenient().when(diagnostic.getOverallProgress()).thenReturn(100);
            lenient().when(diagnostic.getCreatedAt()).thenReturn(LocalDateTime.now());

            Page<Diagnostic> diagnosticPage = new PageImpl<>(List.of(diagnostic), PageRequest.of(0, 10), 1);

            given(userRepository.findById(1L)).willReturn(Optional.of(drafterUser));
            given(diagnosticRepository.findByDrafterIdAndFilters(eq(1L), eq(false), any(), eq(false), any(), any(), any(), any(), any()))
                    .willReturn(diagnosticPage);

            // when
            DiagnosticListResponse response = diagnosticService.getDiagnosticList(1L, null, null, null, null, null, 0, 10);

            // then
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).getStatus()).isEqualTo("REVIEWING");
            assertThat(response.getContent().get(0).getStatusLabel()).isEqualTo("심사중");
            // 캠페인 상태도 기안 상태와 일관되게 "진행중"이어야 함 (Issue #156)
            assertThat(response.getContent().get(0).getCampaign()).isNotNull();
            assertThat(response.getContent().get(0).getCampaign().getStatus()).isEqualTo("ACTIVE");
            assertThat(response.getContent().get(0).getCampaign().getStatusLabel()).isEqualTo("진행중");
        }

        @Test
        @DisplayName("기안이 COMPLETED 상태면 캠페인도 '완료' 표시")
        void getDiagnosticList_CompletedStatus_CampaignShowsCompleted() {
            // given
            Diagnostic diagnostic = mock(Diagnostic.class);
            lenient().when(diagnostic.getDiagnosticId()).thenReturn(1L);
            lenient().when(diagnostic.getDiagnosticCode()).thenReturn("DG-2026-00001");
            lenient().when(diagnostic.getTitle()).thenReturn("2026년 ESG 자가진단");
            lenient().when(diagnostic.getCampaign()).thenReturn(testCampaign);
            lenient().when(diagnostic.getCompany()).thenReturn(testCompany);
            lenient().when(diagnostic.getStatus()).thenReturn(DiagnosticStatus.COMPLETED);  // 완료
            lenient().when(diagnostic.getPeriodStartDate()).thenReturn(LocalDate.of(2026, 1, 1));
            lenient().when(diagnostic.getPeriodEndDate()).thenReturn(LocalDate.of(2026, 12, 31));
            lenient().when(diagnostic.getDeadline()).thenReturn(LocalDate.of(2026, 3, 31));
            lenient().when(diagnostic.getQualitativeProgress()).thenReturn(100);
            lenient().when(diagnostic.getQuantitativeProgress()).thenReturn(100);
            lenient().when(diagnostic.getOverallProgress()).thenReturn(100);
            lenient().when(diagnostic.getCreatedAt()).thenReturn(LocalDateTime.now());

            Page<Diagnostic> diagnosticPage = new PageImpl<>(List.of(diagnostic), PageRequest.of(0, 10), 1);

            given(userRepository.findById(1L)).willReturn(Optional.of(drafterUser));
            given(diagnosticRepository.findByDrafterIdAndFilters(eq(1L), eq(false), any(), eq(false), any(), any(), any(), any(), any()))
                    .willReturn(diagnosticPage);

            // when
            DiagnosticListResponse response = diagnosticService.getDiagnosticList(1L, null, null, null, null, null, 0, 10);

            // then
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).getStatus()).isEqualTo("COMPLETED");
            assertThat(response.getContent().get(0).getStatusLabel()).isEqualTo("완료");
            // 캠페인 상태도 기안 상태와 일관되게 "완료"이어야 함 (Issue #156)
            assertThat(response.getContent().get(0).getCampaign()).isNotNull();
            assertThat(response.getContent().get(0).getCampaign().getStatus()).isEqualTo("COMPLETED");
            assertThat(response.getContent().get(0).getCampaign().getStatusLabel()).isEqualTo("완료");
        }

        @Test
        @DisplayName("기안이 WRITING 상태면 캠페인도 '진행중' 표시")
        void getDiagnosticList_WritingStatus_CampaignShowsActive() {
            // given
            Diagnostic diagnostic = mock(Diagnostic.class);
            lenient().when(diagnostic.getDiagnosticId()).thenReturn(1L);
            lenient().when(diagnostic.getDiagnosticCode()).thenReturn("DG-2026-00001");
            lenient().when(diagnostic.getTitle()).thenReturn("2026년 ESG 자가진단");
            lenient().when(diagnostic.getCampaign()).thenReturn(testCampaign);
            lenient().when(diagnostic.getCompany()).thenReturn(testCompany);
            lenient().when(diagnostic.getStatus()).thenReturn(DiagnosticStatus.WRITING);  // 작성중
            lenient().when(diagnostic.getPeriodStartDate()).thenReturn(LocalDate.of(2026, 1, 1));
            lenient().when(diagnostic.getPeriodEndDate()).thenReturn(LocalDate.of(2026, 12, 31));
            lenient().when(diagnostic.getDeadline()).thenReturn(LocalDate.of(2026, 3, 31));
            lenient().when(diagnostic.getQualitativeProgress()).thenReturn(0);
            lenient().when(diagnostic.getQuantitativeProgress()).thenReturn(0);
            lenient().when(diagnostic.getOverallProgress()).thenReturn(0);
            lenient().when(diagnostic.getCreatedAt()).thenReturn(LocalDateTime.now());

            Page<Diagnostic> diagnosticPage = new PageImpl<>(List.of(diagnostic), PageRequest.of(0, 10), 1);

            given(userRepository.findById(1L)).willReturn(Optional.of(drafterUser));
            given(diagnosticRepository.findByDrafterIdAndFilters(eq(1L), eq(false), any(), eq(false), any(), any(), any(), any(), any()))
                    .willReturn(diagnosticPage);

            // when
            DiagnosticListResponse response = diagnosticService.getDiagnosticList(1L, null, null, null, null, null, 0, 10);

            // then
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).getStatus()).isEqualTo("WRITING");
            // 캠페인 상태는 "진행중" (기안이 아직 완료되지 않음)
            assertThat(response.getContent().get(0).getCampaign()).isNotNull();
            assertThat(response.getContent().get(0).getCampaign().getStatus()).isEqualTo("ACTIVE");
            assertThat(response.getContent().get(0).getCampaign().getStatusLabel()).isEqualTo("진행중");
        }
    }
}
