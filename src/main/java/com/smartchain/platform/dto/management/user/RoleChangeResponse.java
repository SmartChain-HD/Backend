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
public class RoleChangeResponse {
    private Long userId;
    private String previousRole;
    private String newRole;
    private LocalDateTime changedAt;
    private ChangedByDto changedBy;      // v3.0: 변경자 정보 구조화
    private String message;
}
