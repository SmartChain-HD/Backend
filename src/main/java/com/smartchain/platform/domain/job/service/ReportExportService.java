package com.smartchain.platform.domain.job.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartchain.platform.domain.job.entity.AsyncJob;
import com.smartchain.platform.domain.job.repository.AsyncJobRepository;
import com.smartchain.platform.domain.review.entity.Review;
import com.smartchain.platform.domain.review.repository.ReviewRepository;
import com.smartchain.platform.domain.user.entity.User;
import com.smartchain.platform.domain.user.repository.UserRepository;
import com.smartchain.platform.dto.review.export.ExportRequest;
import com.smartchain.platform.dto.review.export.ExportResponse;
import com.smartchain.platform.dto.review.report.BulkReportRequest;
import com.smartchain.platform.dto.review.report.BulkReportResponse;
import com.smartchain.platform.dto.review.report.ReportPublishRequest;
import com.smartchain.platform.dto.review.report.ReportPublishResponse;
import com.smartchain.platform.global.enums.JobType;
import com.smartchain.platform.global.error.CustomException;
import com.smartchain.platform.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportExportService {

    private final AsyncJobRepository asyncJobRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    private static final int REPORT_ESTIMATED_SECONDS = 60;
    private static final int BULK_REPORT_ESTIMATED_SECONDS_PER_ITEM = 30;
    private static final int EXPORT_ESTIMATED_SECONDS = 30;

    /**
     * 단일 보고서 생성 (비동기)
     */
    @Transactional
    public ReportPublishResponse createReport(Long userId, Long reviewId, ReportPublishRequest request) {
        User currentUser = validateReviewerRole(userId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

        String requestPayload = serializeToJson(request);

        AsyncJob job = AsyncJob.builder()
                .jobType(JobType.REPORT_GENERATION)
                .requesterId(userId)
                .targetId(reviewId)
                .requestPayload(requestPayload)
                .estimatedSeconds(REPORT_ESTIMATED_SECONDS)
                .build();

        asyncJobRepository.save(job);

        log.info("Report generation job created: jobId={}, reviewId={}, requesterId={}",
                job.getJobId(), reviewId, userId);

        return ReportPublishResponse.builder()
                .jobId(job.getJobId())
                .status(job.getStatus().name())
                .statusCheckUrl(job.getStatusCheckUrl())
                .estimatedCompletionTime(REPORT_ESTIMATED_SECONDS)
                .message("보고서 생성이 시작되었습니다")
                .build();
    }

    /**
     * 일괄 보고서 생성 (비동기)
     */
    @Transactional
    public BulkReportResponse createBulkReport(Long userId, BulkReportRequest request) {
        User currentUser = validateReviewerRole(userId);

        if (request.getReviewIds() == null || request.getReviewIds().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "심사 ID 목록이 필요합니다");
        }

        // 모든 reviewId가 유효한지 검증
        for (Long reviewId : request.getReviewIds()) {
            if (!reviewRepository.existsById(reviewId)) {
                throw new CustomException(ErrorCode.REVIEW_NOT_FOUND, "심사 ID " + reviewId + "를 찾을 수 없습니다");
            }
        }

        String requestPayload = serializeToJson(request);
        int estimatedSeconds = request.getReviewIds().size() * BULK_REPORT_ESTIMATED_SECONDS_PER_ITEM;

        AsyncJob job = AsyncJob.builder()
                .jobType(JobType.BULK_REPORT)
                .requesterId(userId)
                .requestPayload(requestPayload)
                .estimatedSeconds(estimatedSeconds)
                .build();

        asyncJobRepository.save(job);

        log.info("Bulk report job created: jobId={}, count={}, requesterId={}",
                job.getJobId(), request.getReviewIds().size(), userId);

        return BulkReportResponse.builder()
                .jobId(job.getJobId())
                .totalCount(request.getReviewIds().size())
                .statusCheckUrl(job.getStatusCheckUrl())
                .message("일괄 보고서 생성이 시작되었습니다")
                .build();
    }

    /**
     * CSV/Excel 내보내기 (비동기)
     */
    @Transactional
    public ExportResponse exportReviews(Long userId, ExportRequest request) {
        User currentUser = validateReviewerRole(userId);

        if (request.getFormat() == null || request.getFormat().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "내보내기 형식이 필요합니다");
        }

        String format = request.getFormat().toUpperCase();
        if (!format.equals("CSV") && !format.equals("XLSX")) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "지원하지 않는 형식입니다. CSV 또는 XLSX를 사용하세요");
        }

        String requestPayload = serializeToJson(request);

        AsyncJob job = AsyncJob.builder()
                .jobType(JobType.EXPORT)
                .requesterId(userId)
                .requestPayload(requestPayload)
                .estimatedSeconds(EXPORT_ESTIMATED_SECONDS)
                .build();

        asyncJobRepository.save(job);

        log.info("Export job created: jobId={}, format={}, requesterId={}",
                job.getJobId(), request.getFormat(), userId);

        return ExportResponse.builder()
                .jobId(job.getJobId())
                .statusCheckUrl(job.getStatusCheckUrl())
                .message("내보내기가 시작되었습니다")
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

    private String serializeToJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize object to JSON", e);
            return "{}";
        }
    }
}
