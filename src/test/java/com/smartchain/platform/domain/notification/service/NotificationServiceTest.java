package com.smartchain.platform.domain.notification.service;

import com.smartchain.platform.domain.notification.entity.Notification;
import com.smartchain.platform.domain.notification.repository.NotificationRepository;
import com.smartchain.platform.domain.user.entity.User;
import com.smartchain.platform.dto.notification.NotificationListResponse;
import com.smartchain.platform.dto.notification.NotificationReadRequest;
import com.smartchain.platform.dto.notification.NotificationReadResponse;
import com.smartchain.platform.global.enums.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private Notification testNotification;

    @Mock
    private User testUser;

    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        lenient().when(testUser.getUserId()).thenReturn(userId);

        lenient().when(testNotification.getNotificationId()).thenReturn(1L);
        lenient().when(testNotification.getType()).thenReturn(NotificationType.APPROVAL_COMPLETE);
        lenient().when(testNotification.getTitle()).thenReturn("ESG 진단 결재 완료");
        lenient().when(testNotification.getMessage()).thenReturn("000 ESG 기안이 승인되었습니다");
        lenient().when(testNotification.getLinkUrl()).thenReturn("/diagnostics/1");
        lenient().when(testNotification.isRead()).thenReturn(false);
    }

    @Nested
    @DisplayName("알림 목록 조회 테스트")
    class GetNotificationsTest {

        @Test
        @DisplayName("알림 목록 조회 성공")
        void getNotifications_Success() {
            // given
            Page<Notification> notificationPage = new PageImpl<>(
                    List.of(testNotification),
                    PageRequest.of(0, 20),
                    1
            );

            given(notificationRepository.findByUserUserIdOrderByCreatedAtDesc(eq(userId), any()))
                    .willReturn(notificationPage);
            given(notificationRepository.countByUserUserIdAndIsRead(userId, false)).willReturn(3);

            // when
            NotificationListResponse response = notificationService.getNotifications(userId, null, 0, 20);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getUnreadCount()).isEqualTo(3);
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).getNotificationId()).isEqualTo(1L);
            assertThat(response.getContent().get(0).getType()).isEqualTo("APPROVAL_COMPLETE");
            assertThat(response.getContent().get(0).isRead()).isFalse();
        }

        @Test
        @DisplayName("읽지 않은 알림만 조회")
        void getNotifications_FilterByUnread_Success() {
            // given
            Page<Notification> notificationPage = new PageImpl<>(
                    List.of(testNotification),
                    PageRequest.of(0, 20),
                    1
            );

            given(notificationRepository.findByUserUserIdAndIsReadOrderByCreatedAtDesc(eq(userId), eq(false), any()))
                    .willReturn(notificationPage);
            given(notificationRepository.countByUserUserIdAndIsRead(userId, false)).willReturn(3);

            // when
            NotificationListResponse response = notificationService.getNotifications(userId, false, 0, 20);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getUnreadCount()).isEqualTo(3);
            verify(notificationRepository).findByUserUserIdAndIsReadOrderByCreatedAtDesc(eq(userId), eq(false), any());
        }
    }

    @Nested
    @DisplayName("알림 읽음 처리 테스트")
    class MarkAsReadTest {

        @Test
        @DisplayName("특정 알림 읽음 처리 성공")
        void markAsRead_SpecificIds_Success() {
            // given
            NotificationReadRequest request = new NotificationReadRequest();
            // use reflection to set private fields for test
            setNotificationIds(request, List.of(1L, 2L, 3L));

            given(notificationRepository.markAsReadByIds(List.of(1L, 2L, 3L), userId)).willReturn(3);
            given(notificationRepository.countByUserUserIdAndIsRead(userId, false)).willReturn(0);

            // when
            NotificationReadResponse response = notificationService.markAsRead(userId, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getMarkedCount()).isEqualTo(3);
            assertThat(response.getRemainingUnread()).isEqualTo(0);
            verify(notificationRepository).markAsReadByIds(List.of(1L, 2L, 3L), userId);
        }

        @Test
        @DisplayName("전체 알림 읽음 처리 성공")
        void markAsRead_All_Success() {
            // given
            NotificationReadRequest request = new NotificationReadRequest();
            setMarkAllAsRead(request, true);

            given(notificationRepository.markAllAsReadByUserId(userId)).willReturn(5);
            given(notificationRepository.countByUserUserIdAndIsRead(userId, false)).willReturn(0);

            // when
            NotificationReadResponse response = notificationService.markAsRead(userId, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getMarkedCount()).isEqualTo(5);
            assertThat(response.getRemainingUnread()).isEqualTo(0);
            verify(notificationRepository).markAllAsReadByUserId(userId);
        }
    }

    // Helper methods to set private fields using reflection
    private void setNotificationIds(NotificationReadRequest request, List<Long> ids) {
        try {
            var field = NotificationReadRequest.class.getDeclaredField("notificationIds");
            field.setAccessible(true);
            field.set(request, ids);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setMarkAllAsRead(NotificationReadRequest request, Boolean value) {
        try {
            var field = NotificationReadRequest.class.getDeclaredField("markAllAsRead");
            field.setAccessible(true);
            field.set(request, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
