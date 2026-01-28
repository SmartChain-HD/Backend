package com.smartchain.platform.domain.job.service;

import com.smartchain.platform.domain.diagnostic.entity.Diagnostic;
import com.smartchain.platform.domain.diagnostic.repository.DiagnosticRepository;
import com.smartchain.platform.domain.job.entity.AsyncJob;
import com.smartchain.platform.domain.job.repository.AsyncJobRepository;
import com.smartchain.platform.domain.user.entity.Domain;
import com.smartchain.platform.domain.user.entity.Role;
import com.smartchain.platform.domain.user.entity.User;
import com.smartchain.platform.domain.user.entity.UserDomainRole;
import com.smartchain.platform.domain.user.repository.UserRepository;
import com.smartchain.platform.dto.job.JobStatusResponse;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AiAnalysisJobServiceTest {

    @InjectMocks
    private AiAnalysisJobService aiAnalysisJobService;

    @Mock
    private AsyncJobRepository asyncJobRepository;

    @Mock
    private DiagnosticRepository diagnosticRepository;

    @Mock
    private UserRepository userRepository;

    private User testUser;
    private Diagnostic testDiagnostic;
    private Domain testDomain;

    @BeforeEach
    void setUp() {
        testDomain = Domain.builder()
                .code("ENV")
                .name("환경")
                .build();

        Role drafterRole = new Role("기안자", "DRAFTER");

        testUser = User.builder()
                .name("테스트유저")
                .email("test@test.com")
                .userPassword("password")
                .build();

        UserDomainRole domainRole = UserDomainRole.builder()
                .user(testUser)
                .domain(testDomain)
                .role(drafterRole)
                .build();
        testUser.addDomainRole(domainRole);

        testDiagnostic = Diagnostic.builder()
                .title("테스트 기안")
                .domain(testDomain)
                .drafterId(1L)
                .build();
    }

    @Nested
    @DisplayName("submitAiAnalysis")
    class SubmitAiAnalysis {

        @Test
        @DisplayName("ESG 분석 요청 시 AsyncJob을 생성하고 JobStatusResponse를 반환한다")
        void submitEsgAnalysis_success() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(diagnosticRepository.findById(100L)).willReturn(Optional.of(testDiagnostic));
            given(asyncJobRepository.save(any(AsyncJob.class))).willAnswer(inv -> inv.getArgument(0));
            given(asyncJobRepository.findByJobId(any())).willReturn(Optional.empty());

            // when
            JobStatusResponse response = aiAnalysisJobService.submitAiAnalysis(1L, 100L, "ESG");

            // then
            assertThat(response).isNotNull();
            assertThat(response.getJobType()).isEqualTo(JobType.AI_ESG_ANALYSIS.name());
            assertThat(response.getStatus()).isEqualTo("PENDING");
            assertThat(response.getMessage()).isEqualTo("AI 분석이 시작되었습니다");
            verify(asyncJobRepository).save(any(AsyncJob.class));
        }

        @Test
        @DisplayName("COMPLIANCE 분석 요청 시 AI_COMPLIANCE_ANALYSIS 타입으로 생성된다")
        void submitComplianceAnalysis_success() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(diagnosticRepository.findById(100L)).willReturn(Optional.of(testDiagnostic));
            given(asyncJobRepository.save(any(AsyncJob.class))).willAnswer(inv -> inv.getArgument(0));
            given(asyncJobRepository.findByJobId(any())).willReturn(Optional.empty());

            // when
            JobStatusResponse response = aiAnalysisJobService.submitAiAnalysis(1L, 100L, "COMPLIANCE");

            // then
            assertThat(response.getJobType()).isEqualTo(JobType.AI_COMPLIANCE_ANALYSIS.name());
        }

        @Test
        @DisplayName("SAFETY 분석 요청 시 AI_SAFETY_ANALYSIS 타입으로 생성된다")
        void submitSafetyAnalysis_success() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(diagnosticRepository.findById(100L)).willReturn(Optional.of(testDiagnostic));
            given(asyncJobRepository.save(any(AsyncJob.class))).willAnswer(inv -> inv.getArgument(0));
            given(asyncJobRepository.findByJobId(any())).willReturn(Optional.empty());

            // when
            JobStatusResponse response = aiAnalysisJobService.submitAiAnalysis(1L, 100L, "SAFETY");

            // then
            assertThat(response.getJobType()).isEqualTo(JobType.AI_SAFETY_ANALYSIS.name());
        }

        @Test
        @DisplayName("존재하지 않는 사용자면 USER_NOT_FOUND 예외가 발생한다")
        void submitAnalysis_userNotFound() {
            // given
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // then
            assertThatThrownBy(() -> aiAnalysisJobService.submitAiAnalysis(999L, 100L, "ESG"))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.USER_NOT_FOUND);
        }

        @Test
        @DisplayName("존재하지 않는 기안이면 DIAGNOSTIC_NOT_FOUND 예외가 발생한다")
        void submitAnalysis_diagnosticNotFound() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(diagnosticRepository.findById(999L)).willReturn(Optional.empty());

            // then
            assertThatThrownBy(() -> aiAnalysisJobService.submitAiAnalysis(1L, 999L, "ESG"))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.DIAGNOSTIC_NOT_FOUND);
        }

        @Test
        @DisplayName("도메인 권한이 없는 사용자면 PERMISSION_DENIED_ACTION 예외가 발생한다")
        void submitAnalysis_permissionDenied() {
            // given
            User unauthorizedUser = User.builder()
                    .name("권한없는유저")
                    .email("noauth@test.com")
                    .userPassword("password")
                    .build();
            // 다른 도메인 역할만 부여
            Domain otherDomain = Domain.builder().code("GOV").name("거버넌스").build();
            Role role = new Role("기안자", "DRAFTER");
            UserDomainRole otherRole = UserDomainRole.builder()
                    .user(unauthorizedUser).domain(otherDomain).role(role).build();
            unauthorizedUser.addDomainRole(otherRole);

            given(userRepository.findById(2L)).willReturn(Optional.of(unauthorizedUser));
            given(diagnosticRepository.findById(100L)).willReturn(Optional.of(testDiagnostic));

            // then
            assertThatThrownBy(() -> aiAnalysisJobService.submitAiAnalysis(2L, 100L, "ESG"))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.PERMISSION_DENIED_ACTION);
        }
    }

    @Nested
    @DisplayName("executeAnalysisAsync")
    class ExecuteAnalysisAsync {

        @Test
        @DisplayName("비동기 실행 시 작업 상태가 SUCCEEDED로 변경된다")
        void executeAsync_success() {
            // given
            AsyncJob job = AsyncJob.builder()
                    .jobType(JobType.AI_ESG_ANALYSIS)
                    .requesterId(1L)
                    .targetId(100L)
                    .build();

            given(asyncJobRepository.findByJobId(job.getJobId())).willReturn(Optional.of(job));
            given(asyncJobRepository.save(any(AsyncJob.class))).willAnswer(inv -> inv.getArgument(0));

            // when
            aiAnalysisJobService.executeAnalysisAsync(job.getJobId());

            // then
            assertThat(job.getProgress()).isEqualTo(100);
        }
    }
}
