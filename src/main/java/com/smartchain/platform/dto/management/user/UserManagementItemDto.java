package com.smartchain.platform.dto.management.user;

import com.smartchain.platform.dto.management.common.CompanySimpleDto;
import com.smartchain.platform.dto.management.common.RoleSimpleDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserManagementItemDto {
    private Long userId;
    private String name;
    private String email;
    private CompanySimpleDto company;
    private RoleSimpleDto role;
    private String status;               // ACTIVE, INACTIVE
    private LocalDateTime lastLoginAt;
    private String lastLoginAtLabel;
}
