package com.smartchain.platform.domain.user.controller;

import com.smartchain.platform.domain.user.entity.Domain;
import com.smartchain.platform.domain.user.service.DomainService;
import com.smartchain.platform.dto.domain.DomainResponse;
import com.smartchain.platform.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 도메인 관리 컨트롤러
 * 공개 API - 인증 불필요
 */
@Tag(name = "Domain", description = "서비스 도메인 관리 (ESG, SAFETY, COMPLIANCE)")
@RestController
@RequestMapping("/api/v1/domains")
public class DomainController {

    private static final Logger log = LoggerFactory.getLogger(DomainController.class);

    private final DomainService domainService;

    public DomainController(DomainService domainService) {
        this.domainService = domainService;
    }

    @Operation(
        summary = "도메인 목록 조회",
        description = "활성화된 서비스 도메인 목록을 조회합니다. 인증 불필요."
    )
    @GetMapping
    public ResponseEntity<BaseResponse<List<DomainResponse>>> getDomainList(
        @Parameter(description = "전체 도메인 조회 여부 (true: 비활성 포함)")
        @RequestParam(required = false, defaultValue = "false") Boolean includeInactive
    ) {
        log.info("도메인 목록 조회 - includeInactive: {}", includeInactive);

        List<Domain> domains = includeInactive
            ? domainService.getAllDomains()
            : domainService.getActiveDomains();

        List<DomainResponse> response = domains.stream()
            .map(DomainResponse::from)
            .toList();

        return ResponseEntity.ok(BaseResponse.success("도메인 목록 조회 완료", response));
    }

    @Operation(
        summary = "도메인 상세 조회",
        description = "도메인 코드로 특정 도메인 정보를 조회합니다. 인증 불필요."
    )
    @GetMapping("/{code}")
    public ResponseEntity<BaseResponse<DomainResponse>> getDomainByCode(
        @Parameter(description = "도메인 코드 (ESG, SAFETY, COMPLIANCE)", example = "ESG")
        @PathVariable String code
    ) {
        log.info("도메인 상세 조회 - code: {}", code);

        Domain domain = domainService.getDomainByCode(code);
        DomainResponse response = DomainResponse.from(domain);

        return ResponseEntity.ok(BaseResponse.success("도메인 조회 완료", response));
    }
}
