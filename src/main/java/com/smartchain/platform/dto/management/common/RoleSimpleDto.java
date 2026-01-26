package com.smartchain.platform.dto.management.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleSimpleDto {
    private String code;                 // DRAFTER, APPROVER, REVIEWER
    private String name;                 // 기안자, 결재자, 수신자
}
