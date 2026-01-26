package com.smartchain.platform.domain.management.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartchain.platform.domain.job.repository.AsyncJobRepository;
import com.smartchain.platform.domain.log.entity.ActivityLog;
import com.smartchain.platform.domain.log.repository.ActivityLogRepository;
import com.smartchain.platform.domain.role.repository.RoleRequestRepository;
import com.smartchain.platform.domain.user.entity.Company;
import com.smartchain.platform.domain.user.entity.Industry;
import com.smartchain.platform.domain.user.entity.Role;
import com.smartchain.platform.domain.user.entity.RoleRequest;
import com.smartchain.platform.domain.user.entity.User;
import com.smartchain.platform.domain.user.repository.CompanyRepository;
import com.smartchain.platform.domain.user.repository.IndustryRepository;
import com.smartchain.platform.domain.user.repository.RoleRepository;
import com.smartchain.platform.domain.user.repository.UserRepository;
import com.smartchain.platform.dto.management.company.CompanyManagementResponse;
import com.smartchain.platform.dto.management.company.CompanyRegisterRequest;
import com.smartchain.platform.dto.management.company.CompanyRegisterResponse;
import com.smartchain.platform.dto.management.log.ActivityLogExportRequest;
import com.smartchain.platform.dto.management.log.ActivityLogExportResponse;
import com.smartchain.platform.dto.management.log.ActivityLogResponse;
import com.smartchain.platform.dto.management.permission.PermissionDashboardResponse;
import com.smartchain.platform.dto.management.permission.PermissionDecisionRequest;
import com.smartchain.platform.dto.management.permission.PermissionDecisionResponse;
import com.smartchain.platform.dto.management.user.*;
import com.smartchain.platform.global.enums.ActionType;
import com.smartchain.platform.global.enums.CompanyStatus;
import com.smartchain.platform.global.enums.CompanyType;
import com.smartchain.platform.global.enums.RequestStatus;
import com.smartchain.platform.global.enums.UserStatus;
import com.smartchain.platform.global.error.CustomException;
import com.smartchain.platform.global.error.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

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
class ManagementServiceTest {

    @InjectMocks
    private ManagementService managementService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleRequestRepository roleRequestRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private IndustryRepository industryRepository;

    @Mock
    private ActivityLogRepository activityLogRepository;

    @Mock
    private AsyncJobRepository asyncJobRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private User reviewerUser;

    @Mock
    private User targetUser;

    @Mock
    private User guestUser;

    @Mock
    private Role reviewerRole;

    @Mock
    private Role guestRole;

    @Mock
    private Role drafterRole;

    @Mock
    private Company testCompany;

    @Mock
    private RoleRequest testRoleRequest;

    @Mock
    private Industry testIndustry;

    @Mock
    private ActivityLog testActivityLog;

