package com.smartchain.platform.domain.management.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartchain.platform.domain.job.entity.AsyncJob;
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
import com.smartchain.platform.dto.management.common.CompanySimpleDto;
import com.smartchain.platform.dto.management.common.PageDto;
import com.smartchain.platform.dto.management.common.RoleSimpleDto;
import com.smartchain.platform.dto.management.common.UserSimpleDto;
import com.smartchain.platform.dto.management.company.*;
import com.smartchain.platform.dto.management.log.*;
import com.smartchain.platform.dto.management.permission.*;
import com.smartchain.platform.dto.management.user.*;
import com.smartchain.platform.global.enums.*;
import com.smartchain.platform.global.error.CustomException;
import com.smartchain.platform.global.error.ErrorCode;
import com.smartchain.platform.global.util.NameMaskingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManagementService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RoleRequestRepository roleRequestRepository;
    private final CompanyRepository companyRepository;
    private final IndustryRepository industryRepository;
    private final ActivityLogRepository activityLogRepository;
    private final AsyncJobRepository asyncJobRepository;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 권한 대시보드 조회
     */
    public PermissionDashboardResponse getPermissionDashboard(Long userId, Long companyId, String requestedRole,
                                                               String status, int page, int size) {
        validateReviewerRole(userId);

        Pageable pageable = PageRequest.of(page, size);
        Page<RoleRequest> requestPage;

        // 필터링 로직
        if (status != null && !status.isEmpty()) {
            RequestStatus requestStatus = RequestStatus.valueOf(status.toUpperCase());
            if (requestedRole != null && !requestedRole.isEmpty()) {
                requestPage = roleRequestRepository.findByRequestedRoleAndStatusOrderByCreatedAtDesc(requestedRole, requestStatus, pageable);
            } else {
                requestPage = roleRequestRepository.findByStatusOrderByCreatedAtDesc(requestStatus, pageable);
            }
        } else if (requestedRole != null && !requestedRole.isEmpty()) {
            requestPage = roleRequestRepository.findByRequestedRoleOrderByCreatedAtDesc(requestedRole, pageable);
        } else {
            requestPage = roleRequestRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        // 통계 계산
        PermissionStatsDto stats = PermissionStatsDto.builder()
                .totalPending((int) roleRequestRepository.countByStatus(RequestStatus.PENDING))
                .totalApproved((int) roleRequestRepository.countByStatus(RequestStatus.APPROVED))
                .totalRejected((int) roleRequestRepository.countByStatus(RequestStatus.REJECTED))
                .build();

        List<PermissionRequestItemDto> content = requestPage.getContent().stream()
                .map(this::toPermissionRequestItemDto)
                .collect(Collectors.toList());

        PageDto pageDto = PageDto.builder()
                .number(requestPage.getNumber())
                .size(requestPage.getSize())
                .totalElements(requestPage.getTotalElements())
                .totalPages(requestPage.getTotalPages())
                .build();

        return PermissionDashboardResponse.builder()
                .stats(stats)
                .content(content)
                .page(pageDto)
                .build();
    }

    /**
     * 권한 요청 처리
     */
    @Transactional
    public PermissionDecisionResponse processPermissionRequest(Long userId, Long accessRequestId, PermissionDecisionRequest request) {
        User reviewer = validateReviewerRole(userId);

        RoleRequest roleRequest = roleRequestRepository.findByRequestId(accessRequestId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROLE_REQUEST_NOT_FOUND));

        if (!roleRequest.isPending()) {
            throw new CustomException(ErrorCode.ALREADY_PROCESSED_REQUEST);
        }

        String decision = request.getDecision().toUpperCase();
        if ("APPROVED".equals(decision)) {
            roleRequest.approve(reviewer);

            // 사용자 역할 변경
            Role newRole = roleRepository.findByCode(roleRequest.getRequestedRole())
                    .orElseThrow(() -> new CustomException(ErrorCode.ROLE_NOT_FOUND));
            roleRequest.getUser().changeRole(newRole);
        } else if ("REJECTED".equals(decision)) {
            roleRequest.reject(reviewer, request.getComment());
        } else {
            throw new CustomException(ErrorCode.INVALID_DECISION);
        }

        log.info("Permission request processed: accessRequestId={}, decision={}, processedBy={}",
                accessRequestId, decision, userId);

        return PermissionDecisionResponse.builder()
                .accessRequestId(accessRequestId)
                .status(roleRequest.getStatus().name())
                .processedAt(roleRequest.getDecidedAt())
                .message("권한 요청이 처리되었습니다")
                .build();
    }

    /**
     * 사용자 관리 목록 조회
     */
    public UserManagementResponse getUserList(Long userId, String role, String status,
                                               Long companyId, String keyword, int page, int size) {
        validateReviewerRole(userId);

        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage;

        // 필터링 로직 (단순화 - 복합 필터는 추후 QueryDSL로 개선 가능)
        if (keyword != null && !keyword.isEmpty()) {
            userPage = userRepository.findByKeywordOrderByCreatedAtDesc(keyword, pageable);
        } else if (status != null && !status.isEmpty()) {
            UserStatus userStatus = UserStatus.valueOf(status.toUpperCase());
            userPage = userRepository.findByStatusOrderByCreatedAtDesc(userStatus, pageable);
        } else if (role != null && !role.isEmpty()) {
            userPage = userRepository.findByRoleCodeOrderByCreatedAtDesc(role, pageable);
        } else if (companyId != null) {
            userPage = userRepository.findByCompanyIdOrderByCreatedAtDesc(companyId, pageable);
        } else {
            userPage = userRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        // 통계 계산
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByStatus(UserStatus.ACTIVE);
        long inactiveUsers = userRepository.countByStatus(UserStatus.INACTIVE);
        long recentLogins7d = userRepository.countByLastLoginAtAfter(LocalDateTime.now().minusDays(7));

        UserManagementStatsDto stats = UserManagementStatsDto.builder()
                .totalUsers((int) totalUsers)
                .activeUsers((int) activeUsers)
                .inactiveUsers((int) inactiveUsers)
                .recentLogins7d((int) recentLogins7d)
                .build();

        List<UserManagementItemDto> content = userPage.getContent().stream()
                .map(this::toUserManagementItemDto)
                .collect(Collectors.toList());

        PageDto pageDto = PageDto.builder()
                .number(userPage.getNumber())
                .size(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .build();

        return UserManagementResponse.builder()
                .stats(stats)
                .content(content)
                .page(pageDto)
                .build();
    }

    /**
     * 사용자 역할 변경
     */
    @Transactional
    public RoleChangeResponse changeUserRole(Long reviewerId, Long targetUserId, RoleChangeRequest request) {
        User reviewer = validateReviewerRole(reviewerId);

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String previousRole = targetUser.getRole() != null ? targetUser.getRole().getCode() : "GUEST";

        Role newRole = roleRepository.findByCode(request.getNewRole())
                .orElseThrow(() -> new CustomException(ErrorCode.ROLE_NOT_FOUND));

        targetUser.changeRole(newRole);

        log.info("User role changed: userId={}, previousRole={}, newRole={}, changedBy={}",
                targetUserId, previousRole, request.getNewRole(), reviewerId);

        return RoleChangeResponse.builder()
                .userId(targetUserId)
                .previousRole(previousRole)
                .newRole(request.getNewRole())
                .changedAt(LocalDateTime.now())
                .changedBy(ChangedByDto.builder()
                        .userId(reviewer.getUserId())
                        .name(reviewer.getName())
                        .build())
                .message("역할이 변경되었습니다")
                .build();
    }

    /**
     * 사용자 상태 변경
     */
    @Transactional
    public UserStatusChangeResponse changeUserStatus(Long reviewerId, Long targetUserId, UserStatusChangeRequest request) {
        validateReviewerRole(reviewerId);

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String previousStatus = targetUser.getStatus() != null ? targetUser.getStatus().name() : "ACTIVE";

        UserStatus newStatus = UserStatus.valueOf(request.getNewStatus().toUpperCase());
        targetUser.changeStatus(newStatus);

        log.info("User status changed: userId={}, previousStatus={}, newStatus={}, changedBy={}",
                targetUserId, previousStatus, request.getNewStatus(), reviewerId);

        return UserStatusChangeResponse.builder()
                .userId(targetUserId)
                .previousStatus(previousStatus)
                .newStatus(request.getNewStatus().toUpperCase())
                .changedAt(LocalDateTime.now())
                .message("상태가 변경되었습니다")
                .build();
    }

    // ========== 협력사 관리 ==========

    /**
     * 협력사 목록 조회
     */
    public CompanyManagementResponse getCompanyList(Long userId, String companyTypeStr, String industryCode,
                                                     String keyword, int page, int size) {
        validateReviewerRole(userId);

        Pageable pageable = PageRequest.of(page, size);
        CompanyType companyType = null;
        if (companyTypeStr != null && !companyTypeStr.isEmpty()) {
            companyType = CompanyType.valueOf(companyTypeStr.toUpperCase());
        }

        Page<Company> companyPage = companyRepository.findByFilters(companyType, industryCode, keyword, pageable);

        List<CompanyManagementItemDto> content = companyPage.getContent().stream()
                .map(this::toCompanyManagementItemDto)
                .collect(Collectors.toList());

        PageDto pageDto = PageDto.builder()
                .number(companyPage.getNumber())
                .size(companyPage.getSize())
                .totalElements(companyPage.getTotalElements())
                .totalPages(companyPage.getTotalPages())
                .build();

        return CompanyManagementResponse.builder()
                .content(content)
                .page(pageDto)
                .build();
    }

    /**
     * 협력사 등록
     */
    @Transactional
    public CompanyRegisterResponse registerCompany(Long userId, CompanyRegisterRequest request) {
        validateReviewerRole(userId);

        if (companyRepository.existsByBusinessNumber(request.getBusinessNumber())) {
            throw new CustomException(ErrorCode.DUPLICATE_BUSINESS_NUMBER);
        }

        Industry industry = null;
        if (request.getIndustryCode() != null && !request.getIndustryCode().isEmpty()) {
            industry = industryRepository.findByCode(request.getIndustryCode()).orElse(null);
        }

        Company company = Company.builder()
                .name(request.getCompanyName())
                .businessNumber(request.getBusinessNumber())
                .companyType(CompanyType.valueOf(request.getCompanyType().toUpperCase()))
                .industry(industry)
                .ceoName(request.getCeoName())
                .address(request.getAddress())
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .status(CompanyStatus.ACTIVE)
                .build();

        Company savedCompany = companyRepository.save(company);

        log.info("Company registered: companyId={}, companyName={}, registeredBy={}",
                savedCompany.getCompanyId(), savedCompany.getName(), userId);

        return CompanyRegisterResponse.builder()
                .companyId(savedCompany.getCompanyId())
                .companyName(savedCompany.getName())
                .businessNumber(savedCompany.getBusinessNumber())
                .status(savedCompany.getStatus().name())
                .createdAt(savedCompany.getCreatedAt())
                .message("협력사가 등록되었습니다")
                .build();
    }

    // ========== 활동 로그 ==========

    /**
     * 활동 로그 조회
     */
    public ActivityLogResponse getActivityLogs(Long reviewerId, Long userId, LocalDate fromDate,
                                                LocalDate toDate, String actionTypeStr, int page, int size) {
        validateReviewerRole(reviewerId);

        Pageable pageable = PageRequest.of(page, size);
        ActionType actionType = null;
        if (actionTypeStr != null && !actionTypeStr.isEmpty()) {
            actionType = ActionType.valueOf(actionTypeStr.toUpperCase());
        }

        LocalDateTime fromDateTime = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime toDateTime = toDate != null ? toDate.atTime(LocalTime.MAX) : null;

        Page<ActivityLog> logPage = activityLogRepository.findByFilters(userId, actionType, fromDateTime, toDateTime, pageable);

        List<ActivityLogItemDto> content = logPage.getContent().stream()
                .map(this::toActivityLogItemDto)
                .collect(Collectors.toList());

        PageDto pageDto = PageDto.builder()
                .number(logPage.getNumber())
                .size(logPage.getSize())
                .totalElements(logPage.getTotalElements())
                .totalPages(logPage.getTotalPages())
                .build();

        return ActivityLogResponse.builder()
                .content(content)
                .page(pageDto)
                .build();
    }

    /**
     * 활동 로그 내보내기 (비동기)
     */
    @Transactional
    public ActivityLogExportResponse exportActivityLogs(Long userId, ActivityLogExportRequest request) {
        validateReviewerRole(userId);

        String requestPayload;
        try {
            requestPayload = objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            requestPayload = "{}";
        }

        AsyncJob job = AsyncJob.builder()
                .jobType(JobType.EXPORT)
                .requesterId(userId)
                .requestPayload(requestPayload)
                .estimatedSeconds(60)
                .build();

        AsyncJob savedJob = asyncJobRepository.save(job);

        log.info("Activity log export job created: jobId={}, requestedBy={}", savedJob.getJobId(), userId);

        return ActivityLogExportResponse.builder()
                .jobId(savedJob.getJobId())
                .statusCheckUrl(savedJob.getStatusCheckUrl())
                .message("활동 로그 내보내기가 시작되었습니다")
                .build();
    }

    // ========== Private Helper Methods ==========

    private User validateReviewerRole(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String roleCode = user.getRole() != null ? user.getRole().getCode() : "GUEST";
        if (!"REVIEWER".equals(roleCode)) {
            throw new CustomException(ErrorCode.PERMISSION_DENIED_ACTION);
        }

        return user;
    }

    private PermissionRequestItemDto toPermissionRequestItemDto(RoleRequest roleRequest) {
        User user = roleRequest.getUser();

        return PermissionRequestItemDto.builder()
                .accessRequestId(roleRequest.getRequestId())
                .user(UserSimpleDto.builder()
                        .userId(user.getUserId())
                        .name(user.getName())
                        .maskedName(NameMaskingUtil.mask(user.getName()))
                        .email(user.getEmail())
                        .build())
                .company(user.getCompany() != null ? CompanySimpleDto.builder()
                        .companyId(user.getCompany().getCompanyId())
                        .companyName(user.getCompany().getName())
                        .companyType(user.getCompany().getScale())
                        .build() : null)
                .requestedRole(RoleSimpleDto.builder()
                        .code(roleRequest.getRequestedRole())
                        .name(getRoleName(roleRequest.getRequestedRole()))
                        .build())
                .status(roleRequest.getStatus().name())
                .requestedAt(roleRequest.getCreatedAt())
                .requestedAtLabel(roleRequest.getCreatedAt() != null
                        ? roleRequest.getCreatedAt().format(DATE_TIME_FORMATTER) : null)
                .build();
    }

    private UserManagementItemDto toUserManagementItemDto(User user) {
        return UserManagementItemDto.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .maskedName(NameMaskingUtil.mask(user.getName()))
                .email(user.getEmail())
                .company(user.getCompany() != null ? CompanySimpleDto.builder()
                        .companyId(user.getCompany().getCompanyId())
                        .companyName(user.getCompany().getName())
                        .companyType(user.getCompany().getScale())
                        .build() : null)
                .role(user.getRole() != null ? RoleSimpleDto.builder()
                        .code(user.getRole().getCode())
                        .name(user.getRole().getName())
                        .build() : null)
                .status(user.getStatus() != null ? user.getStatus().name() : "ACTIVE")
                .lastLoginAt(user.getLastLoginAt())
                .lastLoginAtLabel(user.getLastLoginAt() != null
                        ? user.getLastLoginAt().format(DATE_TIME_FORMATTER) : null)
                .build();
    }

    private String getRoleName(String code) {
        return switch (code) {
            case "GUEST" -> "게스트";
            case "DRAFTER" -> "기안자";
            case "APPROVER" -> "결재자";
            case "REVIEWER" -> "수신자";
            default -> code;
        };
    }

    private CompanyManagementItemDto toCompanyManagementItemDto(Company company) {
        IndustryInfoDto industryInfo = null;
        if (company.getIndustry() != null) {
            industryInfo = IndustryInfoDto.builder()
                    .code(company.getIndustry().getCode())
                    .name(company.getIndustry().getName())
                    .build();
        }

        int userCount = companyRepository.countUsersByCompanyId(company.getCompanyId());
        int diagnosticCount = companyRepository.countDiagnosticsByCompanyId(company.getCompanyId());

        return CompanyManagementItemDto.builder()
                .companyId(company.getCompanyId())
                .companyName(company.getName())
                .businessNumber(company.getBusinessNumber())
                .companyType(company.getCompanyType() != null ? company.getCompanyType().name() : null)
                .industry(industryInfo)
                .userCount(userCount)
                .diagnosticCount(diagnosticCount)
                .status(company.getStatus() != null ? company.getStatus().name() : "ACTIVE")
                .registeredAt(company.getCreatedAt())
                .build();
    }

    private ActivityLogItemDto toActivityLogItemDto(ActivityLog log) {
        User user = log.getUser();
        UserLogInfoDto userInfo = null;
        if (user != null) {
            userInfo = UserLogInfoDto.builder()
                    .userId(user.getUserId())
                    .name(user.getName())
                    .maskedUserName(NameMaskingUtil.mask(user.getName()))
                    .role(user.getRole() != null ? user.getRole().getCode() : null)
                    .build();
        }

        ActionInfoDto actionInfo = ActionInfoDto.builder()
                .code(log.getAction().name())
                .name(log.getAction().getDisplayName())
                .build();

        TargetInfoDto targetInfo = null;
        if (log.getTargetType() != null) {
            targetInfo = TargetInfoDto.builder()
                    .type(log.getTargetType())
                    .id(log.getTargetId())
                    .description(log.getDescription())
                    .build();
        }

        LogMetadataDto metadata = LogMetadataDto.builder()
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .build();

        return ActivityLogItemDto.builder()
                .logId(log.getLogId())
                .timestamp(log.getCreatedAt())
                .timestampLabel(log.getCreatedAt() != null ? log.getCreatedAt().format(DATE_TIME_FORMATTER) : null)
                .user(userInfo)
                .action(actionInfo)
                .target(targetInfo)
                .metadata(metadata)
                .build();
    }
}
