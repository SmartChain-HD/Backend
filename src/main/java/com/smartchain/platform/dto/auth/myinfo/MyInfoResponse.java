package com.smartchain.platform.dto.auth.myinfo;

import com.smartchain.platform.dto.auth.common.CompanyInfoDto;
import com.smartchain.platform.dto.auth.common.RoleInfoDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyInfoResponse {
    private Long userId;
    private String email;
    private String name;
    private String profileImageUrl;
    private CompanyInfoDto company;
    private RoleInfoDto role;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}
