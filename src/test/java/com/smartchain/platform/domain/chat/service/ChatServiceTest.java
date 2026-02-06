package com.smartchain.platform.domain.chat.service;

import com.smartchain.platform.domain.chat.client.AiChatApiClient;
import com.smartchain.platform.domain.diagnostic.entity.Diagnostic;
import com.smartchain.platform.domain.evidence.entity.EvidenceFile;
import com.smartchain.platform.domain.evidence.repository.EvidenceFileRepository;
import com.smartchain.platform.domain.file.storage.FileStorageService;
import com.smartchain.platform.domain.user.entity.Company;
import com.smartchain.platform.domain.user.entity.Role;
import com.smartchain.platform.domain.user.entity.User;
import com.smartchain.platform.domain.user.repository.UserRepository;
import com.smartchain.platform.dto.chat.AdminInspectResponse;
import com.smartchain.platform.dto.chat.AdminSyncResponse;
import com.smartchain.platform.dto.chat.ChatRequest;
import com.smartchain.platform.dto.chat.ChatResponse;
import com.smartchain.platform.dto.chat.SourceItem;
import com.smartchain.platform.global.error.CustomException;
import com.smartchain.platform.global.error.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private AiChatApiClient aiChatApiClient;

    @Mock
    private EvidenceFileRepository evidenceFileRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private UserRepository userRepository;

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        chatService = new ChatService(aiChatApiClient, evidenceFileRepository, fileStorageService, userRepository);
        ReflectionTestUtils.setField(chatService, "fileBasePath", "./uploads");
    }

    @Nested
    @DisplayName("chat")
    class ChatTest {

        @Test
        @DisplayName("정상 채팅 요청 시 AI 응답을 반환한다")
        void chat_success_returnsResponse() {
            // given
            User user = createTestUser(1L, "DRAFTER");
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            ChatRequest request = new ChatRequest(
                "하도급법 위반 시 벌점은?",
                null, null,
                List.of(),
                "compliance",
                null, 8, null
            );

            ChatResponse expectedResponse = new ChatResponse(
                "하도급법 위반 시 벌점은 위반 사유에 따라 다릅니다.",
                "high",
                null,
                List.of(new SourceItem("하도급가이드.pdf", "manual", "벌점 부과 기준...", 0.89, null))
            );

            when(aiChatApiClient.chatSync(any(ChatRequest.class))).thenReturn(expectedResponse);

            // when
            ChatResponse response = chatService.chat(request, 1L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.answer()).contains("하도급법");
            assertThat(response.confidence()).isEqualTo("high");

            // sessionId가 없으면 자동 생성됨
            ArgumentCaptor<ChatRequest> requestCaptor = ArgumentCaptor.forClass(ChatRequest.class);
            verify(aiChatApiClient).chatSync(requestCaptor.capture());
            assertThat(requestCaptor.getValue().sessionId()).isNotNull();
        }

        @Test
        @DisplayName("sessionId가 있으면 그대로 사용한다")
        void chat_withSessionId_usesExistingSessionId() {
            // given
            User user = createTestUser(1L, "DRAFTER");
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            String existingSessionId = "existing-session-123";
            ChatRequest request = new ChatRequest(
                "추가 질문입니다",
                null, null,
                List.of(),
                "esg",
                null, 8, existingSessionId
            );

            ChatResponse expectedResponse = new ChatResponse("답변입니다.", "medium", null, List.of());
            when(aiChatApiClient.chatSync(any(ChatRequest.class))).thenReturn(expectedResponse);

            // when
            chatService.chat(request, 1L);

            // then
            ArgumentCaptor<ChatRequest> requestCaptor = ArgumentCaptor.forClass(ChatRequest.class);
            verify(aiChatApiClient).chatSync(requestCaptor.capture());
            assertThat(requestCaptor.getValue().sessionId()).isEqualTo(existingSessionId);
        }

        @Test
        @DisplayName("유효하지 않은 도메인이면 AI_CHAT_INVALID_DOMAIN 에러를 발생시킨다")
        void chat_invalidDomain_throwsException() {
            // given
            User user = createTestUser(1L, "DRAFTER");
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            ChatRequest request = new ChatRequest(
                "질문입니다",
                null, null,
                List.of(),
                "invalid_domain",
                null, 8, null
            );

            // when & then
            assertThatThrownBy(() -> chatService.chat(request, 1L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException customEx = (CustomException) ex;
                    assertThat(customEx.getErrorCode()).isEqualTo(ErrorCode.AI_CHAT_INVALID_DOMAIN);
                });

            verify(aiChatApiClient, never()).chatSync(any());
        }

        @Test
        @DisplayName("AI 서비스 에러 시 예외가 전파된다")
        void chat_aiServiceError_propagatesException() {
            // given
            User user = createTestUser(1L, "DRAFTER");
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            ChatRequest request = new ChatRequest(
                "질문입니다",
                null, null,
                List.of(),
                "all",
                null, 8, null
            );

            when(aiChatApiClient.chatSync(any(ChatRequest.class)))
                .thenThrow(new CustomException(ErrorCode.AI_CHAT_SERVICE_ERROR));

            // when & then
            assertThatThrownBy(() -> chatService.chat(request, 1L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException customEx = (CustomException) ex;
                    assertThat(customEx.getErrorCode()).isEqualTo(ErrorCode.AI_CHAT_SERVICE_ERROR);
                });
        }

        @Test
        @DisplayName("존재하지 않는 사용자면 USER_NOT_FOUND 에러를 발생시킨다")
        void chat_userNotFound_throwsException() {
            // given
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            ChatRequest request = new ChatRequest(
                "질문입니다",
                null, null,
                List.of(),
                "all",
                null, 8, null
            );

            // when & then
            assertThatThrownBy(() -> chatService.chat(request, 999L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException customEx = (CustomException) ex;
                    assertThat(customEx.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
                });

            verify(aiChatApiClient, never()).chatSync(any());
        }
    }

    @Nested
    @DisplayName("chat - fileId")
    class ChatFileTest {

        @Test
        @DisplayName("로컬 환경: fileId가 있으면 절대 경로로 변환하여 Python에 전달한다")
        void chat_withFileId_localEnv_resolvesAbsolutePath() {
            // given
            Company company = createTestCompany(1L);
            User user = createTestUserWithCompany(1L, "DRAFTER", company);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            Diagnostic diagnostic = mock(Diagnostic.class);
            when(diagnostic.getCompany()).thenReturn(company);

            EvidenceFile evidenceFile = mock(EvidenceFile.class);
            when(evidenceFile.getFilePath()).thenReturn("diagnostics/1/abc_report.pdf");
            when(evidenceFile.getDiagnostic()).thenReturn(diagnostic);

            when(evidenceFileRepository.findById(42L)).thenReturn(Optional.of(evidenceFile));

            ChatRequest request = new ChatRequest(
                "이 문서를 요약해줘",
                42L, null,
                List.of(), "esg", null, 8, null
            );

            ChatResponse expectedResponse = new ChatResponse("문서 요약입니다.", "high", null, List.of());
            when(aiChatApiClient.chatSync(any(ChatRequest.class))).thenReturn(expectedResponse);

            // when
            ChatResponse response = chatService.chat(request, 1L);

            // then
            assertThat(response).isNotNull();

            ArgumentCaptor<ChatRequest> requestCaptor = ArgumentCaptor.forClass(ChatRequest.class);
            verify(aiChatApiClient).chatSync(requestCaptor.capture());
            ChatRequest sentRequest = requestCaptor.getValue();
            // 로컬: Paths.get("./uploads", "diagnostics/1/abc_report.pdf").toAbsolutePath()
            assertThat(sentRequest.fileUrl()).endsWith("uploads/diagnostics/1/abc_report.pdf");
            assertThat(sentRequest.fileUrl()).startsWith("/"); // 절대 경로
            assertThat(sentRequest.fileId()).isNull(); // Python에 보내는 요청에는 fileId 제거
            verify(fileStorageService, never()).getPresignedUrl(any(), any()); // presignedUrl 미사용
        }

        @Test
        @DisplayName("Azure 환경: filePath가 URL이면 presigned URL을 생성한다")
        void chat_withFileId_azureEnv_resolvesPresignedUrl() {
            // given
            Company company = createTestCompany(1L);
            User user = createTestUserWithCompany(1L, "DRAFTER", company);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            Diagnostic diagnostic = mock(Diagnostic.class);
            when(diagnostic.getCompany()).thenReturn(company);

            EvidenceFile evidenceFile = mock(EvidenceFile.class);
            when(evidenceFile.getFilePath()).thenReturn("https://account.blob.core.windows.net/container/diagnostics/1/abc_report.pdf");
            when(evidenceFile.getOriginalFileName()).thenReturn("report.pdf");
            when(evidenceFile.getDiagnostic()).thenReturn(diagnostic);

            when(evidenceFileRepository.findById(10L)).thenReturn(Optional.of(evidenceFile));
            when(fileStorageService.getPresignedUrl(eq("https://account.blob.core.windows.net/container/diagnostics/1/abc_report.pdf"), any(Duration.class)))
                .thenReturn("https://account.blob.core.windows.net/container/diagnostics/1/abc_report.pdf?sv=2021&sig=xxx");

            ChatRequest request = new ChatRequest(
                "이 문서를 분석해줘",
                10L, null,
                List.of(), "esg", null, 8, null
            );

            ChatResponse expectedResponse = new ChatResponse("분석 결과입니다.", "medium", null, List.of());
            when(aiChatApiClient.chatSync(any(ChatRequest.class))).thenReturn(expectedResponse);

            // when
            chatService.chat(request, 1L);

            // then
            ArgumentCaptor<ChatRequest> requestCaptor = ArgumentCaptor.forClass(ChatRequest.class);
            verify(aiChatApiClient).chatSync(requestCaptor.capture());
            assertThat(requestCaptor.getValue().fileUrl()).startsWith("https://");
            assertThat(requestCaptor.getValue().fileUrl()).contains("?sv=");
        }

        @Test
        @DisplayName("fileId가 null이면 fileUrl 없이 정상 동작한다")
        void chat_withoutFileId_worksNormally() {
            // given
            User user = createTestUser(1L, "DRAFTER");
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            ChatRequest request = new ChatRequest(
                "일반 질문입니다",
                null, null,
                List.of(), "all", null, 8, null
            );

            ChatResponse expectedResponse = new ChatResponse("답변입니다.", "high", null, List.of());
            when(aiChatApiClient.chatSync(any(ChatRequest.class))).thenReturn(expectedResponse);

            // when
            ChatResponse response = chatService.chat(request, 1L);

            // then
            assertThat(response).isNotNull();
            verify(evidenceFileRepository, never()).findById(any());

            ArgumentCaptor<ChatRequest> requestCaptor = ArgumentCaptor.forClass(ChatRequest.class);
            verify(aiChatApiClient).chatSync(requestCaptor.capture());
            assertThat(requestCaptor.getValue().fileUrl()).isNull();
        }

        @Test
        @DisplayName("존재하지 않는 fileId면 FILE_NOT_FOUND 에러를 발생시킨다")
        void chat_fileNotFound_throwsException() {
            // given
            User user = createTestUser(1L, "DRAFTER");
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            ChatRequest request = new ChatRequest(
                "이 문서를 분석해줘",
                999L, null,
                List.of(), "esg", null, 8, null
            );

            when(evidenceFileRepository.findById(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> chatService.chat(request, 1L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException customEx = (CustomException) ex;
                    assertThat(customEx.getErrorCode()).isEqualTo(ErrorCode.FILE_NOT_FOUND);
                });

            verify(aiChatApiClient, never()).chatSync(any());
        }

        @Test
        @DisplayName("다른 회사의 파일에 접근하면 PERMISSION_DENIED_ACTION 에러를 발생시킨다")
        void chat_differentCompany_throwsException() {
            // given
            Company userCompany = createTestCompany(1L);
            Company fileCompany = createTestCompany(2L);
            User user = createTestUserWithCompany(1L, "DRAFTER", userCompany);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            Diagnostic diagnostic = mock(Diagnostic.class);
            when(diagnostic.getCompany()).thenReturn(fileCompany);

            EvidenceFile evidenceFile = mock(EvidenceFile.class);
            when(evidenceFile.getDiagnostic()).thenReturn(diagnostic);

            when(evidenceFileRepository.findById(42L)).thenReturn(Optional.of(evidenceFile));

            ChatRequest request = new ChatRequest(
                "이 문서를 분석해줘",
                42L, null,
                List.of(), "esg", null, 8, null
            );

            // when & then
            assertThatThrownBy(() -> chatService.chat(request, 1L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException customEx = (CustomException) ex;
                    assertThat(customEx.getErrorCode()).isEqualTo(ErrorCode.PERMISSION_DENIED_ACTION);
                });

            verify(aiChatApiClient, never()).chatSync(any());
        }
    }

    @Nested
    @DisplayName("syncData")
    class SyncDataTest {

        @Test
        @DisplayName("REVIEWER 역할이면 동기화 요청이 성공한다")
        void syncData_reviewer_success() {
            // given
            User user = createTestUser(1L, "REVIEWER");
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            AdminSyncResponse expectedResponse = new AdminSyncResponse("accepted", "동기화가 시작되었습니다.");

            when(aiChatApiClient.syncDataSync()).thenReturn(expectedResponse);

            // when
            AdminSyncResponse response = chatService.syncData(1L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.status()).isEqualTo("accepted");
            verify(aiChatApiClient).syncDataSync();
        }

        @Test
        @DisplayName("REVIEWER가 아니면 PERMISSION_DENIED_ACTION 에러를 발생시킨다")
        void syncData_notReviewer_throwsException() {
            // given
            User user = createTestUser(1L, "DRAFTER");
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            // when & then
            assertThatThrownBy(() -> chatService.syncData(1L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException customEx = (CustomException) ex;
                    assertThat(customEx.getErrorCode()).isEqualTo(ErrorCode.PERMISSION_DENIED_ACTION);
                });

            verify(aiChatApiClient, never()).syncDataSync();
        }

        @Test
        @DisplayName("APPROVER도 동기화 권한이 없다")
        void syncData_approver_throwsException() {
            // given
            User user = createTestUser(1L, "APPROVER");
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            // when & then
            assertThatThrownBy(() -> chatService.syncData(1L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException customEx = (CustomException) ex;
                    assertThat(customEx.getErrorCode()).isEqualTo(ErrorCode.PERMISSION_DENIED_ACTION);
                });

            verify(aiChatApiClient, never()).syncDataSync();
        }
    }

    @Nested
    @DisplayName("getDbStatus")
    class GetDbStatusTest {

        @Test
        @DisplayName("REVIEWER 역할이면 DB 현황 조회가 성공한다")
        void getDbStatus_reviewer_success() {
            // given
            User user = createTestUser(1L, "REVIEWER");
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            AdminInspectResponse expectedResponse = new AdminInspectResponse(
                1542,
                List.of("[manual] 안전작업표준.pdf", "[code] validators.py")
            );

            when(aiChatApiClient.inspectDbSync()).thenReturn(expectedResponse);

            // when
            AdminInspectResponse response = chatService.getDbStatus(1L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.totalDocuments()).isEqualTo(1542);
            assertThat(response.samples()).hasSize(2);
            verify(aiChatApiClient).inspectDbSync();
        }

        @Test
        @DisplayName("REVIEWER가 아니면 PERMISSION_DENIED_ACTION 에러를 발생시킨다")
        void getDbStatus_notReviewer_throwsException() {
            // given
            User user = createTestUser(1L, "DRAFTER");
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            // when & then
            assertThatThrownBy(() -> chatService.getDbStatus(1L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException customEx = (CustomException) ex;
                    assertThat(customEx.getErrorCode()).isEqualTo(ErrorCode.PERMISSION_DENIED_ACTION);
                });

            verify(aiChatApiClient, never()).inspectDbSync();
        }
    }

    // Helper methods
    private User createTestUser(Long userId, String roleCode) {
        Role role = new Role(roleCode + " 역할", roleCode);

        User user = User.builder()
            .email("test@example.com")
            .userPassword("password")
            .name("테스트 사용자")
            .role(role)
            .build();
        ReflectionTestUtils.setField(user, "userId", userId);

        return user;
    }

    private User createTestUserWithCompany(Long userId, String roleCode, Company company) {
        Role role = new Role(roleCode + " 역할", roleCode);

        User user = User.builder()
            .email("test@example.com")
            .userPassword("password")
            .name("테스트 사용자")
            .role(role)
            .company(company)
            .build();
        ReflectionTestUtils.setField(user, "userId", userId);

        return user;
    }

    private Company createTestCompany(Long companyId) {
        Company company = Company.builder()
            .name("테스트 회사 " + companyId)
            .businessNumber("123-45-6789" + companyId)
            .build();
        ReflectionTestUtils.setField(company, "companyId", companyId);
        return company;
    }
}