    @BeforeEach
    void setUp() {
        lenient().when(reviewerRole.getCode()).thenReturn("REVIEWER");
        lenient().when(reviewerRole.getName()).thenReturn("수신자");
        lenient().when(guestRole.getCode()).thenReturn("GUEST");
        lenient().when(guestRole.getName()).thenReturn("게스트");
        lenient().when(drafterRole.getCode()).thenReturn("DRAFTER");
        lenient().when(drafterRole.getName()).thenReturn("기안자");

        lenient().when(reviewerUser.getUserId()).thenReturn(1L);
        lenient().when(reviewerUser.getName()).thenReturn("수신자");
        lenient().when(reviewerUser.getEmail()).thenReturn("reviewer@test.com");
        lenient().when(reviewerUser.getRole()).thenReturn(reviewerRole);
        lenient().when(reviewerUser.getStatus()).thenReturn(UserStatus.ACTIVE);

        lenient().when(guestUser.getUserId()).thenReturn(2L);
        lenient().when(guestUser.getName()).thenReturn("게스트");
        lenient().when(guestUser.getEmail()).thenReturn("guest@test.com");
        lenient().when(guestUser.getRole()).thenReturn(guestRole);
        lenient().when(guestUser.getStatus()).thenReturn(UserStatus.ACTIVE);

        lenient().when(targetUser.getUserId()).thenReturn(3L);
        lenient().when(targetUser.getName()).thenReturn("대상자");
        lenient().when(targetUser.getEmail()).thenReturn("target@test.com");
        lenient().when(targetUser.getRole()).thenReturn(guestRole);
        lenient().when(targetUser.getStatus()).thenReturn(UserStatus.ACTIVE);

        lenient().when(testCompany.getCompanyId()).thenReturn(10L);
        lenient().when(testCompany.getName()).thenReturn("테스트회사");
        lenient().when(testCompany.getBusinessNumber()).thenReturn("123-45-67890");
        lenient().when(testCompany.getCompanyType()).thenReturn(CompanyType.TIER1);
        lenient().when(testCompany.getIndustry()).thenReturn(testIndustry);
        lenient().when(testCompany.getCeoName()).thenReturn("홍길동");
        lenient().when(testCompany.getStatus()).thenReturn(CompanyStatus.ACTIVE);
        lenient().when(testCompany.getCreatedAt()).thenReturn(LocalDateTime.now());

        lenient().when(testIndustry.getIndustryId()).thenReturn(1L);
        lenient().when(testIndustry.getCode()).thenReturn("MFG");
        lenient().when(testIndustry.getName()).thenReturn("제조업");

        lenient().when(testActivityLog.getLogId()).thenReturn(100L);
        lenient().when(testActivityLog.getUser()).thenReturn(targetUser);
        lenient().when(testActivityLog.getAction()).thenReturn(ActionType.LOGIN);
        lenient().when(testActivityLog.getIpAddress()).thenReturn("127.0.0.1");
        lenient().when(testActivityLog.getDescription()).thenReturn("로그인 성공");
        lenient().when(testActivityLog.getCreatedAt()).thenReturn(LocalDateTime.now());
    }

    @Nested
    @DisplayName("권한 대시보드 조회 테스트")
    class GetPermissionDashboardTest {

