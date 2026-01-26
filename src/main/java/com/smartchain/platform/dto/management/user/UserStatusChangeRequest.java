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
public class UserStatusChangeRequest {
    
    @NotBlank(message = "변경할 상태를 선택해주세요")
    private String newStatus;            // ACTIVE, INACTIVE
    
    private String reason;
}
