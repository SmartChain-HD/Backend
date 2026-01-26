package com.smartchain.platform.domain.role.controller;

import com.smartchain.platform.domain.role.service.RoleRequestService;
import com.smartchain.platform.dto.role.approval.RoleApprovalDetailResponse;
import com.smartchain.platform.dto.role.approval.RoleApprovalListResponse;
import com.smartchain.platform.dto.role.approval.RoleDecisionRequest;
import com.smartchain.platform.dto.role.approval.RoleDecisionResponse;
import com.smartchain.platform.dto.role.page.RoleRequestPageResponse;
import com.smartchain.platform.dto.role.request.RoleRequestCreateDto;
import com.smartchain.platform.dto.role.request.RoleRequestResponse;
import com.smartchain.platform.dto.role.request.RoleRequestStatusDto;
import com.smartchain.platform.global.response.BaseResponse;
import com.smartchain.platform.global.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Tag(name = "Role", description = "권한 관리 API")
public class RoleRequestController {

    private final RoleRequestService roleRequestService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "권한 요청 페이지 정보 조회", description = "권한 요청 페이지에 필요한 정보(현재 역할, 가능한 역할, 회사 목록, 대기중인 요청)를 조회합니다.")
    @GetMapping("/request-page")
    public ResponseEntity<BaseResponse<RoleRequestPageResponse>> getRequestPageInfo(HttpServletRequest request) {
        Long userId = extractUserIdFromRequest(request);
        RoleRequestPageResponse response = roleRequestService.getRequestPageInfo(userId);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "권한 요청 생성", description = "새로운 권한 요청을 생성합니다. 이미 대기중인 요청이 있으면 실패합니다.")
    @PostMapping("/requests")
    public ResponseEntity<BaseResponse<RoleRequestResponse>> createRoleRequest(
            HttpServletRequest request,
            @Valid @RequestBody RoleRequestCreateDto createDto) {
        Long userId = extractUserIdFromRequest(request);
        RoleRequestResponse response = roleRequestService.createRoleRequest(userId, createDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.success("권한 요청이 완료되었습니다", response));
    }

    @Operation(summary = "내 권한 요청 상태 조회", description = "현재 로그인된 사용자의 가장 최근 권한 요청 상태를 조회합니다.")
    @GetMapping("/requests/my")
    public ResponseEntity<BaseResponse<RoleRequestStatusDto>> getMyRequestStatus(HttpServletRequest request) {
        Long userId = extractUserIdFromRequest(request);
        RoleRequestStatusDto response = roleRequestService.getMyRequestStatus(userId);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    // ========== 관리자용 API (APPROVER, REVIEWER) ==========

    @Operation(summary = "권한 요청 목록 조회", description = "권한 요청 목록을 조회합니다. APPROVER는 자사만, REVIEWER는 전체 조회 가능합니다.")
    @GetMapping("/requests")
    public ResponseEntity<BaseResponse<RoleApprovalListResponse>> getRoleRequestList(
            HttpServletRequest request,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = extractUserIdFromRequest(request);
        RoleApprovalListResponse response = roleRequestService.getRoleRequestList(userId, status, companyId, page, size);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "권한 요청 상세 조회", description = "특정 권한 요청의 상세 정보를 조회합니다.")
    @GetMapping("/requests/{accessRequestId}")
    public ResponseEntity<BaseResponse<RoleApprovalDetailResponse>> getRoleRequestDetail(
            HttpServletRequest request,
            @PathVariable Long accessRequestId) {
        Long userId = extractUserIdFromRequest(request);
        RoleApprovalDetailResponse response = roleRequestService.getRoleRequestDetail(userId, accessRequestId);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "권한 요청 승인/반려", description = "권한 요청을 승인하거나 반려합니다. PENDING 상태에서만 처리 가능합니다.")
    @PatchMapping("/requests/{accessRequestId}")
    public ResponseEntity<BaseResponse<RoleDecisionResponse>> processRoleRequest(
            HttpServletRequest request,
            @PathVariable Long accessRequestId,
            @Valid @RequestBody RoleDecisionRequest decisionRequest) {
        Long userId = extractUserIdFromRequest(request);
        RoleDecisionResponse response = roleRequestService.processRoleRequest(userId, accessRequestId, decisionRequest);
        return ResponseEntity.ok(BaseResponse.success(response.getMessage(), response));
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
