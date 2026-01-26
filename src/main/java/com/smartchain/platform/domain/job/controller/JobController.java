package com.smartchain.platform.domain.job.controller;

import com.smartchain.platform.domain.job.service.JobService;
import com.smartchain.platform.dto.job.JobRetryResponse;
import com.smartchain.platform.dto.job.JobStatusResponse;
import com.smartchain.platform.global.response.BaseResponse;
import com.smartchain.platform.global.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
@Tag(name = "Job", description = "비동기 작업 관리 API")
public class JobController {

    private final JobService jobService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "작업 상태 조회", description = "비동기 작업의 상태를 조회합니다. 본인이 요청한 작업만 조회 가능합니다.")
    @GetMapping("/{jobId}")
    public ResponseEntity<BaseResponse<JobStatusResponse>> getJobStatus(
            HttpServletRequest request,
            @Parameter(description = "작업 ID")
            @PathVariable String jobId) {
        Long userId = extractUserIdFromRequest(request);
        JobStatusResponse response = jobService.getJobStatus(userId, jobId);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "작업 재시도", description = "실패한 작업을 재시도합니다. retryable=true인 작업만 재시도 가능합니다.")
    @PostMapping("/{jobId}/retry")
    public ResponseEntity<BaseResponse<JobRetryResponse>> retryJob(
            HttpServletRequest request,
            @Parameter(description = "작업 ID")
            @PathVariable String jobId) {
        Long userId = extractUserIdFromRequest(request);
        JobRetryResponse response = jobService.retryJob(userId, jobId);
        return ResponseEntity.accepted().body(BaseResponse.success(response));
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
