package com.smartchain.platform.domain.job.service;

import com.smartchain.platform.domain.job.entity.AsyncJob;
import com.smartchain.platform.domain.job.repository.AsyncJobRepository;
import com.smartchain.platform.dto.job.JobRetryResponse;
import com.smartchain.platform.dto.job.JobStatusResponse;
import com.smartchain.platform.global.enums.JobStatus;
import com.smartchain.platform.global.enums.JobType;
import com.smartchain.platform.global.error.CustomException;
import com.smartchain.platform.global.error.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @InjectMocks
    private JobService jobService;

    @Mock
    private AsyncJobRepository asyncJobRepository;

    @Mock
    private AsyncJob testJob;

    private final Long userId = 1L;
    private final String jobId = "job_parse_abc123";

    @BeforeEach
    void setUp() {
        lenient().when(testJob.getJobId()).thenReturn(jobId);
        lenient().when(testJob.getJobType()).thenReturn(JobType.FILE_PARSING);
        lenient().when(testJob.getStatus()).thenReturn(JobStatus.RUNNING);
        lenient().when(testJob.getProgress()).thenReturn(50);
        lenient().when(testJob.getMessage()).thenReturn("파일 파싱 중...");
        lenient().when(testJob.getRequesterId()).thenReturn(userId);
        lenient().when(testJob.getStartedAt()).thenReturn(LocalDateTime.now());
    }

    @Nested
    @DisplayName("작업 상태 조회 테스트")
    class GetJobStatusTest {

        @Test
        @DisplayName("작업 상태 조회 성공")
        void getJobStatus_Success() {
            // given
            given(asyncJobRepository.findByJobId(jobId)).willReturn(Optional.of(testJob));

            // when
            JobStatusResponse response = jobService.getJobStatus(userId, jobId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getJobId()).isEqualTo(jobId);
            assertThat(response.getJobType()).isEqualTo("FILE_PARSING");
            assertThat(response.getStatus()).isEqualTo("RUNNING");
            assertThat(response.getProgress()).isEqualTo(50);
        }

        @Test
        @DisplayName("존재하지 않는 작업 조회 시 실패")
        void getJobStatus_NotFound_ThrowsException() {
            // given
            given(asyncJobRepository.findByJobId("invalid_job")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> jobService.getJobStatus(userId, "invalid_job"))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.JOB_NOT_FOUND);
                    });
        }

        @Test
        @DisplayName("다른 사용자의 작업 조회 시 권한 에러")
        void getJobStatus_OtherUser_ThrowsException() {
            // given
            given(asyncJobRepository.findByJobId(jobId)).willReturn(Optional.of(testJob));

            // when & then
            assertThatThrownBy(() -> jobService.getJobStatus(999L, jobId))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.PERMISSION_DENIED_RESOURCE);
                    });
        }

        @Test
        @DisplayName("성공한 작업 상태 조회")
        void getJobStatus_Succeeded_Success() {
            // given
            when(testJob.getStatus()).thenReturn(JobStatus.SUCCEEDED);
            when(testJob.getCompletedAt()).thenReturn(LocalDateTime.now());
            when(testJob.getResultUrl()).thenReturn("/diagnostics/1/files/102/parsing-result");

            given(asyncJobRepository.findByJobId(jobId)).willReturn(Optional.of(testJob));

            // when
            JobStatusResponse response = jobService.getJobStatus(userId, jobId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo("SUCCEEDED");
            assertThat(response.getResult()).isNotNull();
            assertThat(response.getResult().getResultUrl()).isEqualTo("/diagnostics/1/files/102/parsing-result");
        }

        @Test
        @DisplayName("실패한 작업 상태 조회")
        void getJobStatus_Failed_Success() {
            // given
            when(testJob.getStatus()).thenReturn(JobStatus.FAILED);
            when(testJob.getCompletedAt()).thenReturn(LocalDateTime.now());
            when(testJob.getErrorCode()).thenReturn("SYS_003");
            when(testJob.getErrorMessage()).thenReturn("파일 형식을 인식할 수 없습니다");
            when(testJob.isRetryable()).thenReturn(true);

            given(asyncJobRepository.findByJobId(jobId)).willReturn(Optional.of(testJob));

            // when
            JobStatusResponse response = jobService.getJobStatus(userId, jobId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo("FAILED");
            assertThat(response.getError()).isNotNull();
            assertThat(response.getError().getCode()).isEqualTo("SYS_003");
            assertThat(response.getError().isRetryable()).isTrue();
        }
    }

    @Nested
    @DisplayName("작업 재시도 테스트")
    class RetryJobTest {

        @Test
        @DisplayName("작업 재시도 성공")
        void retryJob_Success() {
            // given
            when(testJob.getStatus()).thenReturn(JobStatus.FAILED);
            when(testJob.isRetryable()).thenReturn(true);
            when(testJob.getTargetId()).thenReturn(100L);
            when(testJob.getRequestPayload()).thenReturn("{\"fileId\": 102}");

            given(asyncJobRepository.findByJobId(jobId)).willReturn(Optional.of(testJob));
            given(asyncJobRepository.save(any(AsyncJob.class))).willAnswer(invocation -> {
                AsyncJob savedJob = invocation.getArgument(0);
                return savedJob;
            });

            // when
            JobRetryResponse response = jobService.retryJob(userId, jobId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getNewJobId()).isNotNull();
            assertThat(response.getStatus()).isEqualTo("PENDING");
            assertThat(response.getStatusCheckUrl()).contains("/api/v1/jobs/");
            verify(asyncJobRepository).save(any(AsyncJob.class));
        }

        @Test
        @DisplayName("실패하지 않은 작업 재시도 시 실패")
        void retryJob_NotFailed_ThrowsException() {
            // given
            when(testJob.getStatus()).thenReturn(JobStatus.RUNNING);

            given(asyncJobRepository.findByJobId(jobId)).willReturn(Optional.of(testJob));

            // when & then
            assertThatThrownBy(() -> jobService.retryJob(userId, jobId))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.JOB_NOT_FAILED);
                    });
        }

        @Test
        @DisplayName("재시도 불가능한 작업 재시도 시 실패")
        void retryJob_NotRetryable_ThrowsException() {
            // given
            when(testJob.getStatus()).thenReturn(JobStatus.FAILED);
            when(testJob.isRetryable()).thenReturn(false);

            given(asyncJobRepository.findByJobId(jobId)).willReturn(Optional.of(testJob));

            // when & then
            assertThatThrownBy(() -> jobService.retryJob(userId, jobId))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.JOB_NOT_RETRYABLE);
                    });
        }
    }
}
