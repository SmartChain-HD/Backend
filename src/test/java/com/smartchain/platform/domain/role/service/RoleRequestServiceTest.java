package com.smartchain.platform.domain.role.service;

import com.smartchain.platform.domain.role.repository.RoleRequestRepository;
import com.smartchain.platform.domain.user.entity.Company;
import com.smartchain.platform.domain.user.entity.Domain;
import com.smartchain.platform.domain.user.entity.Role;
import com.smartchain.platform.domain.user.entity.RoleRequest;
import com.smartchain.platform.domain.user.entity.User;
import com.smartchain.platform.domain.user.entity.UserDomainRole;
import com.smartchain.platform.domain.user.repository.CompanyRepository;
import com.smartchain.platform.domain.user.repository.DomainRepository;
import com.smartchain.platform.domain.user.repository.RoleRepository;
import com.smartchain.platform.domain.user.repository.UserDomainRoleRepository;
import com.smartchain.platform.domain.user.repository.UserRepository;
import com.smartchain.platform.dto.role.approval.RoleApprovalDetailResponse;
import com.smartchain.platform.dto.role.approval.RoleApprovalListResponse;
import com.smartchain.platform.dto.role.approval.RoleDecisionRequest;
import com.smartchain.platform.dto.role.approval.RoleDecisionResponse;
import com.smartchain.platform.dto.role.page.RoleRequestPageResponse;
import com.smartchain.platform.dto.role.request.RoleRequestCreateDto;
import com.smartchain.platform.dto.role.request.RoleRequestResponse;
import com.smartchain.platform.dto.role.request.RoleRequestStatusDto;
import com.smartchain.platform.global.enums.RequestStatus;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleRequestServiceTest {

    @InjectMocks
    private RoleRequestService roleRequestService;

    @Mock
    private RoleRequestRepository roleRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private DomainRepository domainRepository;

    @Mock
    private UserDomainRoleRepository userDomainRoleRepository;

    @Mock
    private User testUser;

    @Mock
    private Domain testDomain;

    @Mock
    private Company testCompany;

    @Mock
    private Role guestRole;

    @BeforeEach
    void setUp() {
        lenient().when(guestRole.getCode()).thenReturn("GUEST");
        lenient().when(guestRole.getName()).thenReturn("게스트");

        lenient().when(testUser.getUserId()).thenReturn(1L);
        lenient().when(testUser.getName()).thenReturn("테스트유저");
        lenient().when(testUser.getEmail()).thenReturn("test@test.com");
        lenient().when(testUser.getRole()).thenReturn(guestRole);

        lenient().when(testCompany.getCompanyId()).thenReturn(10L);
        lenient().when(testCompany.getName()).thenReturn("(주)테스트회사");
        lenient().when(testCompany.getScale()).thenReturn("TIER1");

        lenient().when(testDomain.getDomainId()).thenReturn(1L);
        lenient().when(testDomain.getCode()).thenReturn("ESG");
        lenient().when(testDomain.getName()).thenReturn("ESG 실사");
        lenient().when(testDomain.getDescription()).thenReturn("ESG 공급망 실사");
    }

    @Nested
    @DisplayName("권한 요청 페이지 정보 조회 테스트")
    class GetRequestPageInfoTest {

        @Test
        @DisplayName("정상적인 페이지 정보 조회 성공")
        void getRequestPageInfo_Success() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(companyRepository.findAll()).willReturn(Collections.singletonList(testCompany));
            given(domainRepository.findByIsActiveTrue()).willReturn(Collections.singletonList(testDomain));
            given(roleRequestRepository.findByUserAndStatus(testUser, RequestStatus.PENDING))
                    .willReturn(Optional.empty());

            // when
            RoleRequestPageResponse response = roleRequestService.getRequestPageInfo(1L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getCurrentRole().getCode()).isEqualTo("GUEST");
            assertThat(response.getCurrentRole().getName()).isEqualTo("게스트");
            assertThat(response.getAvailableRoles()).hasSize(3);
            assertThat(response.getAvailableCompanies()).hasSize(1);
            assertThat(response.getAvailableDomains()).hasSize(1);
            assertThat(response.getPendingRequest()).isNull();
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 조회 실패")
        void getRequestPageInfo_UserNotFound_ThrowsException() {
            // given
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> roleRequestService.getRequestPageInfo(999L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
                    });
        }
    }

    @Nested
    @DisplayName("권한 요청 생성 테스트")
    class CreateRoleRequestTest {

        @Test
        @DisplayName("정상적인 권한 요청 생성 성공")
        void createRoleRequest_Success() {
            // given
            RoleRequestCreateDto createDto = RoleRequestCreateDto.builder()
                    .requestedRole("DRAFTER")
                    .domainId(1L)
                    .companyId(10L)
                    .reason("ESG 담당자 지정")
                    .build();

            RoleRequest savedRequest = mock(RoleRequest.class);
            when(savedRequest.getRequestId()).thenReturn(1L);
            when(savedRequest.getStatus()).thenReturn(RequestStatus.PENDING);
            when(savedRequest.getRequestedRole()).thenReturn("DRAFTER");
            when(savedRequest.getCreatedAt()).thenReturn(null);

            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(domainRepository.findById(1L)).willReturn(Optional.of(testDomain));
            given(userDomainRoleRepository.existsByUserUserIdAndDomainDomainId(1L, 1L)).willReturn(false);
            given(roleRequestRepository.existsByUserAndDomainAndStatus(testUser, testDomain, RequestStatus.PENDING)).willReturn(false);
            given(companyRepository.findById(10L)).willReturn(Optional.of(testCompany));
            given(roleRequestRepository.save(any(RoleRequest.class))).willReturn(savedRequest);

            // when
            RoleRequestResponse response = roleRequestService.createRoleRequest(1L, createDto);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getAccessRequestId()).isEqualTo(1L);
            assertThat(response.getStatus()).isEqualTo("PENDING");
            assertThat(response.getRequestedRole()).isEqualTo("DRAFTER");
            assertThat(response.getCompanyId()).isEqualTo(10L);

            verify(roleRequestRepository).save(any(RoleRequest.class));
        }

        @Test
        @DisplayName("이미 대기중인 요청이 있을 때 생성 실패")
        void createRoleRequest_DuplicateRequest_ThrowsException() {
            // given
            RoleRequestCreateDto createDto = RoleRequestCreateDto.builder()
                    .requestedRole("DRAFTER")
                    .domainId(1L)
                    .companyId(10L)
                    .reason("ESG 담당자 지정")
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(domainRepository.findById(1L)).willReturn(Optional.of(testDomain));
            given(userDomainRoleRepository.existsByUserUserIdAndDomainDomainId(1L, 1L)).willReturn(false);
            given(roleRequestRepository.existsByUserAndDomainAndStatus(testUser, testDomain, RequestStatus.PENDING)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> roleRequestService.createRoleRequest(1L, createDto))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_ROLE_REQUEST);
                    });
        }

        @Test
        @DisplayName("유효하지 않은 역할로 요청 시 실패")
        void createRoleRequest_InvalidRole_ThrowsException() {
            // given
            RoleRequestCreateDto createDto = RoleRequestCreateDto.builder()
                    .requestedRole("INVALID_ROLE")
                    .companyId(10L)
                    .reason("테스트")
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

            // when & then
            assertThatThrownBy(() -> roleRequestService.createRoleRequest(1L, createDto))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.INVALID_ROLE_REQUEST);
                    });
        }

        @Test
        @DisplayName("존재하지 않는 회사로 요청 시 실패")
        void createRoleRequest_CompanyNotFound_ThrowsException() {
            // given
            RoleRequestCreateDto createDto = RoleRequestCreateDto.builder()
                    .requestedRole("DRAFTER")
                    .domainId(1L)
                    .companyId(999L)
                    .reason("테스트")
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(domainRepository.findById(1L)).willReturn(Optional.of(testDomain));
            given(userDomainRoleRepository.existsByUserUserIdAndDomainDomainId(1L, 1L)).willReturn(false);
            given(roleRequestRepository.existsByUserAndDomainAndStatus(testUser, testDomain, RequestStatus.PENDING)).willReturn(false);
            given(companyRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> roleRequestService.createRoleRequest(1L, createDto))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.COMPANY_NOT_FOUND);
                    });
        }
        @Test
        @DisplayName("SAFETY 도메인에서 APPROVER 요청 시 실패")
        void createRoleRequest_ApproverNonEsg_ThrowsException() {
            // given
            Domain safetyDomain = mock(Domain.class);
            lenient().when(safetyDomain.getDomainId()).thenReturn(2L);
            when(safetyDomain.getCode()).thenReturn("SAFETY");

            RoleRequestCreateDto createDto = RoleRequestCreateDto.builder()
                    .requestedRole("APPROVER")
                    .domainId(2L)
                    .companyId(10L)
                    .reason("안전보건 결재자 요청")
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(domainRepository.findById(2L)).willReturn(Optional.of(safetyDomain));

            // when & then
            assertThatThrownBy(() -> roleRequestService.createRoleRequest(1L, createDto))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.APPROVER_ONLY_ESG);
                    });
        }

        @Test
        @DisplayName("ESG 도메인에서 APPROVER 요청 시 정상 처리")
        void createRoleRequest_ApproverEsg_Success() {
            // given
            RoleRequestCreateDto createDto = RoleRequestCreateDto.builder()
                    .requestedRole("APPROVER")
                    .domainId(1L)
                    .companyId(10L)
                    .reason("ESG 결재자 요청")
                    .build();

            RoleRequest savedRequest = mock(RoleRequest.class);
            when(savedRequest.getRequestId()).thenReturn(1L);
            when(savedRequest.getStatus()).thenReturn(RequestStatus.PENDING);
            when(savedRequest.getRequestedRole()).thenReturn("APPROVER");
            when(savedRequest.getCreatedAt()).thenReturn(null);

            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(domainRepository.findById(1L)).willReturn(Optional.of(testDomain));
            given(userDomainRoleRepository.existsByUserUserIdAndDomainDomainId(1L, 1L)).willReturn(false);
            given(roleRequestRepository.existsByUserAndDomainAndStatus(testUser, testDomain, RequestStatus.PENDING)).willReturn(false);
            given(companyRepository.findById(10L)).willReturn(Optional.of(testCompany));
            given(roleRequestRepository.save(any(RoleRequest.class))).willReturn(savedRequest);

            // when
            RoleRequestResponse response = roleRequestService.createRoleRequest(1L, createDto);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getRequestedRole()).isEqualTo("APPROVER");
            verify(roleRequestRepository).save(any(RoleRequest.class));
        }
    }

    @Nested
    @DisplayName("내 권한 요청 상태 조회 테스트")
    class GetMyRequestStatusTest {

        @Test
        @DisplayName("정상적인 내 권한 요청 상태 조회 성공")
        void getMyRequestStatus_Success() {
            // given
            RoleRequest roleRequest = mock(RoleRequest.class);
            when(roleRequest.getRequestId()).thenReturn(1L);
            when(roleRequest.getRequestedRole()).thenReturn("DRAFTER");
            when(roleRequest.getStatus()).thenReturn(RequestStatus.PENDING);
            when(roleRequest.getCompany()).thenReturn(testCompany);
            when(roleRequest.getReason()).thenReturn("ESG 담당자 지정");
            when(roleRequest.getCreatedAt()).thenReturn(null);
            when(roleRequest.getDecidedAt()).thenReturn(null);
            when(roleRequest.getRejectReason()).thenReturn(null);

            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(roleRequestRepository.findTopByUserOrderByCreatedAtDesc(testUser))
                    .willReturn(Optional.of(roleRequest));

            // when
            RoleRequestStatusDto response = roleRequestService.getMyRequestStatus(1L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getAccessRequestId()).isEqualTo(1L);
            assertThat(response.getRequestedRole().getCode()).isEqualTo("DRAFTER");
            assertThat(response.getRequestedRole().getName()).isEqualTo("기안자");
            assertThat(response.getStatus()).isEqualTo("PENDING");
            assertThat(response.getStatusLabel()).isEqualTo("승인 대기중");
            assertThat(response.getCompany().getCompanyId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("권한 요청이 없을 때 조회 실패")
        void getMyRequestStatus_NotFound_ThrowsException() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(roleRequestRepository.findTopByUserOrderByCreatedAtDesc(testUser))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> roleRequestService.getMyRequestStatus(1L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.ROLE_REQUEST_NOT_FOUND);
                    });
        }
    }

    // ========== 관리자용 API 테스트 ==========

    @Nested
    @DisplayName("권한 요청 목록 조회 테스트 (관리자)")
    class GetRoleRequestListTest {

        @Mock
        private User reviewerUser;

        @Mock
        private Role reviewerRole;

        @Test
        @DisplayName("REVIEWER가 전체 권한 요청 목록 조회 성공")
        void getRoleRequestList_AsReviewer_Success() {
            // given
            lenient().when(reviewerRole.getCode()).thenReturn("REVIEWER");
            lenient().when(reviewerUser.getUserId()).thenReturn(2L);
            lenient().when(reviewerUser.getRole()).thenReturn(reviewerRole);

            RoleRequest roleRequest = mock(RoleRequest.class);
            lenient().when(roleRequest.getRequestId()).thenReturn(1L);
            lenient().when(roleRequest.getRequestedRole()).thenReturn("DRAFTER");
            lenient().when(roleRequest.getStatus()).thenReturn(RequestStatus.PENDING);
            lenient().when(roleRequest.getUser()).thenReturn(testUser);
            lenient().when(roleRequest.getCompany()).thenReturn(testCompany);
            lenient().when(roleRequest.getReason()).thenReturn("테스트 사유");
            lenient().when(roleRequest.getCreatedAt()).thenReturn(LocalDateTime.now());

            Page<RoleRequest> requestPage = new PageImpl<>(List.of(roleRequest), PageRequest.of(0, 10), 1);

            given(userRepository.findById(2L)).willReturn(Optional.of(reviewerUser));
            given(roleRequestRepository.findAllByOrderByCreatedAtDesc(any())).willReturn(requestPage);

            // when
            RoleApprovalListResponse response = roleRequestService.getRoleRequestList(2L, null, null, 0, 10);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).getAccessRequestId()).isEqualTo(1L);
            assertThat(response.getPage().getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("권한이 없는 사용자가 목록 조회 시 실패")
        void getRoleRequestList_AsGuest_ThrowsException() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

            // when & then
            assertThatThrownBy(() -> roleRequestService.getRoleRequestList(1L, null, null, 0, 10))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.PERMISSION_DENIED_ACTION);
                    });
        }
    }

    @Nested
    @DisplayName("권한 요청 승인/반려 테스트")
    class ProcessRoleRequestTest {

        @Mock
        private User reviewerUser;

        @Mock
        private Role reviewerRole;

        @Test
        @DisplayName("REVIEWER가 권한 요청 승인 성공")
        void processRoleRequest_Approve_Success() {
            // given
            when(reviewerRole.getCode()).thenReturn("REVIEWER");
            when(reviewerUser.getUserId()).thenReturn(2L);
            when(reviewerUser.getName()).thenReturn("리뷰어");
            when(reviewerUser.getRole()).thenReturn(reviewerRole);

            RoleRequest roleRequest = mock(RoleRequest.class);
            when(roleRequest.getRequestId()).thenReturn(1L);
            when(roleRequest.isPending()).thenReturn(true);
            when(roleRequest.getStatus()).thenReturn(RequestStatus.APPROVED);
            when(roleRequest.getDecidedAt()).thenReturn(LocalDateTime.now());

            RoleDecisionRequest decisionRequest = RoleDecisionRequest.builder()
                    .decision("APPROVED")
                    .build();

            given(userRepository.findById(2L)).willReturn(Optional.of(reviewerUser));
            given(roleRequestRepository.findById(1L)).willReturn(Optional.of(roleRequest));

            // when
            RoleDecisionResponse response = roleRequestService.processRoleRequest(2L, 1L, decisionRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo("APPROVED");
            assertThat(response.getMessage()).isEqualTo("권한 요청이 승인되었습니다");
            verify(roleRequest).approve(reviewerUser);
        }

        @Test
        @DisplayName("승인 시 UserDomainRole 생성 및 GUEST→요청역할 전역 역할 업그레이드")
        void processRoleRequest_Approve_CreatesDomainRoleAndUpgradesGlobalRole() {
            // given
            when(reviewerRole.getCode()).thenReturn("REVIEWER");
            when(reviewerUser.getUserId()).thenReturn(2L);
            when(reviewerUser.getName()).thenReturn("리뷰어");
            when(reviewerUser.getRole()).thenReturn(reviewerRole);

            Role guestRole = mock(Role.class);
            when(guestRole.getCode()).thenReturn("GUEST");

            Role drafterRole = mock(Role.class);
            lenient().when(drafterRole.getCode()).thenReturn("DRAFTER");

            User requestUser = mock(User.class);
            when(requestUser.getUserId()).thenReturn(3L);
            when(requestUser.getRole()).thenReturn(guestRole);

            Domain esgDomain = mock(Domain.class);
            when(esgDomain.getDomainId()).thenReturn(1L);

            RoleRequest roleRequest = mock(RoleRequest.class);
            when(roleRequest.getRequestId()).thenReturn(1L);
            when(roleRequest.isPending()).thenReturn(true);
            when(roleRequest.getStatus()).thenReturn(RequestStatus.APPROVED);
            when(roleRequest.getDecidedAt()).thenReturn(LocalDateTime.now());
            when(roleRequest.getDomain()).thenReturn(esgDomain);
            when(roleRequest.getUser()).thenReturn(requestUser);
            when(roleRequest.getRequestedRole()).thenReturn("DRAFTER");

            RoleDecisionRequest decisionRequest = RoleDecisionRequest.builder()
                    .decision("APPROVED")
                    .build();

            given(userRepository.findById(2L)).willReturn(Optional.of(reviewerUser));
            given(roleRequestRepository.findById(1L)).willReturn(Optional.of(roleRequest));
            given(roleRepository.findByCode("DRAFTER")).willReturn(Optional.of(drafterRole));
            given(userDomainRoleRepository.save(any(UserDomainRole.class))).willAnswer(i -> i.getArgument(0));

            // when
            RoleDecisionResponse response = roleRequestService.processRoleRequest(2L, 1L, decisionRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo("APPROVED");
            verify(userDomainRoleRepository).save(any(UserDomainRole.class));
            verify(requestUser).changeRole(drafterRole);
        }

        @Test
        @DisplayName("이미 처리된 요청을 다시 처리하려고 할 때 실패")
        void processRoleRequest_AlreadyProcessed_ThrowsException() {
            // given
            lenient().when(reviewerRole.getCode()).thenReturn("REVIEWER");
            lenient().when(reviewerUser.getUserId()).thenReturn(2L);
            lenient().when(reviewerUser.getRole()).thenReturn(reviewerRole);

            RoleRequest roleRequest = mock(RoleRequest.class);
            when(roleRequest.isPending()).thenReturn(false); // 이미 처리됨

            RoleDecisionRequest decisionRequest = RoleDecisionRequest.builder()
                    .decision("APPROVED")
                    .build();

            given(userRepository.findById(2L)).willReturn(Optional.of(reviewerUser));
            given(roleRequestRepository.findById(1L)).willReturn(Optional.of(roleRequest));

            // when & then
            assertThatThrownBy(() -> roleRequestService.processRoleRequest(2L, 1L, decisionRequest))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.ALREADY_PROCESSED_REQUEST);
                    });
        }

        @Test
        @DisplayName("REVIEWER가 권한 요청 반려 성공")
        void processRoleRequest_Reject_Success() {
            // given
            when(reviewerRole.getCode()).thenReturn("REVIEWER");
            when(reviewerUser.getUserId()).thenReturn(2L);
            when(reviewerUser.getName()).thenReturn("리뷰어");
            when(reviewerUser.getRole()).thenReturn(reviewerRole);

            RoleRequest roleRequest = mock(RoleRequest.class);
            when(roleRequest.getRequestId()).thenReturn(1L);
            when(roleRequest.isPending()).thenReturn(true);
            when(roleRequest.getStatus()).thenReturn(RequestStatus.REJECTED);
            when(roleRequest.getDecidedAt()).thenReturn(LocalDateTime.now());

            RoleDecisionRequest decisionRequest = RoleDecisionRequest.builder()
                    .decision("REJECTED")
                    .rejectReason("정보 부족")
                    .build();

            given(userRepository.findById(2L)).willReturn(Optional.of(reviewerUser));
            given(roleRequestRepository.findById(1L)).willReturn(Optional.of(roleRequest));

            // when
            RoleDecisionResponse response = roleRequestService.processRoleRequest(2L, 1L, decisionRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo("REJECTED");
            assertThat(response.getMessage()).isEqualTo("권한 요청이 반려되었습니다");
            verify(roleRequest).reject(reviewerUser, "정보 부족");
        }
    }
}
