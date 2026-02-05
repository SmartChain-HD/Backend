package com.smartchain.platform.domain.risk.controller;

import com.smartchain.platform.domain.risk.service.ExternalRiskService;
import com.smartchain.platform.dto.common.page.PageDto;
import com.smartchain.platform.dto.common.page.PagedResponse;
import com.smartchain.platform.dto.risk.ExternalRiskDetectApiRequest;
import com.smartchain.platform.dto.risk.ExternalRiskResultResponse;
import com.smartchain.platform.global.response.BaseResponse;
import com.smartchain.platform.global.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "External Risk Detection", description = "외부 위험 감지 API (REVIEWER 전용)")
@RestController
@RequestMapping("/api/v1/risk/external")
@RequiredArgsConstructor
public class ExternalRiskController {

    private final ExternalRiskService externalRiskService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "협력사 리스크 분석 요청", description = "외부 AI API를 통해 협력사 리스크를 분석합니다. REVIEWER 권한이 필요합니다.")
    @PostMapping("/detect")
    public ResponseEntity<BaseResponse<List<ExternalRiskResultResponse>>> detect(
            HttpServletRequest request,
            @Valid @RequestBody ExternalRiskDetectApiRequest apiRequest) {
        Long userId = extractUserIdFromRequest(request);
        List<ExternalRiskResultResponse> results = externalRiskService.detect(userId, apiRequest.companyIds());
        return ResponseEntity.ok(BaseResponse.success("외부 위험 감지 완료", results));
    }

    @Operation(summary = "특정 협력사 최신 리스크 결과 조회", description = "특정 협력사의 최신 외부 리스크 분석 결과를 조회합니다.")
    @GetMapping("/results/{companyId}")
    public ResponseEntity<BaseResponse<ExternalRiskResultResponse>> getLatestResult(
            HttpServletRequest request,
            @Parameter(description = "회사 ID") @PathVariable Long companyId) {
        Long userId = extractUserIdFromRequest(request);
        ExternalRiskResultResponse result = externalRiskService.getLatestResult(userId, companyId);
        return ResponseEntity.ok(BaseResponse.success("리스크 결과 조회 완료", result));
    }

    @Operation(summary = "전체 리스크 결과 이력 조회", description = "전체 외부 리스크 분석 결과를 페이지네이션으로 조회합니다.")
    @GetMapping("/results")
    public ResponseEntity<BaseResponse<PagedResponse<ExternalRiskResultResponse>>> getAllResults(
            HttpServletRequest request,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size) {
        Long userId = extractUserIdFromRequest(request);
        Pageable pageable = PageRequest.of(page, size);
        Page<ExternalRiskResultResponse> resultPage = externalRiskService.getAllResults(userId, pageable);

        PagedResponse<ExternalRiskResultResponse> response = PagedResponse.<ExternalRiskResultResponse>builder()
            .content(resultPage.getContent())
            .page(PageDto.builder()
                .number(resultPage.getNumber())
                .size(resultPage.getSize())
                .totalElements(resultPage.getTotalElements())
                .totalPages(resultPage.getTotalPages())
                .build())
            .build();

        return ResponseEntity.ok(BaseResponse.success("리스크 결과 이력 조회 완료", response));
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
