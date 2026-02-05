package com.smartchain.platform.dto.auth.myinfo;

import com.smartchain.platform.dto.auth.common.CompanyInfoDto;
import com.smartchain.platform.dto.auth.common.DomainRoleDto;
import com.smartchain.platform.dto.auth.common.RoleInfoDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyInfoResponse {
    private Long userId;
    private String email;
    private String name;
    private String maskedName;
    private String profileImageUrl;
    private CompanyInfoDto company;
    private RoleInfoDto role;
    private List<DomainRoleDto> domainRoles;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}
