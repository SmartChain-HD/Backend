package com.smartchain.platform.domain.notification.controller;

import com.smartchain.platform.domain.notification.service.NotificationService;
import com.smartchain.platform.dto.notification.NotificationListResponse;
import com.smartchain.platform.dto.notification.NotificationReadRequest;
import com.smartchain.platform.dto.notification.NotificationReadResponse;
import com.smartchain.platform.dto.notification.UnreadCountResponse;
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
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "알림 관리 API")
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "알림 목록 조회", description = "사용자의 알림 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<BaseResponse<NotificationListResponse>> getNotifications(
            HttpServletRequest request,
            @Parameter(description = "읽음 여부 필터")
            @RequestParam(required = false) Boolean isRead,
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "20") int size) {
        Long userId = extractUserIdFromRequest(request);
        NotificationListResponse response = notificationService.getNotifications(userId, isRead, page, size);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "알림 읽음 처리", description = "알림을 읽음 상태로 변경합니다. 특정 알림 ID 목록 또는 전체 읽음 처리가 가능합니다.")
    @PatchMapping("/read")
    public ResponseEntity<BaseResponse<NotificationReadResponse>> markAsRead(
            HttpServletRequest request,
            @RequestBody NotificationReadRequest readRequest) {
        Long userId = extractUserIdFromRequest(request);
        NotificationReadResponse response = notificationService.markAsRead(userId, readRequest);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "미읽음 알림 개수 조회", description = "사용자의 읽지 않은 알림 개수를 조회합니다.")
    @GetMapping("/unread-count")
    public ResponseEntity<BaseResponse<UnreadCountResponse>> getUnreadCount(HttpServletRequest request) {
        Long userId = extractUserIdFromRequest(request);
        UnreadCountResponse response = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "개별 알림 읽음 처리", description = "특정 알림을 읽음 상태로 변경합니다.")
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<BaseResponse<NotificationReadResponse>> markSingleAsRead(
            HttpServletRequest request,
            @Parameter(description = "알림 ID") @PathVariable Long notificationId) {
        Long userId = extractUserIdFromRequest(request);
        NotificationReadResponse response = notificationService.markSingleAsRead(userId, notificationId);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "전체 알림 읽음 처리", description = "사용자의 모든 알림을 읽음 상태로 변경합니다.")
    @PatchMapping("/read-all")
    public ResponseEntity<BaseResponse<NotificationReadResponse>> markAllAsRead(HttpServletRequest request) {
        Long userId = extractUserIdFromRequest(request);
        NotificationReadResponse response = notificationService.markAllAsReadForUser(userId);
        return ResponseEntity.ok(BaseResponse.success(response));
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