        @Test
        @DisplayName("REVIEWER가 권한 대시보드 조회 성공")
        void getPermissionDashboard_Success() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(roleRequestRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class)))
                    .willReturn(new PageImpl<>(List.of()));
            given(roleRequestRepository.countByStatus(RequestStatus.PENDING)).willReturn(3L);
            given(roleRequestRepository.countByStatus(RequestStatus.APPROVED)).willReturn(10L);
            given(roleRequestRepository.countByStatus(RequestStatus.REJECTED)).willReturn(2L);

            // when
            PermissionDashboardResponse response = managementService.getPermissionDashboard(1L, null, null, null, 0, 10);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getStats()).isNotNull();
            assertThat(response.getStats().getTotalPending()).isEqualTo(3);
            assertThat(response.getStats().getTotalApproved()).isEqualTo(10);
            assertThat(response.getStats().getTotalRejected()).isEqualTo(2);
        }

        @Test
        @DisplayName("REVIEWER가 아닌 사용자가 권한 대시보드 조회 시 실패")
        void getPermissionDashboard_NotReviewer_ThrowsException() {
            // given
            given(userRepository.findById(2L)).willReturn(Optional.of(guestUser));

            // when & then
            assertThatThrownBy(() -> managementService.getPermissionDashboard(2L, null, null, null, 0, 10))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.PERMISSION_DENIED_ACTION);
                    });
        }
    }

    @Nested
    @DisplayName("권한 요청 처리 테스트")
    class ProcessPermissionRequestTest {

        @Test
        @DisplayName("권한 요청 승인 성공")
        void processPermissionRequest_Approve_Success() {
            // given
            PermissionDecisionRequest request = PermissionDecisionRequest.builder()
                    .decision("APPROVED")
                    .comment("승인합니다")
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(roleRequestRepository.findByRequestId(100L)).willReturn(Optional.of(testRoleRequest));
            given(testRoleRequest.isPending()).willReturn(true);
            given(testRoleRequest.getRequestedRole()).willReturn("DRAFTER");
            given(testRoleRequest.getUser()).willReturn(targetUser);
            given(testRoleRequest.getStatus()).willReturn(RequestStatus.APPROVED);
            given(testRoleRequest.getDecidedAt()).willReturn(LocalDateTime.now());
            given(roleRepository.findByCode("DRAFTER")).willReturn(Optional.of(drafterRole));

            // when
            PermissionDecisionResponse response = managementService.processPermissionRequest(1L, 100L, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getAccessRequestId()).isEqualTo(100L);
            assertThat(response.getStatus()).isEqualTo("APPROVED");
            verify(testRoleRequest).approve(reviewerUser);
            verify(targetUser).changeRole(drafterRole);
        }

        @Test
        @DisplayName("권한 요청 반려 성공")
        void processPermissionRequest_Reject_Success() {
            // given
            PermissionDecisionRequest request = PermissionDecisionRequest.builder()
                    .decision("REJECTED")
                    .comment("반려 사유")
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(roleRequestRepository.findByRequestId(100L)).willReturn(Optional.of(testRoleRequest));
            given(testRoleRequest.isPending()).willReturn(true);
            given(testRoleRequest.getStatus()).willReturn(RequestStatus.REJECTED);
            given(testRoleRequest.getDecidedAt()).willReturn(LocalDateTime.now());

            // when
            PermissionDecisionResponse response = managementService.processPermissionRequest(1L, 100L, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo("REJECTED");
            verify(testRoleRequest).reject(reviewerUser, "반려 사유");
        }

        @Test
        @DisplayName("이미 처리된 권한 요청 처리 시 실패")
        void processPermissionRequest_AlreadyProcessed_ThrowsException() {
            // given
            PermissionDecisionRequest request = PermissionDecisionRequest.builder()
                    .decision("APPROVED")
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(roleRequestRepository.findByRequestId(100L)).willReturn(Optional.of(testRoleRequest));
            given(testRoleRequest.isPending()).willReturn(false);

            // when & then
            assertThatThrownBy(() -> managementService.processPermissionRequest(1L, 100L, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.ALREADY_PROCESSED_REQUEST);
                    });
        }
    }

    @Nested
    @DisplayName("사용자 목록 조회 테스트")
    class GetUserListTest {

        @Test
        @DisplayName("REVIEWER가 사용자 목록 조회 성공")
        void getUserList_Success() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(userRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class)))
                    .willReturn(new PageImpl<>(List.of(targetUser)));
            given(userRepository.count()).willReturn(100L);
            given(userRepository.countByStatus(UserStatus.ACTIVE)).willReturn(90L);
            given(userRepository.countByStatus(UserStatus.INACTIVE)).willReturn(10L);
            given(userRepository.countByLastLoginAtAfter(any(LocalDateTime.class))).willReturn(50L);

            // when
            UserManagementResponse response = managementService.getUserList(1L, null, null, null, null, 0, 20);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getStats()).isNotNull();
            assertThat(response.getStats().getTotalUsers()).isEqualTo(100);
            assertThat(response.getStats().getActiveUsers()).isEqualTo(90);
            assertThat(response.getStats().getInactiveUsers()).isEqualTo(10);
            assertThat(response.getStats().getRecentLogins7d()).isEqualTo(50);
            assertThat(response.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("상태 필터로 사용자 목록 조회 성공")
        void getUserList_WithStatusFilter_Success() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(userRepository.findByStatusOrderByCreatedAtDesc(eq(UserStatus.ACTIVE), any(Pageable.class)))
                    .willReturn(new PageImpl<>(List.of(targetUser)));
            given(userRepository.count()).willReturn(100L);
            given(userRepository.countByStatus(UserStatus.ACTIVE)).willReturn(90L);
            given(userRepository.countByStatus(UserStatus.INACTIVE)).willReturn(10L);
            given(userRepository.countByLastLoginAtAfter(any(LocalDateTime.class))).willReturn(50L);

            // when
            UserManagementResponse response = managementService.getUserList(1L, null, "ACTIVE", null, null, 0, 20);

            // then
            assertThat(response).isNotNull();
            verify(userRepository).findByStatusOrderByCreatedAtDesc(eq(UserStatus.ACTIVE), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("사용자 역할 변경 테스트")
    class ChangeUserRoleTest {

        @Test
        @DisplayName("사용자 역할 변경 성공")
        void changeUserRole_Success() {
            // given
            RoleChangeRequest request = RoleChangeRequest.builder()
                    .newRole("DRAFTER")
                    .reason("업무 담당자 변경")
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(userRepository.findById(3L)).willReturn(Optional.of(targetUser));
            given(roleRepository.findByCode("DRAFTER")).willReturn(Optional.of(drafterRole));

            // when
            RoleChangeResponse response = managementService.changeUserRole(1L, 3L, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getUserId()).isEqualTo(3L);
            assertThat(response.getPreviousRole()).isEqualTo("GUEST");
            assertThat(response.getNewRole()).isEqualTo("DRAFTER");
            assertThat(response.getChangedBy()).isNotNull();
            assertThat(response.getChangedBy().getUserId()).isEqualTo(1L);
            verify(targetUser).changeRole(drafterRole);
        }

        @Test
        @DisplayName("존재하지 않는 역할로 변경 시 실패")
        void changeUserRole_InvalidRole_ThrowsException() {
            // given
            RoleChangeRequest request = RoleChangeRequest.builder()
                    .newRole("INVALID_ROLE")
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(userRepository.findById(3L)).willReturn(Optional.of(targetUser));
            given(roleRepository.findByCode("INVALID_ROLE")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> managementService.changeUserRole(1L, 3L, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.ROLE_NOT_FOUND);
                    });
        }
    }

    @Nested
    @DisplayName("사용자 상태 변경 테스트")
    class ChangeUserStatusTest {

        @Test
        @DisplayName("사용자 상태 변경 성공")
        void changeUserStatus_Success() {
            // given
            UserStatusChangeRequest request = UserStatusChangeRequest.builder()
                    .newStatus("INACTIVE")
                    .reason("퇴사 처리")
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(userRepository.findById(3L)).willReturn(Optional.of(targetUser));

            // when
            UserStatusChangeResponse response = managementService.changeUserStatus(1L, 3L, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getUserId()).isEqualTo(3L);
            assertThat(response.getPreviousStatus()).isEqualTo("ACTIVE");
            assertThat(response.getNewStatus()).isEqualTo("INACTIVE");
            verify(targetUser).changeStatus(UserStatus.INACTIVE);
        }

        @Test
        @DisplayName("존재하지 않는 사용자 상태 변경 시 실패")
        void changeUserStatus_UserNotFound_ThrowsException() {
            // given
            UserStatusChangeRequest request = UserStatusChangeRequest.builder()
                    .newStatus("INACTIVE")
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> managementService.changeUserStatus(1L, 999L, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
                    });
        }
    }

    @Nested
    @DisplayName("협력사 목록 조회 테스트")
    class GetCompanyListTest {

        @Test
        @DisplayName("REVIEWER가 협력사 목록 조회 성공")
        void getCompanyList_Success() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(companyRepository.findByFilters(any(), any(), any(), any(Pageable.class)))
                    .willReturn(new PageImpl<>(List.of(testCompany)));
            given(companyRepository.countUsersByCompanyId(10L)).willReturn(5);
            given(companyRepository.countDiagnosticsByCompanyId(10L)).willReturn(3);

            // when
            CompanyManagementResponse response = managementService.getCompanyList(1L, null, null, null, 0, 20);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).getCompanyId()).isEqualTo(10L);
            assertThat(response.getContent().get(0).getCompanyName()).isEqualTo("테스트회사");
            assertThat(response.getContent().get(0).getUserCount()).isEqualTo(5);
            assertThat(response.getContent().get(0).getDiagnosticCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("REVIEWER가 아닌 사용자가 협력사 목록 조회 시 실패")
        void getCompanyList_NotReviewer_ThrowsException() {
            // given
            given(userRepository.findById(2L)).willReturn(Optional.of(guestUser));

            // when & then
            assertThatThrownBy(() -> managementService.getCompanyList(2L, null, null, null, 0, 20))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.PERMISSION_DENIED_ACTION);
                    });
        }
    }

    @Nested
    @DisplayName("협력사 등록 테스트")
    class RegisterCompanyTest {

        @Test
        @DisplayName("협력사 등록 성공")
        void registerCompany_Success() {
            // given
            CompanyRegisterRequest request = CompanyRegisterRequest.builder()
                    .companyName("신규 협력사")
                    .businessNumber("999-88-77777")
                    .companyType("TIER1")
                    .industryCode("MFG")
                    .ceoName("김대표")
                    .address("서울시 강남구")
                    .contactEmail("contact@newcompany.com")
                    .contactPhone("02-1234-5678")
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(companyRepository.existsByBusinessNumber("999-88-77777")).willReturn(false);
            given(industryRepository.findByCode("MFG")).willReturn(Optional.of(testIndustry));
            // Return the argument directly since Company is created with @Builder
            given(companyRepository.save(any(Company.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            CompanyRegisterResponse response = managementService.registerCompany(1L, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getCompanyName()).isEqualTo("신규 협력사");
            assertThat(response.getBusinessNumber()).isEqualTo("999-88-77777");
            assertThat(response.getStatus()).isEqualTo("ACTIVE");
            verify(companyRepository).save(any(Company.class));
        }

        @Test
        @DisplayName("중복 사업자등록번호로 협력사 등록 시 실패")
        void registerCompany_DuplicateBusinessNumber_ThrowsException() {
            // given
            CompanyRegisterRequest request = CompanyRegisterRequest.builder()
                    .companyName("중복 협력사")
                    .businessNumber("123-45-67890")
                    .companyType("TIER1")
                    .industryCode("MFG")
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(companyRepository.existsByBusinessNumber("123-45-67890")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> managementService.registerCompany(1L, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_BUSINESS_NUMBER);
                    });
        }
    }

    @Nested
    @DisplayName("활동 로그 조회 테스트")
    class GetActivityLogsTest {

        @Test
        @DisplayName("REVIEWER가 활동 로그 조회 성공")
        void getActivityLogs_Success() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(activityLogRepository.findByFilters(any(), any(), any(), any(), any(Pageable.class)))
                    .willReturn(new PageImpl<>(List.of(testActivityLog)));

            // when
            ActivityLogResponse response = managementService.getActivityLogs(1L, null, null, null, null, 0, 50);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).getLogId()).isEqualTo(100L);
            assertThat(response.getContent().get(0).getAction().getCode()).isEqualTo("LOGIN");
        }
    }

    @Nested
    @DisplayName("활동 로그 내보내기 테스트")
    class ExportActivityLogsTest {

        @Test
        @DisplayName("활동 로그 내보내기 요청 성공")
        void exportActivityLogs_Success() {
            // given
            ActivityLogExportRequest request = ActivityLogExportRequest.builder()
                    .format("CSV")
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            // Return the argument since AsyncJob generates jobId in constructor
            given(asyncJobRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

            // when
            ActivityLogExportResponse response = managementService.exportActivityLogs(1L, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getJobId()).isNotNull();
            assertThat(response.getJobId()).startsWith("job_export_");
            assertThat(response.getStatusCheckUrl()).isNotNull();
            assertThat(response.getMessage()).isNotNull();
            verify(asyncJobRepository).save(any());
        }
    }
}
