package com.smartchain.platform.dto.auth.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDto {
    private Long userId;
    private String email;
    private String name;
    private String profileImageUrl;
    private CompanyInfoDto company;
    private RoleInfoDto role;
    private LocalDateTime lastLoginAt;
}
