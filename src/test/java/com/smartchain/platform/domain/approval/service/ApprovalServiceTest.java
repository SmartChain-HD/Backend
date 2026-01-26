package com.smartchain.platform.domain.approval.service;

import com.smartchain.platform.domain.approval.entity.Approval;
import com.smartchain.platform.domain.approval.repository.ApprovalRepository;
import com.smartchain.platform.domain.diagnostic.entity.Diagnostic;
import com.smartchain.platform.domain.user.entity.Company;
import com.smartchain.platform.domain.user.entity.Role;
import com.smartchain.platform.domain.user.entity.User;
import com.smartchain.platform.domain.user.repository.UserRepository;
import com.smartchain.platform.dto.approval.decision.ApprovalDecisionRequest;
import com.smartchain.platform.dto.approval.decision.ApprovalDecisionResponse;
import com.smartchain.platform.dto.approval.detail.ApprovalDetailResponse;
import com.smartchain.platform.dto.approval.list.ApprovalListResponse;
import com.smartchain.platform.dto.approval.submit.SubmitToReviewerRequest;
import com.smartchain.platform.dto.approval.submit.SubmitToReviewerResponse;
import com.smartchain.platform.global.enums.ApprovalStatus;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApprovalServiceTest {

    @InjectMocks
    private ApprovalService approvalService;

    @Mock
    private ApprovalRepository approvalRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private User approverUser;

    @Mock
    private User requesterUser;

    @Mock
    private Company testCompany;

    @Mock
    private Role approverRole;

    @Mock
    private Role guestRole;

    @Mock
    private Diagnostic testDiagnostic;

    @Mock
    private Approval testApproval;

    @BeforeEach
    void setUp() {
        lenient().when(approverRole.getCode()).thenReturn("APPROVER");
        lenient().when(guestRole.getCode()).thenReturn("GUEST");

        lenient().when(approverUser.getUserId()).thenReturn(1L);
        lenient().when(approverUser.getName()).thenReturn("결재자");
        lenient().when(approverUser.getEmail()).thenReturn("approver@test.com");
        lenient().when(approverUser.getRole()).thenReturn(approverRole);
        lenient().when(approverUser.getCompany()).thenReturn(testCompany);

        lenient().when(requesterUser.getUserId()).thenReturn(2L);
        lenient().when(requesterUser.getName()).thenReturn("기안자");
        lenient().when(requesterUser.getEmail()).thenReturn("drafter@test.com");

        lenient().when(testCompany.getCompanyId()).thenReturn(10L);
        lenient().when(testCompany.getName()).thenReturn("(주)테스트회사");

        lenient().when(testDiagnostic.getDiagnosticId()).thenReturn(100L);
        lenient().when(testDiagnostic.getDiagnosticCode()).thenReturn("DG-2026-00001");
        lenient().when(testDiagnostic.getTitle()).thenReturn("2026년 상반기 ESG 자가진단");
        lenient().when(testDiagnostic.getCompany()).thenReturn(testCompany);
        lenient().when(testDiagnostic.getQualitativeProgress()).thenReturn(100);
        lenient().when(testDiagnostic.getQuantitativeProgress()).thenReturn(100);
        lenient().when(testDiagnostic.getOverallScore()).thenReturn(72);

        lenient().when(testApproval.getApprovalId()).thenReturn(1L);
        lenient().when(testApproval.getDiagnostic()).thenReturn(testDiagnostic);
        lenient().when(testApproval.getRequester()).thenReturn(requesterUser);
        lenient().when(testApproval.getStatus()).thenReturn(ApprovalStatus.WAITING);
        lenient().when(testApproval.getDeadline()).thenReturn(LocalDate.now().plusDays(7));
        lenient().when(testApproval.getCreatedAt()).thenReturn(LocalDateTime.now());
    }

    @Nested
    @DisplayName("결재 대기 목록 조회 테스트")
    class GetApprovalListTest {

        @Test
        @DisplayName("APPROVER가 결재 목록 조회 성공")
        void getApprovalList_AsApprover_Success() {
            // given
            Page<Approval> approvalPage = new PageImpl<>(List.of(testApproval), PageRequest.of(0, 10), 1);

            given(userRepository.findById(1L)).willReturn(Optional.of(approverUser));
            given(approvalRepository.findByCompanyOrderByCreatedAtDesc(eq(testCompany), any()))
                    .willReturn(approvalPage);
            given(approvalRepository.countByCompanyAndStatus(testCompany, ApprovalStatus.WAITING)).willReturn(3);
            given(approvalRepository.countByCompanyAndStatus(testCompany, ApprovalStatus.APPROVED)).willReturn(10);
            given(approvalRepository.countByCompanyAndStatus(testCompany, ApprovalStatus.REJECTED)).willReturn(2);

            // when
            ApprovalListResponse response = approvalService.getApprovalList(1L, null, 0, 10);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getStats().getWaiting()).isEqualTo(3);
            assertThat(response.getStats().getApproved()).isEqualTo(10);
            assertThat(response.getStats().getRejected()).isEqualTo(2);
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).getApprovalId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("권한이 없는 사용자가 목록 조회 시 실패")
        void getApprovalList_AsGuest_ThrowsException() {
            // given
            User guestUser = mock(User.class);
            when(guestUser.getRole()).thenReturn(guestRole);

            given(userRepository.findById(3L)).willReturn(Optional.of(guestUser));

            // when & then
            assertThatThrownBy(() -> approvalService.getApprovalList(3L, null, 0, 10))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.PERMISSION_DENIED_ACTION);
                    });
        }

        @Test
        @DisplayName("회사 정보가 없는 APPROVER가 목록 조회 시 실패")
        void getApprovalList_NoCompany_ThrowsException() {
            // given
            User noCompanyApprover = mock(User.class);
            when(noCompanyApprover.getRole()).thenReturn(approverRole);
            when(noCompanyApprover.getCompany()).thenReturn(null);

            given(userRepository.findById(4L)).willReturn(Optional.of(noCompanyApprover));

            // when & then
            assertThatThrownBy(() -> approvalService.getApprovalList(4L, null, 0, 10))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.PERMISSION_DENIED_RESOURCE);
                    });
        }

        @Test
        @DisplayName("상태 필터로 결재 목록 조회 성공 (WAITING)")
        void getApprovalList_WithStatusFilter_Success() {
            // given
            Page<Approval> approvalPage = new PageImpl<>(List.of(testApproval), PageRequest.of(0, 10), 1);

            given(userRepository.findById(1L)).willReturn(Optional.of(approverUser));
            given(approvalRepository.findByCompanyAndStatusOrderByCreatedAtDesc(eq(testCompany), eq(ApprovalStatus.WAITING), any()))
                    .willReturn(approvalPage);
            given(approvalRepository.countByCompanyAndStatus(testCompany, ApprovalStatus.WAITING)).willReturn(3);
            given(approvalRepository.countByCompanyAndStatus(testCompany, ApprovalStatus.APPROVED)).willReturn(10);
            given(approvalRepository.countByCompanyAndStatus(testCompany, ApprovalStatus.REJECTED)).willReturn(2);

            // when
            ApprovalListResponse response = approvalService.getApprovalList(1L, "WAITING", 0, 10);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getStats().getWaiting()).isEqualTo(3);
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).getStatus()).isEqualTo("WAITING");
            verify(approvalRepository).findByCompanyAndStatusOrderByCreatedAtDesc(eq(testCompany), eq(ApprovalStatus.WAITING), any());
        }

        @Test
        @DisplayName("잘못된 상태 필터로 조회 시 실패")
        void getApprovalList_InvalidStatus_ThrowsException() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(approverUser));

            // when & then
            assertThatThrownBy(() -> approvalService.getApprovalList(1L, "INVALID_STATUS", 0, 10))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT);
                    });
        }
    }

    @Nested
    @DisplayName("결재 상세 조회 테스트")
    class GetApprovalDetailTest {

        @Test
        @DisplayName("결재 상세 조회 성공")
        void getApprovalDetail_Success() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(approverUser));
            given(approvalRepository.findById(1L)).willReturn(Optional.of(testApproval));

            // when
            ApprovalDetailResponse response = approvalService.getApprovalDetail(1L, 1L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getApprovalId()).isEqualTo(1L);
            assertThat(response.getDiagnostic().getDiagnosticId()).isEqualTo(100L);
            assertThat(response.getDiagnostic().getDiagnosticCode()).isEqualTo("DG-2026-00001");
            assertThat(response.getRequester().getUserId()).isEqualTo(2L);
            assertThat(response.getStatus()).isEqualTo("WAITING");
        }

        @Test
        @DisplayName("존재하지 않는 결재 조회 시 실패")
        void getApprovalDetail_NotFound_ThrowsException() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(approverUser));
            given(approvalRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> approvalService.getApprovalDetail(1L, 999L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.APPROVAL_NOT_FOUND);
                    });
        }

        @Test
        @DisplayName("다른 회사의 결재 조회 시 실패")
        void getApprovalDetail_DifferentCompany_ThrowsException() {
            // given
            Company otherCompany = mock(Company.class);
            when(otherCompany.getCompanyId()).thenReturn(999L);

            Diagnostic otherDiagnostic = mock(Diagnostic.class);
            when(otherDiagnostic.getCompany()).thenReturn(otherCompany);

            Approval otherApproval = mock(Approval.class);
            when(otherApproval.getDiagnostic()).thenReturn(otherDiagnostic);

            given(userRepository.findById(1L)).willReturn(Optional.of(approverUser));
            given(approvalRepository.findById(2L)).willReturn(Optional.of(otherApproval));

            // when & then
            assertThatThrownBy(() -> approvalService.getApprovalDetail(1L, 2L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.PERMISSION_DENIED_RESOURCE);
                    });
        }
    }

    @Nested
    @DisplayName("결재 처리 테스트")
    class ProcessApprovalTest {

        @Test
        @DisplayName("결재 승인 성공")
        void processApproval_Approve_Success() {
            // given
            when(testApproval.isWaiting()).thenReturn(true);
            when(testApproval.getStatus()).thenReturn(ApprovalStatus.APPROVED);
            when(testApproval.getProcessedAt()).thenReturn(LocalDateTime.now());

            ApprovalDecisionRequest request = ApprovalDecisionRequest.builder()
                    .decision("APPROVED")
                    .comment("검토 완료")
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(approverUser));
            given(approvalRepository.findById(1L)).willReturn(Optional.of(testApproval));

            // when
            ApprovalDecisionResponse response = approvalService.processApproval(1L, 1L, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo("APPROVED");
            assertThat(response.getDiagnosticNewStatus()).isEqualTo("APPROVED");
            assertThat(response.getMessage()).isEqualTo("결재가 완료되었습니다");
            verify(testApproval).approve(approverUser, "검토 완료");
            verify(testDiagnostic).markAsApproved();
        }

        @Test
        @DisplayName("결재 반려 성공")
        void processApproval_Reject_Success() {
            // given
            when(testApproval.isWaiting()).thenReturn(true);
            when(testApproval.getStatus()).thenReturn(ApprovalStatus.REJECTED);
            when(testApproval.getProcessedAt()).thenReturn(LocalDateTime.now());

            ApprovalDecisionRequest request = ApprovalDecisionRequest.builder()
                    .decision("REJECTED")
                    .comment("보완 필요")
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(approverUser));
            given(approvalRepository.findById(1L)).willReturn(Optional.of(testApproval));

            // when
            ApprovalDecisionResponse response = approvalService.processApproval(1L, 1L, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo("REJECTED");
            assertThat(response.getDiagnosticNewStatus()).isEqualTo("RETURNED");
            assertThat(response.getMessage()).isEqualTo("결재가 반려되었습니다");
            verify(testApproval).reject(approverUser, "보완 필요");
            verify(testDiagnostic).markAsReturned();
        }

        @Test
        @DisplayName("이미 처리된 결재를 다시 처리하려고 할 때 실패")
        void processApproval_AlreadyProcessed_ThrowsException() {
            // given
            when(testApproval.isWaiting()).thenReturn(false);

            ApprovalDecisionRequest request = ApprovalDecisionRequest.builder()
                    .decision("APPROVED")
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(approverUser));
            given(approvalRepository.findById(1L)).willReturn(Optional.of(testApproval));

            // when & then
            assertThatThrownBy(() -> approvalService.processApproval(1L, 1L, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.ALREADY_PROCESSED_APPROVAL);
                    });
        }

        @Test
        @DisplayName("유효하지 않은 결정으로 처리 시 실패")
        void processApproval_InvalidDecision_ThrowsException() {
            // given
            when(testApproval.isWaiting()).thenReturn(true);

            ApprovalDecisionRequest request = ApprovalDecisionRequest.builder()
                    .decision("INVALID")
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(approverUser));
            given(approvalRepository.findById(1L)).willReturn(Optional.of(testApproval));

            // when & then
            assertThatThrownBy(() -> approvalService.processApproval(1L, 1L, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.INVALID_DECISION);
                    });
        }
    }

    @Nested
    @DisplayName("원청 제출 테스트")
    class SubmitToReviewerTest {

        @Test
        @DisplayName("원청 제출 성공")
        void submitToReviewer_Success() {
            // given
            when(testApproval.isApproved()).thenReturn(true);
            when(testApproval.getSubmittedToReviewerAt()).thenReturn(LocalDateTime.now());
            when(testDiagnostic.isApproved()).thenReturn(true);
            when(testDiagnostic.getStatus()).thenReturn(DiagnosticStatus.APPROVED);

            SubmitToReviewerRequest request = SubmitToReviewerRequest.builder()
                    .submitComment("원청 제출합니다")
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(approverUser));
            given(approvalRepository.findById(1L)).willReturn(Optional.of(testApproval));

            // when
            SubmitToReviewerResponse response = approvalService.submitToReviewer(1L, 1L, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getApprovalId()).isEqualTo(1L);
            assertThat(response.getDiagnosticId()).isEqualTo(100L);
            assertThat(response.getPreviousStatus()).isEqualTo("APPROVED");
            assertThat(response.getNewStatus()).isEqualTo("REVIEWING");
            assertThat(response.getMessage()).isEqualTo("원청에 제출되었습니다");
            verify(testApproval).submitToReviewer();
            verify(testDiagnostic).markAsReviewing();
        }

        @Test
        @DisplayName("승인되지 않은 결재 제출 시 실패")
        void submitToReviewer_NotApproved_ThrowsException() {
            // given
            when(testApproval.isApproved()).thenReturn(false);

            SubmitToReviewerRequest request = new SubmitToReviewerRequest();

            given(userRepository.findById(1L)).willReturn(Optional.of(approverUser));
            given(approvalRepository.findById(1L)).willReturn(Optional.of(testApproval));

            // when & then
            assertThatThrownBy(() -> approvalService.submitToReviewer(1L, 1L, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.APPROVAL_NOT_APPROVED);
                    });
        }

        @Test
        @DisplayName("진단이 승인되지 않은 상태에서 제출 시 실패")
        void submitToReviewer_DiagnosticNotApproved_ThrowsException() {
            // given
            when(testApproval.isApproved()).thenReturn(true);
            when(testDiagnostic.isApproved()).thenReturn(false);

            SubmitToReviewerRequest request = new SubmitToReviewerRequest();

            given(userRepository.findById(1L)).willReturn(Optional.of(approverUser));
            given(approvalRepository.findById(1L)).willReturn(Optional.of(testApproval));

            // when & then
            assertThatThrownBy(() -> approvalService.submitToReviewer(1L, 1L, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.DIAGNOSTIC_NOT_APPROVED);
                    });
        }
    }
}
