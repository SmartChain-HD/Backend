package com.smartchain.platform.domain.management.controller;

import com.smartchain.platform.domain.management.service.ManagementService;
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
import com.smartchain.platform.global.response.BaseResponse;
import com.smartchain.platform.global.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/management")
@RequiredArgsConstructor
@Tag(name = "Management", description = "사용자/권한 관리 API (REVIEWER 전용)")
public class ManagementController {

    private final ManagementService managementService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "권한 대시보드 조회", description = "권한 요청 통계와 목록을 조회합니다. REVIEWER 권한이 필요합니다.")
    @GetMapping("/permissions/dashboard")
    public ResponseEntity<BaseResponse<PermissionDashboardResponse>> getPermissionDashboard(
            HttpServletRequest request,
            @Parameter(description = "회사 ID 필터")
            @RequestParam(required = false) Long companyId,
            @Parameter(description = "요청 역할 필터 (DRAFTER, APPROVER, REVIEWER)")
            @RequestParam(required = false) String requestedRole,
            @Parameter(description = "상태 필터 (PENDING, APPROVED, REJECTED)")
            @RequestParam(required = false) String status,
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "10") int size) {
        Long userId = extractUserIdFromRequest(request);
        PermissionDashboardResponse response = managementService.getPermissionDashboard(userId, companyId, requestedRole, status, page, size);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "권한 요청 처리", description = "권한 요청을 승인 또는 반려합니다. REVIEWER 권한이 필요합니다.")
    @PatchMapping("/permissions/{accessRequestId}")
    public ResponseEntity<BaseResponse<PermissionDecisionResponse>> processPermissionRequest(
            HttpServletRequest request,
            @Parameter(description = "권한 요청 ID")
            @PathVariable Long accessRequestId,
            @Valid @RequestBody PermissionDecisionRequest decisionRequest) {
        Long userId = extractUserIdFromRequest(request);
        PermissionDecisionResponse response = managementService.processPermissionRequest(userId, accessRequestId, decisionRequest);
        return ResponseEntity.ok(BaseResponse.success(response.getMessage(), response));
    }

    @Operation(summary = "사용자 관리 목록 조회", description = "사용자 통계와 목록을 조회합니다. REVIEWER 권한이 필요합니다.")
    @GetMapping("/users")
    public ResponseEntity<BaseResponse<UserManagementResponse>> getUserList(
            HttpServletRequest request,
            @Parameter(description = "역할 필터 (GUEST, DRAFTER, APPROVER, REVIEWER)")
            @RequestParam(required = false) String role,
            @Parameter(description = "상태 필터 (ACTIVE, INACTIVE)")
            @RequestParam(required = false) String status,
            @Parameter(description = "회사 ID 필터")
            @RequestParam(required = false) Long companyId,
            @Parameter(description = "이름/이메일 검색")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "20") int size) {
        Long userId = extractUserIdFromRequest(request);
        UserManagementResponse response = managementService.getUserList(userId, role, status, companyId, keyword, page, size);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "사용자 역할 변경", description = "사용자의 역할을 변경합니다. REVIEWER 권한이 필요합니다.")
    @PatchMapping("/users/{userId}/role")
    public ResponseEntity<BaseResponse<RoleChangeResponse>> changeUserRole(
            HttpServletRequest request,
            @Parameter(description = "대상 사용자 ID")
            @PathVariable Long userId,
            @Valid @RequestBody RoleChangeRequest roleChangeRequest) {
        Long reviewerId = extractUserIdFromRequest(request);
        RoleChangeResponse response = managementService.changeUserRole(reviewerId, userId, roleChangeRequest);
        return ResponseEntity.ok(BaseResponse.success(response.getMessage(), response));
    }

    @Operation(summary = "사용자 상태 변경", description = "사용자의 상태를 변경합니다. REVIEWER 권한이 필요합니다.")
    @PatchMapping("/users/{userId}/status")
    public ResponseEntity<BaseResponse<UserStatusChangeResponse>> changeUserStatus(
            HttpServletRequest request,
            @Parameter(description = "대상 사용자 ID")
            @PathVariable Long userId,
            @Valid @RequestBody UserStatusChangeRequest statusChangeRequest) {
        Long reviewerId = extractUserIdFromRequest(request);
        UserStatusChangeResponse response = managementService.changeUserStatus(reviewerId, userId, statusChangeRequest);
        return ResponseEntity.ok(BaseResponse.success(response.getMessage(), response));
    }

    // ========== 협력사 관리 API ==========

    @Operation(summary = "협력사 목록 조회", description = "협력사 목록을 조회합니다. REVIEWER 권한이 필요합니다.")
    @GetMapping("/companies")
    public ResponseEntity<BaseResponse<CompanyManagementResponse>> getCompanyList(
            HttpServletRequest request,
            @Parameter(description = "협력사 유형 (TIER1, TIER2)")
            @RequestParam(required = false) String companyType,
            @Parameter(description = "업종 코드")
            @RequestParam(required = false) String industryCode,
            @Parameter(description = "회사명 검색")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "20") int size) {
        Long userId = extractUserIdFromRequest(request);
        CompanyManagementResponse response = managementService.getCompanyList(userId, companyType, industryCode, keyword, page, size);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "협력사 등록", description = "새로운 협력사를 등록합니다. REVIEWER 권한이 필요합니다.")
    @PostMapping("/companies")
    public ResponseEntity<BaseResponse<CompanyRegisterResponse>> registerCompany(
            HttpServletRequest request,
            @Valid @RequestBody CompanyRegisterRequest registerRequest) {
        Long userId = extractUserIdFromRequest(request);
        CompanyRegisterResponse response = managementService.registerCompany(userId, registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success("협력사가 등록되었습니다", response));
    }

    // ========== 활동 로그 API ==========

    @Operation(summary = "활동 로그 조회", description = "활동 로그를 조회합니다. REVIEWER 권한이 필요합니다.")
    @GetMapping("/activity-logs")
    public ResponseEntity<BaseResponse<ActivityLogResponse>> getActivityLogs(
            HttpServletRequest request,
            @Parameter(description = "특정 사용자 ID")
            @RequestParam(required = false) Long userId,
            @Parameter(description = "기간 시작 (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "기간 종료 (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "액션 유형 (LOGIN, UPLOAD, SUBMIT 등)")
            @RequestParam(required = false) String actionType,
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "50") int size) {
        Long reviewerId = extractUserIdFromRequest(request);
        ActivityLogResponse response = managementService.getActivityLogs(reviewerId, userId, fromDate, toDate, actionType, page, size);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "활동 로그 내보내기", description = "활동 로그를 CSV 또는 Excel로 내보냅니다. 비동기 처리됩니다. REVIEWER 권한이 필요합니다.")
    @PostMapping("/activity-logs/export")
    public ResponseEntity<BaseResponse<ActivityLogExportResponse>> exportActivityLogs(
            HttpServletRequest request,
            @Valid @RequestBody ActivityLogExportRequest exportRequest) {
        Long userId = extractUserIdFromRequest(request);
        ActivityLogExportResponse response = managementService.exportActivityLogs(userId, exportRequest);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(BaseResponse.success("활동 로그 내보내기가 시작되었습니다", response));
    }

    private Long extractUserIdFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            return jwtTokenProvider.getUserIdFromToken(token);
        }
        throw new IllegalArgumentException("Authorization header is missing or invalid");
    }
}
