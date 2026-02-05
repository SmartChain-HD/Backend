package com.smartchain.platform.domain.risk.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartchain.platform.domain.risk.client.ExternalRiskApiClient;
import com.smartchain.platform.domain.risk.entity.ExternalRiskResult;
import com.smartchain.platform.domain.risk.repository.ExternalRiskResultRepository;
import com.smartchain.platform.domain.user.entity.Company;
import com.smartchain.platform.domain.user.entity.Role;
import com.smartchain.platform.domain.user.entity.User;
import com.smartchain.platform.domain.user.repository.CompanyRepository;
import com.smartchain.platform.domain.user.repository.UserRepository;
import com.smartchain.platform.dto.risk.ExternalRiskDetectRequest;
import com.smartchain.platform.dto.risk.ExternalRiskDetectResponse;
import com.smartchain.platform.dto.risk.ExternalRiskResultResponse;
import com.smartchain.platform.global.enums.RiskLevel;
import com.smartchain.platform.global.error.CustomException;
import com.smartchain.platform.global.error.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExternalRiskServiceTest {

    @Mock
    private ExternalRiskApiClient externalRiskApiClient;
    @Mock
    private ExternalRiskResultRepository riskResultRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private UserRepository userRepository;

    private ObjectMapper objectMapper;
    private ExternalRiskService externalRiskService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        externalRiskService = new ExternalRiskService(
            externalRiskApiClient,
            riskResultRepository,
            companyRepository,
            userRepository,
            objectMapper
        );
    }

    @Nested
    @DisplayName("detect")
    class DetectTest {

        @Test
        @DisplayName("REVIEWER가 아닌 사용자는 RISK_PERMISSION_DENIED 에러를 발생시킨다")
        void detect_nonReviewer_throwsException() {
            // given
            Long userId = 1L;
            User user = createTestUser(userId, "DRAFTER");
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            // when & then
            assertThatThrownBy(() -> externalRiskService.detect(userId, List.of(1L)))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException customEx = (CustomException) ex;
                    assertThat(customEx.getErrorCode()).isEqualTo(ErrorCode.RISK_PERMISSION_DENIED);
                });

            verify(externalRiskApiClient, never()).detect(any());
        }

        @Test
        @DisplayName("존재하지 않는 회사 ID를 전달하면 RISK_COMPANY_NOT_FOUND 에러를 발생시킨다")
        void detect_companyNotFound_throwsException() {
            // given
            Long userId = 1L;
            User user = createTestUser(userId, "REVIEWER");
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(companyRepository.findById(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> externalRiskService.detect(userId, List.of(999L)))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException customEx = (CustomException) ex;
                    assertThat(customEx.getErrorCode()).isEqualTo(ErrorCode.RISK_COMPANY_NOT_FOUND);
                });

            verify(externalRiskApiClient, never()).detect(any());
        }

        @Test
        @DisplayName("정상 흐름: 회사 조회 → AI 호출 → 결과 저장")
        void detect_success_savesResults() {
            // given
            Long userId = 1L;
            User user = createTestUser(userId, "REVIEWER");
            Company company = createTestCompany(10L, "테스트협력사");

            ExternalRiskDetectResponse aiResponse = new ExternalRiskDetectResponse(
                List.of(new ExternalRiskDetectResponse.VendorRiskResult(
                    "테스트협력사", "HIGH", "뉴스 기사에서 위험 감지",
                    List.of(new ExternalRiskDetectResponse.Evidence(
                        "뉴스", "위험 기사", "내용 발췌", "https://example.com", "2025-01-01"
                    ))
                ))
            );

            ExternalRiskResult savedEntity = ExternalRiskResult.builder()
                .companyId(10L)
                .companyName("테스트협력사")
                .riskLevel(RiskLevel.HIGH)
                .summary("뉴스 기사에서 위험 감지")
                .requestedBy(userId)
                .build();
            ReflectionTestUtils.setField(savedEntity, "id", 100L);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(companyRepository.findById(10L)).thenReturn(Optional.of(company));
            when(externalRiskApiClient.detect(any(ExternalRiskDetectRequest.class))).thenReturn(aiResponse);
            when(riskResultRepository.save(any(ExternalRiskResult.class))).thenReturn(savedEntity);

            // when
            List<ExternalRiskResultResponse> results = externalRiskService.detect(userId, List.of(10L));

            // then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).companyName()).isEqualTo("테스트협력사");
            assertThat(results.get(0).riskLevel()).isEqualTo("HIGH");

            verify(externalRiskApiClient).detect(any(ExternalRiskDetectRequest.class));
            verify(riskResultRepository).save(any(ExternalRiskResult.class));
        }

        @Test
        @DisplayName("AI 서비스 에러 시 예외가 전파된다")
        void detect_aiServiceError_propagatesException() {
            // given
            Long userId = 1L;
            User user = createTestUser(userId, "REVIEWER");
            Company company = createTestCompany(10L, "테스트협력사");

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(companyRepository.findById(10L)).thenReturn(Optional.of(company));
            when(externalRiskApiClient.detect(any(ExternalRiskDetectRequest.class)))
                .thenThrow(new CustomException(ErrorCode.RISK_DETECT_FAILED));

            // when & then
            assertThatThrownBy(() -> externalRiskService.detect(userId, List.of(10L)))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException customEx = (CustomException) ex;
                    assertThat(customEx.getErrorCode()).isEqualTo(ErrorCode.RISK_DETECT_FAILED);
                });

            verify(riskResultRepository, never()).save(any());
        }

        @Test
        @DisplayName("사용자를 찾을 수 없으면 USER_NOT_FOUND 에러를 발생시킨다")
        void detect_userNotFound_throwsException() {
            // given
            Long userId = 999L;
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> externalRiskService.detect(userId, List.of(1L)))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException customEx = (CustomException) ex;
                    assertThat(customEx.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
                });
        }
    }

    @Nested
    @DisplayName("getLatestResult")
    class GetLatestResultTest {

        @Test
        @DisplayName("리스크 결과가 없으면 RISK_RESULT_NOT_FOUND 에러를 발생시킨다")
        void getLatestResult_notFound_throwsException() {
            // given
            Long userId = 1L;
            Long companyId = 10L;
            User user = createTestUser(userId, "REVIEWER");

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(riskResultRepository.findTopByCompanyIdOrderByDetectedAtDesc(companyId))
                .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> externalRiskService.getLatestResult(userId, companyId))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException customEx = (CustomException) ex;
                    assertThat(customEx.getErrorCode()).isEqualTo(ErrorCode.RISK_RESULT_NOT_FOUND);
                });
        }

        @Test
        @DisplayName("리스크 결과가 있으면 최신 결과를 반환한다")
        void getLatestResult_found_returnsResult() {
            // given
            Long userId = 1L;
            Long companyId = 10L;
            User user = createTestUser(userId, "REVIEWER");

            ExternalRiskResult result = ExternalRiskResult.builder()
                .companyId(companyId)
                .companyName("테스트협력사")
                .riskLevel(RiskLevel.MEDIUM)
                .summary("중간 위험")
                .requestedBy(userId)
                .build();
            ReflectionTestUtils.setField(result, "id", 1L);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(riskResultRepository.findTopByCompanyIdOrderByDetectedAtDesc(companyId))
                .thenReturn(Optional.of(result));

            // when
            ExternalRiskResultResponse response = externalRiskService.getLatestResult(userId, companyId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.companyName()).isEqualTo("테스트협력사");
            assertThat(response.riskLevel()).isEqualTo("MEDIUM");
        }

        @Test
        @DisplayName("REVIEWER가 아니면 RISK_PERMISSION_DENIED 에러를 발생시킨다")
        void getLatestResult_nonReviewer_throwsException() {
            // given
            Long userId = 1L;
            User user = createTestUser(userId, "APPROVER");
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            // when & then
            assertThatThrownBy(() -> externalRiskService.getLatestResult(userId, 10L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException customEx = (CustomException) ex;
                    assertThat(customEx.getErrorCode()).isEqualTo(ErrorCode.RISK_PERMISSION_DENIED);
                });
        }
    }

    @Nested
    @DisplayName("getAllResults")
    class GetAllResultsTest {

        @Test
        @DisplayName("전체 결과를 페이지네이션으로 반환한다")
        void getAllResults_success_returnsPagedResults() {
            // given
            Long userId = 1L;
            User user = createTestUser(userId, "REVIEWER");
            Pageable pageable = PageRequest.of(0, 10);

            ExternalRiskResult result = ExternalRiskResult.builder()
                .companyId(10L)
                .companyName("테스트협력사")
                .riskLevel(RiskLevel.LOW)
                .summary("저위험")
                .requestedBy(userId)
                .build();
            ReflectionTestUtils.setField(result, "id", 1L);

            Page<ExternalRiskResult> page = new PageImpl<>(List.of(result), pageable, 1);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(riskResultRepository.findAllByOrderByDetectedAtDesc(pageable)).thenReturn(page);

            // when
            Page<ExternalRiskResultResponse> responsePage = externalRiskService.getAllResults(userId, pageable);

            // then
            assertThat(responsePage.getContent()).hasSize(1);
            assertThat(responsePage.getContent().get(0).riskLevel()).isEqualTo("LOW");
            assertThat(responsePage.getTotalElements()).isEqualTo(1);
        }
    }

    // Helper methods
    private User createTestUser(Long userId, String roleCode) {
        Role role = new Role("테스트역할", roleCode);
        ReflectionTestUtils.setField(role, "roleId", 1L);

        User user = User.builder()
            .name("테스트유저")
            .email("test@test.com")
            .userPassword("password123!")
            .role(role)
            .build();
        ReflectionTestUtils.setField(user, "userId", userId);
        return user;
    }

    private Company createTestCompany(Long companyId, String name) {
        Company company = Company.builder()
            .name(name)
            .build();
        ReflectionTestUtils.setField(company, "companyId", companyId);
        return company;
    }
}
