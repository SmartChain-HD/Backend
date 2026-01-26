package com.smartchain.platform.dto.role.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleSimpleDto {
    private String code;                 // GUEST, DRAFTER, APPROVER, REVIEWER
    private String name;                 // 게스트, 기안자, 결재자, 수신자
}
