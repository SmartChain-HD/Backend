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
import com.smartchain.platform.dto.role.approval.RoleApprovalItemDto;
import com.smartchain.platform.dto.role.approval.RoleApprovalListResponse;
import com.smartchain.platform.dto.role.approval.RoleDecisionRequest;
import com.smartchain.platform.dto.role.approval.RoleDecisionResponse;
import com.smartchain.platform.dto.role.common.CompanySimpleDto;
import com.smartchain.platform.dto.role.common.DomainSimpleDto;
import com.smartchain.platform.dto.role.common.PageDto;
import com.smartchain.platform.dto.role.common.ProcessedByDto;
import com.smartchain.platform.dto.role.common.RoleSimpleDto;
import com.smartchain.platform.dto.role.common.UserSimpleDto;
import com.smartchain.platform.dto.role.page.CompanyOptionDto;
import com.smartchain.platform.dto.role.page.DomainOptionDto;
import com.smartchain.platform.dto.role.page.RoleOptionDto;
import com.smartchain.platform.dto.role.page.RoleRequestPageResponse;
import com.smartchain.platform.dto.role.request.RoleRequestCreateDto;
import com.smartchain.platform.dto.role.request.RoleRequestResponse;
import com.smartchain.platform.dto.role.request.RoleRequestStatusDto;
import com.smartchain.platform.global.enums.RequestStatus;
import com.smartchain.platform.global.error.CustomException;
import com.smartchain.platform.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleRequestService {

    private final RoleRequestRepository roleRequestRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;
    private final DomainRepository domainRepository;
    private final UserDomainRoleRepository userDomainRoleRepository;

    private static final List<String> REQUESTABLE_ROLES = Arrays.asList("DRAFTER", "APPROVER", "REVIEWER");

    private static final Map<String, String> ROLE_NAME_MAP = Map.of(
            "GUEST", "게스트",
            "DRAFTER", "기안자",
            "APPROVER", "결재자",
            "REVIEWER", "수신자"
    );

    private static final Map<String, String> ROLE_DESCRIPTION_MAP = Map.of(
            "DRAFTER", "ESG 자가 진단 데이터 업로드",
            "APPROVER", "회사 정보 관리, ESG 파일 관리, 협력사 권한 관리",
            "REVIEWER", "심사, 보고서 발행, 권한 관리"
    );

    private static final Map<String, String> STATUS_LABEL_MAP = Map.of(
            "PENDING", "승인 대기중",
            "APPROVED", "승인 완료",
            "REJECTED", "승인 반려"
    );

    public RoleRequestPageResponse getRequestPageInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String currentRoleCode = user.getRole() != null ? user.getRole().getCode() : "GUEST";
        String currentRoleName = ROLE_NAME_MAP.getOrDefault(currentRoleCode, currentRoleCode);

        RoleSimpleDto currentRole = RoleSimpleDto.builder()
                .code(currentRoleCode)
                .name(currentRoleName)
                .build();

        List<RoleOptionDto> availableRoles = REQUESTABLE_ROLES.stream()
                .map(roleCode -> RoleOptionDto.builder()
                        .roleCode(roleCode)
                        .roleName(ROLE_NAME_MAP.get(roleCode))
                        .description(ROLE_DESCRIPTION_MAP.get(roleCode))
                        .iconUrl("/icons/" + roleCode.toLowerCase() + ".svg")
                        .selectable(true)
                        .build())
                .toList();

        List<CompanyOptionDto> availableCompanies = companyRepository.findAll().stream()
                .map(company -> CompanyOptionDto.builder()
                        .companyId(company.getCompanyId())
                        .companyName(company.getName())
                        .companyType(company.getScale())
                        .build())
                .toList();

        List<DomainOptionDto> availableDomains = domainRepository.findByIsActiveTrue().stream()
                .map(domain -> DomainOptionDto.builder()
                        .domainId(domain.getDomainId())
                        .domainCode(domain.getCode())
                        .domainName(domain.getName())
                        .description(domain.getDescription())
                        .selectable(true)
                        .build())
                .toList();

        RoleRequestStatusDto pendingRequest = null;
        Optional<RoleRequest> pendingRequestOpt = roleRequestRepository.findByUserAndStatus(user, RequestStatus.PENDING);
        if (pendingRequestOpt.isPresent()) {
            pendingRequest = mapToStatusDto(pendingRequestOpt.get());
        }

        return RoleRequestPageResponse.builder()
                .currentRole(currentRole)
                .availableRoles(availableRoles)
                .availableDomains(availableDomains)
                .availableCompanies(availableCompanies)
                .pendingRequest(pendingRequest)
                .build();
    }

    @Transactional
    public RoleRequestResponse createRoleRequest(Long userId, RoleRequestCreateDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!REQUESTABLE_ROLES.contains(request.getRequestedRole())) {
            throw new CustomException(ErrorCode.INVALID_ROLE_REQUEST);
        }

        Domain domain = domainRepository.findById(request.getDomainId())
                .orElseThrow(() -> new CustomException(ErrorCode.DOMAIN_NOT_FOUND));

        // APPROVER는 ESG 도메인에서만 허용
        if ("APPROVER".equals(request.getRequestedRole()) && !"ESG".equals(domain.getCode())) {
            throw new CustomException(ErrorCode.APPROVER_ONLY_ESG);
        }

        // 해당 도메인에 이미 권한이 있는지 확인
        if (userDomainRoleRepository.existsByUserUserIdAndDomainDomainId(userId, request.getDomainId())) {
            throw new CustomException(ErrorCode.DUPLICATE_DOMAIN_ROLE);
        }

        // 해당 도메인에 대기 중인 요청이 있는지 확인
        if (roleRequestRepository.existsByUserAndDomainAndStatus(user, domain, RequestStatus.PENDING)) {
            throw new CustomException(ErrorCode.DUPLICATE_ROLE_REQUEST);
        }

        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new CustomException(ErrorCode.COMPANY_NOT_FOUND));

        RoleRequest roleRequest = RoleRequest.builder()
                .user(user)
                .company(company)
                .domain(domain)
                .requestedRole(request.getRequestedRole())
                .reason(request.getReason())
                .status(RequestStatus.PENDING)
                .build();

        RoleRequest savedRequest = roleRequestRepository.save(roleRequest);

        log.info("Role request created: userId={}, requestedRole={}, domainId={}, companyId={}",
                userId, request.getRequestedRole(), request.getDomainId(), request.getCompanyId());

        return RoleRequestResponse.builder()
                .accessRequestId(savedRequest.getRequestId())
                .status(savedRequest.getStatus().name())
                .requestedRole(savedRequest.getRequestedRole())
                .companyId(company.getCompanyId())
                .createdAt(savedRequest.getCreatedAt())
                .message("권한 요청이 완료되었습니다")
                .build();
    }

    public RoleRequestStatusDto getMyRequestStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        RoleRequest roleRequest = roleRequestRepository.findTopByUserOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new CustomException(ErrorCode.ROLE_REQUEST_NOT_FOUND));

        return mapToStatusDto(roleRequest);
    }

    private RoleRequestStatusDto mapToStatusDto(RoleRequest roleRequest) {
        RoleSimpleDto requestedRole = RoleSimpleDto.builder()
                .code(roleRequest.getRequestedRole())
                .name(ROLE_NAME_MAP.getOrDefault(roleRequest.getRequestedRole(), roleRequest.getRequestedRole()))
                .build();

        DomainSimpleDto domainDto = null;
        if (roleRequest.getDomain() != null) {
            domainDto = DomainSimpleDto.builder()
                    .domainId(roleRequest.getDomain().getDomainId())
                    .code(roleRequest.getDomain().getCode())
                    .name(roleRequest.getDomain().getName())
                    .build();
        }

        CompanySimpleDto company = null;
        if (roleRequest.getCompany() != null) {
            company = CompanySimpleDto.builder()
                    .companyId(roleRequest.getCompany().getCompanyId())
                    .companyName(roleRequest.getCompany().getName())
                    .build();
        }

        ProcessedByDto processedByDto = null;
        if (roleRequest.getProcessedBy() != null) {
            processedByDto = ProcessedByDto.builder()
                    .userId(roleRequest.getProcessedBy().getUserId())
                    .name(roleRequest.getProcessedBy().getName())
                    .build();
        }

        return RoleRequestStatusDto.builder()
                .accessRequestId(roleRequest.getRequestId())
                .requestedRole(requestedRole)
                .domain(domainDto)
                .company(company)
                .status(roleRequest.getStatus().name())
                .statusLabel(STATUS_LABEL_MAP.getOrDefault(roleRequest.getStatus().name(), roleRequest.getStatus().name()))
                .reason(roleRequest.getReason())
                .requestedAt(roleRequest.getCreatedAt())
                .processedAt(roleRequest.getDecidedAt())
                .processedBy(processedByDto)
                .rejectReason(roleRequest.getRejectReason())
                .build();
    }

    // ========== 관리자용 API (APPROVER, REVIEWER) ==========

    private static final List<String> MANAGER_ROLES = Arrays.asList("APPROVER", "REVIEWER");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yy.MM.dd");

    public RoleApprovalListResponse getRoleRequestList(Long userId, String status, Long companyId, int page, int size) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String userRoleCode = currentUser.getRole() != null ? currentUser.getRole().getCode() : "GUEST";
        if (!MANAGER_ROLES.contains(userRoleCode)) {
            throw new CustomException(ErrorCode.PERMISSION_DENIED_ACTION);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<RoleRequest> requestPage;

        boolean isReviewer = "REVIEWER".equals(userRoleCode);

        if (isReviewer) {
            // REVIEWER: 전체 협력사 조회 가능, companyId 필터 사용 가능
            if (companyId != null) {
                Company filterCompany = companyRepository.findById(companyId)
                        .orElseThrow(() -> new CustomException(ErrorCode.COMPANY_NOT_FOUND));
                requestPage = getRequestsByCompanyAndStatus(filterCompany, status, pageable);
            } else {
                requestPage = getRequestsByStatus(status, pageable);
            }
        } else {
            // APPROVER: 자사 소속 사용자의 권한 요청만 조회
            Company userCompany = currentUser.getCompany();
            if (userCompany == null) {
                throw new CustomException(ErrorCode.PERMISSION_DENIED_RESOURCE);
            }
            requestPage = getRequestsByCompanyAndStatus(userCompany, status, pageable);
        }

        List<RoleApprovalItemDto> content = requestPage.getContent().stream()
                .map(this::mapToApprovalItemDto)
                .toList();

        PageDto pageDto = PageDto.builder()
                .number(requestPage.getNumber())
                .size(requestPage.getSize())
                .totalElements(requestPage.getTotalElements())
                .totalPages(requestPage.getTotalPages())
                .build();

        return RoleApprovalListResponse.builder()
                .content(content)
                .page(pageDto)
                .build();
    }

    private Page<RoleRequest> getRequestsByStatus(String status, Pageable pageable) {
        if (status != null && !status.isEmpty()) {
            RequestStatus requestStatus = parseStatus(status);
            return roleRequestRepository.findByStatusOrderByCreatedAtDesc(requestStatus, pageable);
        }
        return roleRequestRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    private Page<RoleRequest> getRequestsByCompanyAndStatus(Company company, String status, Pageable pageable) {
        if (status != null && !status.isEmpty()) {
            RequestStatus requestStatus = parseStatus(status);
            return roleRequestRepository.findByCompanyAndStatusOrderByCreatedAtDesc(company, requestStatus, pageable);
        }
        return roleRequestRepository.findByCompanyOrderByCreatedAtDesc(company, pageable);
    }

    private RequestStatus parseStatus(String status) {
        try {
            return RequestStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
    }

    private RoleApprovalItemDto mapToApprovalItemDto(RoleRequest roleRequest) {
        UserSimpleDto userDto = UserSimpleDto.builder()
                .userId(roleRequest.getUser().getUserId())
                .name(roleRequest.getUser().getName())
                .email(roleRequest.getUser().getEmail())
                .build();

        DomainSimpleDto domainDto = null;
        if (roleRequest.getDomain() != null) {
            domainDto = DomainSimpleDto.builder()
                    .domainId(roleRequest.getDomain().getDomainId())
                    .code(roleRequest.getDomain().getCode())
                    .name(roleRequest.getDomain().getName())
                    .build();
        }

        CompanySimpleDto companyDto = null;
        if (roleRequest.getCompany() != null) {
            companyDto = CompanySimpleDto.builder()
                    .companyId(roleRequest.getCompany().getCompanyId())
                    .companyName(roleRequest.getCompany().getName())
                    .build();
        }

        RoleSimpleDto requestedRole = RoleSimpleDto.builder()
                .code(roleRequest.getRequestedRole())
                .name(ROLE_NAME_MAP.getOrDefault(roleRequest.getRequestedRole(), roleRequest.getRequestedRole()))
                .build();

        String requestedAtLabel = roleRequest.getCreatedAt() != null
                ? roleRequest.getCreatedAt().format(DATE_FORMATTER)
                : null;

        return RoleApprovalItemDto.builder()
                .accessRequestId(roleRequest.getRequestId())
                .user(userDto)
                .domain(domainDto)
                .company(companyDto)
                .requestedRole(requestedRole)
                .status(roleRequest.getStatus().name())
                .reason(roleRequest.getReason())
                .requestedAt(roleRequest.getCreatedAt())
                .requestedAtLabel(requestedAtLabel)
                .build();
    }

    public RoleApprovalDetailResponse getRoleRequestDetail(Long userId, Long accessRequestId) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String userRoleCode = currentUser.getRole() != null ? currentUser.getRole().getCode() : "GUEST";
        if (!MANAGER_ROLES.contains(userRoleCode)) {
            throw new CustomException(ErrorCode.PERMISSION_DENIED_ACTION);
        }

        RoleRequest roleRequest = roleRequestRepository.findById(accessRequestId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROLE_REQUEST_NOT_FOUND));

        // APPROVER인 경우 자사 요청만 조회 가능
        if ("APPROVER".equals(userRoleCode)) {
            Company userCompany = currentUser.getCompany();
            if (userCompany == null || !userCompany.getCompanyId().equals(roleRequest.getCompany().getCompanyId())) {
                throw new CustomException(ErrorCode.PERMISSION_DENIED_RESOURCE);
            }
        }

        UserSimpleDto userDto = UserSimpleDto.builder()
                .userId(roleRequest.getUser().getUserId())
                .name(roleRequest.getUser().getName())
                .email(roleRequest.getUser().getEmail())
                .build();

        DomainSimpleDto domainDto = null;
        if (roleRequest.getDomain() != null) {
            domainDto = DomainSimpleDto.builder()
                    .domainId(roleRequest.getDomain().getDomainId())
                    .code(roleRequest.getDomain().getCode())
                    .name(roleRequest.getDomain().getName())
                    .build();
        }

        CompanySimpleDto companyDto = null;
        if (roleRequest.getCompany() != null) {
            companyDto = CompanySimpleDto.builder()
                    .companyId(roleRequest.getCompany().getCompanyId())
                    .companyName(roleRequest.getCompany().getName())
                    .build();
        }

        RoleSimpleDto requestedRole = RoleSimpleDto.builder()
                .code(roleRequest.getRequestedRole())
                .name(ROLE_NAME_MAP.getOrDefault(roleRequest.getRequestedRole(), roleRequest.getRequestedRole()))
                .build();

        return RoleApprovalDetailResponse.builder()
                .accessRequestId(roleRequest.getRequestId())
                .user(userDto)
                .domain(domainDto)
                .requestedRole(requestedRole)
                .company(companyDto)
                .status(roleRequest.getStatus().name())
                .reason(roleRequest.getReason())
                .requestedAt(roleRequest.getCreatedAt())
                .build();
    }

    @Transactional
    public RoleDecisionResponse processRoleRequest(Long userId, Long accessRequestId, RoleDecisionRequest request) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String userRoleCode = currentUser.getRole() != null ? currentUser.getRole().getCode() : "GUEST";
        if (!MANAGER_ROLES.contains(userRoleCode)) {
            throw new CustomException(ErrorCode.PERMISSION_DENIED_ACTION);
        }

        RoleRequest roleRequest = roleRequestRepository.findById(accessRequestId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROLE_REQUEST_NOT_FOUND));

        // APPROVER인 경우 자사 요청만 처리 가능
        if ("APPROVER".equals(userRoleCode)) {
            Company userCompany = currentUser.getCompany();
            if (userCompany == null || !userCompany.getCompanyId().equals(roleRequest.getCompany().getCompanyId())) {
                throw new CustomException(ErrorCode.PERMISSION_DENIED_RESOURCE);
            }
        }

        // 이미 처리된 요청인지 확인
        if (!roleRequest.isPending()) {
            throw new CustomException(ErrorCode.ALREADY_PROCESSED_REQUEST);
        }

        String decision = request.getDecision().toUpperCase();
        String message;

        if ("APPROVED".equals(decision)) {
            roleRequest.approve(currentUser);

            // UserDomainRole 생성 (도메인별 권한 부여)
            if (roleRequest.getDomain() != null) {
                Role role = roleRepository.findByCode(roleRequest.getRequestedRole())
                        .orElseThrow(() -> new CustomException(ErrorCode.ROLE_NOT_FOUND));

                UserDomainRole userDomainRole = UserDomainRole.builder()
                        .user(roleRequest.getUser())
                        .domain(roleRequest.getDomain())
                        .role(role)
                        .build();

                userDomainRoleRepository.save(userDomainRole);
                log.info("UserDomainRole created: userId={}, domainId={}, roleCode={}",
                        roleRequest.getUser().getUserId(),
                        roleRequest.getDomain().getDomainId(),
                        roleRequest.getRequestedRole());

                // GUEST인 경우 전역 역할도 업그레이드
                User requestUser = roleRequest.getUser();
                if (requestUser.getRole() != null && "GUEST".equals(requestUser.getRole().getCode())) {
                    requestUser.changeRole(role);
                    log.info("User global role upgraded from GUEST to {}: userId={}",
                            roleRequest.getRequestedRole(), requestUser.getUserId());
                }
            }

            message = "권한 요청이 승인되었습니다";
            log.info("Role request approved: requestId={}, approvedBy={}", accessRequestId, userId);
        } else if ("REJECTED".equals(decision)) {
            roleRequest.reject(currentUser, request.getRejectReason());
            message = "권한 요청이 반려되었습니다";
            log.info("Role request rejected: requestId={}, rejectedBy={}, reason={}",
                    accessRequestId, userId, request.getRejectReason());
        } else {
            throw new CustomException(ErrorCode.INVALID_DECISION);
        }

        ProcessedByDto processedByDto = ProcessedByDto.builder()
                .userId(currentUser.getUserId())
                .name(currentUser.getName())
                .build();

        return RoleDecisionResponse.builder()
                .accessRequestId(roleRequest.getRequestId())
                .status(roleRequest.getStatus().name())
                .processedAt(roleRequest.getDecidedAt())
                .processedBy(processedByDto)
                .message(message)
                .build();
    }
}
