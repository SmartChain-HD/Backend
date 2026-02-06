package com.smartchain.platform.domain.job.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartchain.platform.domain.job.entity.AsyncJob;
import com.smartchain.platform.domain.job.repository.AsyncJobRepository;
import com.smartchain.platform.domain.review.entity.Review;
import com.smartchain.platform.domain.review.repository.ReviewRepository;
import com.smartchain.platform.domain.user.entity.Role;
import com.smartchain.platform.domain.user.entity.User;
import com.smartchain.platform.domain.user.repository.UserRepository;
import com.smartchain.platform.dto.review.export.ExportFilterDto;
import com.smartchain.platform.dto.review.export.ExportRequest;
import com.smartchain.platform.dto.review.export.ExportResponse;
import com.smartchain.platform.dto.review.report.BulkReportRequest;
import com.smartchain.platform.dto.review.report.BulkReportResponse;
import com.smartchain.platform.dto.review.report.ReportPublishRequest;
import com.smartchain.platform.dto.review.report.ReportPublishResponse;
import com.smartchain.platform.global.enums.JobStatus;
import com.smartchain.platform.global.enums.JobType;
import com.smartchain.platform.global.error.CustomException;
import com.smartchain.platform.global.error.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportExportServiceTest {

    @InjectMocks
    private ReportExportService reportExportService;

    @Mock
    private AsyncJobRepository asyncJobRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private User reviewerUser;

    @Mock
    private User guestUser;

    @Mock
    private Role reviewerRole;

    @Mock
    private Role guestRole;

    @Mock
    private Review testReview;

    @BeforeEach
    void setUp() {
        lenient().when(reviewerRole.getCode()).thenReturn("REVIEWER");
        lenient().when(guestRole.getCode()).thenReturn("GUEST");

        lenient().when(reviewerUser.getUserId()).thenReturn(1L);
        lenient().when(reviewerUser.getName()).thenReturn("수신자");
        lenient().when(reviewerUser.getRole()).thenReturn(reviewerRole);

        lenient().when(guestUser.getUserId()).thenReturn(2L);
        lenient().when(guestUser.getName()).thenReturn("게스트");
        lenient().when(guestUser.getRole()).thenReturn(guestRole);

        lenient().when(testReview.getReviewId()).thenReturn(100L);
    }

    @Nested
    @DisplayName("보고서 생성 테스트")
    class CreateReportTest {

        @Test
        @DisplayName("REVIEWER가 보고서 생성 요청 성공")
        void createReport_Success() {
            // given
            ReportPublishRequest request = ReportPublishRequest.builder()
                    .reportType("FULL")
                    .includeAiAnalysis(true)
                    .includeRecommendations(true)
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(reviewRepository.findById(100L)).willReturn(Optional.of(testReview));
            given(asyncJobRepository.save(any(AsyncJob.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            ReportPublishResponse response = reportExportService.createReport(1L, 100L, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getJobId()).startsWith("job_report_");
            assertThat(response.getStatus()).isEqualTo("PENDING");
            assertThat(response.getStatusCheckUrl()).contains("/api/v1/jobs/");
            assertThat(response.getEstimatedCompletionTime()).isEqualTo(60);
            assertThat(response.getMessage()).isEqualTo("보고서 생성이 시작되었습니다");

            ArgumentCaptor<AsyncJob> jobCaptor = ArgumentCaptor.forClass(AsyncJob.class);
            verify(asyncJobRepository).save(jobCaptor.capture());

            AsyncJob savedJob = jobCaptor.getValue();
            assertThat(savedJob.getJobType()).isEqualTo(JobType.REPORT_GENERATION);
            assertThat(savedJob.getRequesterId()).isEqualTo(1L);
            assertThat(savedJob.getTargetId()).isEqualTo(100L);
            assertThat(savedJob.getStatus()).isEqualTo(JobStatus.PENDING);
        }

        @Test
        @DisplayName("REVIEWER가 아닌 사용자가 보고서 생성 요청 시 실패")
        void createReport_AsGuest_ThrowsException() {
            // given
            ReportPublishRequest request = new ReportPublishRequest();
            given(userRepository.findById(2L)).willReturn(Optional.of(guestUser));

            // when & then
            assertThatThrownBy(() -> reportExportService.createReport(2L, 100L, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.PERMISSION_DENIED_ACTION);
                    });
        }

        @Test
        @DisplayName("존재하지 않는 심사에 대한 보고서 생성 요청 시 실패")
        void createReport_ReviewNotFound_ThrowsException() {
            // given
            ReportPublishRequest request = new ReportPublishRequest();
            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(reviewRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reportExportService.createReport(1L, 999L, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.REVIEW_NOT_FOUND);
                    });
        }
    }

    @Nested
    @DisplayName("일괄 보고서 생성 테스트")
    class CreateBulkReportTest {

        @Test
        @DisplayName("REVIEWER가 일괄 보고서 생성 요청 성공")
        void createBulkReport_Success() {
            // given
            BulkReportRequest request = BulkReportRequest.builder()
                    .reviewIds(List.of(1L, 2L, 3L))
                    .reportType("SUMMARY")
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(reviewRepository.existsById(any(Long.class))).willReturn(true);
            given(asyncJobRepository.save(any(AsyncJob.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            BulkReportResponse response = reportExportService.createBulkReport(1L, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getJobId()).startsWith("job_bulk_report_");
            assertThat(response.getTotalCount()).isEqualTo(3);
            assertThat(response.getStatusCheckUrl()).contains("/api/v1/jobs/");
            assertThat(response.getMessage()).isEqualTo("일괄 보고서 생성이 시작되었습니다");

            ArgumentCaptor<AsyncJob> jobCaptor = ArgumentCaptor.forClass(AsyncJob.class);
            verify(asyncJobRepository).save(jobCaptor.capture());

            AsyncJob savedJob = jobCaptor.getValue();
            assertThat(savedJob.getJobType()).isEqualTo(JobType.BULK_REPORT);
            assertThat(savedJob.getStatus()).isEqualTo(JobStatus.PENDING);
        }

        @Test
        @DisplayName("빈 reviewIds로 일괄 보고서 생성 요청 시 실패")
        void createBulkReport_EmptyIds_ThrowsException() {
            // given
            BulkReportRequest request = BulkReportRequest.builder()
                    .reviewIds(List.of())
                    .reportType("SUMMARY")
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));

            // when & then
            assertThatThrownBy(() -> reportExportService.createBulkReport(1L, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT);
                    });
        }

        @Test
        @DisplayName("존재하지 않는 심사 ID가 포함된 경우 실패")
        void createBulkReport_InvalidReviewId_ThrowsException() {
            // given
            BulkReportRequest request = BulkReportRequest.builder()
                    .reviewIds(List.of(1L, 999L))
                    .reportType("SUMMARY")
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(reviewRepository.existsById(1L)).willReturn(true);
            given(reviewRepository.existsById(999L)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> reportExportService.createBulkReport(1L, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.REVIEW_NOT_FOUND);
                    });
        }
    }

    @Nested
    @DisplayName("내보내기 테스트")
    class ExportReviewsTest {

        @Test
        @DisplayName("REVIEWER가 XLSX 내보내기 요청 성공")
        void exportReviews_Xlsx_Success() {
            // given
            ExportRequest request = ExportRequest.builder()
                    .format("XLSX")
                    .filters(ExportFilterDto.builder()
                            .riskLevels(List.of("HIGH", "MEDIUM"))
                            .build())
                    .columns(List.of("companyName", "riskLevel", "score"))
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));
            given(asyncJobRepository.save(any(AsyncJob.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            ExportResponse response = reportExportService.exportReviews(1L, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getJobId()).startsWith("job_export_");
            assertThat(response.getStatusCheckUrl()).contains("/api/v1/jobs/");
            assertThat(response.getMessage()).isEqualTo("내보내기가 시작되었습니다");

            ArgumentCaptor<AsyncJob> jobCaptor = ArgumentCaptor.forClass(AsyncJob.class);
            verify(asyncJobRepository).save(jobCaptor.capture());

            AsyncJob savedJob = jobCaptor.getValue();
            assertThat(savedJob.getJobType()).isEqualTo(JobType.EXPORT);
            assertThat(savedJob.getStatus()).isEqualTo(JobStatus.PENDING);
        }

        @Test
        @DisplayName("지원하지 않는 형식으로 내보내기 요청 시 실패")
        void exportReviews_InvalidFormat_ThrowsException() {
            // given
            ExportRequest request = ExportRequest.builder()
                    .format("PDF")
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));

            // when & then
            assertThatThrownBy(() -> reportExportService.exportReviews(1L, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT);
                    });
        }

        @Test
        @DisplayName("형식 없이 내보내기 요청 시 실패")
        void exportReviews_NoFormat_ThrowsException() {
            // given
            ExportRequest request = ExportRequest.builder().build();

            given(userRepository.findById(1L)).willReturn(Optional.of(reviewerUser));

            // when & then
            assertThatThrownBy(() -> reportExportService.exportReviews(1L, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT);
                    });
        }
    }
}
