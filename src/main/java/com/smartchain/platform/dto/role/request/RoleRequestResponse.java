package com.smartchain.platform.dto.role.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequestResponse {
    private Long accessRequestId;        // v3.0: requestId → accessRequestId
    private String status;               // PENDING
    private String requestedRole;
    private Long companyId;
    private LocalDateTime createdAt;
    private String message;              // "권한 요청이 완료되었습니다"
}
