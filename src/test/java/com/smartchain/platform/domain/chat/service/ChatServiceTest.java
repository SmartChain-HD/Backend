package com.smartchain.platform.domain.chat.service;

import com.smartchain.platform.domain.chat.client.AiChatApiClient;
import com.smartchain.platform.domain.user.entity.Role;
import com.smartchain.platform.domain.user.entity.User;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private AiChatApiClient aiChatApiClient;

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        chatService = new ChatService(aiChatApiClient);
    }

    @Nested
    @DisplayName("chat")
    class ChatTest {

        @Test
        @DisplayName("정상 채팅 요청 시 AI 응답을 반환한다")
        void chat_success_returnsResponse() {
            // given
            User user = createTestUser("DRAFTER");
            ChatRequest request = new ChatRequest(
                "하도급법 위반 시 벌점은?",
                List.of(),
                "compliance",
                null,
                8,
                null
            );

            ChatResponse expectedResponse = new ChatResponse(
                "하도급법 위반 시 벌점은 위반 사유에 따라 다릅니다.",
                "high",
                null,
                List.of(new SourceItem("하도급가이드.pdf", "manual", "벌점 부과 기준...", 0.89, null))
            );

            when(aiChatApiClient.chatSync(any(ChatRequest.class))).thenReturn(expectedResponse);

            // when
            ChatResponse response = chatService.chat(request, user);

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
            User user = createTestUser("DRAFTER");
            String existingSessionId = "existing-session-123";
            ChatRequest request = new ChatRequest(
                "추가 질문입니다",
                List.of(),
                "esg",
                null,
                8,
                existingSessionId
            );

            ChatResponse expectedResponse = new ChatResponse("답변입니다.", "medium", null, List.of());
            when(aiChatApiClient.chatSync(any(ChatRequest.class))).thenReturn(expectedResponse);

            // when
            chatService.chat(request, user);

            // then
            ArgumentCaptor<ChatRequest> requestCaptor = ArgumentCaptor.forClass(ChatRequest.class);
            verify(aiChatApiClient).chatSync(requestCaptor.capture());
            assertThat(requestCaptor.getValue().sessionId()).isEqualTo(existingSessionId);
        }

        @Test
        @DisplayName("유효하지 않은 도메인이면 AI_CHAT_INVALID_DOMAIN 에러를 발생시킨다")
        void chat_invalidDomain_throwsException() {
            // given
            User user = createTestUser("DRAFTER");
            ChatRequest request = new ChatRequest(
                "질문입니다",
                List.of(),
                "invalid_domain",
                null,
                8,
                null
            );

            // when & then
            assertThatThrownBy(() -> chatService.chat(request, user))
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
            User user = createTestUser("DRAFTER");
            ChatRequest request = new ChatRequest(
                "질문입니다",
                List.of(),
                "all",
                null,
                8,
                null
            );

            when(aiChatApiClient.chatSync(any(ChatRequest.class)))
                .thenThrow(new CustomException(ErrorCode.AI_CHAT_SERVICE_ERROR));

            // when & then
            assertThatThrownBy(() -> chatService.chat(request, user))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException customEx = (CustomException) ex;
                    assertThat(customEx.getErrorCode()).isEqualTo(ErrorCode.AI_CHAT_SERVICE_ERROR);
                });
        }
    }

    @Nested
    @DisplayName("syncData")
    class SyncDataTest {

        @Test
        @DisplayName("REVIEWER 역할이면 동기화 요청이 성공한다")
        void syncData_reviewer_success() {
            // given
            User user = createTestUser("REVIEWER");
            AdminSyncResponse expectedResponse = new AdminSyncResponse("accepted", "동기화가 시작되었습니다.");

            when(aiChatApiClient.syncDataSync()).thenReturn(expectedResponse);

            // when
            AdminSyncResponse response = chatService.syncData(user);

            // then
            assertThat(response).isNotNull();
            assertThat(response.status()).isEqualTo("accepted");
            verify(aiChatApiClient).syncDataSync();
        }

        @Test
        @DisplayName("REVIEWER가 아니면 PERMISSION_DENIED_ACTION 에러를 발생시킨다")
        void syncData_notReviewer_throwsException() {
            // given
            User user = createTestUser("DRAFTER");

            // when & then
            assertThatThrownBy(() -> chatService.syncData(user))
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
            User user = createTestUser("APPROVER");

            // when & then
            assertThatThrownBy(() -> chatService.syncData(user))
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
            User user = createTestUser("REVIEWER");
            AdminInspectResponse expectedResponse = new AdminInspectResponse(
                1542,
                List.of("[manual] 안전작업표준.pdf", "[code] validators.py")
            );

            when(aiChatApiClient.inspectDbSync()).thenReturn(expectedResponse);

            // when
            AdminInspectResponse response = chatService.getDbStatus(user);

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
            User user = createTestUser("DRAFTER");

            // when & then
            assertThatThrownBy(() -> chatService.getDbStatus(user))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException customEx = (CustomException) ex;
                    assertThat(customEx.getErrorCode()).isEqualTo(ErrorCode.PERMISSION_DENIED_ACTION);
                });

            verify(aiChatApiClient, never()).inspectDbSync();
        }
    }

    // Helper methods
    private User createTestUser(String roleCode) {
        Role role = new Role(roleCode + " 역할", roleCode);

        User user = User.builder()
            .email("test@example.com")
            .userPassword("password")
            .name("테스트 사용자")
            .role(role)
            .build();
        ReflectionTestUtils.setField(user, "userId", 1L);

        return user;
    }
}
