package com.smartchain.platform.dto.management.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleChangeRequest {
    
    @NotBlank(message = "변경할 역할을 선택해주세요")
    private String newRole;              // GUEST, DRAFTER, APPROVER, REVIEWER (v3.0: ADMIN 제거)
    
    private String reason;
}
