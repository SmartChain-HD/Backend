package com.smartchain.platform.domain.ai.controller;

import com.smartchain.platform.domain.ai.entity.AiAnalysisResult;
import com.smartchain.platform.domain.ai.service.AiAnalysisService;
import com.smartchain.platform.dto.ai.AiAnalysisRequest;
import com.smartchain.platform.dto.ai.AiAnalysisResultDetailResponse;
import com.smartchain.platform.dto.ai.AiAnalysisResultResponse;
import com.smartchain.platform.dto.ai.AiPreviewRequest;
import com.smartchain.platform.dto.ai.run.RunPreviewResponse;
import com.smartchain.platform.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI 분석 컨트롤러
 */
@Tag(name = "AI Run Analysis", description = "AI Run API 기반 분석")
@RestController
@RequestMapping("/api/v1/ai/run/diagnostics/{diagnosticId}")
public class AiAnalysisController {

    private static final Logger log = LoggerFactory.getLogger(AiAnalysisController.class);

    private final AiAnalysisService aiAnalysisService;

    public AiAnalysisController(AiAnalysisService aiAnalysisService) {
        this.aiAnalysisService = aiAnalysisService;
    }

    @Operation(summary = "AI Run Preview", description = "파일 추가 시 슬롯 추정 및 필수 항목 현황 확인")
    @PostMapping("/preview")
    public ResponseEntity<BaseResponse<RunPreviewResponse>> preview(
        @PathVariable Long diagnosticId,
        @RequestBody AiPreviewRequest request
    ) {
        log.info("AI 분석 미리보기 요청 - diagnosticId: {}, fileIds: {}",
            diagnosticId, request.fileIds());

        RunPreviewResponse response = aiAnalysisService.preview(diagnosticId, request.fileIds());

        return ResponseEntity.ok(BaseResponse.success("슬롯 추정 완료", response));
    }

    @Operation(summary = "AI Run Submit", description = "전체 파일에 대한 AI 검증 요청 (비동기)")
    @PostMapping("/submit")
    public ResponseEntity<BaseResponse<Map<String, Object>>> requestAnalysis(
        @PathVariable Long diagnosticId,
        @RequestBody(required = false) AiAnalysisRequest request
    ) {
        log.info("AI 분석 요청 - diagnosticId: {}", diagnosticId);

        // 비동기로 분석 시작
        aiAnalysisService.submitAsync(diagnosticId);

        Map<String, Object> response = Map.of(
            "diagnosticId", diagnosticId,
            "status", "PENDING",
            "message", "AI 분석이 시작되었습니다. 결과는 조회 API로 확인하세요."
        );

        return ResponseEntity.accepted()
            .body(BaseResponse.success("AI 분석 요청이 접수되었습니다", response));
    }

    @Operation(summary = "AI Run 결과 조회", description = "진단의 최신 AI Run 분석 결과 조회")
    @GetMapping("/result")
    public ResponseEntity<BaseResponse<AiAnalysisResultResponse>> getLatestResult(
        @PathVariable Long diagnosticId
    ) {
        log.info("AI 분석 결과 조회 - diagnosticId: {}", diagnosticId);

        AiAnalysisResult result = aiAnalysisService.getLatestResult(diagnosticId);
        AiAnalysisResultResponse response = AiAnalysisResultResponse.from(result);

        return ResponseEntity.ok(BaseResponse.success("분석 결과 조회 완료", response));
    }

    @Operation(summary = "AI Run 결과 상세 조회", description = "진단의 최신 AI Run 분석 결과를 슬롯별로 파싱하여 조회")
    @GetMapping("/result/detail")
    public ResponseEntity<BaseResponse<AiAnalysisResultDetailResponse>> getLatestResultDetail(
        @PathVariable Long diagnosticId
    ) {
        log.info("AI 분석 결과 상세 조회 - diagnosticId: {}", diagnosticId);

        AiAnalysisResult result = aiAnalysisService.getLatestResult(diagnosticId);
        AiAnalysisResultDetailResponse response = AiAnalysisResultDetailResponse.from(result);

        return ResponseEntity.ok(BaseResponse.success("분석 결과 상세 조회 완료", response));
    }

    @Operation(summary = "AI Run 이력 조회", description = "진단의 AI Run 분석 이력 전체 조회")
    @GetMapping("/history")
    public ResponseEntity<BaseResponse<List<AiAnalysisResultResponse>>> getAnalysisHistory(
        @PathVariable Long diagnosticId
    ) {
        log.info("AI 분석 이력 조회 - diagnosticId: {}", diagnosticId);

        List<AiAnalysisResult> results = aiAnalysisService.getAnalysisHistory(diagnosticId);
        List<AiAnalysisResultResponse> response = results.stream()
            .map(AiAnalysisResultResponse::from)
            .toList();

        return ResponseEntity.ok(BaseResponse.success("분석 이력 조회 완료", response));
    }
}
