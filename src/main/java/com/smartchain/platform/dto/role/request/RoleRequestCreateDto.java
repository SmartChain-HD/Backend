package com.smartchain.platform.dto.role.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequestCreateDto {
    
    /**
     * 요청 권한
     * 
     * ⚠️ v3.0 변경: ADMIN 역할 제거
     * 가능한 값: DRAFTER, APPROVER, REVIEWER
     */
    @NotBlank(message = "요청 권한을 선택해주세요")
    private String requestedRole;
    
    @NotNull(message = "회사를 선택해주세요")
    private Long companyId;
    
    private String reason;               // 요청 사유 (선택)
}
