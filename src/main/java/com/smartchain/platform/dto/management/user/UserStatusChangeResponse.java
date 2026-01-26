package com.smartchain.platform.dto.management.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusChangeResponse {
    private Long userId;
    private String previousStatus;
    private String newStatus;
    private LocalDateTime changedAt;
    private String message;
}
